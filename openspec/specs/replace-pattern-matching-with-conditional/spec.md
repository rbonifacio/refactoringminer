### Requirement: Detect Replace Pattern Matching With Conditional
RefactoringMiner SHALL detect the `Replace Pattern Matching With Conditional` refactoring when a Python `match`/`case` structural pattern matching statement in version 1 is replaced by an `if`/`elif`/`else` chain in the same function in version 2.

#### Scenario: Simple match replaced by if-elif-else
- **WHEN** a Python function contains a `match` statement on a subject expression in version 1
- **WHEN** the same function in version 2 replaces that `match` statement with an `if`/`elif`/`else` chain testing the same subject expression
- **THEN** exactly one `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring SHALL be reported

#### Scenario: Match with multiple case branches replaced by elif chain
- **WHEN** a Python function contains a `match` statement with three or more `case` branches in version 1
- **WHEN** the same function in version 2 collapses all branches into an `if`/`elif`/`else` chain
- **THEN** exactly one `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring SHALL be reported

#### Scenario: Match with guard clause replaced by if-elif
- **WHEN** version 1 contains a `match` statement where at least one `case` has an `if` guard clause (e.g., `case x if x > 0:`)
- **WHEN** version 2 replaces the entire `match` with an `if`/`elif` chain
- **THEN** exactly one `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring SHALL be reported

#### Scenario: Refactoring is scoped to Python files only
- **WHEN** a Java or Kotlin file undergoes a `switch`-to-`if` transformation
- **THEN** NO `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring SHALL be reported for those files

#### Scenario: No refactoring when match is unchanged
- **WHEN** the `match` statement in a Python function is identical in both versions
- **THEN** NO `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring SHALL be reported

#### Scenario: No refactoring when conditional is unchanged
- **WHEN** an `if`/`elif`/`else` chain in a Python function is identical in both versions
- **THEN** NO `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring SHALL be reported

### Requirement: Refactoring record captures before and after fragments
The `ReplacePatternMatchingWithConditionalRefactoring` record SHALL expose the `match` composite fragment from version 1 and the set of `if`/`elif` composite fragments from version 2, together with their enclosing `VariableDeclarationContainer` references.

#### Scenario: Before fragment contains the match composite
- **WHEN** a `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring is reported
- **THEN** `getCodeFragmentsBefore()` SHALL return exactly one `AbstractCodeFragment` whose `CodeElementType` is `SWITCH_STATEMENT`

#### Scenario: After fragments contain the if-chain composites
- **WHEN** a `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring is reported
- **THEN** `getCodeFragmentsAfter()` SHALL return at least one `AbstractCodeFragment` whose `CodeElementType` is `IF_STATEMENT`

#### Scenario: Enclosing function containers are populated
- **WHEN** a `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` refactoring is reported
- **THEN** `getOperationBefore()` and `getOperationAfter()` SHALL both be non-null and SHALL reference the same function (by qualified name)

### Requirement: RefactoringType enum entry
The `RefactoringType` enum SHALL contain a `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` constant with the display name `"Replace Pattern Matching With Conditional"`.

#### Scenario: Display name is correct
- **WHEN** `RefactoringType.REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL.getDisplayName()` is called
- **THEN** it SHALL return `"Replace Pattern Matching With Conditional"`

#### Scenario: toString description format
- **WHEN** `ReplacePatternMatchingWithConditionalRefactoring.toString()` is called
- **THEN** the result SHALL start with `"Replace Pattern Matching With Conditional"` and SHALL include the enclosing function name and class/module name
