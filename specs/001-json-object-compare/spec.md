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

1. **Given** two JSON objects stored in persistent object storage where all characteristics defined as relevant by the DMN decision have the same values in both objects, **When** the process retrieves both objects and evaluates the DMN decision, **Then** the decision result is true and the process data contains true.
2. **Given** two JSON objects stored in persistent object storage where at least one characteristic defined as relevant by the DMN decision has a different value, **When** the process evaluates the decision, **Then** the decision result is false and the process data contains false.
3. **Given** a configured characteristic is missing from one or both objects, **When** the process evaluates the decision, **Then** the decision result is false.
4. **Given** the DMN model is updated to change which characteristics are relevant for comparison or to change the FEEL comparison logic, **When** the same process runs without code or BPMN changes, **Then** the process uses the updated decision logic and produces the result based on the revised configuration.

---

### User Story 2 - Manage business comparison rules in the decision model (Priority: P2)

A rule maintainer needs to change which characteristics are considered relevant for comparison and how their values are compared without touching application code or the BPMN process. This allows comparison behavior to evolve as business requirements change.

**Why this priority**: It keeps comparison logic configurable within DMN and reduces the risk of deployment delays or code changes for routine rule updates.

**Independent Test**: The DMN decision model can be updated in a separate deployment and used by the same process to compare objects with a different set of relevant characteristics or revised comparison semantics without requiring process or application code changes.

**Acceptance Scenarios**:

1. **Given** a DMN decision that defines a specific set of relevant characteristics for comparison, **When** the process compares two objects, **Then** only those defined characteristics are considered for equality, and characteristics outside that set are ignored.
2. **Given** a DMN decision that changes the FEEL comparison logic for a characteristic (e.g., case sensitivity, numeric tolerance), **When** the process is rerun, **Then** the comparison result reflects the updated logic without requiring code or BPMN changes.
3. **Given** the DMN decision can declare the relevant characteristic set through a separate decision or configuration (e.g., returning a list of characteristic names to compare), **When** that configuration is updated, **Then** the comparison uses only the newly configured characteristics without requiring changes to the main comparison logic or application code.

---

### Edge Cases

- What happens when a characteristic defined as relevant by the DMN decision is missing from one or both objects? (Expected: comparison result is false)
- How does the system handle objects that contain characteristics outside the DMN-defined relevant set? (Expected: those characteristics are ignored; they do not affect the comparison result)
- How does the system behave when characteristic values are different data types or formats that may not directly compare via FEEL equality? (Expected: the DMN decision's FEEL logic determines equality semantics; type coercion is a FEEL concern, not an application concern)
- What happens when a process is started with more or fewer than two objects in scope for comparison? (Expected: feature supports exactly two objects; additional or missing objects are out of scope for the initial version)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The application MUST retrieve exactly two JSON objects from persistent object storage before the comparison decision is evaluated.
- **FR-002**: The process MUST make each retrieved JSON object available as process data so it can be used by the DMN decision.
- **FR-002a**: Retrieved JSON objects MUST be transported to the DMN decision in their complete, unmodified form. The application/worker code MUST NOT filter, select, extract, normalize, or transform individual characteristics for business comparison purposes. All characteristic selection, extraction, and comparison logic belongs to the DMN decision.
- **FR-003**: The comparison MUST be evaluated by a DMN decision that uses FEEL expressions to determine equality.
- **FR-004**: The DMN decision MUST return a boolean result: true when all characteristics defined as relevant by the DMN model have equal values in both objects, and false when at least one relevant characteristic differs or is missing from either object.
- **FR-005**: The set of relevant characteristics for comparison MUST be defined within the DMN model. The DMN model MUST own this configuration independently of application code and BPMN orchestration.
- **FR-006**: The comparison rules for relevant characteristics MUST be defined in the DMN model using FEEL expressions and MUST be configurable without changing application code.
- **FR-007**: Changing the set of relevant characteristics or updating the FEEL comparison logic in the DMN model MUST NOT require changes to the BPMN process definition or application code.
- **FR-008**: After the DMN decision is evaluated, the process MUST expose the boolean comparison result as process data for downstream use.
- **FR-009**: The feature MUST support only the initial use case of comparing exactly two JSON objects with characteristics and comparison rules defined by the DMN model.
- **FR-010**: The initial version MUST NOT require a user interface or complex transformation logic beyond retrieving stored JSON objects and passing them in complete form to the DMN decision.

### Key Entities *(include if feature involves data)*

- **Stored JSON Object**: A JSON value persisted in stable storage and retrieved by the process for comparison. The complete object structure, including all its characteristics and nested values, is transported to the DMN decision unmodified.
- **Relevant Characteristic Set**: The set of characteristics (field names) that the DMN model defines as the basis for comparison. The DMN model owns this configuration independently of application code and BPMN orchestration.
- **Comparison Rule**: The FEEL-based logic in the DMN decision that determines whether two characteristic values are equal. The application code does not implement comparison semantics; all comparison logic resides in the DMN decision.
- **Comparison Result**: The boolean outcome produced by the DMN decision, representing whether all characteristics in the Relevant Characteristic Set have equal values in both objects (true) or whether at least one characteristic differs or is missing (false).
- **Process Instance Data**: The runtime variables available to the BPMN process, including the complete retrieved objects (objectA and objectB) and the final boolean comparison result.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For every valid comparison scenario in the test set, the DMN decision returns true when all characteristics in the Relevant Characteristic Set have equal values in both objects, and returns false when any configured characteristic is missing from either object or has a different value.
- **SC-002**: When the DMN model is updated to change which characteristics are relevant for comparison or to update the FEEL comparison logic, the same BPMN process and application code continue to function without modifications.
- **SC-003**: The process exposes the final boolean comparison result as process data in 100% of successful executions of the comparison decision.
- **SC-004**: The feature supports the baseline use case of comparing exactly two JSON objects with characteristics and comparison logic defined by the DMN model without requiring user interface development or additional transformation logic in the application.
- **SC-005**: Routine updates to comparison configuration or FEEL logic can be implemented through DMN model changes without requiring changes to application code or redesign of the BPMN process.

## Assumptions

- The two JSON objects already exist in persistent storage and are available to the process when the comparison starts.
- The initial implementation supports only the use case of comparing exactly two objects and does not need to handle multi-object or streaming scenarios.
- The set of relevant characteristics for comparison is defined within the DMN model. The DMN model owns this configuration independently of application code and BPMN orchestration.
- The comparison semantics are determined by FEEL rules in the DMN decision without requiring additional transformation or comparison logic in application code.
- No user interface is required for this initial release; the feature is delivered as a process and decision capability.
- The DMN model is deployed and managed separately from the BPMN process and application code, allowing comparison logic to evolve independently without requiring application or process changes.
