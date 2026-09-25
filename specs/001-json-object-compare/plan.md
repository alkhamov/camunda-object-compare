# Implementation Plan: JSON Object Comparison

**Branch**: `001-json-object-compare` | **Date**: 2026-09-26 | **Spec**: `spec.md`

**Input**: Feature specification from `/specs/001-json-object-compare/spec.md`

## Summary

This feature creates a minimal local proof of concept that compares two stored JSON objects using Camunda 7 BPMN orchestration and a DMN decision model. The runtime will consist of Camunda 7, MongoDB, and a thin Java worker application. The worker retrieves full JSON payloads from MongoDB and exposes them as process variables; BPMN orchestrates the retrieval and DMN invocation; and DMN/FEEL owns the configurable comparison logic for the relevant `serviceCharacteristic` names and comparison semantics.

## Technical Context

**Language/Version**: Java 17 with Spring Boot 2.7.x, using the Camunda 7 Spring Boot starter on Camunda 7.22.x

**Primary Dependencies**: Camunda 7 BPMN engine + DMN engine, Spring Boot 2.7.x, MongoDB Java driver, Docker Compose, optional Camunda Modeler

**Compatibility Decision (Phase 0 research)**: We do not assume Java 21 + Spring Boot 3.x compatibility with Camunda 7. Official Camunda 7 Spring Boot starter documentation states the starter requires Java 17, and the safest local proof-of-concept stack is the established Camunda 7.22.x + Java 17 + Spring Boot 2.7.x combination. The engine itself may support newer Java runtimes in general, but the worker stack is intentionally bounded to the documented starter-compatible configuration to keep the implementation reproducible and low-risk.

**Storage**: MongoDB 7 running locally via Docker Compose with a persisted volume

**Testing**: JUnit 5 for worker tests; DMN decisions validated via Camunda DMN/unit-style checks; BPMN integration tests for retrieval → decision → boolean result

**Target Platform**: macOS developer machines using Docker Compose for the complete local runtime

**Project Type**: local orchestration / decision proof-of-concept service

**Performance Goals**: compare small JSON documents (< 2 MB each) in a single process invocation with sub-second end-to-end execution in local development

**Constraints**: local-only runtime, no UI, exactly two JSON objects in v1, no comparison logic in application code or BPMN, DMN model must be redeployable independently

**Scale/Scope**: proof of concept for a small object-comparison workflow; no multi-object, streaming, or advanced transformation scenarios

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Local runtime on macOS: PASS. The design uses Docker Compose and local containers; no cloud dependency.
- Camunda 7 for orchestration and DMN decisions: PASS. BPMN orchestrates retrieval and decision invocation, DMN handles comparison.
- Separation of orchestration and decision logic: PASS. BPMN does not implement field rules or evaluation logic; the worker does not decide equality.
- Worker responsibility boundary: PASS. The worker retrieves both complete JSON objects from storage and passes both object structures intact to the process/DMN. It does not extract, filter, select, normalize, map, or inspect individual `serviceCharacteristic` entries; it does not know which characteristics are configured for comparison; it does not navigate inside the JSON to choose values. Navigation, selection, extraction, and comparison remain entirely in DMN/FEEL.
- DMN/FEEL as single source of truth: PASS. Comparison behavior is defined in DMN and FEEL, not in Java or BPMN.
- Configurability and minimalism: PASS. The DMN model controls relevant characteristic names and FEEL logic, and the runtime remains intentionally small.
- No implementation of complex transformation or UI: PASS. v1 only handles exactly two JSON documents and a boolean result.

## Project Structure

### Documentation (this feature)

```text
specs/001-json-object-compare/
├── plan.md              # This file
├── research.md          # Decision log and resolved unknowns
├── data-model.md        # Core domain entities and validation rules
├── quickstart.md        # Local day-1 validation steps
├── contracts/           # Internal contracts for process variables and DMN input/output
│   ├── README.md
│   └── dmn-comparison-contract.md
├── spec.md              # Feature specification
└── tasks.md             # Generated later by /speckit-tasks
```

### Source Code (repository root)

```text
.docker/
├── camunda/
├── mongo/
├── worker/

camunda/
├── bpmn/
│   └── object-comparison.bpmn
├── dmn/
│   └── object-comparison.dmn
├── scripts/
│   ├── start-local.sh
│   └── stop-local.sh

worker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       └── java/
├── pom.xml
└── application.yml

mongo/
├── init/
│   └── seed-data.js
└── data/

docker-compose.yml
```

**Structure Decision**: Keep the proof of concept intentionally small and conventional: one root-level Docker Compose file, one Camunda configuration area, one MongoDB initialization area, and one thin Java worker application. This structure supports the constitution’s local runtime and separation-of-responsibilities constraints without introducing unnecessary infrastructure or abstraction layers.

## Phase plan

### Phase 0: Research and decision resolution

- Confirm the local runtime topology: Camunda 7 + MongoDB + worker under Docker Compose.
- Confirm the worker responsibility boundary: retrieve both full JSON objects, expose both object structures intact as process variables, and keep all comparison logic out of Java. The worker must not extract, filter, select, normalize, map, or otherwise inspect individual `serviceCharacteristic` entries for comparison purposes, and it must not know the configured characteristic names.
- Confirm the DMN configuration pattern: the DMN model owns all navigation, selection, value extraction, and comparison logic via FEEL; the worker passes the complete objects and leaves decision semantics to DMN.
- Confirm the compatible technology stack: Camunda 7 Spring Boot starter requires Java 17, so the project will not assume Java 21 + Spring Boot 3.x compatibility. We will use a safe Camunda 7.22.x release on Java 17 and Spring Boot 2.7.x for the local proof of concept, and document the exact version in implementation setup.
- Confirm the minimal validation path: local start, seed sample objects, trigger BPMN process, inspect boolean result.

### Phase 1: Design and contracts

- Define the persisted JSON object model and the `serviceCharacteristic` array semantics.
- Define the BPMN process variables and the DMN decision input/output contract.
- Create the local startup, validation, and teardown instructions.
- Document the change mechanism for DMN-only reconfiguration without code or BPMN changes.

### Phase 2: Implementation (future work)

- Add Docker Compose configuration for Camunda 7 and MongoDB.
- Add a thin Java worker that fetches Object A and Object B from MongoDB.
- Deploy the BPMN process and DMN decision into the local Camunda runtime.
- Validate the true/false comparison scenarios against the seed data set.
- Confirm DMN-only updates can alter comparison behavior without rebuilding the worker or BPMN process.

## Complexity Tracking

No constitution violations are expected for this project. The design intentionally avoids unnecessary services or abstraction layers, which keeps the runtime compliant and easy to operate locally.
