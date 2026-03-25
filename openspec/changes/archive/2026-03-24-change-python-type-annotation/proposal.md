## Why

RefactoringMiner detects when Python type annotations are added or removed, but not when they are *changed*. Modifying an existing annotation — for example, replacing `List[int]` with `list[int]` (PEP 585) or `Optional[str]` with `str | None` (PEP 604) — is a common modernisation pattern that should be captured as a first-class refactoring alongside the add/remove counterparts.

## What Changes

- Add three new `RefactoringType` entries: `CHANGE_PARAMETER_TYPE_ANNOTATION`, `CHANGE_RETURN_TYPE_ANNOTATION`, `CHANGE_VARIABLE_TYPE_ANNOTATION`
- Add detection in `UMLOperationDiff` for parameter and return type annotation changes
- Add detection in `UMLAttributeDiff` for class attribute type annotation changes
- Add detection in `UMLOperationBodyMapper` for local variable type annotation changes
- New refactoring class `ChangeTypeAnnotationRefactoring` to represent parameter/return cases
- New refactoring class `ChangeVariableTypeAnnotationRefactoring` to represent variable/attribute cases
- Test scenarios covering all three detection sites

## Capabilities

### New Capabilities
- `python-change-function-type-annotation`: Detection of parameter and return type annotation changes in Python function definitions
- `python-change-variable-type-annotation`: Detection of variable and class attribute type annotation changes in Python

### Modified Capabilities

## Impact

- `src/main/java/org/refactoringminer/api/RefactoringType.java` — three new enum entries
- `src/main/java/gr/uom/java/xmi/diff/UMLOperationDiff.java` — change detection for parameters and return type
- `src/main/java/gr/uom/java/xmi/diff/UMLAttributeDiff.java` — change detection for attributes
- `src/main/java/gr/uom/java/xmi/decomposition/UMLOperationBodyMapper.java` — change detection for local variables
- New: `ChangeTypeAnnotationRefactoring.java`, `ChangeVariableTypeAnnotationRefactoring.java`
- New test classes and Python test resource files
