## Context

RefactoringMiner already detects `ADD_*` and `REMOVE_*` type annotation refactorings for Python across three sites:

- **`UMLOperationDiff`** — parameter and return type annotations, using `hasTypeAnnotation()` on `UMLParameter` and `hasExplicitReturnTypeAnnotation()` on `UMLOperation`
- **`UMLAttributeDiff`** — class attribute annotations, using `hasExplicitTypeAnnotation()` on `UMLAttribute`
- **`UMLOperationBodyMapper`** — local variable annotations, using the `"typed"` modifier on `VariableDeclaration`

The `CHANGE_*` variants follow exactly the same detection structure: both before and after have an annotation present, but the type strings differ. The proposal introduces two new refactoring classes (`ChangeTypeAnnotationRefactoring` for parameters/return, `ChangeVariableTypeAnnotationRefactoring` for variables/attributes) and three new `RefactoringType` entries.

## Goals / Non-Goals

**Goals:**
- Detect when a Python type annotation is changed to a different type on a parameter, return position, class attribute, or local variable
- Reuse the flag infrastructure (`hasTypeAnnotation`, `hasExplicitReturnTypeAnnotation`, `hasExplicitTypeAnnotation`, `"typed"` modifier) already in place
- Mirror the structure of existing `AddTypeAnnotationRefactoring` and `AddVariableTypeAnnotationRefactoring` classes

**Non-Goals:**
- Detecting semantically equivalent type changes (e.g., `List[int]` vs `list[int]` treated as synonyms) — all textual differences are reported
- Supporting Java or TypeScript type annotation changes (Python-only, consistent with the existing add/remove detection)
- Detecting changes to decorator-based annotations (`@dataclass`, etc.)

## Decisions

### D1: Two new refactoring classes (not reusing existing ones)

`AddTypeAnnotationRefactoring` is already parameterised by `RefactoringType` (covering both ADD and REMOVE), but its `toString()` format is `"<type> in parameter/method/class"`. The CHANGE case requires capturing *both* the old type and the new type (e.g., `"List[int] to list[int]"`), which cannot fit the existing format without breaking it.

**Decision:** Create `ChangeTypeAnnotationRefactoring` (for parameters and return) and `ChangeVariableTypeAnnotationRefactoring` (for variables and attributes) as new classes, each storing `typeBefore` and `typeAfter`.

**Alternative considered:** Add an optional `typeBefore` field to the existing classes. Rejected because it complicates `toString()` and `equals()`/`hashCode()` logic for the common case.

### D2: Detection condition — both typed, types differ

For all three sites the predicate is:
- **Parameters**: `before.hasTypeAnnotation() && after.hasTypeAnnotation() && !before.getType().equals(after.getType())`
- **Return**: `before.hasExplicitReturnTypeAnnotation() && after.hasExplicitReturnTypeAnnotation() && !before.getReturnParameter().getType().equals(after.getReturnParameter().getType())`
- **Attributes**: `removedAttr.hasExplicitTypeAnnotation() && addedAttr.hasExplicitTypeAnnotation() && !removedAttr.getType().equals(addedAttr.getType())`
- **Local variables**: both have `"typed"` modifier and types differ

Type comparison uses `.equals()` on the `UMLType` object (which compares by classifier string). This is consistent with how existing `CHANGE_PARAMETER_TYPE` detection works in the Java path.

### D3: toString() format

Consistent with the existing family:
```
Change Parameter Type Annotation    List[int] to list[int] in parameter items in method process() from class Processor
Change Return Type Annotation       int to float in method compute() from class Calculator
Change Variable Type Annotation     int to float in variable count in method process() from class DataProcessor
Change Variable Type Annotation     List[str] to list[str] in attribute items from class Container
```

### D4: Detection ordering — CHANGE checked before ADD/REMOVE

At each detection site, the CHANGE check is inserted *before* the existing ADD/REMOVE checks, so a parameter that has both annotations present but different is classified as CHANGE, not as an ADD plus a REMOVE of the same parameter.

## Risks / Trade-offs

- **Type alias resolution**: `List[int]` (from `typing`) and `list[int]` (built-in) are semantically equivalent but textually different. This implementation reports them as CHANGE. Alias-aware comparison is out of scope.
- **UMLType equality**: Relies on `UMLType.equals()` behaving correctly for Python type strings (including generics like `list[str]`). Verified in existing tests for the add/remove path.
- **Local variable deduplication**: The `processedNames` / `removedProcessedNames` pattern from the ADD/REMOVE loops is extended with a `changedProcessedNames` set using the same approach to avoid duplicate CHANGE events for re-assigned variables.
