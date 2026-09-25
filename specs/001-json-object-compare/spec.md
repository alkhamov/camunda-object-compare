# Feature Specification: JSON Object Comparison

**Feature Branch**: `[001-json-object-compare]`

**Created**: 2026-09-25

**Status**: Draft

**Input**: User description: "Create the first feature specification for an application that compares two JSON objects. The application processes two JSON objects that are stored in persistent object storage. A process must retrieve both objects and evaluate whether their relevant fields contain equal values. The comparison must be performed by a DMN decision using FEEL expressions. The comparison result is boolean: - true if all relevant fields have equal values in both objects - false if at least one relevant field has a different value. The set of relevant fields and the comparison rules must be configurable through the DMN decision model. Changing which fields are compared, or changing the comparison rules, must not require changes to application code or to the BPMN process. The process must make the boolean comparison result available as process data after the decision has been evaluated. For the initial version, keep the use case intentionally simple: - exactly two JSON objects are compared - comparison is based on explicitly defined relevant fields - no complex transformations are required - no user interface is required"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Compare two stored JSON objects with a configurable decision (Priority: P1)

A process owner needs to compare two JSON objects stored in persistent object storage and determine whether the relevant values match according to the current business rules. The comparison must be driven by a DMN decision model so it can be changed without altering the process or application code.

**Why this priority**: This is the core business value of the feature. Without a reliable decision result, the process cannot determine whether the compared objects are equivalent for the defined business use case.

**Independent Test**: A process can be started with two stored JSON objects, retrieve them, pass them to the DMN decision, and verify that the process data contains a true or false result based on the configured relevant fields.

**Acceptance Scenarios**:

1. **Given** two JSON objects stored in persistent object storage whose relevant fields have the same values, **When** the process retrieves both objects and evaluates the DMN decision, **Then** the decision result is true and the process data contains true.
2. **Given** two JSON objects stored in persistent object storage where at least one relevant field has a different value, **When** the process evaluates the decision, **Then** the decision result is false and the process data contains false.
3. **Given** the DMN model is updated to compare a different field set or a different FEEL comparison rule, **When** the same process runs without code or BPMN changes, **Then** the process uses the updated decision logic and produces the result based on the revised configuration.

---

### User Story 2 - Manage business comparison rules in the decision model (Priority: P2)

A rule maintainer needs to change which fields are considered relevant and how each field is compared without touching application code or the BPMN process. This allows the comparison behavior to evolve as business requirements change.

**Why this priority**: It keeps the business logic configurable and reduces the risk of deployment delays or code changes for routine rule updates.

**Independent Test**: A DMN model can be updated in a separate deployment and then used by the same process to compare objects using a different set of relevant fields or a different FEEL equality rule.

**Acceptance Scenarios**:

1. **Given** a DMN decision that declares a specific list of relevant fields, **When** the process compares two objects, **Then** only those explicitly defined fields are considered for equality.
2. **Given** a DMN decision that changes the comparison rule for a field, **When** the process is rerun, **Then** the comparison result reflects the updated rule without requiring code or BPMN changes.

---

### Edge Cases

- What happens when one or both objects are missing required relevant fields?
- How does the system handle objects that contain extra fields outside the configured comparison set?
- How does the system behave when the object contents are valid JSON but the relevant field values are different in type or format?
- What happens when a process is started with more or fewer than two objects in scope for comparison?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The application MUST retrieve exactly two JSON objects from persistent object storage before the comparison decision is evaluated.
- **FR-002**: The process MUST make each retrieved JSON object available as process data so it can be used by the DMN decision.
- **FR-003**: The comparison MUST be evaluated by a DMN decision that uses FEEL expressions to determine equality.
- **FR-004**: The DMN decision MUST return a boolean result: true when all relevant fields have equal values, and false when at least one relevant field differs.
- **FR-005**: The set of relevant fields MUST be explicitly defined in the DMN model and MUST govern which values are compared.
- **FR-006**: The comparison rules for relevant fields MUST be defined in the DMN model and MUST be configurable without changing application code.
- **FR-007**: Changing the relevant field list or comparison rules in the DMN model MUST NOT require changes to the BPMN process definition or application code.
- **FR-008**: After the DMN decision is evaluated, the process MUST expose the boolean comparison result as process data for downstream use.
- **FR-009**: The feature MUST support only the initial use case of comparing exactly two JSON objects, with comparison based on explicitly defined relevant fields.
- **FR-010**: The initial version MUST NOT require a user interface or complex transformation logic beyond retrieving stored JSON objects and passing them to the decision model.
- **FR-011**: If any configured relevant field differs between the two JSON objects, the overall comparison result MUST be false even if all other fields match.
- **FR-012**: If all configured relevant fields match between the two JSON objects, the overall comparison result MUST be true, regardless of differences in unrelated fields that are outside the configured comparison set.

### Key Entities *(include if feature involves data)*

- **Stored JSON Object**: A JSON value persisted in stable storage and retrieved by the process for comparison. It contains one or more fields, some of which may be relevant to the decision.
- **Relevant Field Set**: The explicit list of fields that the DMN model chooses to compare between the two JSON objects.
- **Comparison Rule**: The FEEL-based logic used by the DMN decision to determine whether a field value is equal between the two objects.
- **Decision Result**: The boolean outcome produced by the DMN decision, representing whether all relevant fields match.
- **Process Instance Data**: The runtime data available to the BPMN process, including the retrieved objects and the final boolean comparison result.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For every valid comparison scenario in the test set, the DMN decision returns true only when all configured relevant fields are equal and false when any configured relevant field differs.
- **SC-002**: When the DMN model is updated to change the relevant field list or comparison semantics, the same process continues to function without code or BPMN modifications.
- **SC-003**: The process exposes the final boolean result as process data in 100% of successful executions of the comparison decision.
- **SC-004**: The feature supports the baseline business scenario of comparing exactly two JSON objects with explicitly defined relevant fields without any user interface or additional transformation layer.
- **SC-005**: The process supports routine rule changes without requiring a software release or process redesign, enabling non-code rule updates within the decision model.

## Assumptions

- The two JSON objects already exist in persistent storage and are available to the process when the comparison starts.
- The initial implementation is limited to comparing exactly two objects and does not need to handle multi-object or streaming scenarios.
- The set of relevant fields is declared explicitly in the DMN model, and any field outside that set is intentionally ignored for the purpose of the comparison.
- The comparison semantics in v1 are based on equality as defined by FEEL rules in the DMN decision, without additional cross-object transformation logic.
- No user interface is required for this initial release; the feature is delivered as a process and decision capability.
- The DMN model is deployed and managed separately from the BPMN process and application code so it can evolve independently.
