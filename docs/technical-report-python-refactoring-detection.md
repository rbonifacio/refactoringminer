# Technical Report: Python Refactoring Detection in RefactoringMiner

## Overview

This report describes two sets of new refactoring detection capabilities added to RefactoringMiner for Python source code. The changes were implemented across two sessions and cover nine new refactoring types: six related to **PEP 484/526 type hint annotations** and three related to **structural pattern matching** (Python 3.10+).

---

## 1. Motivation

### 1.1 Type Hint Annotations

Python introduced optional type hints via [PEP 484](https://peps.python.org/pep-0484/) (Python 3.5) and variable annotations via [PEP 526](https://peps.python.org/pep-0526/) (Python 3.6). Since then, two major forces have driven annotation-related changes in real-world Python codebases:

1. **Gradual typing adoption**: Teams incrementally annotate previously unannotated functions and variables as they mature their codebase or integrate static analysis tools (mypy, pyright, Pytype).
2. **Modernisation from `typing` module to built-in generics**: Python 3.9 ([PEP 585](https://peps.python.org/pep-0585/)) allowed `list[int]` instead of `typing.List[int]`, and Python 3.10 ([PEP 604](https://peps.python.org/pep-0604/)) introduced the `X | Y` union syntax as a replacement for `typing.Optional[X]` / `typing.Union[X, Y]`. Many projects have been migrating to these shorter, import-free forms.

RefactoringMiner already detected analogous changes in Java (e.g., `Change Parameter Type`). Extending detection to Python type annotations closes this gap and enables empirical studies of typing evolution in Python projects.

### 1.2 Structural Pattern Matching

Python 3.10 introduced structural pattern matching (`match`/`case`) via [PEP 634](https://peps.python.org/pep-0634/). It provides a more expressive alternative to long `if`/`elif`/`else` chains when dispatching on the shape or value of data. Developers may migrate in either direction:

- **Forward**: replace a chain of equality comparisons with a `match` statement when the logic maps cleanly to case patterns.
- **Backward**: revert a `match` statement to a conditional chain, for instance when targeting older Python interpreters or when the conditional form is judged more readable.

Detecting both directions allows researchers and tool authors to study adoption patterns of this language feature in the wild.

---

## 2. Implemented Refactoring Types

### 2.1 Type Hint Annotation Refactorings (nine new `RefactoringType` entries)

| Refactoring Type | Trigger |
|---|---|
| `ADD_PARAMETER_TYPE_ANNOTATION` | A parameter that had no type hint gains one |
| `REMOVE_PARAMETER_TYPE_ANNOTATION` | A parameter loses its type hint |
| `CHANGE_PARAMETER_TYPE_ANNOTATION` | A parameter's type hint changes (e.g., `List[int]` → `list[int]`) |
| `ADD_RETURN_TYPE_ANNOTATION` | A function gains a `-> T` return annotation |
| `REMOVE_RETURN_TYPE_ANNOTATION` | A function loses its `-> T` return annotation |
| `CHANGE_RETURN_TYPE_ANNOTATION` | A function's return annotation changes |
| `ADD_VARIABLE_TYPE_ANNOTATION` | A local variable or class attribute gains `: T` annotation |
| `REMOVE_VARIABLE_TYPE_ANNOTATION` | A local variable or class attribute loses its annotation |
| `CHANGE_VARIABLE_TYPE_ANNOTATION` | A variable's annotation changes |

### 2.2 Structural Pattern Matching Refactorings (two new `RefactoringType` entries)

| Refactoring Type | Trigger |
|---|---|
| `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` | An `if`/`elif`/`else` chain is replaced by a `match`/`case` statement |
| `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` | A `match`/`case` statement is replaced by an `if`/`elif`/`else` chain |

---

## 3. Key Examples

### 3.1 Add Parameter and Return Type Annotations

A function with no type information gains full annotations — a typical first step when adopting static analysis tooling.

**Before:**
```python
class Calculator:
    def add(self, x, y):
        return x + y
```

**After:**
```python
class Calculator:
    def add(self, x: int, y: int) -> int:
        return x + y
```

Detected refactorings:
- `ADD_PARAMETER_TYPE_ANNOTATION` for `x` (added `: int`)
- `ADD_PARAMETER_TYPE_ANNOTATION` for `y` (added `: int`)
- `ADD_RETURN_TYPE_ANNOTATION` (added `-> int`)

---

### 3.2 Add Return Type Annotation Only

A function already has parameter annotations but is missing a return annotation — a common partial-typing scenario.

**Before:**
```python
class DataService:
    def get_name(self):
        return ""
```

**After:**
```python
class DataService:
    def get_name(self) -> str:
        return ""
```

Detected refactoring:
- `ADD_RETURN_TYPE_ANNOTATION` (added `-> str`)

---

### 3.3 Remove All Type Annotations

The reverse: all annotations are stripped from a function, for example when removing a static analysis dependency or simplifying a prototype.

**Before:**
```python
class Calculator:
    def add(self, x: int, y: int) -> int:
        return x + y
```

**After:**
```python
class Calculator:
    def add(self, x, y):
        return x + y
```

Detected refactorings:
- `REMOVE_PARAMETER_TYPE_ANNOTATION` for `x`
- `REMOVE_PARAMETER_TYPE_ANNOTATION` for `y`
- `REMOVE_RETURN_TYPE_ANNOTATION`

---

### 3.4 Change Parameter Type — `typing` module to built-in generics (PEP 585)

A common modernisation pattern when dropping support for Python < 3.9.

**Before:**
```python
from typing import List

class Calculator:
    def add(self, x: List[int], y: List[int]) -> List[int]:
        return x + y
```

**After:**
```python
class Calculator:
    def add(self, x: list[int], y: list[int]) -> list[int]:
        return x + y
```

Detected refactorings:
- `CHANGE_PARAMETER_TYPE_ANNOTATION` for `x`: `List[int]` → `list[int]`
- `CHANGE_PARAMETER_TYPE_ANNOTATION` for `y`: `List[int]` → `list[int]`
- `CHANGE_RETURN_TYPE_ANNOTATION`: `List[int]` → `list[int]`

---

### 3.5 Change Parameter Type — `Optional[T]` to `T | None` (PEP 604)

**Before:**
```python
from typing import Optional

class Processor:
    def process(self, items: Optional[str]):
        pass
```

**After:**
```python
class Processor:
    def process(self, items: str | None):
        pass
```

Detected refactoring:
- `CHANGE_PARAMETER_TYPE_ANNOTATION` for `items`: `Optional[str]` → `str | None`

---

### 3.6 Change Variable Type Annotation — class attribute

**Before:**
```python
from typing import List

class Shape:
    items: List[str] = []
```

**After:**
```python
class Shape:
    items: list[str] = []
```

Detected refactoring:
- `CHANGE_VARIABLE_TYPE_ANNOTATION` for `items`: `List[str]` → `list[str]`

---

### 3.7 Change Variable Type Annotation — local variable

**Before:**
```python
class DataProcessor:
    def process(self):
        count: int = 0
        count = count + 1
        return count
```

**After:**
```python
class DataProcessor:
    def process(self):
        count: float = 0
        count = count + 1
        return count
```

Detected refactoring:
- `CHANGE_VARIABLE_TYPE_ANNOTATION` for `count`: `int` → `float`

---

### 3.8 Replace Conditional With Pattern Matching — simple value dispatch

The canonical motivating case: a chain of equality tests on a single variable is replaced by a `match` statement with literal patterns.

**Before:**
```python
class ShapeProcessor:
    def describe(self, shape):
        if shape == "circle":
            return "A round shape"
        elif shape == "square":
            return "A shape with four equal sides"
        elif shape == "triangle":
            return "A shape with three sides"
        else:
            return "Unknown shape"
```

**After:**
```python
class ShapeProcessor:
    def describe(self, shape):
        match shape:
            case "circle":
                return "A round shape"
            case "square":
                return "A shape with four equal sides"
            case "triangle":
                return "A shape with three sides"
            case _:
                return "Unknown shape"
```

Detected refactoring:
- `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` in function `describe` of class `ShapeProcessor`

---

### 3.9 Replace Conditional With Pattern Matching — with guard clauses

When at least one branch in the `match` uses an `if` guard (`case x if x < 0:`), the resulting code mixes literal patterns and guard-based dispatch. RefactoringMiner detects the refactoring as a single event and does not report the guards as additional refactorings.

**Before:**
```python
class Classifier:
    def classify(self, value):
        if value < 0:
            return "negative"
        elif value == 0:
            return "zero"
        elif value < 100:
            return "small positive"
        else:
            return "large positive"
```

**After:**
```python
class Classifier:
    def classify(self, value):
        match value:
            case x if x < 0:
                return "negative"
            case 0:
                return "zero"
            case x if x < 100:
                return "small positive"
            case _:
                return "large positive"
```

Detected refactoring:
- `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` in function `classify` of class `Classifier`

---

### 3.10 Replace Pattern Matching With Conditional — inverse direction

The same transformation in reverse. Useful for projects that revert to conditionals for compatibility with Python 3.9 or for readability preferences.

**Before:**
```python
class ShapeProcessor:
    def describe(self, shape):
        match shape:
            case "circle":
                return "A round shape"
            case "square":
                return "A shape with four equal sides"
            case "triangle":
                return "A shape with three sides"
            case _:
                return "Unknown shape"
```

**After:**
```python
class ShapeProcessor:
    def describe(self, shape):
        if shape == "circle":
            return "A round shape"
        elif shape == "square":
            return "A shape with four equal sides"
        elif shape == "triangle":
            return "A shape with three sides"
        else:
            return "Unknown shape"
```

Detected refactoring:
- `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` in function `describe` of class `ShapeProcessor`

---

## 4. Design and Implementation Notes

### 4.1 Type Annotations

Detection happens at three distinct AST levels, each handled by a dedicated diff class:

- **`UMLOperationDiff`** — parameters and return type. The diff compares matched `UMLParameter` objects from the before/after versions of a function and inspects the `hasTypeAnnotation` / `hasExplicitReturnTypeAnnotation` flags propagated by the Python AST builder (`PyDeclarationASTBuilder`).
- **`UMLAttributeDiff`** — class-level attributes. The diff inspects `UMLAttribute` annotation strings from both versions.
- **`UMLOperationBodyMapper`** — local variables. Variable declarations inside function bodies are compared via `VariableDeclaration` objects in the `VariableReplacementAnalysis` pass.

Two new refactoring record classes carry the results:

- **`AddTypeAnnotationRefactoring`** — parameterised by `RefactoringType` to cover both add and remove cases for parameters and return types.
- **`AddVariableTypeAnnotationRefactoring`** — covers local variables and class attributes.
- **`ChangeTypeAnnotationRefactoring`** — captures `typeBefore` and `typeAfter` for function-level changes.
- **`ChangeVariableTypeAnnotationRefactoring`** — captures `typeBefore` and `typeAfter` for variable-level changes.

The CHANGE variants are checked **before** the ADD/REMOVE variants so that a `List[int]` → `list[int]` change is not double-reported as a remove + add.

### 4.2 Structural Pattern Matching

The Python AST builder already represents `match`/`case` as `CodeElementType.SWITCH_STATEMENT` (via `LangSwitchStatement`), matching the conceptual equivalence with Java `switch`. `elif` branches are nested `LangIfStatement` nodes, giving `CodeElementType.IF_STATEMENT`.

Detection runs inside `UMLOperationBodyMapper` as two new private methods:

- **`detectIfToPatternMatch`** — scans unmatched composite nodes from v1 (IF_STATEMENT) and v2 (SWITCH_STATEMENT). For each pair, it applies a *subject-in-condition heuristic*: it checks whether the subject expression of the `match` statement (the first expression of the SWITCH node) appears as a substring of any condition expression of the `if` statement. Nested `elif` branches are excluded by skipping any IF_STATEMENT whose parent is also an IF_STATEMENT.
- **`detectPatternMatchToIf`** — the exact mirror, scanning SWITCH in v1 and outermost IF in v2.

Both methods are called from two sites in `UMLOperationBodyMapper` to cover the two code paths that Python method comparisons traverse: the direct `processInnerNodes` path used by the `UMLOperation` constructor, and the `processCompositeStatements` path.

---

## 5. Files Changed

### New production files
| File | Purpose |
|---|---|
| `gr.uom.java.xmi.diff.AddTypeAnnotationRefactoring` | Refactoring record for add/remove parameter and return annotations |
| `gr.uom.java.xmi.diff.AddVariableTypeAnnotationRefactoring` | Refactoring record for add/remove variable annotations |
| `gr.uom.java.xmi.diff.ChangeTypeAnnotationRefactoring` | Refactoring record for changed parameter/return annotations |
| `gr.uom.java.xmi.diff.ChangeVariableTypeAnnotationRefactoring` | Refactoring record for changed variable annotations |
| `gr.uom.java.xmi.diff.ReplaceConditionalWithPatternMatchingRefactoring` | Refactoring record for if→match |
| `gr.uom.java.xmi.diff.ReplacePatternMatchingWithConditionalRefactoring` | Refactoring record for match→if |

### Modified production files
| File | Change |
|---|---|
| `org.refactoringminer.api.RefactoringType` | 11 new enum constants and entries in the `ALL` array |
| `gr.uom.java.xmi.decomposition.UMLOperationBodyMapper` | `detectIfToPatternMatch`, `detectPatternMatchToIf`, and local-variable annotation detection |
| `gr.uom.java.xmi.diff.UMLOperationDiff` | Parameter and return annotation add/change/remove detection |
| `gr.uom.java.xmi.diff.UMLAttributeDiff` | Class attribute annotation add/change/remove detection |
| `gr.uom.java.xmi.UMLOperation` | `hasExplicitReturnTypeAnnotation` flag |
| `gr.uom.java.xmi.UMLParameter` | `hasTypeAnnotation` flag |
| `java.extension.umladapter.UMLModelAdapter` | Propagates annotation flags from AST nodes |
| `java.extension.ast.node.declaration.LangMethodDeclaration` | Stores return annotation presence |
| `java.extension.ast.node.declaration.LangSingleVariableDeclaration` | Stores parameter annotation presence |
| `java.extension.python.component.PyDeclarationASTBuilder` | Reads annotation presence from Python parse tree |

### New test files (8 test classes, ~30 test scenarios)
- `TestAddTypeAnnotationRefactoring`
- `TestAddVariableTypeAnnotationRefactoring`
- `TestRemoveFunctionTypeAnnotationRefactoring`
- `TestRemoveVariableTypeAnnotationRefactoring`
- `TestChangeFunctionTypeAnnotationRefactoring`
- `TestChangeVariableTypeAnnotationRefactoring`
- `TestReplaceConditionalWithPatternMatchingRefactoring`
- `TestReplacePatternMatchingWithConditionalRefactoring`
