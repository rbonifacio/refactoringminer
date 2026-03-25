## ADDED Requirements

### Requirement: Detect parameter type annotation change
The system SHALL detect when a Python function parameter's explicit type annotation is replaced with a different type. For each parameter that has an explicit type annotation in both before and after versions, but where the type string differs (e.g., `x: List[int]` → `x: list[int]`), the system SHALL emit one `CHANGE_PARAMETER_TYPE_ANNOTATION` refactoring instance recording the before type, the after type, the parameter name, and the containing method and class.

#### Scenario: Parameter annotation changed to built-in generic
- **WHEN** a Python function changes from `def process(items: List[int])` to `def process(items: list[int])`
- **THEN** exactly one `CHANGE_PARAMETER_TYPE_ANNOTATION` refactoring is reported for parameter `items` with type before `List[int]` and type after `list[int]`

#### Scenario: Parameter annotation changed to union syntax
- **WHEN** a Python function changes from `def greet(name: Optional[str])` to `def greet(name: str | None)`
- **THEN** exactly one `CHANGE_PARAMETER_TYPE_ANNOTATION` refactoring is reported for parameter `name` with type before `Optional[str]` and type after `str|None` (spaces around `|` are normalized away by the AST)

#### Scenario: Multiple parameters changed at once
- **WHEN** a Python function changes from `def add(x: List[int], y: List[int])` to `def add(x: list[int], y: list[int])`
- **THEN** two `CHANGE_PARAMETER_TYPE_ANNOTATION` refactorings are reported, one for `x` and one for `y`

#### Scenario: Unchanged annotation on sibling parameter is not reported
- **WHEN** a Python function changes from `def add(x: int, y: List[int])` to `def add(x: int, y: list[int])`
- **THEN** exactly one `CHANGE_PARAMETER_TYPE_ANNOTATION` refactoring is reported for `y` only

#### Scenario: Addition of an annotation is not reported as change
- **WHEN** a Python function changes from `def add(x, y: int)` to `def add(x: float, y: int)`
- **THEN** no `CHANGE_PARAMETER_TYPE_ANNOTATION` refactoring is reported (this is ADD_PARAMETER_TYPE_ANNOTATION)

#### Scenario: Removal of an annotation is not reported as change
- **WHEN** a Python function changes from `def add(x: int, y: int)` to `def add(x, y: int)`
- **THEN** no `CHANGE_PARAMETER_TYPE_ANNOTATION` refactoring is reported for `x` (this is REMOVE_PARAMETER_TYPE_ANNOTATION)

### Requirement: Detect return type annotation change
The system SHALL detect when a Python function's explicit `->` return type annotation changes to a different type. A function where both before and after have an explicit `->` clause, but the type string differs, SHALL cause one `CHANGE_RETURN_TYPE_ANNOTATION` refactoring to be emitted, recording the before type, the after type, and the containing method and class.

#### Scenario: Return type changed to built-in generic
- **WHEN** a Python function changes from `def get_items() -> List[str]:` to `def get_items() -> list[str]:`
- **THEN** one `CHANGE_RETURN_TYPE_ANNOTATION` refactoring is reported with type before `List[str]` and type after `list[str]`

#### Scenario: Return type changed to union syntax
- **WHEN** a Python function changes from `def find() -> Optional[int]:` to `def find() -> int | None:`
- **THEN** one `CHANGE_RETURN_TYPE_ANNOTATION` refactoring is reported with type before `Optional[int]` and type after `int | None`

#### Scenario: Addition of return annotation is not reported as change
- **WHEN** a Python function changes from `def get():` to `def get() -> str:`
- **THEN** no `CHANGE_RETURN_TYPE_ANNOTATION` refactoring is reported (this is ADD_RETURN_TYPE_ANNOTATION)

#### Scenario: Removal of return annotation is not reported as change
- **WHEN** a Python function changes from `def get() -> str:` to `def get():`
- **THEN** no `CHANGE_RETURN_TYPE_ANNOTATION` refactoring is reported (this is REMOVE_RETURN_TYPE_ANNOTATION)

### Requirement: Refactoring description format
The system SHALL produce human-readable descriptions for each detected refactoring using the following formats:

- `CHANGE_PARAMETER_TYPE_ANNOTATION`: `"Change Parameter Type Annotation {typeBefore} to {typeAfter} in parameter {paramName} in method {methodSignature} from class {className}"`
- `CHANGE_RETURN_TYPE_ANNOTATION`: `"Change Return Type Annotation {typeBefore} to {typeAfter} in method {methodSignature} from class {className}"`

#### Scenario: Parameter change description is well-formed
- **WHEN** parameter `items` changes from `List[int]` to `list[int]` in method `process(items)` in class `Processor`
- **THEN** the refactoring description is `"Change Parameter Type Annotation List[int] to list[int] in parameter items in method process(items) from class Processor"`

#### Scenario: Return type change description is well-formed
- **WHEN** return type changes from `List[str]` to `list[str]` in method `get_items()` in class `Store`
- **THEN** the refactoring description is `"Change Return Type Annotation List[str] to list[str] in method get_items() from class Store"`

### Requirement: Detection is scoped to Python files
The system SHALL apply `CHANGE_PARAMETER_TYPE_ANNOTATION` and `CHANGE_RETURN_TYPE_ANNOTATION` detection only to Python (`.py`) source files.

#### Scenario: Non-Python file does not trigger change detection
- **WHEN** a Java or TypeScript method's parameter type changes
- **THEN** no `CHANGE_PARAMETER_TYPE_ANNOTATION` or `CHANGE_RETURN_TYPE_ANNOTATION` refactoring is reported for that file
