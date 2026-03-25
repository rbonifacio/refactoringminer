## Context

The forward refactoring (`Replace Conditional With Pattern Matching`) was implemented in the sibling change and introduced `detectIfToPatternMatch` in `UMLOperationBodyMapper`. That method fires when an `IF_STATEMENT` in version 1 maps to a `SWITCH_STATEMENT` in version 2, using the match subject as a guard to avoid false positives, and is scoped to Python files via a `.py` suffix check.

This change implements the exact symmetric inverse: detecting when a `SWITCH_STATEMENT` (Python `match`) in version 1 is replaced by an `IF_STATEMENT` chain in version 2. The infrastructure (node types, flattener, detection call sites) is already in place — only the direction of the check needs to be reversed.

## Goals / Non-Goals

**Goals:**
- Detect `Replace Pattern Matching With Conditional` when a Python `match`/`case` statement is replaced by an `if`/`elif`/`else` chain
- Produce a `Refactoring` object recording the before fragment (`match`) and the after fragments (the `if` chain) together with enclosing containers
- Add `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` to `RefactoringType`
- Add tests with before/after Python file scenarios

**Non-Goals:**
- Handling Java/Kotlin switch-to-if transformations (different node semantics, already handled elsewhere)
- Detecting partial rewrites where only some `case` branches are converted

## Decisions

**D1 — Mirror `detectIfToPatternMatch` rather than generalising it**

The cleanest implementation is to add a new private method `detectPatternMatchToIf` in `UMLOperationBodyMapper` that is the exact mirror of `detectIfToPatternMatch`: it iterates unmatched `SWITCH_STATEMENT` nodes in version 1 and unmatched `IF_STATEMENT` nodes in version 2, applies the same Python-file guard and subject-in-condition heuristic, and emits a `ReplacePatternMatchingWithConditionalRefactoring`.

*Alternative considered:* Merge both directions into a single method with a direction parameter. Rejected because the two directions have subtly different iteration logic (outermost-IF guard applies to the after-side here), making a unified method harder to read and test independently.

**D2 — Outermost-IF guard applies to the after-side (version 2)**

In `detectIfToPatternMatch` the outermost-IF guard filters `innerNodes1`. Here the guard must filter `innerNodes2` — we want the outermost `IF_STATEMENT` in version 2 (the one that replaced the match), not a nested elif branch.

**D3 — Refactoring record class mirrors `ReplaceConditionalWithPatternMatchingRefactoring`**

`ReplacePatternMatchingWithConditionalRefactoring` holds:
- `Set<AbstractCodeFragment> codeFragmentsBefore` — the match composite
- `Set<AbstractCodeFragment> codeFragmentsAfter` — the if/elif composite(s)
- `VariableDeclarationContainer operationBefore` / `operationAfter`

The `toString` format will be:
```
Replace Pattern Matching With Conditional\tmatch <expr>:\twith\tif <expr>:\tin <elementType> <name> from class <class>
```

**D4 — Call site mirrors the forward refactoring**

`detectPatternMatchToIf` is called at the same two sites as `detectIfToPatternMatch`:
1. At the end of the `UMLOperation` constructor's inner-node reconciliation loop (after `processStreamAPIStatements`)
2. At the end of `processCompositeStatements` (after `processStreamAPIStatements`)

## Risks / Trade-offs

- **False positives for Java/Kotlin** — Same risk as the forward direction: Java and Kotlin also produce `SWITCH_STATEMENT` nodes. The `.py` suffix guard prevents misclassification.
  → Mitigation: same guard as forward direction.

- **Guard-clause if statements nested inside the match body** — The `match` body in version 1 may contain `case x if x > 0:` guard clauses which appear as nested `IF_STATEMENT` nodes. After replacement in version 2 the outer `if` chain is what we want to match; nested ifs inside branches should not interfere since we only look at top-level unmatched `IF_STATEMENT` nodes.
  → Mitigation: outermost-IF guard (parent must not be another `IF_STATEMENT`) on the version-2 side.

## Migration Plan

No data migration. Additive change with no breaking API changes. Rollout: merge to `structural-pattern-matching` branch, run existing tests to confirm no regressions.

## Open Questions

- Should the detection fire if the `match` statement had only one `case` (essentially an `if` with no `elif`)? The current approach would still fire since the subject-in-condition heuristic works for single-branch cases as well.
