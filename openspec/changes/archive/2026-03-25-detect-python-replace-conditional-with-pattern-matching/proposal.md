## Why

Python 3.10 introduced structural pattern matching (`match`/`case`) as a modern alternative to chains of `if-elif-else` statements. Detecting when developers replace conventional conditionals with `match` statements is a valuable refactoring signal for tools that analyze code evolution, as it captures an intentional modernization of control flow.

## What Changes

- Add a new `RefactoringType` enum entry: `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING`
- Add a new `ReplaceConditionalWithPatternMatchingRefactoring` class that records the replaced `if`/`elif` composite statements and the new `match` statement
- Add detection logic in `UMLOperationBodyMapper` to identify when one or more `IF_STATEMENT` composite nodes in version 1 are mapped to a `SWITCH_STATEMENT` (which represents `match` in Python) in version 2
- Add a test class `TestReplaceConditionalWithPatternMatchingRefactoring` with representative Python commit examples

## Capabilities

### New Capabilities
- `replace-conditional-with-pattern-matching`: Detection of the refactoring where `if`/`elif`/`else` chains in Python are replaced by a `match`/`case` structural pattern matching statement

### Modified Capabilities
*(none)*

## Impact

- `src/main/java/org/refactoringminer/api/RefactoringType.java` — new enum constant
- `src/main/java/gr/uom/java/xmi/diff/` — new `ReplaceConditionalWithPatternMatchingRefactoring.java`
- `src/main/java/gr/uom/java/xmi/decomposition/UMLOperationBodyMapper.java` — detection logic leveraging the existing `ifToSwitch` helper and `SWITCH_STATEMENT` / `IF_STATEMENT` `CodeElementType` mapping
- `src/test/java/org/refactoringminer/test/` — new test class
- No breaking changes; no new external dependencies
