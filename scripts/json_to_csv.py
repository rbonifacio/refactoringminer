#!/usr/bin/env python3
"""
Convert RefactoringMiner JSON result files to a single CSV for data analysis.

Each row represents one detected refactoring. When a refactoring has multiple
left/right side locations they are serialised as pipe-separated ( | ) values
within a single cell, keeping the row count 1-to-1 with refactorings.

Usage (from the repo root):
    python3 scripts/json_to_csv.py
    python3 scripts/json_to_csv.py --input dataset/all-results --output dataset/refactorings.csv
"""

import argparse
import csv
import json
import sys
from datetime import datetime, timezone
from pathlib import Path


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def fmt_timestamp(ms) -> str:
    """Convert epoch-milliseconds to an ISO-8601 UTC string."""
    if ms is None or ms == "":
        return ""
    try:
        dt = datetime.fromtimestamp(int(ms) / 1000, tz=timezone.utc)
        return dt.strftime("%Y-%m-%dT%H:%M:%SZ")
    except (ValueError, OSError, OverflowError):
        return str(ms)


SEP = " | "


def serialise_locations(locations: list) -> tuple:
    """
    Return a 6-tuple of pipe-joined strings for location columns:
        (file_path, start_line, end_line, code_element_type,
         code_element, location_description)
    """
    if not locations:
        return ("", "", "", "", "", "")

    def pick(loc, key):
        return str(loc.get(key, ""))

    return (
        SEP.join(pick(l, "filePath")        for l in locations),
        SEP.join(pick(l, "startLine")       for l in locations),
        SEP.join(pick(l, "endLine")         for l in locations),
        SEP.join(pick(l, "codeElementType") for l in locations),
        SEP.join(pick(l, "codeElement")     for l in locations),
        SEP.join(pick(l, "description")     for l in locations),
    )


# ---------------------------------------------------------------------------
# Schema
# ---------------------------------------------------------------------------

FIELDNAMES = [
    # provenance
    "project_name",
    "repository",
    "commit_id",
    "commit_url",
    # authorship
    "author_name",
    "author_email",
    "commit_time",
    # refactoring
    "refactoring_type",
    "description",
    # before (left side)
    "left_file_path",
    "left_start_line",
    "left_end_line",
    "left_code_element_type",
    "left_code_element",
    "left_location_description",
    # after (right side)
    "right_file_path",
    "right_start_line",
    "right_end_line",
    "right_code_element_type",
    "right_code_element",
    "right_location_description",
]


# ---------------------------------------------------------------------------
# Core logic
# ---------------------------------------------------------------------------

def iter_json_files(root: Path):
    """Yield (project_name, json_path) for every project JSON under *root*."""
    for json_path in sorted(root.rglob("*.json")):
        if json_path.name.startswith("checkpoint"):
            continue
        yield json_path.stem, json_path


def process_file(project_name: str, json_path: Path) -> list:
    try:
        with json_path.open(encoding="utf-8") as fh:
            data = json.load(fh)
    except (json.JSONDecodeError, OSError) as exc:
        print(f"  [WARN] skipping {json_path}: {exc}", file=sys.stderr)
        return []

    if not isinstance(data, list):
        print(f"  [WARN] unexpected top-level type in {json_path}", file=sys.stderr)
        return []

    rows = []
    for entry in data:
        left  = serialise_locations(entry.get("leftSideLocations",  []))
        right = serialise_locations(entry.get("rightSideLocations", []))

        rows.append({
            "project_name":               project_name,
            "repository":                 entry.get("repository", ""),
            "commit_id":                  entry.get("commit", ""),
            "commit_url":                 entry.get("url", ""),
            "author_name":                entry.get("authorName", ""),
            "author_email":               entry.get("authorEmail", ""),
            "commit_time":                fmt_timestamp(entry.get("commitTime")),
            "refactoring_type":           entry.get("type", ""),
            "description":                entry.get("description", ""),
            "left_file_path":             left[0],
            "left_start_line":            left[1],
            "left_end_line":              left[2],
            "left_code_element_type":     left[3],
            "left_code_element":          left[4],
            "left_location_description":  left[5],
            "right_file_path":            right[0],
            "right_start_line":           right[1],
            "right_end_line":             right[2],
            "right_code_element_type":    right[3],
            "right_code_element":         right[4],
            "right_location_description": right[5],
        })
    return rows


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "--input", default="dataset/all-results",
        help="Root directory containing experiment sub-folders (default: dataset/all-results)",
    )
    parser.add_argument(
        "--output", default="dataset/refactorings.csv",
        help="Output CSV file path (default: dataset/refactorings.csv)",
    )
    args = parser.parse_args()

    input_root  = Path(args.input)
    output_path = Path(args.output)

    if not input_root.is_dir():
        sys.exit(f"[ERROR] Input directory not found: {input_root}")

    output_path.parent.mkdir(parents=True, exist_ok=True)

    total_files = 0
    total_rows  = 0

    with output_path.open("w", newline="", encoding="utf-8") as csv_fh:
        writer = csv.DictWriter(csv_fh, fieldnames=FIELDNAMES, quoting=csv.QUOTE_ALL)
        writer.writeheader()

        for project_name, json_path in iter_json_files(input_root):
            rows = process_file(project_name, json_path)
            if rows:
                writer.writerows(rows)
                total_files += 1
                total_rows  += len(rows)
                print(f"  {project_name:45s}  {len(rows):>7,} refactorings")

    print(f"\nDone.  {total_files} project files -> {total_rows:,} total rows -> {output_path}")


if __name__ == "__main__":
    main()
