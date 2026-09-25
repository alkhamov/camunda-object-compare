# DMN Comparison Contract

## Purpose

The DMN decision is the authoritative comparison engine for the feature. It accepts two complete JSON objects and returns a single boolean result.

## Input contract

### `objectA`

Type: object

A complete JSON document retrieved from MongoDB.

### `objectB`

Type: object

A complete JSON document retrieved from MongoDB.

### `comparisonConfiguration`

Type: object or list

A DMN-owned configuration that defines the relevant characteristic names and the rules used to evaluate them.

This configuration is not implemented in Java, BPMN, or worker code. It is part of the DMN deployment.

## Output contract

### `comparisonResult`

Type: boolean

- `true`: all configured relevant characteristics match
- `false`: at least one configured relevant characteristic differs

## Behavioral contract

- The DMN decision receives the full object payloads without any application-side field extraction.
- FEEL expressions locate matching `serviceCharacteristic` entries by name in both objects.
- FEEL extracts the nested `value.value` and compares the results.
- The decision ignores unrelated fields outside the configured comparison set.
- Changing the configuration in DMN is sufficient to change behavior without altering BPMN or application code.
