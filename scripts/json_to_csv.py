#!/usr/bin/env python3
"""
Convert RefactoringMiner JSON output to CSV.

Each row represents one code location involved in a refactoring.
Columns:
  - commit_id         : SHA of the commit
  - repository        : repository URL
  - refactoring_type  : e.g. "Add Variable Type Annotation"
  - description       : human-readable description of the refactoring
  - side              : "left" (before) or "right" (after) the refactoring
  - file_path         : path of the affected file
  - start_line        : first affected line
  - end_line          : last affected line
  - code_element_type : e.g. METHOD_DECLARATION, VARIABLE_DECLARATION_STATEMENT
  - code_element      : the actual element name / signature
  - location_desc     : description of the specific location's role
"""

import csv
import json
import sys
from pathlib import Path


FIELDNAMES = [
    "commit_id",
    "repository",
    "refactoring_type",
    "description",
    "side",
    "file_path",
    "start_line",
    "end_line",
    "code_element_type",
    "code_element",
    "location_desc",
]


def iter_rows(data: list[dict]):
    for entry in data:
        commit_id = entry.get("commit", "")
        repository = entry.get("repository", "")
        refactoring_type = entry.get("type", "")
        description = entry.get("description", "")

        for side_key, side_label in (
            ("leftSideLocations", "left"),
            ("rightSideLocations", "right"),
        ):
            for loc in entry.get(side_key, []):
                yield {
                    "commit_id": commit_id,
                    "repository": repository,
                    "refactoring_type": refactoring_type,
                    "description": description,
                    "side": side_label,
                    "file_path": loc.get("filePath", ""),
                    "start_line": loc.get("startLine", ""),
                    "end_line": loc.get("endLine", ""),
                    "code_element_type": loc.get("codeElementType", ""),
                    "code_element": loc.get("codeElement", ""),
                    "location_desc": loc.get("description", ""),
                }


def convert(input_path: Path, output_path: Path) -> int:
    with input_path.open(encoding="utf-8") as f:
        data = json.load(f)

    if not isinstance(data, list):
        print(f"Error: expected a JSON array at the top level, got {type(data).__name__}", file=sys.stderr)
        sys.exit(1)

    with output_path.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=FIELDNAMES)
        writer.writeheader()
        count = 0
        for row in iter_rows(data):
            writer.writerow(row)
            count += 1

    return count


def main():
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <input.json> [output.csv]", file=sys.stderr)
        sys.exit(1)

    input_path = Path(sys.argv[1])
    if not input_path.exists():
        print(f"Error: file not found: {input_path}", file=sys.stderr)
        sys.exit(1)

    output_path = Path(sys.argv[2]) if len(sys.argv) >= 3 else input_path.with_suffix(".csv")

    count = convert(input_path, output_path)
    print(f"Wrote {count} rows to {output_path}")


if __name__ == "__main__":
    main()
