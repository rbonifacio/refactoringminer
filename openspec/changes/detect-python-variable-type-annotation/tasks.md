## 1. RefactoringType enum

- [x] 1.1 Add `ADD_VARIABLE_TYPE_ANNOTATION` entry to `RefactoringType.java` with display name `"Add Variable Type Annotation"` and two regex patterns (attribute and local variable formats)
- [x] 1.2 Add `ADD_VARIABLE_TYPE_ANNOTATION` to the `ALL` array in `RefactoringType`

## 2. AddVariableTypeAnnotationRefactoring class

- [x] 2.1 Create `src/main/java/gr/uom/java/xmi/diff/AddVariableTypeAnnotationRefactoring.java` implementing `MethodLevelRefactoring`
- [x] 2.2 Constructor takes `VariableDeclaration varBefore`, `VariableDeclaration varAfter`, `VariableDeclarationContainer operationBefore`, `VariableDeclarationContainer operationAfter`
- [x] 2.3 Implement `getRefactoringType()` returning `ADD_VARIABLE_TYPE_ANNOTATION`
- [x] 2.4 Implement `toString()` branching on `varAfter.isAttribute()` to produce the two description formats
- [x] 2.5 Implement `leftSide()`, `rightSide()`, `getInvolvedClassesBeforeRefactoring()`, `getInvolvedClassesAfterRefactoring()`, `hashCode()`, `equals()`

## 3. Fix PyExpressionASTBuilder.visitAssignment()

- [x] 3.1 Read `PyExpressionASTBuilder.java` to understand the current `visitAssignment()` implementation and the COLON branch
- [x] 3.2 When `ctx.COLON() != null` and `ctx.annotated_rhs() != null`, construct a `LangSingleVariableDeclaration` with the variable name, `hasTypeAnnotation=true`, `rawTypeAnnotationText` from `ctx.expression().getText()`, and `defaultValue` from `ctx.annotated_rhs()`
- [x] 3.3 When `ctx.COLON() != null` but no `annotated_rhs` (stand-alone annotation like `x: int`), keep the existing behavior unchanged (do not regress)
- [x] 3.4 Verify all existing Python AST/expression tests still pass after the change

## 4. UMLAttribute: hasExplicitTypeAnnotation flag

- [x] 4.1 Add `private boolean hasExplicitTypeAnnotation` field to `UMLAttribute.java` with getter/setter
- [x] 4.2 Read `UMLModelAdapter.processClassLevelAssignmentForAttribute()` to understand how class attributes are built
- [x] 4.3 In `UMLModelAdapter`, when the body statement is a `LangSingleVariableDeclaration` with `hasTypeAnnotation == true`, set `umlAttribute.setHasExplicitTypeAnnotation(true)`

## 5. UMLAttributeDiff: detect annotation addition

- [x] 5.1 Read `UMLAttributeDiff.getRefactorings()` to find the right insertion point
- [x] 5.2 Add check: if `!removed.hasExplicitTypeAnnotation() && added.hasExplicitTypeAnnotation()`, emit `AddVariableTypeAnnotationRefactoring` for the attribute
- [x] 5.3 Guard the check with `PathFileUtils.isPythonFile()` on the attribute's file path

## 6. Local variable detection via VariableDeclaration "typed" modifier

- [x] 6.1 Read `UMLOperationBodyMapper` to find where matched variable declarations are compared
- [x] 6.2 Add detection: for each matched `VariableDeclaration` pair where `before` lacks the `"typed"` modifier and `after` has it, emit `AddVariableTypeAnnotationRefactoring`
- [x] 6.3 Guard with `PathFileUtils.isPythonFile()` on the operation's file path

## 7. Test resource files

- [x] 7.1 Create `src/test/resources/python/add-variable-type-annotation/scenario1/{before,after}/Shape.py` — class attribute annotation (`x = 0` → `x: int = 0`)
- [x] 7.2 Create `src/test/resources/python/add-variable-type-annotation/scenario2/{before,after}/DataProcessor.py` — local variable annotation inside a method body
- [x] 7.3 Create `src/test/resources/python/add-variable-type-annotation/scenario3/{before,after}/Container.py` — complex type annotation (`items = []` → `items: list[str] = []`)
- [x] 7.4 Create `src/test/resources/python/add-variable-type-annotation/scenario4/{before,after}/Unchanged.py` — no change (no-false-positive scenario)

## 8. Test class

- [x] 8.1 Create `src/test/java/org/refactoringminer/test/TestAddVariableTypeAnnotationRefactoring.java`
- [x] 8.2 Add `testClassAttributeAnnotationAdded()` — expects 1 `ADD_VARIABLE_TYPE_ANNOTATION` for scenario1
- [x] 8.3 Add `testLocalVariableAnnotationAdded()` — expects 1 `ADD_VARIABLE_TYPE_ANNOTATION` for scenario2
- [x] 8.4 Add `testComplexTypeAnnotationAdded()` — expects annotated type `list[str]` for scenario3
- [x] 8.5 Add `testNoFalsePositivesWhenAnnotationsUnchanged()` — expects 0 refactorings for scenario4
- [x] 8.6 Run `TestAddVariableTypeAnnotationRefactoring` and confirm all tests pass
- [x] 8.7 Run `TestPythonDatasetRefactorings` to confirm no regressions
