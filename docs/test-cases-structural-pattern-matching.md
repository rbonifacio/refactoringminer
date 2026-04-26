# Test Cases: Structural Pattern Matching Refactorings

Structural pattern matching (SPM) was introduced in Python 3.10 via PEP 634, adding `match`/`case` syntax as an alternative to chains of `if`/`elif` statements. RefactoringMiner detects two refactoring directions: **Replace Conditional with Pattern Matching** (if/elif → match/case) and **Replace Pattern Matching with Conditional** (match/case → if/elif).

---

## 1. Replace Conditional with Pattern Matching (if/elif → match/case)

Test class: `TestReplaceConditionalWithPatternMatchingRefactoring.java`
Total: **27 tests** — 22 passing, 5 skipped (`@Disabled`)

### 1.1 Passing Tests (22)

| Scenario | Test Method | Condition Type | Subject Type | Has elif? | Has Default? | Status | Notes |
|---|---|---|---|---|---|---|---|
| sc1 | `testSimpleIfElifElseReplacedByMatch` | `==` | simple | ✓ | ✓ | ✅ Passing | String dispatch + else; fixture: shape.py |
| sc2 | `testMultipleElifBranchesReplacedByMatch` | `==` | simple | ✓ | ✗ | ✅ Passing | Integer status codes, 4 elif; fixture: http.py |
| sc3 | `testMatchWithGuardClauseNoFalsePositives` | `==` | simple | ✓ | ✗ | ✅ Passing | Guard clauses `case x if x < 0:`; fixture: guard.py |
| sc4 | `testIfElifWithNestedIfReplacedByMatch` | `==` | simple | ✓ | ✗ | ✅ Passing | Nested if inside branch body; fixture: nested.py |
| sc5 | `testNestedIfsInsideElifBranchesDoNotBlockDetection` | `==` | simple | ✓ | ✗ | ✅ Passing | Nested ifs inside elif branches (ansible-inspired); fixture: transport.py |
| sc6 | `testIfElifOnStringProviderReplacedByMatch` | `==` | simple | ✓ | ✓ | ✅ Passing | String provider + nested ifs + wildcard default; fixture: provider.py |
| sc7 | `testIfElifWithSetMembershipReplacedByMatchWithOrPattern` | `in` tuple | attribute | ✓ | ✓ | ✅ Passing | `spec.scheme in (...)` → OR patterns; fixture: scheme.py |
| sc8 | `testSingleIfWithoutElifReplacedByMatch` | `in` tuple | simple | ✗ | ✓ | ✅ Passing | Single if, fonttype in (1,3) splits to separate cases; fixture: encoding.py |
| sc9 | `testIfElifWithWildcardGuardReplacedByMatch` | `==` | simple | ✓ | ✗ | ✅ Passing | Wildcard guard `case _ if key in dict:` as last case (locust-inspired); fixture: swarm.py |
| sc10 | `testIfElifOnAttributeSubjectReplacedByMatch` | `==` | attribute | ✓ | ✗ | ✅ Passing | Attribute subject `message.type`; fixture: message.py |
| sc11 | `testIsinstanceElifWithDefaultReplacedByClassPatterns` | `isinstance` | simple | ✓ | ✓ | ✅ Passing | Class patterns `case list():` + callable guard; fixture: samples.py |
| sc12 | `testIsinstanceElifNoDefaultReplacedByClassPatterns` | `isinstance` | simple | ✓ | ✗ | ✅ Passing | Class patterns for message types; fixture: messages.py |
| sc13 | `testInDictKeyMembershipReplacedByMappingPatterns` | `in` dict | simple | ✗ | ✓ | ✅ Passing | Key-in-dict → mapping patterns; detector fires on subject match despite structural difference; fixture: config.py |
| sc14 | `testInTupleElifNoDefaultReplacedByOrPatterns` | `in` tuple | simple | ✓ | ✗ | ✅ Passing | Multiple in-tuple branches → OR patterns, no default; fixture: converter.py |
| sc15 | `testInSetSingleIfReplacedByOrPatternCase` | `in` set | simple | ✗ | ✓ | ✅ Passing | Set membership → single OR-pattern case + wildcard; fixture: mapper.py |
| sc18 | `testIfElifOnAttrSubjectWithDefaultReplacedByMatch` | `==` | attribute | ✓ | ✓ | ✅ Passing | Attribute subject with default (`message.type`); fixture: handler.py |
| sc19 | `testSingleIfExpandedToMultiCaseMatch` | `==` | simple | ✗ | ✗ | ✅ Passing | Single if → match that adds a new case (ansible-inspired); fixture: compat.py |
| sc21 | `testEqAttrSingleIfExpandedToMatch` | `==` | attribute | ✗ | ✗ | ✅ Passing | Single if on attribute → match with expanded cases (sentry-inspired); fixture: artifact.py |
| sc22 | `testIsinstanceSimpleSingleIfReplacedByClassPattern` | `isinstance` | simple | ✗ | ✗ | ✅ Passing | Single isinstance → single class-pattern case; fixture: cleaner.py |
| sc23 | `testIsinstanceAttrElifNoDefaultReplacedByClassPatterns` | `isinstance` | attribute | ✓ | ✗ | ✅ Passing | isinstance on attr subject `item.value` → class patterns; fixture: parser.py |
| sc26 | `testMultiLineIfConditionOnAttrWithDefaultReplacedByMatch` | `==` (multi-line) | attribute | ✗ | ✓ | ✅ Passing | Multi-line `if (... or ...)` on attr → OR-pattern match/case; fixture: deployer.py |
| — | `testNoRefactoringWhenConditionalUnchanged` | — | — | — | — | ✅ Passing | Regression guard: identical files → 0 detections |

### 1.2 Skipped Tests (5)

These tests are marked `@Disabled` because the detection does not yet support cases where the `match` subject differs from the subject of the original `if` condition — i.e., the developer changed *what* is being dispatched on, not just the syntax.

| Scenario | Test Method | Status | Reason |
|---|---|---|---|
| sc16 | `testIndirectSubjectSimpleElifWithDefaultNotDetected` | ⏭ Skipped | Indirect: match subject `key` absent from if-condition `self.use_new_engine`; cross-variable dispatch analysis not yet supported |
| sc17 | `testIndirectAttrSubjectChangedNotDetected` | ⏭ Skipped | Indirect: match subject `artifact.state` absent from if-condition on `artifact.artifact_type`; dispatching attribute changed |
| sc20 | `testSubscriptSubjectChangedNotDetected` | ⏭ Skipped | Indirect: match subject `m["type"]` absent from if-condition on `m["part"]`; subscript key changed |
| sc24 | `testIndirectAttrSingleIfNoDefaultNotDetected` | ⏭ Skipped | Indirect: match subject `problem.identifier.typ` absent from if-condition `problem.identifier is not None` |
| sc25 | `testInAttrCompoundConditionSubjectChangedNotDetected` | ⏭ Skipped | Indirect: match subject `self.encrypted_string_behavior` absent from compound if-condition on `self.apply_transforms and value_type in ...` |

---

## 2. Replace Pattern Matching with Conditional (match/case → if/elif)

Test class: `TestReplacePatternMatchingWithConditionalRefactoring.java`
Total: **4 tests** — all passing

| Scenario | Test Method | Pattern Type | Status | Notes |
|---|---|---|---|---|
| sc1 | `testSimpleMatchReplacedByIfElifElse` | String cases + wildcard | ✅ Passing | match/case on string → if/elif/else; fixture: shape.py |
| sc2 | `testMultipleCaseBranchesReplacedByElifChain` | Integer cases + wildcard | ✅ Passing | 4 integer cases + wildcard → if/elif chain; fixture: http.py |
| sc3 | `testMatchWithGuardClauseReplacedByIfElif` | Guard clauses | ✅ Passing | `case x if x < 0:` → `if x < 0:` etc.; fixture: guard.py |
| — | `testNoRefactoringWhenMatchUnchanged` | — | ✅ Passing | Regression guard: identical match/case → 0 detections |

---

## 3. Coverage Analysis

Coverage is measured against the `spm-manual.csv` dataset, which contains 57 `gt=True` rows representing real-world refactoring instances.

All 57 ground-truth rows are reachable by the enabled tests. The 5 `@Disabled` tests correspond to the **indirect dispatch** family (14 rows in the dataset) where the `match` subject does not syntactically appear in the `if` condition — the developer refactored to a different dispatch key at the same time. This is the only systematically unsupported category.

### Coverage by dataset category

| Category | Condition | Subject | Scenarios | Dataset rows | Supported |
|---|---|---|---|---|---|
| Simple equality dispatch | `==` | simple | sc1–sc6, sc9, sc19 | 19 | Yes |
| Attribute equality dispatch | `==` | attribute | sc10, sc18, sc21, sc26 | 10 | Yes |
| Subscript equality (subject changed) | `==` | subscript | sc20 | 1 | No (indirect) |
| Tuple membership, attribute subject | `in` tuple | attribute | sc7 | 1 | Yes |
| Tuple/set/dict membership, simple subject | `in` tuple/set/dict | simple | sc8, sc13, sc14, sc15 | 10 | Yes |
| isinstance dispatch | `isinstance` | simple + attribute | sc11, sc12, sc22, sc23 | 7 | Yes |
| Indirect dispatch (all sub-types) | mixed | indirect | sc16, sc17, sc20, sc24, sc25 | 14 | No (`@Disabled`) |

### What "indirect" means

In the indirect family the developer both switched to `match`/`case` syntax *and* changed the dispatch subject. For example, an `if self.use_new_engine:` chain becomes `match key:` — the subject `key` never appeared in the original condition. Detecting this reliably requires cross-variable dataflow analysis that is not yet implemented.
