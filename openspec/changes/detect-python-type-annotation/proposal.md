## Why

Python type hints (PEP 484, PEP 526) are now mainstream, and developers frequently annotate legacy, untyped codebases incrementally. RefactoringMiner already detects annotation-related refactorings for Java (e.g., `ADD_METHOD_ANNOTATION`, `ADD_PARAMETER_ANNOTATION`), but has no equivalent for Python's type hint syntax — leaving a significant class of Python refactorings undetected.

## What Changes

- Add a new `ADD_TYPE_ANNOTATION` refactoring type to `RefactoringType` enum covering the addition of type hints to Python function signatures (parameter types and/or return type).
- Add detection logic that compares before/after `UMLOperation` representations of Python functions and identifies when type annotations are introduced without other semantic changes.
- Produce one `Refactoring` instance per annotated element (parameter or return type) so that fine-grained history can be reconstructed.
- Add test cases with real Python before/after file pairs covering the three annotation sub-cases: parameter only, return type only, and both.

## Capabilities

### New Capabilities

- `python-type-annotation-detection`: Detection of type hint additions in Python function signatures, covering parameter annotations (`x: int`), return type annotations (`-> int`), and combined cases (`def add(x: int, y: int) -> int`). Reports one refactoring per annotated element.

### Modified Capabilities

_(none — no existing spec-level requirements change)_

## Impact

- **`RefactoringType.java`** — new enum constant(s) for `ADD_TYPE_ANNOTATION` (and optionally `ADD_RETURN_TYPE_ANNOTATION` if separate granularity is desired).
- **`UMLOperation` / Python parameter model** — must carry type annotation information extracted by `PyDeclarationASTBuilder`; verify that parameter type hints and return type annotations are already stored (partial support exists) or extend the model.
- **`UMLOperationDiff` or equivalent Python diff class** — comparison logic to detect annotation additions vs. other signature changes.
- **Refactoring class** — new `AddTypeAnnotationRefactoring` (implements `Refactoring`) for Python.
- **Test infrastructure** — new test resource files (Python `.py` pairs) and test cases in the existing test harness.
- No breaking changes to existing refactoring types or public APIs.
