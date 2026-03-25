## 1. RefactoringType enum entries

- [x] 1.1 Add `REMOVE_VARIABLE_TYPE_ANNOTATION` enum entry to `RefactoringType.java` with display name `"Remove Variable Type Annotation"` and regex `"Remove Variable Type Annotation (.+) in (attribute|variable) (.+) (from class|in method .+ from class) (.+)"`
- [x] 1.2 Add `REMOVE_PARAMETER_TYPE_ANNOTATION` enum entry with display name `"Remove Parameter Type Annotation"` and regex `"Remove Parameter Type Annotation (.+) in parameter (.+) in method (.+) from class (.+)"`
- [x] 1.3 Add `REMOVE_RETURN_TYPE_ANNOTATION` enum entry with display name `"Remove Return Type Annotation"` and regex `"Remove Return Type Annotation (.+) in method (.+) from class (.+)"`
- [x] 1.4 Add all three new entries to the `ALL` array in `RefactoringType`

## 2. Parameterise AddVariableTypeAnnotationRefactoring

- [x] 2.1 Add a `private final RefactoringType type` field to `AddVariableTypeAnnotationRefactoring`
- [x] 2.2 Add `RefactoringType type` as the first constructor parameter; update `getRefactoringType()` to return `this.type` instead of the hardcoded constant
- [x] 2.3 Update call site in `UMLAttributeDiff.java` to pass `RefactoringType.ADD_VARIABLE_TYPE_ANNOTATION` as the first argument
- [x] 2.4 Update call site in `UMLOperationBodyMapper.java` to pass `RefactoringType.ADD_VARIABLE_TYPE_ANNOTATION` as the first argument
- [x] 2.5 Update call site in `VariableReplacementAnalysis.java` to pass `RefactoringType.ADD_VARIABLE_TYPE_ANNOTATION` as the first argument

## 3. Detect REMOVE_VARIABLE_TYPE_ANNOTATION for class attributes

- [x] 3.1 In `UMLAttributeDiff.getRefactorings()`, directly after the existing ADD check, add a REMOVE check: `removedAttribute.hasExplicitTypeAnnotation() && !addedAttribute.hasExplicitTypeAnnotation()`
- [x] 3.2 Use `removedAttribute.getVariableDeclaration().getType().toString()` as the type string (from the before model)
- [x] 3.3 Emit `new AddVariableTypeAnnotationRefactoring(RefactoringType.REMOVE_VARIABLE_TYPE_ANNOTATION, ...)` with `removedAttribute.getVariableDeclaration()` as varBefore and `addedAttribute.getVariableDeclaration()` as varAfter

## 4. Detect REMOVE_VARIABLE_TYPE_ANNOTATION for local variables

- [x] 4.1 In `UMLOperationBodyMapper.getRefactorings()`, after the existing ADD loop, add a symmetric REMOVE loop iterating `vars1` for variables where `before_typed == true`
- [x] 4.2 For each such variable, find the matching name in `vars2` where `after_typed == false`
- [x] 4.3 Emit `new AddVariableTypeAnnotationRefactoring(RefactoringType.REMOVE_VARIABLE_TYPE_ANNOTATION, vd1, vd2, container1, container2)` with the same `processedNames` deduplication guard

## 5. Detect REMOVE_PARAMETER_TYPE_ANNOTATION

- [x] 5.1 In `UMLOperationDiff.getRefactorings()`, directly after the existing ADD parameter loop, add a REMOVE loop: iterate `removed.getParameters()`, skip `return` kind and parameters that have NO type annotation (`!removedParam.hasTypeAnnotation()`); for each remaining, find the matching named parameter in `added` that has no type annotation (`!addedParam.hasTypeAnnotation()`)
- [x] 5.2 Emit `new AddTypeAnnotationRefactoring(RefactoringType.REMOVE_PARAMETER_TYPE_ANNOTATION, removedParam.getType().toString(), removedParam.getName(), removed, added)`

## 6. Detect REMOVE_RETURN_TYPE_ANNOTATION

- [x] 6.1 In `UMLOperationDiff.getRefactorings()`, directly after the existing ADD return-type check, add a REMOVE check: `removed.hasExplicitReturnTypeAnnotation() && !added.hasExplicitReturnTypeAnnotation()`
- [x] 6.2 Emit `new AddTypeAnnotationRefactoring(RefactoringType.REMOVE_RETURN_TYPE_ANNOTATION, returnType, null, removed, added)` using `removed.getReturnParameter().getType().toString()` as the type string

## 7. Test resource files

- [x] 7.1 Create `src/test/resources/python/remove-variable-type-annotation/scenario1/{before,after}/Shape.py` — attribute removal: `x: int = 0` → `x = 0`
- [x] 7.2 Create `src/test/resources/python/remove-variable-type-annotation/scenario2/{before,after}/DataProcessor.py` — local variable removal: `count: int = 0` → `count = 0`
- [x] 7.3 Create `src/test/resources/python/remove-variable-type-annotation/scenario3/{before,after}/Container.py` — complex type removal: `items: list[str] = []` → `items = []`
- [x] 7.4 Create `src/test/resources/python/remove-variable-type-annotation/scenario4/{before,after}/Unchanged.py` — no change (no-false-positive: both versions annotated)
- [x] 7.5 Create `src/test/resources/python/remove-function-type-annotation/scenario1/{before,after}/Calculator.py` — full removal: `def add(x: int, y: int) -> int:` → `def add(x, y):`
- [x] 7.6 Create `src/test/resources/python/remove-function-type-annotation/scenario2/{before,after}/Processor.py` — partial removal: `def process(items: list[int])` → `def process(items)`
- [x] 7.7 Create `src/test/resources/python/remove-function-type-annotation/scenario3/{before,after}/Getter.py` — return-only removal: `def get() -> str:` → `def get():`
- [x] 7.8 Create `src/test/resources/python/remove-function-type-annotation/scenario4/{before,after}/NoChange.py` — no change (no-false-positive: already unannotated)

## 8. Test class for variable annotation removal

- [x] 8.1 Create `src/test/java/org/refactoringminer/test/TestRemoveVariableTypeAnnotationRefactoring.java`
- [x] 8.2 Add `testClassAttributeAnnotationRemoved()` — expects 1 `REMOVE_VARIABLE_TYPE_ANNOTATION` for scenario1, description contains `"in attribute x"` and `"from class Shape"`
- [x] 8.3 Add `testLocalVariableAnnotationRemoved()` — expects 1 `REMOVE_VARIABLE_TYPE_ANNOTATION` for scenario2, description contains `"in variable count"`
- [x] 8.4 Add `testComplexTypeAnnotationRemoved()` — expects 1 `REMOVE_VARIABLE_TYPE_ANNOTATION` for scenario3, description contains `"list[str]"`
- [x] 8.5 Add `testNoFalsePositivesWhenAnnotationsUnchanged()` — expects 0 `REMOVE_VARIABLE_TYPE_ANNOTATION` for scenario4

## 9. Test class for function annotation removal

- [x] 9.1 Create `src/test/java/org/refactoringminer/test/TestRemoveFunctionTypeAnnotationRefactoring.java`
- [x] 9.2 Add `testAllAnnotationsRemoved()` — expects 2 `REMOVE_PARAMETER_TYPE_ANNOTATION` and 1 `REMOVE_RETURN_TYPE_ANNOTATION` for scenario1
- [x] 9.3 Add `testComplexParameterAnnotationRemoved()` — expects 1 `REMOVE_PARAMETER_TYPE_ANNOTATION` with type `list[int]` for scenario2
- [x] 9.4 Add `testReturnTypeAnnotationRemoved()` — expects 1 `REMOVE_RETURN_TYPE_ANNOTATION` with type `str` for scenario3
- [x] 9.5 Add `testNoFalsePositivesWhenAlreadyUnannotated()` — expects 0 removal refactorings for scenario4

## 10. Regression check

- [x] 10.1 Run `TestAddVariableTypeAnnotationRefactoring` to confirm ADD detection still works after `AddVariableTypeAnnotationRefactoring` constructor change
- [x] 10.2 Run `TestPythonDatasetRefactorings` to confirm no regressions in the full Python dataset
