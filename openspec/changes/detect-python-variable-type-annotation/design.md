## Context

Python annotated assignments (`x: int = 0`, `items: list[str] = []`) follow PEP 526. RefactoringMiner can already detect type annotations added to function *parameters* (via `LangSingleVariableDeclaration.hasTypeAnnotation`, added in `detect-python-type-annotation`). Variable annotations — both class-level attributes and local variables — are not yet tracked.

A codebase exploration revealed a critical gap: `PyExpressionASTBuilder.visitAssignment()` currently **discards the entire type annotation** when a `COLON` is present in an assignment. For `x: int = 0`, the method returns just `LangSimpleName("x")`, throwing away both the type (`int`) and the right-hand value (`0`). As a result, annotated class-level variables are not even recognised as attributes today. Fixing this data-loss is the prerequisite for all detection logic in this change.

Key existing infrastructure to reuse:
- `LangSingleVariableDeclaration.hasTypeAnnotation` and `rawTypeAnnotationText` (added in `detect-python-type-annotation`)
- `VariableDeclaration` already adds a `"typed"` `UMLModifier` when built from a `LangSingleVariableDeclaration` with `hasTypeAnnotation == true` (lines 150-158)
- `UMLAttributeDiff.getRefactorings()` already fires for type changes and annotation changes — we add a new check alongside those

## Goals / Non-Goals

**Goals:**
- Fix `PyExpressionASTBuilder.visitAssignment()` to preserve type annotation info for annotated assignments
- Detect when a Python class-level attribute gains a type annotation (`x = 0` → `x: int = 0`)
- Detect when a Python local variable inside a function gains a type annotation (`result = []` → `result: list[int] = []`)
- Emit one `ADD_VARIABLE_TYPE_ANNOTATION` refactoring per newly annotated variable
- Reuse the `hasTypeAnnotation` / `rawTypeAnnotationText` machinery from `LangSingleVariableDeclaration`

**Non-Goals:**
- Detecting removal of variable type annotations (defer to a follow-up)
- Detecting changes to an existing variable type annotation (separate refactoring)
- Stand-alone annotations without a value (`x: int` with no `= ...`) — deferred; the value-less form is rare in real refactorings
- TypeScript, Java, or other languages

## Decisions

### D1: Fix `visitAssignment()` to emit a `LangSingleVariableDeclaration` for annotated assignments

**Decision:** When `ctx.COLON() != null`, instead of returning only the target name, construct a `LangSingleVariableDeclaration` with:
- name = variable name from `ctx.name()` or `ctx.single_target()`
- type annotation = `ctx.expression()` text (the type between `:` and `=`)
- default value = the RHS from `ctx.annotated_rhs()` (if present)
- `hasTypeAnnotation = true`, `rawTypeAnnotationText` set to the raw type string

**Why a `LangSingleVariableDeclaration` rather than extending `LangAssignment`:** `LangSingleVariableDeclaration` already carries `hasTypeAnnotation` and `rawTypeAnnotationText`. The `UMLModelAdapter` and `VariableDeclaration` constructors already know how to handle it for parameters; we can extend that path to class attributes and local variables without introducing a new node type.

**Alternatives considered:**
- *Add a `hasTypeAnnotation` flag to `LangAssignment`*: Requires changing a shared node type used across all languages; higher risk of unintended side effects.
- *Keep returning `LangSimpleName` but attach metadata*: `LangSimpleName` has no extension points for type info; would require a new node type anyway.

### D2: Class attribute detection via `UMLAttributeDiff`

**Decision:** Add a `hasExplicitTypeAnnotation` boolean field to `UMLAttribute`. Populate it in `UMLModelAdapter.processClassLevelAssignmentForAttribute()` when the body statement is a `LangSingleVariableDeclaration` (i.e., the result of the fixed `visitAssignment()`). Detect in `UMLAttributeDiff.getRefactorings()`: if `!removed.hasExplicitTypeAnnotation() && added.hasExplicitTypeAnnotation()`, emit `ADD_VARIABLE_TYPE_ANNOTATION`.

**Why `UMLAttribute` rather than using the existing `"typed"` modifier on `VariableDeclaration`:** The `"typed"` modifier is already added by `VariableDeclaration` when built from a `LangSingleVariableDeclaration` with `hasTypeAnnotation`. However, `UMLAttributeDiff` compares `UMLAttribute` objects directly and does not currently inspect modifiers on their `VariableDeclaration`. Adding a dedicated boolean field on `UMLAttribute` keeps the detection self-contained and explicit, following the same pattern used by `UMLOperation.hasExplicitReturnTypeAnnotation` in the previous change.

### D3: Local variable detection via `VariableDeclaration` "typed" modifier

**Decision:** For local variables, reuse the existing `"typed"` `UMLModifier` that `VariableDeclaration` already adds when built from a `LangSingleVariableDeclaration` with `hasTypeAnnotation == true`. After fix D1, annotated local variables inside function bodies will also be represented as `LangSingleVariableDeclaration` nodes, so `VariableDeclaration` will automatically gain the `"typed"` modifier. Detection fires in `UMLOperationBodyMapper` by comparing matched variable declarations: if `before` has no `"typed"` modifier and `after` does, emit `ADD_VARIABLE_TYPE_ANNOTATION`.

**Why leverage the modifier rather than a dedicated flag:** The `"typed"` modifier path is already in place for parameters (`VariableDeclaration` lines 150-158); extending it to local variables via D1 is zero additional model change. A dedicated flag on `VariableDeclaration` would duplicate what the modifier already encodes.

### D4: Single `ADD_VARIABLE_TYPE_ANNOTATION` refactoring type

**Decision:** One new `RefactoringType` entry covering both class attributes and local variables. The description format encodes the context:
- For attributes: `"Add Variable Type Annotation {type} in attribute {name} from class {class}"`
- For local variables: `"Add Variable Type Annotation {type} in variable {name} in method {method} from class {class}"`

**Why not split into two types:** Consistent with `ADD_VARIABLE_ANNOTATION` in Java, which uses one type for both. The contextual distinction (attribute vs. local) is captured in the description string and is recoverable from the `VariableDeclaration.isAttribute()` flag on the refactoring object.

### D5: New `AddVariableTypeAnnotationRefactoring` class

**Decision:** Follow the pattern of `AddVariableAnnotationRefactoring`: constructor takes `VariableDeclaration varBefore`, `VariableDeclaration varAfter`, `VariableDeclarationContainer operationBefore/After`. `getRefactoringType()` always returns `ADD_VARIABLE_TYPE_ANNOTATION`. `toString()` branches on `varAfter.isAttribute()` to produce the correct description format.

## Risks / Trade-offs

- **`visitAssignment()` change is broad** — This method is called for all Python assignment parsing, not just annotated ones. The fix is guarded by `ctx.COLON() != null` so non-annotated assignments are unaffected, but it changes what was previously a no-op (discarded annotation) into an active node construction. All existing Python tests must pass after the fix. → Mitigation: run `TestPythonDatasetRefactorings` as the regression gate.
- **Stand-alone annotations (`x: int` with no value) are excluded** — In the grammar, `x: int` without `=` also hits the `COLON` branch. For now, only annotated assignments with a value are handled. Stand-alone annotations are rare as a *refactoring* (they add no behaviour); skipping them avoids the complexity of creating a `LangSingleVariableDeclaration` with no `defaultValue`. → Mitigation: the `COLON` guard can be extended later; document as a known limitation.
- **`processClassLevelAssignmentForAttribute()` currently only handles `LangAssignment`** — After D1, annotated assignments produce `LangSingleVariableDeclaration` instead. The adapter must be updated to handle both node types to avoid silently dropping annotated attributes. → Mitigation: explicit `instanceof` check in the adapter.
- **Matched variable pairs in `UMLOperationBodyMapper`** — The statement-level diff matches code fragments by their string representation. After D1, an annotated local variable (`x: int = 0`) will have a different string representation than the un-annotated one (`x = 0`). The matcher must still pair them correctly. → Mitigation: verify with existing statement-mapping tests; the mapper uses edit-distance fallback for near-miss matches.

## Open Questions

- Should stand-alone annotations (`x: int`) with no right-hand value also be detected as a refactoring? Current decision: no — defer to a follow-up.
- For `self.x: int = value` (annotated instance attribute assignment), the `COLON` branch in `visitAssignment()` returns `single_target` — does `single_target` preserve the `self.x` qualified name correctly? Needs a targeted test during implementation.
