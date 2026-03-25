## Context

RefactoringMiner parses Python function signatures via `PyDeclarationASTBuilder` and stores them as `UMLOperation`/`UMLParameter` objects. The existing model already has partial infrastructure for type annotations:

- `LangSingleVariableDeclaration` has a `hasTypeAnnotation: boolean` field (present but **never set** in the Python builder).
- `PyDeclarationASTBuilder.createParam()` sets `typeAnnotation = TypeObjectEnum.OBJECT` for unannotated parameters and the actual type for annotated ones — but does not set `hasTypeAnnotation = true` when an annotation is present.
- `UMLModelAdapter` uses `param.getTypeAnnotation().getName()` to construct the `UMLType` on `UMLParameter`, but never transfers the `hasTypeAnnotation` flag.
- `UMLOperationDiff` already detects return type changes (`returnTypeChanged`) and Java-style annotation additions, but has no concept of Python type hint addition.

The result: comparing `def add(x, y)` (before) to `def add(x: int, y: int) -> int` (after) currently looks like a return type change and/or parameter type changes — indistinguishable from a developer renaming/changing types rather than annotating for the first time.

## Goals / Non-Goals

**Goals:**
- Detect when a parameter gains an explicit type hint for the first time (`x` → `x: int`), emitting one `ADD_PARAMETER_TYPE_ANNOTATION` refactoring per newly annotated parameter.
- Detect when a return type annotation is added for the first time (no `->` → `-> int`), emitting one `ADD_RETURN_TYPE_ANNOTATION` refactoring per function.
- Reuse the existing model infrastructure as much as possible (the `hasTypeAnnotation` field already exists).
- Follow the existing refactoring detection pattern used for Java annotation refactorings.

**Non-Goals:**
- Detecting *removal* of type hints (out of scope for this change; can be added later as `REMOVE_*`).
- Detecting *changes* of an existing type hint (e.g., `x: int` → `x: float`); this is a separate "change type annotation" refactoring.
- Type hints on variables or class attributes (`x: int = 0` at module/class level).
- TypeScript or other languages; Python only.

## Decisions

### D1: Distinguish "no annotation" from "annotated as Object" using `hasTypeAnnotation`

**Decision:** Fix `PyDeclarationASTBuilder.createParam()` to call `setHasTypeAnnotation(true)` when `paramContext.annotation() != null`. Transfer this flag through `UMLModelAdapter` to a new `hasTypeAnnotation` field on `UMLParameter`.

**Alternatives considered:**
- *Sentinel type string*: Use a special sentinel like `"__unannotated__"` instead of `"Object"` when no annotation exists. Rejected: would contaminate the type system and break existing type comparisons in `equalReturnParameter()`.
- *String comparison only*: Check `type.equals("Object")` as a proxy for "unannotated". Rejected: a developer could write `x: object` (lowercase), and a parameter explicitly annotated as `Object` would be misidentified.

**Why `hasTypeAnnotation`:** The field already exists in `LangSingleVariableDeclaration` — it was designed for this purpose but never wired up.

### D2: Track explicit return type annotation in `LangMethodDeclaration`

**Decision:** Add a `hasExplicitReturnTypeAnnotation: boolean` field to `LangMethodDeclaration`. Set it to `true` in `PyDeclarationASTBuilder.visitFunction_def_raw()` only when `ctx.expression() != null` (i.e., a `->` clause is present). Transfer to a matching field on `UMLOperation` via `UMLModelAdapter`.

**Why:** Currently, when there is no `->`, `PyDeclarationASTBuilder` infers a return type (`"None"` or `"object"`). There is no way after the fact to tell whether the return type was explicit or inferred. This field preserves that distinction at the right point.

### D3: Two separate `RefactoringType` entries

**Decision:** Add `ADD_PARAMETER_TYPE_ANNOTATION` and `ADD_RETURN_TYPE_ANNOTATION` as two distinct enum entries in `RefactoringType`.

**Alternatives considered:**
- *Single `ADD_TYPE_ANNOTATION` entry with sub-type*: Simpler enum but harder to filter in downstream tools.
- *Reuse `CHANGE_PARAMETER_TYPE`*: Rejected — the semantics are different. Adding an annotation for the first time is not a type change; the runtime behaviour is identical before and after (Python type hints are not enforced at runtime).

**Description format:**
```
ADD_PARAMETER_TYPE_ANNOTATION:
  "Add Parameter Type Annotation {type} in parameter {param} in method {method} from class {class}"

ADD_RETURN_TYPE_ANNOTATION:
  "Add Return Type Annotation {type} in method {method} from class {class}"
```

### D4: Detection in `UMLOperationDiff.getRefactorings()`

**Decision:** Add Python-specific detection inside `UMLOperationDiff.getRefactorings()`, paired with `UMLParameterListDiff` iteration. For each matched parameter pair, if `!before.hasTypeAnnotation() && after.hasTypeAnnotation()`, emit `ADD_PARAMETER_TYPE_ANNOTATION`. For the return parameter, check `!removedOperation.hasExplicitReturnTypeAnnotation() && addedOperation.hasExplicitReturnTypeAnnotation()`.

**Why `UMLOperationDiff` rather than `UMLParameterDiff`:** `UMLParameterDiff` is Java-centric (handles `@Annotation` style); Python type hints are not Java annotations. Keeping Python-specific logic in `UMLOperationDiff` makes it easy to guard with a language check and avoids polluting the Java annotation detection path.

### D5: One `AddTypeAnnotationRefactoring` class, two roles

**Decision:** A single `AddTypeAnnotationRefactoring` class carries a `RefactoringType` (either `ADD_PARAMETER_TYPE_ANNOTATION` or `ADD_RETURN_TYPE_ANNOTATION`), the annotated type string, the parameter name (null for return), and the before/after `UMLOperation`.

**Why:** The same data shape covers both cases; branching on `getRefactoringType()` in `toString()` produces the correct description string for each.

## Risks / Trade-offs

- **`hasTypeAnnotation` not set for `createParam(NameContext, ...)` overload** — `PyDeclarationASTBuilder` has a second `createParam` overload (line 294) used for `*args` star-only parameters that have no annotation context. This overload should NOT set `hasTypeAnnotation = true`. Must be careful not to apply the fix to this overload.
- **Matching parameters across versions** — `UMLParameterListDiff` matches parameters by position/name. If a refactoring also renames a parameter while adding a type hint, the match may already be detected as a rename; the type annotation detection should still fire on the matched pair.
- **Return type inference ambiguity** — `def add(x, y): return x + y` produces inferred return type `"object"`, while `def add(x, y) -> object:` produces explicit `"object"`. After the fix, these will be distinguishable via `hasExplicitReturnTypeAnnotation`. No regression risk since the field is additive.
- **`self` / `cls` parameters** — `UMLModelAdapter` skips the first parameter for instance methods (via `paramOffset`). Type hints on `self` are rare and are skipped by the same offset, so no special handling needed.

## Open Questions

- Should we detect type hint *removal* (`REMOVE_PARAMETER_TYPE_ANNOTATION`) in this same change or defer to a follow-up? Current plan: defer.
- Should we support PEP 604 union types (`x: int | str`) and complex annotations (`x: list[int]`)? The type is stored as a raw string from the source, so these will be captured correctly without special handling. No action needed.
