# Implementation Plan: JSON Object Comparison

**Branch**: `001-json-object-compare` | **Date**: 2026-09-26 | **Spec**: `spec.md`

**Input**: Feature specification from `/specs/001-json-object-compare/spec.md`

## Summary

This feature implements a minimal local proof of concept that compares two stored JSON objects using Camunda 7 BPMN orchestration, a DMN decision model with configurable comparison rules, and a thin Java worker application. The runtime consists of Camunda 7, MongoDB, and a worker service running under Docker Compose. The worker retrieves complete JSON objects from MongoDB and exposes them as process variables; BPMN orchestrates the retrieval and DMN invocation; and DMN/FEEL owns all characteristic navigation, selection, extraction, and comparison logic. The evolved V2 architecture demonstrates explicit DMN ownership of the comparison configuration through a separate DMN decision.

## Technical Context

**Language/Version**: Java 17, Spring Boot 2.7.18, Camunda 7.22.0, MongoDB 7

**Runtime Stack**: Docker Compose with three services:
- **Camunda**: Camunda 7.22.0 platform (BPMN engine, DMN engine, REST API, Cockpit UI)
- **MongoDB**: MongoDB 7 (application object storage, initialized with seed data)
- **Worker**: Spring Boot 2.7.18 service with Camunda External Task Client library and MongoDB Java driver

**Architecture Pattern**: 
- BPMN orchestration: process retrieval flow and DMN invocation
- DMN decision models: V2 architecture with separate Comparison Configuration decision
- External Tasks: Java ExternalTaskHandler implementations for retrieve-object-a and retrieve-object-b topics
- MongoDB driver: independent connection for object retrieval (separate from Camunda engine database)

**Database Configuration**:
- **Application Storage**: MongoDB 7 for domain objects (objects collection with serviceCharacteristic array structure)
- **Camunda Engine**: H2 in-memory database for process instances, decision history, and engine state (suitable for local PoC)

**Current Validation Approach**: Manual integration testing using the running Docker Compose environment:
- Camunda REST API or Camunda Cockpit to start process instances
- MongoDB seed data for test objects
- Boolean comparison result verification through process instance history

**Target Platform**: macOS developer machines using Docker Compose for reproducible local runtime

**Project Type**: Local orchestration/decision proof-of-concept service

**Performance Goals**: Compare small JSON documents (< 2 MB each) in a single process invocation with sub-second end-to-end execution in local development

**Constraints**: Local-only runtime, no UI, exactly two JSON objects, no comparison logic in Java or BPMN, DMN model independently deployable

**Scale/Scope**: Proof of concept for small object-comparison workflow; no multi-object, streaming, or advanced transformation scenarios

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
camunda/
├── bpmn/
│   └── object-comparison.bpmn             # Base BPMN artifact
├── dmn/
│   └── object-comparison.dmn              # Base DMN artifact
├── v1/                                    # Baseline PoC implementation (asymmetric semantics)
│   ├── object-comparison-v1.bpmn
│   └── object-comparison-v1.dmn
└── v2/                                    # Current intended architecture (explicit config)
    ├── object-comparison-v2.bpmn
    └── object-comparison-v2.dmn

worker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/objectcompare/
│   │   │       ├── config/                # Spring Boot configuration
│   │   │       ├── service/               # External-task handlers and object retrieval
│   │   │       └── ObjectCompareApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/                              # Test suite (future work)
├── pom.xml
└── Dockerfile

mongo/
├── init/
│   └── seed-data.js                       # MongoDB initialization script
└── data/                                  # Persisted volume mount

docker-compose.yml                         # Defines camunda, mongo, worker services
```

**Structure Decision**: Intentionally minimal, conventional structure aligned with Docker Compose deployment:
- Base BPMN/DMN artifacts retained for project reference
- Versioned V1/V2 artifacts document architectural evolution; V2 represents current intended design
- Single worker application with external-task handlers and MongoDB retrieval service
- MongoDB initialization isolated in init/ directory with persisted volume for local development
- Docker Compose as the primary runtime mechanism

## Phase plan

### Phase 0: Research and decision resolution (COMPLETED)

- ✓ Confirmed runtime topology: Camunda 7 + MongoDB + worker under Docker Compose
- ✓ Confirmed worker responsibility boundary: retrieve complete JSON objects from MongoDB, expose as process variables with complete object content without business-level filtering, selection, extraction, normalization, or transformation. Worker implements ExternalTaskHandler to handle retrieve-object-a and retrieve-object-b topics; does not inspect serviceCharacteristic or know configured comparison set
- ✓ Confirmed DMN architecture: DMN model (V2) owns all object navigation, characteristic selection via configuration decision, nested value extraction, and comparison logic via FEEL
- ✓ Confirmed technology stack: Java 17, Spring Boot 2.7.18 (for documented Camunda 7.22.0 starter compatibility), MongoDB 7, Docker Compose local runtime
- ✓ Confirmed validation path: Docker Compose startup, MongoDB seed data initialization, REST API or Camunda Cockpit process invocation, boolean result inspection

### Phase 1: Design and contracts (COMPLETED)

- ✓ Defined persisted JSON object model: MongoDB documents with nested serviceCharacteristic array and value.value structure
- ✓ Defined BPMN process variables and DMN contract:
  - Process inputs: objectAId, objectBId (string IDs for MongoDB retrieval)
  - After retrieve-object-a: objectA (complete MongoDB document as Map)
  - After retrieve-object-b: objectB (complete MongoDB document as Map)
  - After evaluate-comparison: comparisonResult (boolean)
- ✓ Documented V2 DRD structure: Comparison Configuration decision provides characteristic list → Object Comparison V2 decision receives objectA, objectB, and comparisonConfiguration → returns comparisonResult
- ✓ Documented DMN-only change mechanism: edit Comparison Configuration decision's FEEL expression (list of characteristic names), redeploy DMN file; no Java or BPMN changes required

### Phase 2: Implementation (COMPLETED)

#### 2.1 Runtime Environment
- ✓ Docker Compose stack operational: three services (camunda, mongo, worker)
- ✓ Camunda 7.22.0 configured with H2 in-memory engine database, REST API at localhost:8080/engine-rest
- ✓ MongoDB 7 initialized with seed data (mongo/init/seed-data.js), persisted volume (mongo-data), accessible at localhost:27017
- ✓ Worker Spring Boot application built from worker/ directory, connects to Camunda via External Task Client at docker-compose network address, connects to MongoDB independently at docker-compose network address

#### 2.2 Worker Implementation - External Task Pattern
- ✓ Java ExternalTaskHandler implementations for two retrieval topics:
  - RetrieveObjectAHandler (topic: retrieve-object-a)
  - RetrieveObjectBHandler (topic: retrieve-object-b)
- ✓ Each handler:
  - Retrieves process variable (objectAId or objectBId)
  - Calls ObjectRetrievalService.fetchObject() to query MongoDB objects collection
  - Receives complete BSON Document, converts to Map<String, Object> (technical serialization; no business filtering)
  - Completes external task with externalTaskService.complete(), setting objectA or objectB process variable
- ✓ BPMN service tasks configured as external: camunda:type="external" camunda:topic="retrieve-object-a" (and retrieve-object-b)

#### 2.3 BPMN Orchestration
- ✓ Base BPMN process structure: Start → Retrieve Object A → Retrieve Object B → Evaluate Comparison → End
- ✓ V1 BPMN (process id: "object-comparison"): maps to object-comparison DMN decision
- ✓ V2 BPMN (process id: "object-comparison-v2"): maps to object-comparison-v2 DMN decision
- ✓ Business rule task configured: camunda:decisionRef="object-comparison-v2" (or object-comparison for V1), camunda:mapDecisionResult="singleEntry", camunda:resultVariable="comparisonResult"

#### 2.4 DMN Decision Models
- ✓ V1 (baseline): decision id="object-comparison" with asymmetric FEEL logic
  - Semantics: every characteristic in Object A must exist in Object B with equal value
  - Object A implicitly defines required comparison set
  - Characteristics in Object B not in Object A are ignored
- ✓ V2 (current intended architecture): 
  - comparison-configuration-v2 decision: returns explicit list of characteristic names (currently ["characteristicA", "characteristicC"])
  - object-comparison-v2 decision: depends on comparison-configuration-v2 + objectA + objectB inputs
  - FEEL logic: for each configured characteristic name, finds matching entries in both objects and compares nested values
  - Missing configured characteristics result in false

#### 2.5 Validation
- ✓ Manual integration validation via Docker Compose:
  - Start environment: docker-compose up -d
  - Camunda REST API at localhost:8080/engine-rest to trigger process instances
  - Camunda Cockpit for process instance inspection
  - Inspect process instance history for comparisonResult variable
  - Stop environment: docker-compose down

## Complexity Tracking

No constitution violations present. The design maintains clean separation of concerns:

- **Orchestration**: BPMN process flow only (Start → Retrieve A → Retrieve B → Evaluate → End)
- **Worker responsibility**: External task handlers retrieve complete objects and set process variables (no business comparison logic)
- **Comparison logic**: DMN/FEEL exclusively owns characteristic selection via configuration decision, extraction, and comparison semantics
- **Runtime**: Docker Compose local deployment with minimal services (Camunda engine + MongoDB app storage + worker)

The evolved V2 architecture demonstrates how DMN configuration decision enables business rule changes (which characteristics to compare) without requiring Java code changes or BPMN process redefinition.
