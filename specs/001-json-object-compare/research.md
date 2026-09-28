# Research: JSON Object Comparison Proof of Concept

## Decision

Use a minimal local runtime built with Docker Compose and Camunda 7, backed by MongoDB and a thin Java Spring Boot worker. The BPMN process orchestrates object retrieval and DMN invocation, while the DMN model owns the configurable comparison rules and FEEL logic.

## Rationale

- Local-first compliance: the constitution requires the full runtime to run locally on macOS and to be easy to start/stop with Docker Compose.
- Camunda 7 alignment: BPMN and DMN are the required orchestration and decision engines, and the Java stack offers the most mature and straightforward integration pattern for a small local proof of concept.
- Separation of responsibilities: the worker retrieves full JSON documents from MongoDB but does not select, extract, or compare individual characteristics. All business-level field selection, extraction, and comparison logic belongs to DMN/FEEL.
- Configurability: the comparison configuration lives inside the DMN model through a separate Comparison Configuration decision, which can be redeployed independently of application code and BPMN.

## Alternatives considered

1. Node.js worker with MongoDB and BPMN integration
   - Pros: lower runtime footprint and familiar JSON handling.
   - Cons: more awkward for a strict Camunda 7 Java-centric workflow and less aligned with the default enterprise BPMN/DMN integration patterns.

2. Hardcoded comparison logic in BPMN or worker code
   - Rejected because it violates the constitution and makes field selection and comparison rules unchangeable without process or code edits.

3. Complex multi-service architecture
   - Rejected because the project is explicitly a small proof of concept and should remain intentionally minimal.

4. No dedicated worker; direct DB access in BPMN or script task
   - Rejected because the constitution requires the worker/application layer to retrieve MongoDB data while BPMN remains orchestration-only.

## Design findings

- Object A and Object B are stored as MongoDB documents with a shared `serviceCharacteristic` array pattern.
- The worker retrieves both complete documents without business-level selection, filtering, extraction, or normalization, and exposes them as process variables.
- BPMN orchestrates the retrieval flow and invokes the DMN comparison decision.
- The DMN decision model (via FEEL expressions) owns all object navigation, characteristic selection, nested value extraction, and comparison semantics.
- The Relevant Characteristic Set is explicitly defined by the DMN model through a separate Comparison Configuration decision.
- For each characteristic in the Comparison Configuration:
  - Both objects must contain a matching characteristic by name.
  - The corresponding nested values must compare equal.
  - If a configured characteristic is missing from either object, the result is false.
  - Characteristics outside the configured set are ignored.
- The DMN comparison decision returns a single boolean result: true if all configured characteristics match, otherwise false.

**Architecture evolution**: V1 (baseline) used asymmetric implicit logic where Object A defined the required comparison set. V2 (current intended) replaces this with an explicit DMN-owned Comparison Configuration decision that returns the relevant characteristic names, enabling reconfiguration without Java or BPMN changes.

## Open design choices resolved for implementation

- Runtime composition: Docker Compose for Camunda 7 + MongoDB + worker.
- Worker technology: Java 17 + Spring Boot 2.7.x using the Camunda 7.22.x Spring Boot starter and MongoDB Java driver.
- Worker-Camunda integration: The Camunda External Task Client pattern (Spring Boot starter) with topic-based external service tasks. The worker and Camunda communicate via external tasks and process variables. MongoDB connectivity is independent of the Camunda integration.
- Object transport: Complete BSON Document structures are converted to generic Map<String,Object> for Camunda process variable exposure; this is technical serialization only, not business transformation.
- Comparison configuration authority: DMN model, specifically a separate Comparison Configuration decision that feeds into the main Object Comparison V2 decision via DMN decision dependencies.
- Comparison scope: exactly two JSON objects; no user interface; no detailed difference report in the current PoC scope.
