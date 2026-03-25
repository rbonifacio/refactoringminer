## ADDED Requirements

### Requirement: Detect parameter type annotation addition
The system SHALL detect when a Python function parameter gains an explicit type hint for the first time. For each parameter that transitions from having no type annotation to having an explicit type annotation (e.g., `x` → `x: int`), the system SHALL emit one `ADD_PARAMETER_TYPE_ANNOTATION` refactoring instance. The refactoring SHALL record the annotated type, the parameter name, and the containing method and class.

#### Scenario: Single parameter annotated
- **WHEN** a Python function changes from `def add(x, y)` to `def add(x: int, y)`
- **THEN** exactly one `ADD_PARAMETER_TYPE_ANNOTATION` refactoring is reported for parameter `x` with type `int`

#### Scenario: Multiple parameters annotated at once
- **WHEN** a Python function changes from `def add(x, y)` to `def add(x: int, y: int)`
- **THEN** two `ADD_PARAMETER_TYPE_ANNOTATION` refactorings are reported, one for `x` with type `int` and one for `y` with type `int`

#### Scenario: Complex type annotation on parameter
- **WHEN** a Python function changes from `def process(items)` to `def process(items: list[int])`
- **THEN** one `ADD_PARAMETER_TYPE_ANNOTATION` refactoring is reported for parameter `items` with type `list[int]`

#### Scenario: Already-annotated parameter is not re-reported
- **WHEN** a Python function changes from `def add(x: int, y)` to `def add(x: int, y: int)`
- **THEN** exactly one `ADD_PARAMETER_TYPE_ANNOTATION` refactoring is reported for parameter `y` only

#### Scenario: No annotation change is not reported
- **WHEN** a Python function's body changes but the parameter list remains identical with no type hints
- **THEN** no `ADD_PARAMETER_TYPE_ANNOTATION` refactoring is reported

### Requirement: Detect return type annotation addition
The system SHALL detect when a Python function gains an explicit return type annotation for the first time. A function that changes from having no `->` clause to having an explicit `-> <type>` clause SHALL cause one `ADD_RETURN_TYPE_ANNOTATION` refactoring to be emitted, recording the annotated return type and the containing method and class.

#### Scenario: Return type annotation added
- **WHEN** a Python function changes from `def add(x, y):` to `def add(x, y) -> int:`
- **THEN** one `ADD_RETURN_TYPE_ANNOTATION` refactoring is reported with return type `int`

#### Scenario: Complex return type annotation added
- **WHEN** a Python function changes from `def get_items():` to `def get_items() -> list[str]:`
- **THEN** one `ADD_RETURN_TYPE_ANNOTATION` refactoring is reported with return type `list[str]`

#### Scenario: Parameter and return type added together
- **WHEN** a Python function changes from `def add(x, y):` to `def add(x: int, y: int) -> int:`
- **THEN** two `ADD_PARAMETER_TYPE_ANNOTATION` refactorings and one `ADD_RETURN_TYPE_ANNOTATION` refactoring are all reported

#### Scenario: Existing return type annotation is not re-reported
- **WHEN** a Python function already has `-> int` and its body changes
- **THEN** no `ADD_RETURN_TYPE_ANNOTATION` refactoring is reported

### Requirement: Refactoring description format
The system SHALL produce human-readable descriptions for each detected refactoring using the following formats:

- `ADD_PARAMETER_TYPE_ANNOTATION`: `"Add Parameter Type Annotation {type} in parameter {paramName} in method {methodSignature} from class {className}"`
- `ADD_RETURN_TYPE_ANNOTATION`: `"Add Return Type Annotation {type} in method {methodSignature} from class {className}"`

#### Scenario: Parameter annotation description is well-formed
- **WHEN** parameter `x` is annotated with type `int` in method `add(x, y)` in class `Calculator`
- **THEN** the refactoring description is `"Add Parameter Type Annotation int in parameter x in method add(x, y) from class Calculator"`

#### Scenario: Return type annotation description is well-formed
- **WHEN** return type `int` is added to method `add(x, y)` in class `Calculator`
- **THEN** the refactoring description is `"Add Return Type Annotation int in method add(x, y) from class Calculator"`

### Requirement: No false positives for type changes
The system SHALL NOT emit `ADD_PARAMETER_TYPE_ANNOTATION` or `ADD_RETURN_TYPE_ANNOTATION` when a parameter or return type that already had an explicit annotation changes to a different type. Type changes on already-annotated elements are out of scope for this detection.

#### Scenario: Type change on annotated parameter is not reported as addition
- **WHEN** a Python function changes from `def add(x: int, y: int) -> int:` to `def add(x: float, y: float) -> float:`
- **THEN** no `ADD_PARAMETER_TYPE_ANNOTATION` and no `ADD_RETURN_TYPE_ANNOTATION` refactorings are reported

### Requirement: Detection is scoped to Python files
The system SHALL apply `ADD_PARAMETER_TYPE_ANNOTATION` and `ADD_RETURN_TYPE_ANNOTATION` detection only to Python (`.py`) source files. Java, TypeScript, and other language files SHALL NOT be affected.

#### Scenario: Java file with annotation change does not trigger Python detection
- **WHEN** a Java method's parameter annotation changes
- **THEN** no `ADD_PARAMETER_TYPE_ANNOTATION` (Python kind) refactoring is reported for that file
