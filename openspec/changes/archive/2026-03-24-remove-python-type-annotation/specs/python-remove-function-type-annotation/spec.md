## ADDED Requirements

### Requirement: Detect parameter type annotation removal
The system SHALL detect when a Python function parameter loses an explicit type hint. For each parameter that transitions from having an explicit type annotation to having none (e.g., `x: int` → `x`), the system SHALL emit one `REMOVE_PARAMETER_TYPE_ANNOTATION` refactoring instance recording the removed type, the parameter name, and the containing method and class.

#### Scenario: Single parameter de-annotated
- **WHEN** a Python function changes from `def add(x: int, y)` to `def add(x, y)`
- **THEN** exactly one `REMOVE_PARAMETER_TYPE_ANNOTATION` refactoring is reported for parameter `x` with type `int`

#### Scenario: Multiple parameters de-annotated at once
- **WHEN** a Python function changes from `def add(x: int, y: int)` to `def add(x, y)`
- **THEN** two `REMOVE_PARAMETER_TYPE_ANNOTATION` refactorings are reported, one for `x` with type `int` and one for `y` with type `int`

#### Scenario: Complex type annotation removed from parameter
- **WHEN** a Python function changes from `def process(items: list[int])` to `def process(items)`
- **THEN** one `REMOVE_PARAMETER_TYPE_ANNOTATION` refactoring is reported for parameter `items` with type `list[int]`

#### Scenario: Already-unannotated parameter is not reported
- **WHEN** a Python function changes from `def add(x: int, y)` to `def add(x, y: int)`
- **THEN** exactly one `REMOVE_PARAMETER_TYPE_ANNOTATION` refactoring is reported for parameter `x` only

#### Scenario: No annotation change is not reported
- **WHEN** a Python function's body changes but the parameter list remains identical with no type hints
- **THEN** no `REMOVE_PARAMETER_TYPE_ANNOTATION` refactoring is reported

### Requirement: Detect return type annotation removal
The system SHALL detect when a Python function loses its explicit return type annotation. A function that changes from having an explicit `-> <type>` clause to having no `->` clause SHALL cause one `REMOVE_RETURN_TYPE_ANNOTATION` refactoring to be emitted, recording the removed return type and the containing method and class.

#### Scenario: Return type annotation removed
- **WHEN** a Python function changes from `def add(x, y) -> int:` to `def add(x, y):`
- **THEN** one `REMOVE_RETURN_TYPE_ANNOTATION` refactoring is reported with return type `int`

#### Scenario: Complex return type annotation removed
- **WHEN** a Python function changes from `def get_items() -> list[str]:` to `def get_items():`
- **THEN** one `REMOVE_RETURN_TYPE_ANNOTATION` refactoring is reported with return type `list[str]`

#### Scenario: Parameter and return type removed together
- **WHEN** a Python function changes from `def add(x: int, y: int) -> int:` to `def add(x, y):`
- **THEN** two `REMOVE_PARAMETER_TYPE_ANNOTATION` refactorings and one `REMOVE_RETURN_TYPE_ANNOTATION` refactoring are all reported

#### Scenario: Absent return type annotation is not reported
- **WHEN** a Python function has no `->` clause in either before or after versions
- **THEN** no `REMOVE_RETURN_TYPE_ANNOTATION` refactoring is reported

### Requirement: Refactoring description format
The system SHALL produce human-readable descriptions for each detected refactoring using the following formats, symmetric to the ADD counterparts:

- `REMOVE_PARAMETER_TYPE_ANNOTATION`: `"Remove Parameter Type Annotation {type} in parameter {paramName} in method {methodSignature} from class {className}"`
- `REMOVE_RETURN_TYPE_ANNOTATION`: `"Remove Return Type Annotation {type} in method {methodSignature} from class {className}"`

#### Scenario: Parameter de-annotation description is well-formed
- **WHEN** parameter `x` of type `int` is de-annotated in method `add(x, y)` in class `Calculator`
- **THEN** the refactoring description is `"Remove Parameter Type Annotation int in parameter x in method add(x, y) from class Calculator"`

#### Scenario: Return type de-annotation description is well-formed
- **WHEN** return type `int` is removed from method `add(x, y)` in class `Calculator`
- **THEN** the refactoring description is `"Remove Return Type Annotation int in method add(x, y) from class Calculator"`

### Requirement: No false positives for type changes
The system SHALL NOT emit `REMOVE_PARAMETER_TYPE_ANNOTATION` or `REMOVE_RETURN_TYPE_ANNOTATION` when a parameter or return type that had an explicit annotation changes to a different type. Type changes on already-annotated elements are out of scope for this detection.

#### Scenario: Type change on annotated parameter is not reported as removal
- **WHEN** a Python function changes from `def add(x: int, y: int) -> int:` to `def add(x: float, y: float) -> float:`
- **THEN** no `REMOVE_PARAMETER_TYPE_ANNOTATION` and no `REMOVE_RETURN_TYPE_ANNOTATION` refactorings are reported

### Requirement: Detection is scoped to Python files
The system SHALL apply `REMOVE_PARAMETER_TYPE_ANNOTATION` and `REMOVE_RETURN_TYPE_ANNOTATION` detection only to Python (`.py`) source files.

#### Scenario: Non-Python file does not trigger removal detection
- **WHEN** a Java or TypeScript method's parameter annotation changes
- **THEN** no `REMOVE_PARAMETER_TYPE_ANNOTATION` or `REMOVE_RETURN_TYPE_ANNOTATION` refactoring is reported for that file
