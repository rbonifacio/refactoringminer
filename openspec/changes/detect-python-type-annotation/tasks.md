## 1. Refactoring Type Registration

- [x] 1.1 Add `ADD_PARAMETER_TYPE_ANNOTATION` enum entry to `RefactoringType.java` with display name `"Add Parameter Type Annotation"` and regex `"Add Parameter Type Annotation (.+) in parameter (.+) in method (.+) from class (.+)"`
- [x] 1.2 Add `ADD_RETURN_TYPE_ANNOTATION` enum entry to `RefactoringType.java` with display name `"Add Return Type Annotation"` and regex `"Add Return Type Annotation (.+) in method (.+) from class (.+)"`

## 2. Fix hasTypeAnnotation Tracking in Python AST Builder

- [x] 2.1 In `PyDeclarationASTBuilder.createParam(ParamContext, ...)`, add `decl.setHasTypeAnnotation(true)` immediately after `decl.setTypeAnnotation(...)` inside the `if (paramContext.annotation() != null)` block
- [x] 2.2 Verify the second `createParam(NameContext, ...)` overload does NOT set `hasTypeAnnotation` (star-only params have no annotation context)

## 3. Track Explicit Return Type Annotation in LangMethodDeclaration

- [x] 3.1 Add `private boolean hasExplicitReturnTypeAnnotation` field with getter/setter to `LangMethodDeclaration`
- [x] 3.2 In `PyDeclarationASTBuilder.visitFunction_def_raw()`, call `methodDeclaration.setHasExplicitReturnTypeAnnotation(true)` inside the `if (ctx.expression() != null)` branch (explicit `->` annotation)

## 4. Propagate Flags to UML Model

- [x] 4.1 Add `private boolean hasTypeAnnotation` field with getter/setter to `UMLParameter`
- [x] 4.2 In `UMLModelAdapter` (lines 352–372), after creating `umlParam`, set `umlParam.setHasTypeAnnotation(param.hasTypeAnnotation())` for Python language
- [x] 4.3 Add `private boolean hasExplicitReturnTypeAnnotation` field with getter/setter to `UMLOperation`
- [x] 4.4 In `UMLModelAdapter` (return parameter section, lines 385–403), after creating the return `UMLParameter`, set `umlOperation.setHasExplicitReturnTypeAnnotation(methodDecl.hasExplicitReturnTypeAnnotation())` for Python language

## 5. Create AddTypeAnnotationRefactoring Class

- [x] 5.1 Create `gr/uom/java/xmi/diff/AddTypeAnnotationRefactoring.java` implementing `Refactoring`
- [x] 5.2 Add constructor fields: `RefactoringType type`, `String annotatedType`, `String parameterName` (null for return), `UMLOperation operationBefore`, `UMLOperation operationAfter`
- [x] 5.3 Implement `getRefactoringType()` returning the stored `type` field
- [x] 5.4 Implement `toString()` producing the description format from the spec:
  - For `ADD_PARAMETER_TYPE_ANNOTATION`: `"Add Parameter Type Annotation {type} in parameter {param} in method {signature} from class {class}"`
  - For `ADD_RETURN_TYPE_ANNOTATION`: `"Add Return Type Annotation {type} in method {signature} from class {class}"`
- [x] 5.5 Implement `getInvolvedClassesBeforeRefactoring()` and `getInvolvedClassesAfterRefactoring()` returning the class name from the respective operation

## 6. Detection Logic in UMLOperationDiff

- [x] 6.1 In `UMLOperationDiff.getRefactorings()`, add a Python parameter type annotation detection block: iterate over matched parameter pairs from `parameterListDiff`; for each pair where `!before.hasTypeAnnotation() && after.hasTypeAnnotation()`, create and add an `AddTypeAnnotationRefactoring` with type `ADD_PARAMETER_TYPE_ANNOTATION`
- [x] 6.2 In the same method, add return type annotation detection: if `!removedOperation.hasExplicitReturnTypeAnnotation() && addedOperation.hasExplicitReturnTypeAnnotation()`, create and add an `AddTypeAnnotationRefactoring` with type `ADD_RETURN_TYPE_ANNOTATION`
- [x] 6.3 Ensure both checks are guarded so they only fire when the operations are Python (check file extension or a language flag on `UMLOperation`)

## 7. Test Cases

- [x] 7.1 Create test resource files: `before.py` and `after.py` for the scenario `def add(x, y)` → `def add(x: int, y: int) -> int:`
- [x] 7.2 Create test resource files for partial annotation: `def add(x: int, y)` → `def add(x: int, y: int)`
- [x] 7.3 Create test resource files for return-only annotation: `def get() -> str:` scenario
- [x] 7.4 Create test resource files for complex types: `def process(items) →  def process(items: list[int])`
- [x] 7.5 Write a test asserting that two `ADD_PARAMETER_TYPE_ANNOTATION` and one `ADD_RETURN_TYPE_ANNOTATION` refactorings are detected for task 7.1 resource
- [x] 7.6 Write a test asserting no `ADD_PARAMETER_TYPE_ANNOTATION` is reported when both before/after already have annotations (no false positives)
