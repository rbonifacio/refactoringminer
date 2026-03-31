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

  # Process in batches of 50 commits to avoid OOM (default is 200)
  python collect_python_refactorings.py -a /path/to/repo main --batch-size 50
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
_OOM_RE = re.compile(r'java\.lang\.OutOfMemoryError')


class JarOOMError(RuntimeError):
    """Raised when the JAR process exits with a Java OutOfMemoryError."""


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


def get_commit_list(repo: str, rev_range: str) -> list[str]:
    """Return commits in chronological order (oldest first)."""
    cmd = ["git", "-C", repo, "rev-list", "--reverse", rev_range]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        sys.exit(f"[error] git rev-list failed: {result.stderr.strip()}")
    return [sha for sha in result.stdout.splitlines() if sha]


def run_jar_once(jar: Path, jar_args: list[str], json_output: Path,
                 verbose: bool, done_so_far: int, total: int | None,
                 batch_total: int | None, jvm_args: str = "-Xmx32g") -> None:
    """Invoke the JAR for a single batch and show progress."""
    extra = jvm_args.split() if jvm_args else []
    cmd = ["java"] + extra + ["-jar", str(jar)] + jar_args + ["-json", str(json_output)]
    print(f"[info] Running: {' '.join(cmd)}", file=sys.stderr)

    if verbose:
        result = subprocess.run(cmd)
        if result.returncode != 0:
            sys.exit(f"[error] JAR exited with code {result.returncode}")
        return

    proc = subprocess.Popen(
        cmd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.PIPE,
        text=True,
        errors="replace",
    )

    last_pct = -1
    captured_lines: list[str] = []
    for line in proc.stderr:
        captured_lines.append(line)
        m = _COMMIT_PROGRESS_RE.search(line)
        if m:
            batch_done = int(m.group(1))
            overall_done = done_so_far + batch_done
            if total and total > 0:
                pct = min(int(overall_done * 100 / total), 99)
                if pct != last_pct:
                    print(f"\r  Progress: {pct:3d}% ({overall_done}/{total} commits)",
                          end="", flush=True, file=sys.stderr)
                    last_pct = pct
            elif batch_total:
                print(f"\r  Processed: {batch_done}/{batch_total} in batch ({overall_done} total)",
                      end="", flush=True, file=sys.stderr)
            else:
                print(f"\r  Processed: {overall_done} commit(s)",
                      end="", flush=True, file=sys.stderr)

    proc.wait()

    if proc.returncode != 0:
        oom = any(_OOM_RE.search(line) for line in captured_lines)
        if oom:
            raise JarOOMError("JAR ran out of memory")
        print("[error] JAR output:", file=sys.stderr)
        for line in captured_lines:
            print(" ", line, end="", file=sys.stderr)
        sys.exit(f"[error] JAR exited with code {proc.returncode}")


def run_jar(jar: Path, jar_args: list[str], json_output: Path,
            verbose: bool, total: int | None, jvm_args: str = "-Xmx32g",
            batch_size: int | None = None,
            args: argparse.Namespace | None = None) -> list[str]:
    """Run the JAR, optionally splitting into commit batches to avoid OOM.

    Returns a (possibly empty) list of commit SHAs that were skipped due to OOM.
    """

    # Determine if batching is applicable and needed
    repo = None
    commits: list[str] = []

    if batch_size and args is not None:
        if args.a:
            repo = args.a[0]
            branch = args.a[1] if len(args.a) > 1 else "HEAD"
            commits = get_commit_list(repo, branch)
        elif args.bc:
            repo, start, end = args.bc
            commits = get_commit_list(repo, f"{start}..{end}")

    if commits and batch_size and len(commits) > batch_size:
        return _run_jar_batched(jar, repo, commits, json_output, verbose,
                                total, jvm_args, batch_size)

    # Single invocation (no batching needed or applicable)
    run_jar_once(jar, jar_args, json_output, verbose,
                 done_so_far=0, total=total, batch_total=None, jvm_args=jvm_args)

    if total and total > 0 and not verbose:
        print(f"\r  Progress: 100% ({total}/{total} commits)", file=sys.stderr)
    elif not verbose:
        print("", file=sys.stderr)
    return []


def _run_jar_batched(jar: Path, repo: str, commits: list[str],
                     final_output: Path, verbose: bool, total: int | None,
                     jvm_args: str, batch_size: int,
                     failed_commits: list[str] | None = None) -> list[str]:
    """Split commits into batches of batch_size, merge results into final_output.

    Returns the list of commit SHAs skipped due to persistent OOM errors.
    """
    if failed_commits is None:
        failed_commits = []

    all_commits_out: list[dict] = []
    n = len(commits)
    done_so_far = 0

    for batch_start in range(0, n, batch_size):
        batch = commits[batch_start:batch_start + batch_size]
        start_sha = commits[batch_start - 1] if batch_start > 0 else None
        end_sha = batch[-1]

        batch_num = batch_start // batch_size + 1
        num_batches = (n + batch_size - 1) // batch_size
        print(f"\n[info] Batch {batch_num}/{num_batches} "
              f"({len(batch)} commits, {done_so_far}/{n} total done)",
              file=sys.stderr)

        if start_sha is None:
            batch_jar_args = ["-bc", repo, batch[0] + "^", end_sha]
            # If batch[0] has no parent (initial commit), fall back to -c
            check = subprocess.run(
                ["git", "-C", repo, "rev-parse", batch[0] + "^"],
                capture_output=True
            )
            if check.returncode != 0:
                # Initial commit — use -c for just this commit, then bc for rest
                if len(batch) == 1:
                    batch_jar_args = ["-c", repo, batch[0]]
                else:
                    # Run initial commit separately
                    _run_single_commit_batch(jar, repo, batch[0], all_commits_out,
                                             verbose, done_so_far, total, jvm_args,
                                             failed_commits)
                    done_so_far += 1
                    if len(batch) > 1:
                        batch_jar_args = ["-bc", repo, batch[0], end_sha]
                    else:
                        continue
            else:
                batch_jar_args = ["-bc", repo, batch[0] + "^", end_sha]
        else:
            batch_jar_args = ["-bc", repo, start_sha, end_sha]

        with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as tf:
            tmp_path = Path(tf.name)

        try:
            try:
                run_jar_once(jar, batch_jar_args, tmp_path, verbose,
                             done_so_far=done_so_far, total=total,
                             batch_total=len(batch), jvm_args=jvm_args)
                with open(tmp_path, encoding="utf-8") as f:
                    raw = json.load(f)
                all_commits_out.extend(raw.get("commits", []))
            except JarOOMError:
                if len(batch) == 1:
                    print(f"\n[warn] Skipping {batch[0][:10]} — OOM on single commit",
                          file=sys.stderr)
                    failed_commits.append(batch[0])
                else:
                    smaller = max(1, len(batch) // 2)
                    print(f"\n[warn] OOM on batch of {len(batch)} — retrying as "
                          f"sub-batches of {smaller}", file=sys.stderr)
                    tmp_path.unlink(missing_ok=True)
                    _run_jar_batched(jar, repo, batch, tmp_path, verbose,
                                     total, jvm_args, smaller, failed_commits)
                    with open(tmp_path, encoding="utf-8") as f:
                        raw = json.load(f)
                    all_commits_out.extend(raw.get("commits", []))
        finally:
            tmp_path.unlink(missing_ok=True)

        done_so_far += len(batch)

    if not verbose:
        if total and total > 0:
            print(f"\r  Progress: 100% ({total}/{total} commits)", file=sys.stderr)
        else:
            print("", file=sys.stderr)

    with open(final_output, "w", encoding="utf-8") as f:
        json.dump({"commits": all_commits_out}, f)

    return failed_commits


def _run_single_commit_batch(jar: Path, repo: str, sha: str,
                              all_commits_out: list[dict],
                              verbose: bool, done_so_far: int,
                              total: int | None, jvm_args: str,
                              failed_commits: list[str]) -> None:
    with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as tf:
        tmp_path = Path(tf.name)
    try:
        try:
            run_jar_once(jar, ["-c", repo, sha], tmp_path, verbose,
                         done_so_far=done_so_far, total=total,
                         batch_total=1, jvm_args=jvm_args)
            with open(tmp_path, encoding="utf-8") as f:
                raw = json.load(f)
            all_commits_out.extend(raw.get("commits", []))
        except JarOOMError:
            print(f"\n[warn] Skipping {sha[:10]} — OOM on single commit",
                  file=sys.stderr)
            failed_commits.append(sha)
    finally:
        tmp_path.unlink(missing_ok=True)


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
            entry = {
                "commit":             commit.get("sha1"),
                "repository":         commit.get("repository"),
                "url":                commit.get("url"),
                "type":               ref["type"],
                "description":        ref.get("description"),
                "leftSideLocations":  ref.get("leftSideLocations", []),
                "rightSideLocations": ref.get("rightSideLocations", []),
            }
            if commit.get("authorName") is not None:
                entry["authorName"]  = commit.get("authorName")
                entry["authorEmail"] = commit.get("authorEmail")
                entry["commitTime"]  = commit.get("commitTime")
            results.append(entry)
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

def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="Collect Python type-annotation and pattern-matching "
                    "refactorings using RefactoringMiner.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
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
    p.add_argument("--jvm-args", metavar="ARGS", default="-Xmx32g",
                   help="JVM arguments passed before -jar (default: -Xmx32g)")
    p.add_argument("--batch-size", metavar="N", type=int, default=200,
                   help="Max commits per JAR invocation to avoid OOM "
                        "(default: 200; use 0 to disable batching)")

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

    batch_size = args.batch_size if args.batch_size > 0 else None
    total = None if args.verbose else count_commits(args)

    with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as tf:
        tmp_path = Path(tf.name)

    try:
        failed = run_jar(jar, jar_args_for(args), tmp_path, args.verbose, total,
                         args.jvm_args, batch_size=batch_size, args=args)

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

        if failed:
            print(f"\n[warn] {len(failed)} commit(s) skipped due to OOM:", file=sys.stderr)
            for sha in failed:
                print(f"  {sha}", file=sys.stderr)

        if args.output:
            out_path = Path(args.output)
            with open(out_path, "w", encoding="utf-8") as f:
                json.dump(results, f, indent=2)
            print(f"\n[info] Results saved to {out_path}")

    finally:
        tmp_path.unlink(missing_ok=True)


if __name__ == "__main__":
    main()
