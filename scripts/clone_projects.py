#!/usr/bin/env python3
"""Clone repositories listed in projects.csv to a given target directory."""

import argparse
import csv
import subprocess
import sys
from pathlib import Path


def clone_repo(repo: str, target_dir: Path) -> bool:
    url = f"https://github.com/{repo}.git"
    dest = target_dir / repo.split("/")[1]

    if dest.exists():
        print(f"[skip] {repo} already exists at {dest}")
        return True

    print(f"[clone] {repo} -> {dest}")
    result = subprocess.run(
        ["git", "clone", url, str(dest)],
        capture_output=True,
        text=True,
    )

    if result.returncode != 0:
        print(f"[error] Failed to clone {repo}: {result.stderr.strip()}", file=sys.stderr)
        return False

    return True


def main():
    parser = argparse.ArgumentParser(description="Clone repositories from projects.csv")
    parser.add_argument("target_dir", help="Directory where repositories will be cloned")
    parser.add_argument(
        "--csv",
        default=Path(__file__).parent / "projects.csv",
        help="Path to projects.csv (default: projects.csv next to this script)",
    )
    args = parser.parse_args()

    target = Path(args.target_dir)
    target.mkdir(parents=True, exist_ok=True)

    csv_path = Path(args.csv)
    if not csv_path.exists():
        print(f"CSV file not found: {csv_path}", file=sys.stderr)
        sys.exit(1)

    failed = []
    with csv_path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        repos = [row["name"].strip() for row in reader if row.get("name", "").strip()]

    print(f"Found {len(repos)} repositories to clone into {target}\n")

    for repo in repos:
        if not clone_repo(repo, target):
            failed.append(repo)

    print(f"\nDone. {len(repos) - len(failed)}/{len(repos)} cloned successfully.")
    if failed:
        print("Failed repositories:")
        for r in failed:
            print(f"  {r}")
        sys.exit(1)


if __name__ == "__main__":
    main()
