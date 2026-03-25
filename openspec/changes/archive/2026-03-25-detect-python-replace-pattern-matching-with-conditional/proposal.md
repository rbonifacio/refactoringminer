## Why

The inverse of detecting `Replace Conditional With Pattern Matching` is equally important: developers sometimes revert Python `match`/`case` statements back to `if`/`elif`/`else` chains for compatibility (e.g., targeting Python < 3.10), readability preferences, or tooling constraints. Detecting this inverse refactoring completes the symmetric picture of structural-pattern-matching evolution in Python codebases.

## What Changes

- Add a new `RefactoringType` enum entry: `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL`
- Add a new `ReplacePatternMatchingWithConditionalRefactoring` class that records the replaced `match` statement (before) and the new `if`/`elif`/`else` chain (after)
- Extend the existing `detectIfToPatternMatch` method in `UMLOperationBodyMapper` (or add a symmetric `detectPatternMatchToIf` path) to also fire when a `SWITCH_STATEMENT` in version 1 maps to an `IF_STATEMENT` in version 2
- Add a test class `TestReplacePatternMatchingWithConditionalRefactoring` with representative Python commit examples

## Capabilities

### New Capabilities
- `replace-pattern-matching-with-conditional`: Detection of the refactoring where a `match`/`case` structural pattern matching statement in Python is replaced by an `if`/`elif`/`else` chain

### Modified Capabilities
*(none)*

## Impact

- `src/main/java/org/refactoringminer/api/RefactoringType.java` — new enum constant
- `src/main/java/gr/uom/java/xmi/diff/` — new `ReplacePatternMatchingWithConditionalRefactoring.java`
- `src/main/java/gr/uom/java/xmi/decomposition/UMLOperationBodyMapper.java` — extend or mirror detection logic from the forward refactoring; the symmetric check (SWITCH_STATEMENT in v1, IF_STATEMENT in v2) is the inverse of `detectIfToPatternMatch`
- `src/test/java/org/refactoringminer/test/` — new test class
- No breaking changes; no new external dependencies
