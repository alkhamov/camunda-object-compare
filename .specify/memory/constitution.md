<!--
Sync Impact Report
- Version change: none -> 1.0.0
- Modified principles: placeholders -> concrete principles (Local Runtime; Camunda 7; Separation of Orchestration and Decision Logic; DMN/FEEL as single source; Configurability & Simplicity)
- Added sections: Scope & Technical Constraints; Operational Guidance & Development Workflow
- Removed sections: none
- Follow-up TODOs: none
-->

# camunda-object-compare Constitution

## Core Principles

### I. Local Runtime (NON-NEGOTIABLE)
The application MUST run completely locally on a developer laptop. The initial version MUST NOT require cloud deployment.

- The complete runtime MUST be easy to start and stop locally using documented scripts or commands.
- Local-first operation is required to keep development, testing, and review fast and accessible to all contributors.

Rationale: Local execution minimizes infrastructure overhead, speeds iteration, and ensures the project remains accessible to contributors without cloud accounts or complex provisioning.

### II. Camunda 7 for Orchestration
Camunda 7 MUST be used as the process and decision engine for the project.

- BPMN MUST be used for process orchestration.
- DMN MUST be used for business decision logic executed by the Camunda 7 DMN engine.

Rationale: Locking to Camunda 7 and BPMN/DMN ensures a focused demonstration of process orchestration and decision automation without introducing multiple competing engines.

### III. Separation of Orchestration and Decision Logic (NON-NEGOTIABLE)
BPMN MUST be responsible for orchestration only. Business comparison logic MUST NOT be implemented in BPMN, nor in application/worker code.

- BPMN processes may orchestrate workers, data retrieval, and DMN evaluation calls, but MUST NOT encode business comparison rules.
- Application/worker code MAY handle transport, transformation, and retrieval of JSON objects, but MUST NOT make the comparison decision.

Rationale: Strict separation keeps orchestration declarative and simple while placing business rules in the single authoritative location (DMN), improving auditability and configurability.

### IV. DMN and FEEL as the Single Source of Truth for Comparison
All object comparison rules MUST be implemented in DMN using FEEL expressions. DMN is the single source of truth that determines whether two objects match.

- The final comparison result returned by DMN MUST be a boolean value: true when the objects match, false otherwise.
- Which fields are relevant and how they are compared MUST be controlled by DMN/FEEL, not by application code.

Rationale: Encoding rules in DMN/FEEL enables business users or rule authors to modify comparison logic without touching orchestration or code, and ensures consistent, testable decisions.

### V. Configurability and Minimalism
Comparison rules MUST be independently configurable and deployable. Changes to comparison rules MUST NOT require changes to application code or BPMN processes.

- A changed DMN model MUST be deployable independently of application code and without modifying BPMN models.
- Keep the initial implementation intentionally small and understandable; avoid unnecessary infrastructure and abstractions.
- Prefer the simplest architecture that demonstrates BPMN orchestration and configurable DMN/FEEL decision logic.

Rationale: Decoupling rules from code and processes reduces friction for maintenance and enables rapid iterations of business logic.

## Scope & Technical Constraints

- The system compares a defined set of relevant fields from two JSON objects. If all relevant fields are equal (as defined by the DMN model), the DMN decision returns true; if any relevant field differs, DMN returns false.
- Application/worker code MAY retrieve objects from persistent storage and MAY perform minimal transformation required for integration (for example converting storage formats to canonical JSON). Such code MUST NOT perform business comparisons.
- DMN/FEEL must express which fields to compare and the comparison semantics (exact equality, normalized equality, tolerance thresholds, existence checks, etc.).
- BPMN workflows orchestrate retrieval, DMN invocation, and downstream routing based on the boolean decision result.

## Operational Guidance & Development Workflow

- Provide simple local scripts to start Camunda 7 (engine + webapps) and the workers needed for the demo, and scripts to stop them. Documentation MUST describe startup steps clearly.
- DMN models used for comparison MUST be versioned independently (e.g., separate DMN deployment artifacts) so they can be updated without rebuilding application code.
- Tests:
  - Unit tests for DMN/FEEL decision tables or expressions are encouraged (where tooling permits).
  - Integration tests SHOULD validate end-to-end behavior: worker retrieval → DMN evaluation → BPMN routing.
- Keep the initial repository surface area small; add complexity only when necessary and justified in the constitution.

## Governance

- Amendments: Changes to this constitution MUST be proposed in a PR that documents the rationale, migration impact, and a test or verification plan. A change that is materially additive (new principle or significant expansion) constitutes a MINOR version bump. Editorial clarifications or wording fixes constitute a PATCH bump.
- Versioning policy:
  - MAJOR: Backward incompatible governance or removal/redefinition of essential principles.
  - MINOR: Addition of a new principle or material expansion of guidance.
  - PATCH: Clarifications, typo fixes, or non-substantive refinements.
- Compliance review: Every PR that implements features related to orchestration, decision logic, or comparison behavior MUST reference this constitution and include a short compliance checklist in the PR description explaining how the change adheres to the relevant principles.

**Version**: 1.0.0 | **Ratified**: 2026-09-25 | **Last Amended**: 2026-09-25
