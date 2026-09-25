# Data Model: JSON Object Comparison

## Core entities

### StoredJsonObject

Represents one persisted JSON document retrieved for comparison.

- `id`: string or unique object identifier used by MongoDB
- `documentType`: string, identifies the logical kind of object (for example, `objectA`/`objectB` or a domain-specific identifier)
- `payload`: object, the complete JSON document as stored in MongoDB
- `serviceCharacteristic`: array of `CharacteristicEntry` objects, optional but expected in the v1 domain model
- `updatedAt`: date-time, persistence metadata

Validation rules:
- Each stored document must be valid JSON.
- Object A and Object B must be retrieved as complete payloads before the DMN decision runs.
- The worker must not extract or normalize individual comparison keys in application code.

### CharacteristicEntry

A single attribute entry inside the `serviceCharacteristic` collection.

- `name`: string, the characteristic's logical name (for example, `Home-ID`, `Technology`)
- `valueType`: string, the value's declared type
- `value`: object, containing nested value information, typically with `@type` and `value`

Example structure:

```json
{
  "name": "Home-ID",
  "valueType": "string7",
  "value": {
    "@type": "string7",
    "value": "5F8RGZQ"
  }
}
```

Validation rules:
- `name` is the lookup key used by the DMN comparison configuration.
- `value.value` is the actual compared value after FEEL extraction.
- Extra fields are allowed but are outside the comparison configuration.

### ComparisonConfiguration

Defines the subset of characteristic names and comparison semantics controlled by DMN.

- `relevantCharacteristicNames`: array of strings
- `comparisonSemantics`: object or decision table entries defining equality logic per characteristic
- `version`: string or version identifier for the deployed DMN model

Validation rules:
- Only names declared in the DMN configuration are considered relevant.
- The list can be changed by deploying a new DMN model without changing BPMN or the worker.

### DecisionResult

The output of the DMN comparison decision.

- `comparisonResult`: boolean
- `sourceDecision`: string, identifies the DMN decision definition
- `evaluatedAt`: date-time, optional for traceability

Validation rules:
- `comparisonResult` must be exactly `true` or `false`.
- It must be set as process data after DMN execution.

### ProcessInstanceData

The runtime data visible to the BPMN process.

- `objectA`: full JSON object from MongoDB
- `objectB`: full JSON object from MongoDB
- `comparisonResult`: boolean returned by DMN
- `processState`: string such as `retrieved`, `evaluated`, `completed`

Relationships:
- One process instance references exactly two stored JSON objects and one comparison result.
- The comparison configuration is not stored in application code; it is loaded from the DMN model at runtime.

## Relationship summary

- `StoredJsonObject` contains zero or more `CharacteristicEntry` records.
- `ComparisonConfiguration` references the relevant characteristic names used to filter records in both objects.
- `DecisionResult` is derived from comparing `objectA` and `objectB` according to the DMN configuration.
- `ProcessInstanceData` binds the full objects and final result into the BPMN execution context.

## Data handling constraints

- The worker retrieves complete objects without value-level knowledge.
- The DMN decision owns the field-selection rules and equality logic.
- No additional transformation layer is required in v1 beyond retrieval and process-variable exposure.
