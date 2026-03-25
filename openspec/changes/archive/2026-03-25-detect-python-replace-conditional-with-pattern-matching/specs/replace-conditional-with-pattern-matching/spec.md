## ADDED Requirements

### Requirement: Detect Replace Conditional With Pattern Matching
RefactoringMiner SHALL detect the `Replace Conditional With Pattern Matching` refactoring when one or more `if`/`elif`/`else` composite statements in a Python function are replaced by a single `match`/`case` structural pattern matching statement across two versions of the same file.

#### Scenario: Simple if-elif-else replaced by match
- **WHEN** a Python function contains an `if`/`elif`/`else` chain testing a single subject expression across multiple branches in version 1
- **WHEN** the same function in version 2 replaces that chain with a `match` statement on the same subject expression
- **THEN** exactly one `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring SHALL be reported

#### Scenario: Multiple elif branches replaced by match
- **WHEN** a Python function contains three or more `elif` branches in version 1
- **WHEN** the same function in version 2 collapses all branches into a `match` statement
- **THEN** exactly one `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring SHALL be reported

#### Scenario: Match with guard clause does not produce false positives
- **WHEN** version 2 contains a `match` statement with `case` patterns that include `if` guard clauses (e.g., `case x if x > 0:`)
- **THEN** the nested guard `if` SHALL NOT be reported as an additional refactoring
- **THEN** only the top-level `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring SHALL be reported

#### Scenario: Refactoring is scoped to Python files only
- **WHEN** a Java or Kotlin file undergoes an `if`-to-`switch` transformation
- **THEN** NO `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring SHALL be reported for those files

#### Scenario: No refactoring when conditional is unchanged
- **WHEN** the `if`/`elif`/`else` chain in a Python function is identical in both versions
- **THEN** NO `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring SHALL be reported

#### Scenario: No refactoring when match is unchanged
- **WHEN** a `match` statement in a Python function is identical in both versions
- **THEN** NO `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring SHALL be reported

### Requirement: Refactoring record captures before and after fragments
The `ReplaceConditionalWithPatternMatchingRefactoring` record SHALL expose the set of `if`/`elif` composite fragments from version 1 and the `match` composite fragment from version 2, together with their enclosing `VariableDeclarationContainer` references (the function in which the refactoring occurred).

#### Scenario: Before fragments contain the if-chain composites
- **WHEN** a `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring is reported
- **THEN** `getCodeFragmentsBefore()` SHALL return at least one `AbstractCodeFragment` whose `CodeElementType` is `IF_STATEMENT`

#### Scenario: After fragment contains the match composite
- **WHEN** a `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring is reported
- **THEN** `getCodeFragmentsAfter()` SHALL return exactly one `AbstractCodeFragment` whose `CodeElementType` is `SWITCH_STATEMENT`

#### Scenario: Enclosing function containers are populated
- **WHEN** a `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` refactoring is reported
- **THEN** `getOperationBefore()` and `getOperationAfter()` SHALL both be non-null and SHALL reference the same function (by qualified name)

### Requirement: RefactoringType enum entry
The `RefactoringType` enum SHALL contain a `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` constant with the display name `"Replace Conditional With Pattern Matching"`.

#### Scenario: Display name is correct
- **WHEN** `RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING.getDisplayName()` is called
- **THEN** it SHALL return `"Replace Conditional With Pattern Matching"`

#### Scenario: toString description format
- **WHEN** `ReplaceConditionalWithPatternMatchingRefactoring.toString()` is called
- **THEN** the result SHALL start with `"Replace Conditional With Pattern Matching"` and SHALL include the enclosing function name and class/module name
