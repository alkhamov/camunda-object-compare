# Data Model: JSON Object Comparison

## Core entities

### StoredJsonObject

Represents one persisted JSON document retrieved for comparison.

- `_id`: unique object identifier used by MongoDB (ObjectId)
- `documentType`: string, identifies the logical kind of object (for example, `objectA`, `objectB`)
- `serviceCharacteristic`: array of `CharacteristicEntry` objects that define comparable characteristics
- `updatedAt`: date-time, persistence metadata

Validation rules:
- Each stored document must be valid JSON.
- Object A and Object B must be retrieved as complete documents before the DMN decision runs.
- The worker must not select, filter, extract, or normalize individual characteristics in application code.

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
- `name` is the lookup key used by the DMN comparison logic.
- `value.value` contains the actual value used in comparison after FEEL extraction.
- Extra fields are allowed but are outside the comparison configuration.

### ComparisonConfiguration

Defines the subset of relevant characteristics for comparison.

**Note**: This is not a MongoDB-persisted entity. It is a runtime DMN-owned configuration produced by the separate Comparison Configuration decision in the DMN model. It represents the Relevant Characteristic Set consumed by Object Comparison V2.

Conceptually:
- `relevantCharacteristicNames`: array of characteristic names that participate in comparison
- Returned at runtime by the DMN Comparison Configuration decision
- Determines which characteristics must be present in both objects and compared

Validation rules:
- Only names declared in the Comparison Configuration are considered relevant.
- For every configured characteristic, a matching characteristic by name must exist in both objects.
- If a configured characteristic is missing from either object, the comparison result is false.
- Characteristics outside the configured set are ignored.
- The configuration can be changed by redeploying the DMN model without changing BPMN or application code.

### DecisionResult

The conceptual output of the DMN comparison decision.

- `comparisonResult`: boolean

Validation rules:
- `comparisonResult` must be exactly `true` or `false`.
- It represents the conclusion of comparing the two objects according to the Comparison Configuration.

### ProcessInstanceData

Transient data used during BPMN process execution.

- `objectA`: complete JSON object from MongoDB (Map<String, Object>)
- `objectB`: complete JSON object from MongoDB (Map<String, Object>)
- `comparisonResult`: boolean returned by the DMN decision

Relationships:
- One process instance works with exactly two stored JSON objects and produces one comparison result.
- These variables are exposed to the process for orchestration and decision invocation.

## Relationship summary

### Persistent MongoDB data

- StoredJsonObject represents a persisted document and contains a serviceCharacteristic array of CharacteristicEntry objects.

### Runtime DMN configuration

- `ComparisonConfiguration` is produced by a separate DMN decision and represents the Relevant Characteristic Set.
- It is not persisted in MongoDB or application code; it is evaluated at runtime by the DMN model.
- Object Comparison V2 decision consumes `ComparisonConfiguration` along with `objectA` and `objectB` to evaluate equality.

### Transient BPMN process data

- `ProcessInstanceData` binds the complete objects and the comparison result into the BPMN execution context.
- `objectA` and `objectB` are exposed as complete Map structures without business-level transformation.
- `comparisonResult` is the Boolean output from the DMN comparison decision.

### Comparison flow

```
objectA (StoredJsonObject)   \
                              |
objectB (StoredJsonObject)   | -> Object Comparison V2 -> comparisonResult (Boolean)
                              |
ComparisonConfiguration   ---/
(DMN decision output)
```

## Comparison semantics (V2)

- The Relevant Characteristic Set is explicitly defined by the Comparison Configuration decision.
- For every characteristic in the Comparison Configuration:
  - A matching characteristic by name must exist in both objects.
  - The corresponding nested values must compare equal.
  - If the configured characteristic is missing from either object, the result is false.
  - Characteristics outside the configured set are ignored.
- The overall result is true only if all configured characteristics match across both objects.

**Architecture note**: V1 (baseline) used asymmetric implicit semantics where Object A defined the required comparison set. V2 (current) replaces this with an explicit DMN-owned Comparison Configuration decision that returns the relevant characteristic names.

## Data handling constraints

- Complete MongoDB document content is transported to the BPMN process without business-level filtering, selection, extraction, normalization, or transformation.
- BSON Document to Map<String, Object> conversion is technical serialization required for Camunda process variable exposure; it is not a business-level transformation.
- The worker retrieves complete objects without selecting, filtering, extracting, or normalizing individual characteristics.
- The DMN decision model owns all characteristic navigation, selection, value extraction, and comparison logic.
- The comparison configuration is not hardcoded in application code; it is defined and controlled through the DMN model.
