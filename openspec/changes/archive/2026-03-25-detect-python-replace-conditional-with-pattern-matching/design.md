## Context

RefactoringMiner already represents Python's `match` statement as a `LangSwitchStatement` with `CodeElementType.SWITCH_STATEMENT`, and Python's `if`/`elif`/`else` chains as `LangIfStatement` with `CodeElementType.IF_STATEMENT`. The `UMLOperationBodyMapper` has an existing `ifToSwitch` helper that recognises IF↔SWITCH pairs, and `identicalBody` already returns `false` for cross-type IF/SWITCH comparisons to prevent false merges.

The Python parser (`PythonParser`) parses `match_stmt` into the `LangSwitchStatement` / `LangCaseStatement` node tree. All the structural prerequisites are in place — what is missing is a dedicated `RefactoringType`, a `Refactoring` record class, and detection logic that fires when an `IF_STATEMENT` composite in version 1 is mapped (structurally) to a `SWITCH_STATEMENT` in version 2.

## Goals / Non-Goals

**Goals:**
- Detect `Replace Conditional With Pattern Matching` when one or more `if`/`elif`/`else` composite statements are replaced by a single `match`/`case` block in Python
- Produce a `Refactoring` object that records the before fragments (the if-chain) and the after fragment (the match statement), together with the enclosing method/function containers
- Add an entry to `RefactoringType` with a stable display name usable in descriptions and regex matching
- Add a test class with before/after Python file scenarios exercising the refactoring

**Non-Goals:**
- Supporting the inverse detection (match → if-chain); that would be a separate refactoring type
- Handling Java or Kotlin switch expressions (they use different node types and separate detection paths already exist)
- Modifying any AST builder or parser logic — the representation of `match` as `SWITCH_STATEMENT` is already correct

## Decisions

**D1 — Reuse `SWITCH_STATEMENT` rather than introducing a new node type**

The `match` statement is already mapped to `SWITCH_STATEMENT` by `PyStatementASTBuilder.visitMatch_stmt`. Introducing a separate `MATCH_STATEMENT` node type would require changes across the AST layer, visitor hierarchy, flattener, and stringifier. Since the refactoring type name (not the node type) is what differentiates the Python match case from a Java/Kotlin switch, keeping the shared node type and distinguishing at the refactoring detection level is the right trade-off.

*Alternative considered:* Add a `MATCH_STATEMENT` `CodeElementType` specific to Python. Rejected because it would touch ~15 files across unrelated modules without improving detection quality.

**D2 — Detection site: `UMLOperationBodyMapper.detectIfToPatternMatch`**

Detection will be added as a dedicated method `detectIfToPatternMatch` called from the inner-node reconciliation phase of `UMLOperationBodyMapper`, analogous to where `ReplaceLoopWithPipelineRefactoring` is created. The detection predicate is:
- version-1 composite has `CodeElementType.IF_STATEMENT`
- version-2 composite has `CodeElementType.SWITCH_STATEMENT`
- the two composites share overlapping body content (checked via the existing `ifToSwitch` logic already used by `identicalBody`)

*Alternative considered:* Piggyback on `identicalBody` and emit the refactoring there. Rejected because `identicalBody` is a pure boolean predicate — grafting side-effects onto it would violate single responsibility and make it harder to test independently.

**D3 — Refactoring class shape mirrors `ReplaceLoopWithPipelineRefactoring`**

`ReplaceConditionalWithPatternMatchingRefactoring` will hold:
- `Set<AbstractCodeFragment> codeFragmentsBefore` — the if/elif composite(s)
- `Set<AbstractCodeFragment> codeFragmentsAfter` — the match composite
- `VariableDeclarationContainer operationBefore` / `operationAfter` — enclosing function

This matches the `MethodLevelRefactoring` interface contract and the pattern used by `ReplaceLoopWithPipelineRefactoring`, making the new class immediately compatible with all downstream consumers (JSON serialisation, `leftSide`/`rightSide` code ranges, display name rendering).

**D4 — `toString` format**

```
Replace Conditional With Pattern Matching    if <expr>:    with    match <expr>:    in method <name> from class <class>
```

The display name is `"Replace Conditional With Pattern Matching"`. The regex for `RefactoringType` will match:
```
Replace Conditional With Pattern Matching in (method|function) (.+) from (class|module) (.+)
```

**D5 — Test scenarios use local before/after Python files**

Following the pattern of `TestAddTypeAnnotationRefactoring`, test fixtures are stored under `src/test/resources/python/replace-conditional-with-pattern-matching/scenario<N>/before|after/<file>.py`. Tests call `UMLModelASTReader` directly, diff the two models, and assert the expected refactoring type and count.

## Risks / Trade-offs

- **False positives for Java/Kotlin switch** — Java and Kotlin also produce `SWITCH_STATEMENT` nodes. The detection guard must confirm the source file is Python (`.py` extension via `LocationInfo.getFilePath()`), or confirm the node's `getString()` starts with `match ` (Python keyword). Without this guard, a Java if→switch refactoring could be misclassified.
  → Mitigation: add a file-path suffix check (`filePath.endsWith(".py")`) in `detectIfToPatternMatch` before emitting the refactoring.

- **Partial matches (only some branches migrate)** — A developer might move only a subset of `elif` branches into a `match` statement while leaving one branch as an `if`. The before fragments set may include multiple IF_STATEMENT nodes while the after set contains one SWITCH_STATEMENT.
  → Mitigation: the `Set<AbstractCodeFragment>` design already supports multiple before-side fragments; no special handling needed.

- **Guard clauses in `match`** — Python `match` case blocks can include `if` guards (`case x if x > 0:`). These appear as nested `IF_STATEMENT` nodes inside the SWITCH body and should not trigger a spurious second detection.
  → Mitigation: detection only fires at the top-level composite pairing (IF_STATEMENT ↔ SWITCH_STATEMENT), not recursively within the match body.

## Migration Plan

No data migration needed. This is a net-new detection path with no breaking API changes. The new `RefactoringType` constant is additive to the enum; the `ALL` array can be updated to include it if desired for testing coverage tools.

Rollout: merge to `structural-pattern-matching` branch, run existing test suite to confirm no regressions on Java/Kotlin paths, then add the new Python test scenarios.

## Open Questions

- Should `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` be added to the `RefactoringType.ALL` array? Currently that array is used in some test harnesses to enumerate all detectable types. Adding it would make it visible to those harnesses immediately.
- Is there a need to detect the inverse (match → if)? If yes, a symmetric `ReplacePatternMatchingWithConditionalRefactoring` can follow the same pattern in a subsequent change.
