## ADDED Requirements

### Requirement: Detect type annotation added to Python class-level attribute
The system SHALL detect when a Python class-level attribute gains an explicit type annotation (e.g., `x = 0` → `x: int = 0`) and emit an `ADD_VARIABLE_TYPE_ANNOTATION` refactoring. Detection SHALL only apply to Python source files.

#### Scenario: Simple type annotation added to class attribute
- **WHEN** a class attribute changes from `x = 0` to `x: int = 0` between versions
- **THEN** exactly one `ADD_VARIABLE_TYPE_ANNOTATION` refactoring is emitted with description matching `Add Variable Type Annotation int in attribute x from class <ClassName>`

#### Scenario: Complex type annotation added to class attribute
- **WHEN** a class attribute changes from `items = []` to `items: list[str] = []` between versions
- **THEN** exactly one `ADD_VARIABLE_TYPE_ANNOTATION` refactoring is emitted with annotated type `list[str]`

#### Scenario: Multiple attributes annotated in same class
- **WHEN** two class attributes both gain type annotations between versions
- **THEN** exactly two `ADD_VARIABLE_TYPE_ANNOTATION` refactorings are emitted, one per annotated attribute

#### Scenario: No refactoring when annotation already present
- **WHEN** a class attribute has the same type annotation in both before and after versions
- **THEN** no `ADD_VARIABLE_TYPE_ANNOTATION` refactoring is emitted for that attribute

### Requirement: Detect type annotation added to Python local variable
The system SHALL detect when a local variable inside a Python method body gains an explicit type annotation (e.g., `result = []` → `result: list[int] = []`) and emit an `ADD_VARIABLE_TYPE_ANNOTATION` refactoring.

#### Scenario: Simple type annotation added to local variable
- **WHEN** a local variable in a method changes from `count = 0` to `count: int = 0` between versions
- **THEN** exactly one `ADD_VARIABLE_TYPE_ANNOTATION` refactoring is emitted with description matching `Add Variable Type Annotation int in variable count in method <methodName> from class <ClassName>`

#### Scenario: Complex type annotation added to local variable
- **WHEN** a local variable changes from `result = []` to `result: list[int] = []`
- **THEN** the emitted refactoring has annotated type `list[int]`

#### Scenario: No refactoring when annotation already present on local variable
- **WHEN** a local variable has the same type annotation in both before and after versions
- **THEN** no `ADD_VARIABLE_TYPE_ANNOTATION` refactoring is emitted for that variable

### Requirement: ADD_VARIABLE_TYPE_ANNOTATION refactoring type
The system SHALL expose `ADD_VARIABLE_TYPE_ANNOTATION` as a member of the `RefactoringType` enum. The enum entry SHALL have a display name of `"Add Variable Type Annotation"` and a regex pattern matching the two description formats (attribute and local variable).

#### Scenario: Enum entry exists and is accessible
- **WHEN** `RefactoringType.ADD_VARIABLE_TYPE_ANNOTATION` is referenced in code
- **THEN** it resolves without compile error and its `getDisplayName()` returns `"Add Variable Type Annotation"`

### Requirement: Description format distinguishes attribute from local variable
The `ADD_VARIABLE_TYPE_ANNOTATION` refactoring description SHALL encode context to distinguish class attributes from local variables.

#### Scenario: Description for class attribute
- **WHEN** an `ADD_VARIABLE_TYPE_ANNOTATION` refactoring targets a class-level attribute named `x` of type `int` in class `MyClass`
- **THEN** `toString()` returns `"Add Variable Type Annotation int in attribute x from class MyClass"`

#### Scenario: Description for local variable
- **WHEN** an `ADD_VARIABLE_TYPE_ANNOTATION` refactoring targets a local variable named `result` of type `list[int]` in method `compute` in class `MyClass`
- **THEN** `toString()` returns `"Add Variable Type Annotation list[int] in variable result in method compute from class MyClass"`

### Requirement: No false positives for non-Python files
The `ADD_VARIABLE_TYPE_ANNOTATION` detection SHALL NOT fire for Java, TypeScript, Kotlin, or other non-Python source files when variables are modified.

#### Scenario: Java variable modification does not trigger Python detection
- **WHEN** a Java variable declaration is modified between versions
- **THEN** no `ADD_VARIABLE_TYPE_ANNOTATION` refactoring is emitted

### Requirement: Annotated assignment AST fix
The Python AST builder SHALL correctly preserve type annotation information when parsing annotated assignments (`x: int = 0`). The resulting AST node SHALL be a `LangSingleVariableDeclaration` with `hasTypeAnnotation == true` and `rawTypeAnnotationText` set to the annotation string.

#### Scenario: Annotated assignment with value produces LangSingleVariableDeclaration
- **WHEN** the Python parser processes `x: int = 0`
- **THEN** the resulting AST node is a `LangSingleVariableDeclaration` with `hasTypeAnnotation == true`, `rawTypeAnnotationText == "int"`, and a non-null `defaultValue`

#### Scenario: Non-annotated assignment is unaffected
- **WHEN** the Python parser processes `x = 0` (no colon)
- **THEN** the resulting AST node is unchanged from existing behavior (not a `LangSingleVariableDeclaration` with type annotation)
