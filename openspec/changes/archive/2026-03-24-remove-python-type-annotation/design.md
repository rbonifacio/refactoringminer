## Context

RefactoringMiner already detects three Python type annotation addition refactorings:

| Refactoring | Detection point |
|---|---|
| `ADD_VARIABLE_TYPE_ANNOTATION` (attribute) | `UMLAttributeDiff.getRefactorings()` — checks `!removed.hasExplicitTypeAnnotation() && added.hasExplicitTypeAnnotation()` |
| `ADD_VARIABLE_TYPE_ANNOTATION` (local var) | `UMLOperationBodyMapper.getRefactorings()` — compares `vars1`/`vars2` by "typed" modifier presence |
| `ADD_PARAMETER_TYPE_ANNOTATION` | `UMLOperationDiff.getRefactorings()` — checks `!removedParam.hasTypeAnnotation() && addedParam.hasTypeAnnotation()` |
| `ADD_RETURN_TYPE_ANNOTATION` | `UMLOperationDiff.getRefactorings()` — checks `!removed.hasExplicitReturnTypeAnnotation() && added.hasExplicitReturnTypeAnnotation()` |

The REMOVE counterparts require reversing exactly these conditions and supplying the type string from the **before** version (the type being removed) rather than the after version.

Two refactoring classes are involved:
- `AddVariableTypeAnnotationRefactoring` — hardcodes `ADD_VARIABLE_TYPE_ANNOTATION` in `getRefactoringType()`; needs to be made type-parameterised to support `REMOVE_VARIABLE_TYPE_ANNOTATION`.
- `AddTypeAnnotationRefactoring` — already accepts `RefactoringType` as a constructor argument; can be reused directly with `REMOVE_PARAMETER_TYPE_ANNOTATION` and `REMOVE_RETURN_TYPE_ANNOTATION`.

## Goals / Non-Goals

**Goals:**
- Add three new `RefactoringType` enum entries: `REMOVE_VARIABLE_TYPE_ANNOTATION`, `REMOVE_PARAMETER_TYPE_ANNOTATION`, `REMOVE_RETURN_TYPE_ANNOTATION`
- Detect all three removal cases with the same precision and false-positive avoidance as the ADD counterparts
- Reuse existing refactoring classes where possible to minimise new code

**Non-Goals:**
- Detecting *changes* to type annotations (e.g., `int` → `str`) — that is a separate concern already handled by `ChangeVariableType` / `ChangeReturnType`
- Supporting non-Python files
- Detecting removal of PEP 695 `type X = ...` type alias statements

## Decisions

### Decision 1: Parameterise `AddVariableTypeAnnotationRefactoring` rather than creating a new class

`AddVariableTypeAnnotationRefactoring` currently hardcodes `getRefactoringType()` to return `ADD_VARIABLE_TYPE_ANNOTATION`. Add a `RefactoringType type` field and constructor parameter, defaulting existing call sites to `ADD_VARIABLE_TYPE_ANNOTATION`. The `toString()` prefix already uses `getName()` which delegates to `type.getDisplayName()`, so description generation will be correct automatically.

**Alternative considered:** Create a separate `RemoveVariableTypeAnnotationRefactoring` class. Rejected because it would be a line-for-line duplicate with only the enum constant differing — pure boilerplate with no benefit.

### Decision 2: Source of the "annotated type" string for REMOVE refactorings

For ADD refactorings, the type string comes from the **after** model (the newly added annotation). For REMOVE refactorings, the type string MUST come from the **before** model (the annotation being lost). Concretely:
- Attribute: `removedAttribute.getVariableDeclaration().getType().toString()`
- Parameter: `removedParam.getType().toString()`
- Return: `removed.getReturnParameter().getType().toString()`
- Local variable: `vd1.getType().toString()` (vd1 is always the before-version declaration)

### Decision 3: Detection condition is a strict reversal

The REMOVE check is the logical negation of the ADD check at each site:

| Site | ADD condition | REMOVE condition |
|---|---|---|
| `UMLAttributeDiff` | `!removed.hasExplicitTypeAnnotation() && added.hasExplicitTypeAnnotation()` | `removed.hasExplicitTypeAnnotation() && !added.hasExplicitTypeAnnotation()` |
| `UMLOperationDiff` (param) | `!removedParam.hasTypeAnnotation() && addedParam.hasTypeAnnotation()` | `removedParam.hasTypeAnnotation() && !addedParam.hasTypeAnnotation()` |
| `UMLOperationDiff` (return) | `!removed.hasExplicitReturnTypeAnnotation() && added.hasExplicitReturnTypeAnnotation()` | `removed.hasExplicitReturnTypeAnnotation() && !added.hasExplicitReturnTypeAnnotation()` |
| `UMLOperationBodyMapper` | `!before_typed && after_typed` | `before_typed && !after_typed` |

No new flags or AST changes are needed — all required information is already present in the model.

### Decision 4: Local variable REMOVE detection uses the same `processedNames` deduplication

The existing ADD detection in `UMLOperationBodyMapper` iterates `vars1` looking for untyped variables that become typed in `vars2`. The REMOVE detection iterates `vars1` looking for **typed** variables that are **untyped** in `vars2`. The same `processedNames` guard per variable name prevents duplicate refactorings from re-assignments.

## Risks / Trade-offs

- **Type string from `Object` default** → If a variable's `before` declaration was created via the `LangAssignment` path (no annotation) its type is `"Object"`. However, REMOVE detection only fires when `before_typed == true` (i.e., `before` has the "typed" modifier), which is only set in the `LangSingleVariableDeclaration` path. So a `before` variable will always carry the real annotation type when REMOVE fires. Risk: low.

- **Modifying `AddVariableTypeAnnotationRefactoring` is a compile-wide change** → All existing call sites pass two `VariableDeclaration` and two `VariableDeclarationContainer` arguments; adding a `RefactoringType` parameter requires updating those call sites. There are three call sites (attribute diff, local var mapper, and potentially VariableReplacementAnalysis). All are within this codebase and are straightforward to update. Risk: low.

- **`AddTypeAnnotationRefactoring` reuse for REMOVE** → The class is named "Add…" but will now also represent "Remove…" refactorings. This naming mismatch is a minor code-smell. Acceptable because the class is package-private to `gr.uom.java.xmi.diff` and is not part of a public API. A rename to `TypeAnnotationRefactoring` is a possible follow-up but is out of scope here.

## Open Questions

_(none — the existing ADD infrastructure provides a complete and stable template for the REMOVE implementation)_
