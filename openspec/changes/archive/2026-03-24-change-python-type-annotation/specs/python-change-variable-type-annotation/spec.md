## ADDED Requirements

### Requirement: Detect class attribute type annotation change
The system SHALL detect when a Python class attribute's explicit type annotation is replaced with a different type. For an attribute that has an explicit type annotation in both before and after versions but where the type string differs (e.g., `items: List[str] = []` → `items: list[str] = []`), the system SHALL emit one `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring instance recording the before type, the after type, the attribute name, and the containing class.

#### Scenario: Attribute annotation changed to built-in generic
- **WHEN** a Python class attribute changes from `items: List[str] = []` to `items: list[str] = []`
- **THEN** one `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported for attribute `items` with type before `List[str]` and type after `list[str]`

#### Scenario: Attribute annotation changed to narrower type
- **WHEN** a Python class attribute changes from `count: int = 0` to `count: float = 0`
- **THEN** one `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported for attribute `count` with type before `int` and type after `float`

#### Scenario: Attribute annotation addition is not reported as change
- **WHEN** a Python class attribute changes from `x = 0` to `x: int = 0`
- **THEN** no `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported (this is ADD_VARIABLE_TYPE_ANNOTATION)

#### Scenario: Attribute annotation removal is not reported as change
- **WHEN** a Python class attribute changes from `x: int = 0` to `x = 0`
- **THEN** no `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported (this is REMOVE_VARIABLE_TYPE_ANNOTATION)

#### Scenario: Unchanged attribute annotation is not reported
- **WHEN** a Python class attribute retains the same type annotation across versions
- **THEN** no `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported for that attribute

### Requirement: Detect local variable type annotation change
The system SHALL detect when a Python local variable's explicit type annotation is replaced with a different type inside a method body. For a local variable that has an explicit type annotation in both before and after versions but where the type string differs (e.g., `count: int = 0` → `count: float = 0`), the system SHALL emit one `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring instance recording the before type, the after type, the variable name, and the containing method and class.

#### Scenario: Local variable annotation changed
- **WHEN** a Python method body changes from `count: int = 0` to `count: float = 0`
- **THEN** one `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported for variable `count` with type before `int` and type after `float`

#### Scenario: Local variable annotation changed to generic
- **WHEN** a Python method body changes from `items: List[str] = []` to `items: list[str] = []`
- **THEN** one `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported for variable `items` with type before `List[str]` and type after `list[str]`

#### Scenario: Re-assigned variable without annotation change is not reported
- **WHEN** a Python method body contains `count: int = 0` and `count = count + 1` in both versions without type change
- **THEN** no `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported

### Requirement: Refactoring description format
The system SHALL produce human-readable descriptions for `CHANGE_VARIABLE_TYPE_ANNOTATION` refactorings using the following formats:

- For class attributes: `"Change Variable Type Annotation {typeBefore} to {typeAfter} in attribute {attrName} from class {className}"`
- For local variables: `"Change Variable Type Annotation {typeBefore} to {typeAfter} in variable {varName} in method {methodSignature} from class {className}"`

#### Scenario: Attribute change description is well-formed
- **WHEN** attribute `items` changes from `List[str]` to `list[str]` in class `Container`
- **THEN** the refactoring description is `"Change Variable Type Annotation List[str] to list[str] in attribute items from class Container"`

#### Scenario: Local variable change description is well-formed
- **WHEN** variable `count` changes from `int` to `float` in method `process()` in class `DataProcessor`
- **THEN** the refactoring description is `"Change Variable Type Annotation int to float in variable count in method process() from class DataProcessor"`

### Requirement: Detection is scoped to Python files
The system SHALL apply `CHANGE_VARIABLE_TYPE_ANNOTATION` detection only to Python (`.py`) source files.

#### Scenario: Non-Python file does not trigger change detection
- **WHEN** a Java or TypeScript variable's type changes
- **THEN** no `CHANGE_VARIABLE_TYPE_ANNOTATION` refactoring is reported for that file
