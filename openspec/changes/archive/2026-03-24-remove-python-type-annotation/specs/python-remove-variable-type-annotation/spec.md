## ADDED Requirements

### Requirement: Detect type annotation removed from Python class-level attribute
The system SHALL detect when a Python class-level attribute loses an explicit type annotation (e.g., `x: int = 0` → `x = 0`) and emit a `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring. Detection SHALL only apply to Python source files.

#### Scenario: Simple type annotation removed from class attribute
- **WHEN** a class attribute changes from `x: int = 0` to `x = 0` between versions
- **THEN** exactly one `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring is emitted with description matching `Remove Variable Type Annotation int in attribute x from class <ClassName>`

#### Scenario: Complex type annotation removed from class attribute
- **WHEN** a class attribute changes from `items: list[str] = []` to `items = []` between versions
- **THEN** exactly one `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring is emitted with annotated type `list[str]`

#### Scenario: Multiple attributes de-annotated in same class
- **WHEN** two class attributes both lose type annotations between versions
- **THEN** exactly two `REMOVE_VARIABLE_TYPE_ANNOTATION` refactorings are emitted, one per de-annotated attribute

#### Scenario: No refactoring when annotation absent in both versions
- **WHEN** a class attribute has no type annotation in either before or after versions
- **THEN** no `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring is emitted for that attribute

### Requirement: Detect type annotation removed from Python local variable
The system SHALL detect when a local variable inside a Python method body loses an explicit type annotation (e.g., `count: int = 0` → `count = 0`) and emit a `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring.

#### Scenario: Simple type annotation removed from local variable
- **WHEN** a local variable in a method changes from `count: int = 0` to `count = 0` between versions
- **THEN** exactly one `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring is emitted with description matching `Remove Variable Type Annotation int in variable count in method <methodName> from class <ClassName>`

#### Scenario: Complex type annotation removed from local variable
- **WHEN** a local variable changes from `result: list[int] = []` to `result = []`
- **THEN** the emitted refactoring has annotated type `list[int]`

#### Scenario: No refactoring when annotation absent in both versions for local variable
- **WHEN** a local variable has no type annotation in either before or after versions
- **THEN** no `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring is emitted for that variable

### Requirement: REMOVE_VARIABLE_TYPE_ANNOTATION refactoring type
The system SHALL expose `REMOVE_VARIABLE_TYPE_ANNOTATION` as a member of the `RefactoringType` enum. The enum entry SHALL have a display name of `"Remove Variable Type Annotation"` and a regex pattern matching the two description formats (attribute and local variable).

#### Scenario: Enum entry exists and is accessible
- **WHEN** `RefactoringType.REMOVE_VARIABLE_TYPE_ANNOTATION` is referenced in code
- **THEN** it resolves without compile error and its `getDisplayName()` returns `"Remove Variable Type Annotation"`

### Requirement: Description format distinguishes attribute from local variable
The `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring description SHALL encode context to distinguish class attributes from local variables, symmetric to the ADD counterpart.

#### Scenario: Description for class attribute removal
- **WHEN** a `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring targets a class-level attribute named `x` of type `int` in class `MyClass`
- **THEN** `toString()` returns `"Remove Variable Type Annotation int in attribute x from class MyClass"`

#### Scenario: Description for local variable removal
- **WHEN** a `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring targets a local variable named `result` of type `list[int]` in method `compute` in class `MyClass`
- **THEN** `toString()` returns `"Remove Variable Type Annotation list[int] in variable result in method compute from class MyClass"`

### Requirement: No false positives for non-Python files
The `REMOVE_VARIABLE_TYPE_ANNOTATION` detection SHALL NOT fire for Java, TypeScript, Kotlin, or other non-Python source files.

#### Scenario: Java variable modification does not trigger Python removal detection
- **WHEN** a Java variable declaration is modified between versions
- **THEN** no `REMOVE_VARIABLE_TYPE_ANNOTATION` refactoring is emitted
