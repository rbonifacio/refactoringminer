## 1. RefactoringType entries

- [x] 1.1 Add `CHANGE_PARAMETER_TYPE_ANNOTATION` to `RefactoringType.java` with display name and regex pattern
- [x] 1.2 Add `CHANGE_RETURN_TYPE_ANNOTATION` to `RefactoringType.java` with display name and regex pattern
- [x] 1.3 Add `CHANGE_VARIABLE_TYPE_ANNOTATION` to `RefactoringType.java` with display name and regex pattern
- [x] 1.4 Include all three new entries in the `ALL` array in `RefactoringType.java`

## 2. ChangeTypeAnnotationRefactoring class

- [x] 2.1 Create `src/main/java/gr/uom/java/xmi/diff/ChangeTypeAnnotationRefactoring.java` implementing `MethodLevelRefactoring`
- [x] 2.2 Add fields: `RefactoringType type`, `String typeBefore`, `String typeAfter`, `String parameterName`, `UMLOperation operationBefore`, `UMLOperation operationAfter`
- [x] 2.3 Implement `getRefactoringType()`, `getName()`, `getOperationBefore()`, `getOperationAfter()`
- [x] 2.4 Implement `toString()` producing `"Change Parameter Type Annotation {typeBefore} to {typeAfter} in parameter {paramName} in method {sig} from class {cls}"` for parameter case and `"Change Return Type Annotation {typeBefore} to {typeAfter} in method {sig} from class {cls}"` for return case
- [x] 2.5 Implement `leftSide()` and `rightSide()` returning operation code ranges
- [x] 2.6 Implement `getInvolvedClassesBeforeRefactoring()` and `getInvolvedClassesAfterRefactoring()`
- [x] 2.7 Implement `hashCode()` and `equals()`

## 3. ChangeVariableTypeAnnotationRefactoring class

- [x] 3.1 Create `src/main/java/gr/uom/java/xmi/diff/ChangeVariableTypeAnnotationRefactoring.java` implementing `MethodLevelRefactoring`
- [x] 3.2 Add fields: `VariableDeclaration variableBefore`, `VariableDeclaration variableAfter`, `VariableDeclarationContainer operationBefore`, `VariableDeclarationContainer operationAfter`
- [x] 3.3 Implement `getRefactoringType()` returning `CHANGE_VARIABLE_TYPE_ANNOTATION`, `getName()`, `getOperationBefore()`, `getOperationAfter()`
- [x] 3.4 Implement `toString()` producing `"Change Variable Type Annotation {typeBefore} to {typeAfter} in attribute {name} from class {cls}"` for attributes and `"Change Variable Type Annotation {typeBefore} to {typeAfter} in variable {name} in method {sig} from class {cls}"` for local variables
- [x] 3.5 Implement `leftSide()` and `rightSide()` returning both variable and operation code ranges
- [x] 3.6 Implement `getInvolvedClassesBeforeRefactoring()` and `getInvolvedClassesAfterRefactoring()`
- [x] 3.7 Implement `hashCode()` and `equals()`

## 4. Detection — UMLOperationDiff (parameters and return)

- [x] 4.1 In `UMLOperationDiff.getRefactorings()`, add a CHANGE_PARAMETER loop before the existing ADD/REMOVE loops: for each parameter present in both `removed` and `added` with the same name, if both `hasTypeAnnotation()` and types differ, emit `ChangeTypeAnnotationRefactoring`
- [x] 4.2 In the same method, add a CHANGE_RETURN check before the existing ADD/REMOVE return checks: if both `removed.hasExplicitReturnTypeAnnotation()` and `added.hasExplicitReturnTypeAnnotation()` and return types differ, emit `ChangeTypeAnnotationRefactoring`
- [x] 4.3 Add import for `ChangeTypeAnnotationRefactoring` in `UMLOperationDiff.java`

## 5. Detection — UMLAttributeDiff (class attributes)

- [x] 5.1 In `UMLAttributeDiff.getRefactorings()`, add a CHANGE check before the existing ADD/REMOVE checks: if both `removedAttribute.hasExplicitTypeAnnotation()` and `addedAttribute.hasExplicitTypeAnnotation()` and types differ, emit `ChangeVariableTypeAnnotationRefactoring`
- [x] 5.2 Add import for `ChangeVariableTypeAnnotationRefactoring` in `UMLAttributeDiff.java`

## 6. Detection — UMLOperationBodyMapper (local variables)

- [x] 6.1 In the Python-scoped block in `UMLOperationBodyMapper.getRefactorings()`, add a CHANGE loop before the existing ADD/REMOVE loops: for each local variable with the `"typed"` modifier in both `vars1` and `vars2` with the same name and differing types, emit `ChangeVariableTypeAnnotationRefactoring`
- [x] 6.2 Use a `changedProcessedNames` set to prevent duplicate CHANGE events for re-assigned variables
- [x] 6.3 Add import for `ChangeVariableTypeAnnotationRefactoring` in `UMLOperationBodyMapper.java`

## 7. Test resources — function type annotation change

- [x] 7.1 Create `src/test/resources/python/change-function-type-annotation/scenario1/before/Calculator.py` — `def add(self, x: List[int], y: List[int]) -> List[int]:`
- [x] 7.2 Create `src/test/resources/python/change-function-type-annotation/scenario1/after/Calculator.py` — `def add(self, x: list[int], y: list[int]) -> list[int]:`
- [x] 7.3 Create `src/test/resources/python/change-function-type-annotation/scenario2/before/Processor.py` — `def process(self, items: Optional[str]):`
- [x] 7.4 Create `src/test/resources/python/change-function-type-annotation/scenario2/after/Processor.py` — `def process(self, items: str | None):`
- [x] 7.5 Create `src/test/resources/python/change-function-type-annotation/scenario3/before/Getter.py` — `def get(self) -> List[str]:`
- [x] 7.6 Create `src/test/resources/python/change-function-type-annotation/scenario3/after/Getter.py` — `def get(self) -> list[str]:`
- [x] 7.7 Create `src/test/resources/python/change-function-type-annotation/scenario4/before/NoChange.py` — unannotated function (no annotations in either version)
- [x] 7.8 Create `src/test/resources/python/change-function-type-annotation/scenario4/after/NoChange.py` — identical to before

## 8. Test resources — variable type annotation change

- [x] 8.1 Create `src/test/resources/python/change-variable-type-annotation/scenario1/before/Shape.py` — class attribute `items: List[str] = []`
- [x] 8.2 Create `src/test/resources/python/change-variable-type-annotation/scenario1/after/Shape.py` — class attribute `items: list[str] = []`
- [x] 8.3 Create `src/test/resources/python/change-variable-type-annotation/scenario2/before/DataProcessor.py` — local variable `count: int = 0`
- [x] 8.4 Create `src/test/resources/python/change-variable-type-annotation/scenario2/after/DataProcessor.py` — local variable `count: float = 0`
- [x] 8.5 Create `src/test/resources/python/change-variable-type-annotation/scenario3/before/Container.py` — class attribute `items: List[str] = []`
- [x] 8.6 Create `src/test/resources/python/change-variable-type-annotation/scenario3/after/Container.py` — class attribute `items: list[str] = []`
- [x] 8.7 Create `src/test/resources/python/change-variable-type-annotation/scenario4/before/Unchanged.py` — attribute and variable both annotated, no change
- [x] 8.8 Create `src/test/resources/python/change-variable-type-annotation/scenario4/after/Unchanged.py` — identical to before

## 9. Test class — function type annotation change

- [x] 9.1 Create `src/test/java/org/refactoringminer/test/TestChangeFunctionTypeAnnotationRefactoring.java`
- [x] 9.2 Add `testParameterAnnotationChanged()` — scenario1: expects 2 `CHANGE_PARAMETER_TYPE_ANNOTATION` and 1 `CHANGE_RETURN_TYPE_ANNOTATION`
- [x] 9.3 Add `testParameterAnnotationChangedToUnion()` — scenario2: expects 1 `CHANGE_PARAMETER_TYPE_ANNOTATION` containing `Optional[str]` and `str | None`
- [x] 9.4 Add `testReturnTypeAnnotationChanged()` — scenario3: expects 1 `CHANGE_RETURN_TYPE_ANNOTATION` with before type `List[str]` and after type `list[str]`
- [x] 9.5 Add `testNoFalsePositivesWhenUnannotated()` — scenario4: expects 0 change refactorings

## 10. Test class — variable type annotation change

- [x] 10.1 Create `src/test/java/org/refactoringminer/test/TestChangeVariableTypeAnnotationRefactoring.java`
- [x] 10.2 Add `testClassAttributeAnnotationChanged()` — scenario1: expects 1 `CHANGE_VARIABLE_TYPE_ANNOTATION` for attribute with type change
- [x] 10.3 Add `testLocalVariableAnnotationChanged()` — scenario2: expects 1 `CHANGE_VARIABLE_TYPE_ANNOTATION` for local variable with type change
- [x] 10.4 Add `testComplexTypeAnnotationChanged()` — scenario3: expects 1 `CHANGE_VARIABLE_TYPE_ANNOTATION` containing type before/after in description
- [x] 10.5 Add `testNoFalsePositivesWhenAnnotationUnchanged()` — scenario4: expects 0 `CHANGE_VARIABLE_TYPE_ANNOTATION` refactorings
