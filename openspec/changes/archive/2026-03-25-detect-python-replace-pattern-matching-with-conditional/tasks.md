## 1. RefactoringType Enum

- [x] 1.1 Add `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL("Replace Pattern Matching With Conditional", "Replace Pattern Matching With Conditional in (method|function) (.+) from (class|module) (.+)")` constant to `RefactoringType.java`
- [x] 1.2 Add the new constant to the `RefactoringType.ALL` array in `RefactoringType.java`

## 2. Refactoring Record Class

- [x] 2.1 Create `src/main/java/gr/uom/java/xmi/diff/ReplacePatternMatchingWithConditionalRefactoring.java` implementing `MethodLevelRefactoring`
- [x] 2.2 Add fields: `Set<AbstractCodeFragment> codeFragmentsBefore`, `Set<AbstractCodeFragment> codeFragmentsAfter`, `VariableDeclarationContainer operationBefore`, `VariableDeclarationContainer operationAfter`
- [x] 2.3 Implement constructor, getters, `getRefactoringType()`, `getName()`, `toString()`, `leftSide()`, `rightSide()`, `getInvolvedClassesBeforeRefactoring()`, `getInvolvedClassesAfterRefactoring()`, `hashCode()`, and `equals()`
- [x] 2.4 Verify `toString()` output matches the format: `Replace Pattern Matching With Conditional\tmatch <expr>:\twith\tif <expr>:\tin <elementType> <qualifiedName> from class <className>`

## 3. Detection Logic

- [x] 3.1 Add private method `detectPatternMatchToIf` in `UMLOperationBodyMapper` that iterates unmatched `SWITCH_STATEMENT` composite nodes in version 1 and unmatched outermost `IF_STATEMENT` composite nodes in version 2
- [x] 3.2 In `detectPatternMatchToIf`, add the same Python-file guard: only proceed when `container1.getLocationInfo().getFilePath().endsWith(".py")`
- [x] 3.3 Apply the outermost-IF guard on the version-2 side: skip `IF_STATEMENT` nodes in `innerNodes2` whose parent is also an `IF_STATEMENT`
- [x] 3.4 Match using the subject-in-condition heuristic: check that the `match` subject expression (`node1.getExpressions().get(0)`) appears in at least one condition expression of the candidate `IF_STATEMENT` (`node2.getExpressions()`)
- [x] 3.5 On a successful match, create a `ReplacePatternMatchingWithConditionalRefactoring`, attach it via `addRefactoring` on a new `LeafMapping`, and remove both matched nodes from their respective iterator lists
- [x] 3.6 Call `detectPatternMatchToIf` immediately after the `detectIfToPatternMatch` call in the `UMLOperation` constructor (after `processStreamAPIStatements`)
- [x] 3.7 Call `detectPatternMatchToIf` immediately after the `detectIfToPatternMatch` call in `processCompositeStatements`

## 4. Test Fixtures

- [x] 4.1 Create directory `src/test/resources/python/replace-pattern-matching-with-conditional/`
- [x] 4.2 Add `scenario1/before/shape.py` — a function using `match`/`case` to dispatch on a string value
- [x] 4.3 Add `scenario1/after/shape.py` — same function rewritten with `if`/`elif`/`else` on the same subject
- [x] 4.4 Add `scenario2/before/http.py` — a function with a `match` statement dispatching on an integer status code (four `case` branches)
- [x] 4.5 Add `scenario2/after/http.py` — same function rewritten with `if`/`elif`/`else`
- [x] 4.6 Add `scenario3/before/guard.py` — a function using `match`/`case` where at least one `case` has an `if` guard clause
- [x] 4.7 Add `scenario3/after/guard.py` — same function rewritten with `if`/`elif`

## 5. Test Class

- [x] 5.1 Create `src/test/java/org/refactoringminer/test/TestReplacePatternMatchingWithConditionalRefactoring.java`
- [x] 5.2 Add `testSimpleMatchReplacedByIfElifElse` — detects exactly one `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` for scenario1
- [x] 5.3 Add `testMultipleCaseBranchesReplacedByElifChain` — detects exactly one `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` for scenario2
- [x] 5.4 Add `testMatchWithGuardClauseReplacedByIfElif` — detects exactly one `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` for scenario3
- [x] 5.5 Add `testNoRefactoringWhenMatchUnchanged` — detects zero `REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL` when before and after are identical
- [x] 5.6 Run the full test class and confirm all tests pass with no regressions on existing Python and Java/Kotlin tests
