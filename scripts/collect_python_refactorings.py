#!/usr/bin/env python3
"""
collect_python_refactorings.py
-------------------------------
Runs the RefactoringMiner fat JAR and collects Python refactorings related to
type hint annotations and structural pattern matching.

The mode flags mirror the JAR CLI directly:

  -c   <repo> <commit>                 Single commit
  -bc  <repo> <start-sha> <end-sha>    Range between two commits
  -bt  <repo> <start-tag> <end-tag>    Range between two tags
  -a   <repo> [<branch>]               All commits on a branch (branch optional)

Usage examples
--------------
  # Analyse a single commit
  python collect_python_refactorings.py -c /path/to/repo abc123

  # Analyse a range of commits
  python collect_python_refactorings.py -bc /path/to/repo abc123 def456

  # Analyse all commits on a branch and save results to a file
  python collect_python_refactorings.py -a /path/to/repo main --output results.json

  # Specify a custom JAR location
  python collect_python_refactorings.py --jar /opt/RM-fat.jar -c /path/to/repo abc123
"""

import argparse
import json
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

# ---------------------------------------------------------------------------
# Refactoring types of interest
# ---------------------------------------------------------------------------

ANNOTATION_TYPES = {
    "Add Parameter Type Annotation",
    "Remove Parameter Type Annotation",
    "Change Parameter Type Annotation",
    "Add Return Type Annotation",
    "Remove Return Type Annotation",
    "Change Return Type Annotation",
    "Add Variable Type Annotation",
    "Remove Variable Type Annotation",
    "Change Variable Type Annotation",
}

PATTERN_MATCHING_TYPES = {
    "Replace Conditional With Pattern Matching",
    "Replace Pattern Matching With Conditional",
}

TARGET_TYPES = ANNOTATION_TYPES | PATTERN_MATCHING_TYPES

# ---------------------------------------------------------------------------
# JAR discovery
# ---------------------------------------------------------------------------

def find_jar(explicit_path: str | None) -> Path:
    if explicit_path:
        jar = Path(explicit_path)
        if not jar.is_file():
            sys.exit(f"[error] JAR not found: {jar}")
        return jar

    script_dir = Path(__file__).resolve().parent
    candidates = [
        script_dir.parent / "build" / "libs" / "RM-fat.jar",
        Path("build") / "libs" / "RM-fat.jar",
    ]
    for c in candidates:
        if c.is_file():
            return c

    sys.exit(
        "[error] Could not find RM-fat.jar. Build it with `./gradlew shadowJar` "
        "or pass --jar <path>."
    )

# ---------------------------------------------------------------------------
# JAR invocation
# ---------------------------------------------------------------------------

_COMMIT_PROGRESS_RE = re.compile(r'\[Commits:\s*(\d+),')


def count_commits(args: argparse.Namespace) -> int | None:
    """Pre-count commits via git so we can display a percentage."""
    try:
        if args.c:
            return 1
        if args.bc:
            repo, start, end = args.bc
            rev_range = f"{start}..{end}"
        elif args.bt:
            repo, start, end = args.bt
            rev_range = f"{start}..{end}"
        elif args.a:
            repo = args.a[0]
            branch = args.a[1] if len(args.a) > 1 else "HEAD"
            rev_range = branch
        else:
            return None
        cmd = ["git", "-C", repo, "rev-list", "--count", rev_range]
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode == 0:
            return int(result.stdout.strip())
    except Exception:
        pass
    return None


def run_jar(jar: Path, jar_args: list[str], json_output: Path,
            verbose: bool, total: int | None) -> None:
    cmd = ["java", "-jar", str(jar)] + jar_args + ["-json", str(json_output)]
    print(f"[info] Running: {' '.join(cmd)}", file=sys.stderr)

    if verbose:
        result = subprocess.run(cmd)
        if result.returncode != 0:
            sys.exit(f"[error] JAR exited with code {result.returncode}")
        return

    # Quiet mode: capture JAR stderr and derive progress from it.
    proc = subprocess.Popen(
        cmd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.PIPE,
        text=True,
        errors="replace",
    )

    last_pct = -1
    for line in proc.stderr:
        m = _COMMIT_PROGRESS_RE.search(line)
        if m:
            done = int(m.group(1))
            if total and total > 0:
                pct = min(int(done * 100 / total), 99)
                if pct != last_pct:
                    print(f"\r  Progress: {pct:3d}% ({done}/{total} commits)",
                          end="", flush=True, file=sys.stderr)
                    last_pct = pct
            else:
                print(f"\r  Processed: {done} commit(s)",
                      end="", flush=True, file=sys.stderr)

    proc.wait()
    if total and total > 0:
        print(f"\r  Progress: 100% ({total}/{total} commits)",
              file=sys.stderr)
    else:
        print("", file=sys.stderr)  # newline after \r

    if proc.returncode != 0:
        sys.exit(f"[error] JAR exited with code {proc.returncode}")

# ---------------------------------------------------------------------------
# Filtering
# ---------------------------------------------------------------------------

def is_python_location(locations: list[dict]) -> bool:
    return any(
        loc.get("filePath", "").endswith(".py")
        for loc in locations
    )

def filter_refactorings(raw_json: dict) -> list[dict]:
    results = []
    for commit in raw_json.get("commits", []):
        for ref in commit.get("refactorings", []):
            if ref.get("type") not in TARGET_TYPES:
                continue
            all_locs = ref.get("leftSideLocations", []) + ref.get("rightSideLocations", [])
            if not is_python_location(all_locs):
                continue
            results.append({
                "commit":             commit.get("sha1"),
                "repository":         commit.get("repository"),
                "url":                commit.get("url"),
                "type":               ref["type"],
                "description":        ref.get("description"),
                "leftSideLocations":  ref.get("leftSideLocations", []),
                "rightSideLocations": ref.get("rightSideLocations", []),
            })
    return results

# ---------------------------------------------------------------------------
# Reporting
# ---------------------------------------------------------------------------

def print_summary(results: list[dict]) -> None:
    if not results:
        print("\n[info] No matching refactorings found.")
        return

    by_type: dict[str, int] = {}
    for r in results:
        by_type[r["type"]] = by_type.get(r["type"], 0) + 1

    print(f"\n{'='*70}")
    print(f"  Found {len(results)} Python refactoring(s) across "
          f"{len({r['commit'] for r in results})} commit(s)")
    print(f"{'='*70}")

    print("\n--- By type ---")
    for rtype, count in sorted(by_type.items(), key=lambda x: -x[1]):
        print(f"  {count:4d}  {rtype}")

    print("\n--- Details ---")
    for r in results:
        sha = (r["commit"] or "")[:10]
        print(f"\n  [{sha}] {r['type']}")
        print(f"         {r['description']}")
        for loc in r["leftSideLocations"] + r["rightSideLocations"]:
            fp   = loc.get("filePath", "")
            sl   = loc.get("startLine", "")
            el   = loc.get("endLine", "")
            elem = loc.get("codeElement") or loc.get("codeElementType", "")
            print(f"           {fp}:{sl}-{el}  ({elem})")

# ---------------------------------------------------------------------------
# Argument parsing  (mirrors JAR flags directly)
# ---------------------------------------------------------------------------

def parse_args() -> tuple[argparse.Namespace, list[str]]:
    p = argparse.ArgumentParser(
        description="Collect Python type-annotation and pattern-matching "
                    "refactorings using RefactoringMiner.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
        # allow unknown so we can forward -c/-bc/-bt/-a to the JAR arg builder
        add_help=True,
    )

    p.add_argument("--jar", metavar="PATH",
                   help="Path to RM-fat.jar (auto-detected if omitted)")
    p.add_argument("--output", metavar="FILE",
                   help="Save filtered results as JSON to this file")
    p.add_argument("--all-types", action="store_true",
                   help="Include all refactoring types, not just Python ones")
    p.add_argument("--verbose", action="store_true",
                   help="Show full JAR output instead of a progress percentage")

    # Mode flags (mutually exclusive)
    mode = p.add_mutually_exclusive_group(required=True)
    mode.add_argument("-c",  nargs=2,   metavar=("REPO", "SHA"),
                      help="Analyse a single commit")
    mode.add_argument("-bc", nargs=3,   metavar=("REPO", "START", "END"),
                      help="Analyse commits between two SHAs")
    mode.add_argument("-bt", nargs=3,   metavar=("REPO", "START_TAG", "END_TAG"),
                      help="Analyse commits between two tags")
    mode.add_argument("-a",  nargs="+", metavar=("REPO", "BRANCH"),
                      help="Analyse all commits on a branch (branch optional)")

    return p.parse_args()


def jar_args_for(args: argparse.Namespace) -> list[str]:
    if args.c:
        return ["-c"] + args.c
    if args.bc:
        return ["-bc"] + args.bc
    if args.bt:
        return ["-bt"] + args.bt
    if args.a:
        if len(args.a) > 2:
            sys.exit("[error] -a accepts at most two arguments: <repo> [<branch>]")
        return ["-a"] + args.a
    sys.exit("[error] No mode specified.")

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    args = parse_args()
    jar  = find_jar(args.jar)

    total = None if args.verbose else count_commits(args)

    with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as tf:
        tmp_path = Path(tf.name)

    try:
        run_jar(jar, jar_args_for(args), tmp_path, args.verbose, total)

        with open(tmp_path, encoding="utf-8") as f:
            raw = json.load(f)

        if args.all_types:
            results = [
                {
                    **ref,
                    "commit":     c.get("sha1"),
                    "repository": c.get("repository"),
                    "url":        c.get("url"),
                }
                for c in raw.get("commits", [])
                for ref in c.get("refactorings", [])
            ]
        else:
            results = filter_refactorings(raw)

        print_summary(results)

        if args.output:
            out_path = Path(args.output)
            with open(out_path, "w", encoding="utf-8") as f:
                json.dump(results, f, indent=2)
            print(f"\n[info] Results saved to {out_path}")

    finally:
        tmp_path.unlink(missing_ok=True)


if __name__ == "__main__":
    main()
