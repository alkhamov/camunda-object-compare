# Research: JSON Object Comparison Proof of Concept

## Decision

Use a minimal local runtime built with Docker Compose and Camunda 7, backed by MongoDB and a thin Java Spring Boot worker. The BPMN process will orchestrate object retrieval and DMN invocation, while the DMN model will own the configurable comparison rules and FEEL logic.

## Rationale

- Local-first compliance: the constitution requires the full runtime to run locally on macOS and to be easy to start/stop with scripts and Docker Compose.
- Camunda 7 alignment: BPMN and DMN are the required orchestration and decision engines, and the Java stack offers the most mature and straightforward integration pattern for a small local proof of concept.
- Separation of responsibilities: the worker retrieves full JSON documents from MongoDB but does not know which fields are relevant or how they are compared.
- Configurability: all comparison configuration lives inside the DMN model, which can be redeployed independently of code and BPMN.

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

- Object A and Object B will be stored as two MongoDB documents with the shared `serviceCharacteristic` array pattern.
- The worker will load both documents as complete JSON values and expose them as process variables for DMN evaluation.
- The DMN decision will receive the complete objects and define a dynamic list of relevant names via configuration entries.
- FEEL will iterate over the configured characteristic names, find the matching entries in each object's `serviceCharacteristic` array, extract the nested `value.value`, and compare them.
- The DMN output will be a single boolean result: `true` if all relevant characteristics match, otherwise `false`.

## Open design choices resolved for implementation

- Runtime composition: Docker Compose for Camunda 7 + MongoDB + worker.
- Worker technology: Java 17 + Spring Boot 2.7.x using the Camunda 7.22.x Spring Boot starter and MongoDB Java driver.
- Comparison scope: exactly two JSON objects; no user interface; no detailed difference report in v1.
- Comparison configuration authority: DMN model only.
