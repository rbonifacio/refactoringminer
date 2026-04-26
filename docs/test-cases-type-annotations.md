# Test Cases: Type Annotation Refactorings

Python type hints (PEP 484 for functions, PEP 526 for variables) allow developers to attach type information to parameters, return values, and variable declarations without affecting runtime behaviour. RefactoringMiner detects six annotation refactoring types — add, change, and remove for both function-level annotations (parameters + return types) and variable-level annotations (class attributes + local variables). The tests cover all three lifecycle operations on the full range of annotatable targets.

---

## 1. Add Type Annotation — Functions

Test class: `TestAddTypeAnnotationRefactoring`
Refactoring types: `ADD_PARAMETER_TYPE_ANNOTATION`, `ADD_RETURN_TYPE_ANNOTATION`
Total: **5 tests** — all passing

| Scenario | Test Method | Change | Expected Refactorings | Status |
|---|---|---|---|---|
| sc1 | `testFullAnnotationAdded` | `add(self, x, y)` → `add(self, x: int, y: int) -> int` | 2 ADD_PARAMETER + 1 ADD_RETURN | ✅ Passing |
| sc2 | `testPartialAnnotationAdded` | `add(self, x: int, y)` → `add(self, x: int, y: int)` | 1 ADD_PARAMETER for `y` | ✅ Passing |
| sc3 | `testReturnTypeAnnotationAdded` | `get_name(self)` → `get_name(self) -> str` | 1 ADD_RETURN | ✅ Passing |
| sc4 | `testComplexTypeAnnotationAdded` | `process(self, items)` → `process(self, items: list[int])` | 1 ADD_PARAMETER with complex type | ✅ Passing |
| — | `testNoFalsePositivesWhenAnnotationsUnchanged` | Identical files (already annotated) | 0 detections | ✅ Passing |

---

## 2. Add Type Annotation — Variables

Test class: `TestAddVariableTypeAnnotationRefactoring`
Refactoring type: `ADD_VARIABLE_TYPE_ANNOTATION`
Total: **4 tests** — all passing

| Scenario | Test Method | Change | Target | Status |
|---|---|---|---|---|
| sc1 | `testClassAttributeAnnotationAdded` | `x = 0` → `x: int = 0` | Class attribute in `Shape` | ✅ Passing |
| sc2 | `testLocalVariableAnnotationAdded` | `count = 0` → `count: int = 0` | Local variable in `DataProcessor.process` | ✅ Passing |
| sc3 | `testComplexTypeAnnotationAdded` | `items = []` → `items: list[str] = []` | Class attribute with generic type | ✅ Passing |
| sc4 | `testNoFalsePositivesWhenAnnotationsUnchanged` | Identical files (already annotated) | 0 detections | ✅ Passing |

---

## 3. Change Type Annotation — Functions

Test class: `TestChangeFunctionTypeAnnotationRefactoring`
Refactoring types: `CHANGE_PARAMETER_TYPE_ANNOTATION`, `CHANGE_RETURN_TYPE_ANNOTATION`
Total: **4 tests** — all passing

| Scenario | Test Method | Change | Expected Refactorings | Status |
|---|---|---|---|---|
| sc1 | `testParameterAnnotationChanged` | `List[int]` → `list[int]` (2 params + return) | 2 CHANGE_PARAMETER + 1 CHANGE_RETURN | ✅ Passing |
| sc2 | `testParameterAnnotationChangedToUnion` | `Optional[str]` → `str \| None` | 1 CHANGE_PARAMETER | ✅ Passing |
| sc3 | `testReturnTypeAnnotationChanged` | `List[str]` → `list[str]` (return only) | 1 CHANGE_RETURN | ✅ Passing |
| sc4 | `testNoFalsePositivesWhenUnannotated` | No annotations in either version | 0 detections | ✅ Passing |

---

## 4. Change Type Annotation — Variables

Test class: `TestChangeVariableTypeAnnotationRefactoring`
Refactoring type: `CHANGE_VARIABLE_TYPE_ANNOTATION`
Total: **4 tests** — all passing

| Scenario | Test Method | Change | Target | Status |
|---|---|---|---|---|
| sc1 | `testClassAttributeAnnotationChanged` | `items: List[str]` → `items: list[str]` | Class attribute in `Shape` | ✅ Passing |
| sc2 | `testLocalVariableAnnotationChanged` | `count: int` → `count: float` | Local variable in `DataProcessor.process` | ✅ Passing |
| sc3 | `testComplexTypeAnnotationChanged` | `List[str]` → `list[str]` (attribute) | Generic type modernisation | ✅ Passing |
| sc4 | `testNoFalsePositivesWhenAnnotationUnchanged` | Identical files (types unchanged) | 0 detections | ✅ Passing |

---

## 5. Remove Type Annotation — Functions

Test class: `TestRemoveFunctionTypeAnnotationRefactoring`
Refactoring types: `REMOVE_PARAMETER_TYPE_ANNOTATION`, `REMOVE_RETURN_TYPE_ANNOTATION`
Total: **4 tests** — all passing

| Scenario | Test Method | Change | Expected Refactorings | Status |
|---|---|---|---|---|
| sc1 | `testAllAnnotationsRemoved` | `add(self, x: int, y: int) -> int` → `add(self, x, y)` | 2 REMOVE_PARAMETER + 1 REMOVE_RETURN | ✅ Passing |
| sc2 | `testComplexParameterAnnotationRemoved` | `items: list[int]` → `items` | 1 REMOVE_PARAMETER | ✅ Passing |
| sc3 | `testReturnTypeAnnotationRemoved` | `get(self) -> str` → `get(self)` | 1 REMOVE_RETURN | ✅ Passing |
| sc4 | `testNoFalsePositivesWhenAlreadyUnannotated` | No annotations in either version | 0 detections | ✅ Passing |

---

## 6. Remove Type Annotation — Variables

Test class: `TestRemoveVariableTypeAnnotationRefactoring`
Refactoring type: `REMOVE_VARIABLE_TYPE_ANNOTATION`
Total: **4 tests** — all passing

| Scenario | Test Method | Change | Target | Status |
|---|---|---|---|---|
| sc1 | `testClassAttributeAnnotationRemoved` | `x: int = 0` → `x = 0` | Class attribute in `Shape` | ✅ Passing |
| sc2 | `testLocalVariableAnnotationRemoved` | `count: int = 0` → `count = 0` | Local variable in `DataProcessor.process` | ✅ Passing |
| sc3 | `testComplexTypeAnnotationRemoved` | `items: list[str] = []` → `items = []` | Complex type erased | ✅ Passing |
| sc4 | `testNoFalsePositivesWhenAnnotationsUnchanged` | Identical files (annotated unchanged) | 0 detections | ✅ Passing |

---

## Summary

| Test Class | Refactoring Types | Tests | Status |
|---|---|---|---|
| `TestAddTypeAnnotationRefactoring` | ADD_PARAMETER_TYPE_ANNOTATION, ADD_RETURN_TYPE_ANNOTATION | 5 | All passing |
| `TestAddVariableTypeAnnotationRefactoring` | ADD_VARIABLE_TYPE_ANNOTATION | 4 | All passing |
| `TestChangeFunctionTypeAnnotationRefactoring` | CHANGE_PARAMETER_TYPE_ANNOTATION, CHANGE_RETURN_TYPE_ANNOTATION | 4 | All passing |
| `TestChangeVariableTypeAnnotationRefactoring` | CHANGE_VARIABLE_TYPE_ANNOTATION | 4 | All passing |
| `TestRemoveFunctionTypeAnnotationRefactoring` | REMOVE_PARAMETER_TYPE_ANNOTATION, REMOVE_RETURN_TYPE_ANNOTATION | 4 | All passing |
| `TestRemoveVariableTypeAnnotationRefactoring` | REMOVE_VARIABLE_TYPE_ANNOTATION | 4 | All passing |
| **Total** | **6 refactoring types** | **25** | **All passing** |

Each test suite includes at least one negative (no-false-positive) case using identical file versions to verify the detector does not fire spuriously.
