## Why

Python PEP 526 (Python 3.6+) allows developers to add type annotations to variable declarations (`x: int = 0`, `items: list[str] = []`), yet RefactoringMiner has no detection for this class of annotation refactoring. The recently added `detect-python-type-annotation` change covered function signatures; this change completes the picture by detecting when type hints are added to variables — both local variables inside method bodies and class-level attributes.

## What Changes

- Add a new `ADD_VARIABLE_TYPE_ANNOTATION` refactoring type to `RefactoringType` enum covering the addition of a type hint to an existing variable declaration (`x = 0` → `x: int = 0`).
- Add detection logic that compares before/after variable declarations in Python — both at the class attribute level (via `UMLAttributeDiff`) and at the local variable level (via statement-level diff in `UMLOperationBodyMapper`).
- Produce one `Refactoring` instance per newly annotated variable.
- Add test cases with Python before/after file pairs covering: class attribute annotation, local variable annotation, and combined cases.

## Capabilities

### New Capabilities

- `python-variable-type-annotation-detection`: Detection of type hint additions on Python variable declarations. A single `ADD_VARIABLE_TYPE_ANNOTATION` refactoring type covers both class-level attributes (`self.x = 0` / `x: int` in a class body) and local variables inside method bodies (`result = []` → `result: list[int] = []`). The description encodes whether the variable is an attribute or a local variable via the containing class and method context.

### Modified Capabilities

_(none)_

## Impact

- **`RefactoringType.java`** — new `ADD_VARIABLE_TYPE_ANNOTATION` enum entry.
- **`UMLAttribute` / `UMLAttributeDiff`** — must expose whether an attribute has an explicit type annotation; detection fires when `before` has no explicit type and `after` does.
- **`VariableDeclaration`** / Python statement diff — must expose type annotation presence for local variables; detection fires in the statement-mapping diff path.
- **`LangSingleVariableDeclaration`** — already has `hasTypeAnnotation` and `rawTypeAnnotationText` (added in `detect-python-type-annotation`); these will be reused for class attributes.
- **Python AST builder** — class-level annotated assignments (`x: int = 0`) need `hasTypeAnnotation` set on the resulting `VariableDeclaration`.
- New: `AddVariableTypeAnnotationRefactoring` class (implements `Refactoring`).
- **Test infrastructure** — new `.py` resource file pairs and test cases.
- No breaking changes to existing refactoring types or public APIs.
