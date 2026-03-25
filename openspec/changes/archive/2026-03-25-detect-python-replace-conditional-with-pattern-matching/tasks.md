## 1. RefactoringType Enum

- [x] 1.1 Add `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING("Replace Conditional With Pattern Matching", "Replace Conditional With Pattern Matching in (method|function) (.+) from (class|module) (.+)")` constant to `RefactoringType.java`
- [x] 1.2 Add the new constant to the `RefactoringType.ALL` array in `RefactoringType.java`

## 2. Refactoring Record Class

- [x] 2.1 Create `src/main/java/gr/uom/java/xmi/diff/ReplaceConditionalWithPatternMatchingRefactoring.java` implementing `MethodLevelRefactoring`
- [x] 2.2 Add fields: `Set<AbstractCodeFragment> codeFragmentsBefore`, `Set<AbstractCodeFragment> codeFragmentsAfter`, `VariableDeclarationContainer operationBefore`, `VariableDeclarationContainer operationAfter`
- [x] 2.3 Implement constructor, getters, `getRefactoringType()`, `getName()`, `toString()`, `leftSide()`, `rightSide()`, `getInvolvedClassesBeforeRefactoring()`, `getInvolvedClassesAfterRefactoring()`, `hashCode()`, and `equals()`
- [x] 2.4 Verify `toString()` output matches the format: `Replace Conditional With Pattern Matching\t<if-expr>\twith\tmatch <expr>\tin <elementType> <qualifiedName> from class <className>`

## 3. Detection Logic

- [x] 3.1 Add private method `detectIfToPatternMatch` in `UMLOperationBodyMapper` that iterates unmatched `IF_STATEMENT` composite nodes in version 1 and unmatched `SWITCH_STATEMENT` composite nodes in version 2
- [x] 3.2 In `detectIfToPatternMatch`, add a Python-file guard: only proceed when `container1.getLocationInfo().getFilePath().endsWith(".py")`
- [x] 3.3 When a candidate IF↔SWITCH pair is found with sufficient body overlap (reuse `ifToSwitch` predicate), create a `ReplaceConditionalWithPatternMatchingRefactoring` and attach it to the relevant mapping via `addRefactoring`
- [x] 3.4 Call `detectIfToPatternMatch` from the appropriate reconciliation phase in `UMLOperationBodyMapper` (after the main composite matching loop, alongside the existing pipeline/loop detection calls)

## 4. Test Fixtures

- [x] 4.1 Create directory `src/test/resources/python/replace-conditional-with-pattern-matching/`
- [x] 4.2 Add `scenario1/before/shape.py` — a function using `if`/`elif`/`else` to dispatch on a string value
- [x] 4.3 Add `scenario1/after/shape.py` — same function rewritten with `match`/`case` on the same subject
- [x] 4.4 Add `scenario2/before/http.py` — a function with three `elif` branches dispatching on an integer status code
- [x] 4.5 Add `scenario2/after/http.py` — same function rewritten with `match`/`case`
- [x] 4.6 Add `scenario3/before/guard.py` — a function using `if`/`elif` in version 1
- [x] 4.7 Add `scenario3/after/guard.py` — same function rewritten with `match`/`case` where at least one `case` has an `if` guard clause

## 5. Test Class

- [x] 5.1 Create `src/test/java/org/refactoringminer/test/TestReplaceConditionalWithPatternMatchingRefactoring.java`
- [x] 5.2 Add `testSimpleIfElifElseReplacedByMatch` — detects exactly one `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` for scenario1
- [x] 5.3 Add `testMultipleElifBranchesReplacedByMatch` — detects exactly one `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` for scenario2
- [x] 5.4 Add `testMatchWithGuardClauseNoFalsePositives` — detects exactly one `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` (not more) for scenario3
- [x] 5.5 Add `testNoRefactoringWhenConditionalUnchanged` — detects zero `REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING` when before and after are identical
- [x] 5.6 Run the full test class and confirm all tests pass with no regressions on existing Java/Kotlin tests
