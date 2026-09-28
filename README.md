# Camunda DMN Object Comparison PoC

A small proof of concept exploring how **DMN and FEEL can be used to move selected system logic from application code into configurable decision models**.

The example compares characteristics contained in two nested JSON objects.

The main idea is to keep the application responsible for retrieving and transporting data, while DMN determines **what should be compared and how the comparison is performed**.

## Motivation

DMN is often associated with decision tables and relatively straightforward business rules.

Combined with FEEL expressions, however, a DMN decision can also work with structured data, navigate nested values, iterate over collections, and compose multiple decisions.

This PoC explores one possible application of that capability:

> Can comparison logic for complex objects be moved out of application code and into a configurable DMN decision?

Doing so can be useful when selected system behavior should be changeable without modifying and redeploying application code.

## Architecture

The local runtime consists of:

- **Camunda 7** — BPMN orchestration and DMN execution
- **DMN / FEEL** — comparison logic and configuration
- **Java external worker** — retrieval and transport of complete objects
- **MongoDB** — storage of sample objects
- **Docker Compose** — local runtime

Conceptually:

```text
MongoDB
   │
   ▼
Java External Worker
   │
   │ complete object structures
   ▼
BPMN Process
   │
   ▼
DMN / FEEL
   │
   ▼
comparisonResult : Boolean
```

The Java worker does not know which characteristics participate in the comparison. Characteristic selection and nested value comparison belong to the DMN decision model.

## V1 — Moving Comparison Logic into DMN

V1 moves the comparison algorithm itself into DMN.

```text
Object A ─────┐
              ├──> Object Comparison ──> comparisonResult
Object B ─────┘
```

The FEEL expression navigates the `serviceCharacteristic` collections and compares nested values.

Object A implicitly defines the set of characteristics that must have corresponding equal values in Object B.

Implementation:

- [`camunda/v1/object-comparison-v1.dmn`](camunda/v1/object-comparison-v1.dmn)
- [`camunda/v1/object-comparison-v1.bpmn`](camunda/v1/object-comparison-v1.bpmn)

## V2 — Moving Comparison Configuration into DMN

V2 takes the idea one step further.

A separate DMN decision defines which characteristics participate in the comparison.

```text
           Comparison Configuration
                    │
                    ▼
Object A ─────> Object Comparison V2 <───── Object B
                    │
                    ▼
             comparisonResult
```

For example:

```feel
["characteristicA", "characteristicC"]
```

With this configuration, differences in `characteristicB` do not affect the result.

This means the comparison scope can change at the decision-model level without adding characteristic-specific logic to the Java worker or BPMN process.

Implementation:

- [`camunda/v2/object-comparison-v2.dmn`](camunda/v2/object-comparison-v2.dmn)
- [`camunda/v2/object-comparison-v2.bpmn`](camunda/v2/object-comparison-v2.bpmn)

## Example

The repository contains seeded MongoDB objects such as:

```text
                     Object A       Object B
characteristicA      valueA         valueA
characteristicB      valueB         DIFFERENT
characteristicC      valueC         valueC
```

With the V2 comparison configuration:

```feel
["characteristicA", "characteristicC"]
```

the result is:

```text
comparisonResult = true
```

`characteristicB` differs, but it is deliberately outside the configured comparison set.

## Run the PoC

A tested step-by-step procedure is available here:

[`specs/001-json-object-compare/quickstart.md`](specs/001-json-object-compare/quickstart.md)

It covers:

- starting the Docker Compose runtime;
- verifying MongoDB seed data;
- deploying the V2 DMN and BPMN models;
- starting the comparison process;
- verifying process completion;
- verifying the Boolean comparison result.

## Repository Structure

```text
camunda/
├── v1/                         # V1 BPMN and DMN
└── v2/                         # V2 BPMN and DMN

mongo/
└── init/
    └── seed-data.js            # Sample comparison objects

worker/
└── src/main/                   # Java external worker

specs/
└── 001-json-object-compare/
    ├── spec.md                 # Functional specification
    ├── research.md             # Architecture/design decisions
    ├── data-model.md           # Data model
    ├── plan.md                 # Implementation plan
    ├── tasks.md                # Implementation tasks
    └── quickstart.md           # Reproducible runtime validation
```

## Scope

This project is intentionally a small proof of concept.

It demonstrates how DMN/FEEL can own configurable comparison behavior; it is not intended to present a production-ready generic object-comparison framework.

Possible future extensions include:

- dynamic comparison configuration;
- characteristic-specific comparison rules;
- optional and mandatory characteristics;
- normalization rules;
- context-dependent comparison behavior.

## Local Development Notice

The Docker Compose configuration is intended for **local demonstration only**.

It uses local development defaults, including the Camunda `demo` user and an unauthenticated local MongoDB instance. These settings are not intended for production deployment.