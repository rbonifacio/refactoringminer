## Why

RefactoringMiner now detects when Python type annotations are added (`ADD_PARAMETER_TYPE_ANNOTATION`, `ADD_RETURN_TYPE_ANNOTATION`, `ADD_VARIABLE_TYPE_ANNOTATION`), but has no counterpart detection for their removal. Removing type annotations is a real developer action — it happens when hints prove incorrect, overly restrictive, or when code is simplified — and omitting the inverse refactorings leaves asymmetric coverage of Python's type hint lifecycle.

## What Changes

- Add `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring type: detects when a class attribute or local variable loses its type annotation (`x: int = 0` → `x = 0`)
- Add `REMOVE_PARAMETER_TYPE_ANNOTATION` refactoring type: detects when a function parameter loses its type annotation (`def f(x: int)` → `def f(x)`)
- Add `REMOVE_RETURN_TYPE_ANNOTATION` refactoring type: detects when a function return type annotation is removed (`def f() -> int:` → `def f():`)
- Reuse the existing detection infrastructure (`UMLAttributeDiff`, `UMLOperationDiff`, `UMLOperationBodyMapper`) — the same comparison points used for ADD, checked in reverse

## Capabilities

### New Capabilities

- `python-remove-variable-type-annotation`: Detection of type annotation removal on class attributes and local variables in Python (`x: int = 0` → `x = 0`)
- `python-remove-function-type-annotation`: Detection of type annotation removal on function parameters and return types in Python (`def f(x: int) -> int` → `def f(x)`)

### Modified Capabilities

_(none)_

## Impact

- **`RefactoringType.java`** — three new enum entries: `REMOVE_VARIABLE_TYPE_ANNOTATION`, `REMOVE_PARAMETER_TYPE_ANNOTATION`, `REMOVE_RETURN_TYPE_ANNOTATION`
- **`UMLAttributeDiff.java`** — add reverse check (was annotated, now is not)
- **`UMLOperationDiff.java`** — add reverse checks for parameters and return type
- **`UMLOperationBodyMapper.java`** — add reverse check for local variables
- New refactoring classes or reuse of `AddVariableTypeAnnotationRefactoring` / `AddTypeAnnotationRefactoring` with a type field (preferred: reuse with the new `REMOVE_*` type constants)
- New test resource files (Python `.py` pairs) and test cases
