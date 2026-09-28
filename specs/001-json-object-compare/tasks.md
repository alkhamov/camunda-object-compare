# Tasks: JSON Object Comparison

**Input**: Design documents from `/specs/001-json-object-compare/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: No automated test tasks were requested in the specification; validation is handled via local runtime checks and DMN/BPMN execution verification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and local runtime scaffolding

- [x] T001 Create the local project structure per plan: `docker-compose.yml`, `.docker/`, `camunda/`, `mongo/`, and `worker/` at the repository root.
- [x] T002 Initialize the worker Maven project in `worker/pom.xml` with Java 17, Spring Boot 2.7.x, the Camunda 7.22.x Spring Boot starter, and the MongoDB Java driver.
- [x] T003 [P] Create the base config and deployment directories: `worker/src/main/resources/`, `camunda/bpmn/`, `camunda/dmn/`, `mongo/init/`, and `mongo/data/`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that must be complete before any user story work begins

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 Configure `docker-compose.yml` to start Camunda 7 and MongoDB locally with the required persisted volume and initialization path.
- [x] T005 [P] Add `worker/application.yml` and the worker configuration needed for Camunda and MongoDB connectivity in the local runtime.
- [x] T006 [P] Implement the MongoDB access layer in `worker/src/main/java/` to fetch both complete JSON documents without value extraction or field selection in Java.
- [x] T007 Define the BPMN process-variable contract in `camunda/bpmn/object-comparison.bpmn` and the DMN input/output contract in `camunda/dmn/object-comparison.dmn` so the worker passes complete object payloads to DMN.
- [x] T008 Create the seed fixture documents in `mongo/init/seed-data.js` that exercise matching and non-matching `serviceCharacteristic` scenarios for the initial validation path.

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Compare two stored JSON objects with a configurable decision (Priority: P1) 🎯 MVP

**Goal**: Retrieve two complete JSON objects, pass them to the DMN decision, and expose the boolean result as process data.

**Independent Test**: Start the local stack, seed two stored JSON objects, execute the process, and verify that `comparisonResult` is `true` when relevant values match and `false` when at least one relevant value differs.

### Implementation for User Story 1

- [x] T009 [US1] Implement the worker retrieval service in `worker/src/main/java/` to fetch object A and object B from MongoDB as complete JSON payloads and expose both objects as process variables without extracting `serviceCharacteristic` entries in Java.
- [x] T010 [US1] Implement the BPMN orchestration in `camunda/bpmn/object-comparison.bpmn` so the process retrieves both objects, invokes the DMN decision, and stores the boolean result as process data.
- [x] T011 [US1] Implement the DMN decision in `camunda/dmn/object-comparison.dmn` to accept both complete objects and return a single boolean `comparisonResult` using FEEL expressions.
- [x] T012 [US1] Ensure the DMN logic evaluates the Relevant Characteristic Set as defined by the DMN model. For every configured characteristic: it must exist in both objects, and the corresponding values must compare equal. A missing configured characteristic results in false. Characteristics outside the configured set are ignored.
- [x] T013 [US1] Validate the success path in the local runtime by running the comparison against a matching dataset and confirming the process result is `true`.
- [x] T014 [US1] Validate the failure path in the local runtime by running the comparison against a differing dataset and confirming the process result is `false`.

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Manage business comparison rules in the decision model (Priority: P2)

**Goal**: Prove the comparison rules are configurable in DMN/FEEL without changing application code or BPMN.

**Independent Test**: Update the DMN model to use a different characteristic list or comparison rule, rerun the process without code or BPMN changes, and verify the result changes accordingly.

### Implementation for User Story 2

- [x] T015 [US2] Update the DMN configuration design in `camunda/dmn/object-comparison.dmn` so the relevant characteristic names are declared in DMN rather than in Java or BPMN.
- [x] T016 [US2] Encode the FEEL comparison logic in `camunda/dmn/object-comparison.dmn` so it selects, extracts, and compares the configured values within the DMN model itself, leaving the worker blind to the configured names.
- [ ] T017 [US2] Document the DMN-only reconfiguration flow in `specs/001-json-object-compare/quickstart.md` so a rule maintainer can replace the comparison set or comparison semantics without rebuilding the worker or BPMN process.
- [x] T018 [US2] Validate the rule-change path by deploying a revised DMN model, rerunning the same process, and confirming the result reflects the updated configuration without code or BPMN edits. A controlled test with Object B having characteristicB = DIFFERENT confirmed that V2 Comparison Configuration containing ["characteristicA", "characteristicC"] (excluding characteristicB) returned true, while V1 returned false, demonstrating DMN-owned configuration independence from worker and BPMN changes.

**Checkpoint**: At this point, User Stories 1 and 2 should both work independently

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final project quality checks and documentation completeness

- [ ] T019 [P] Review and finalize the local startup and teardown instructions in `specs/001-json-object-compare/quickstart.md` so the runtime can be started and validated consistently.
- [ ] T020 [P] Confirm the docs in `specs/001-json-object-compare/research.md`, `specs/001-json-object-compare/plan.md`, and `specs/001-json-object-compare/data-model.md` are mutually consistent on the worker technology stack and the architecture boundary.
- [x] T021 Confirm the worker boundary is preserved: the Java code only retrieves full JSON objects and passes them through; no extraction, normalization, or comparison logic is implemented outside DMN/FEEL.
- [ ] T022 Run the end-to-end local validation path for the MVP scenario and record any remaining gaps before implementation is considered complete.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3 and 4)**: Depend on Foundational completion
- **Polish (Phase 5)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) and is the MVP
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) and should validate DMN-only rule updates independently of application code

### Parallel Opportunities

- Setup tasks T001-T003 can be executed in parallel, except where a directory must exist before files are created.
- Foundational tasks T004-T008 can be split across the runtime configuration, worker retrieval layer, and DMN/BPMN contract definition.
- User Story 1 tasks T009-T014 can proceed in parallel within the story once the runner and DMN contract are available.
- User Story 2 tasks T015-T018 are DMN-specific and can be implemented in parallel with the final validation steps once the story boundary is established.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. Stop and validate the matching and non-matching JSON comparison scenarios
5. Only then continue to User Story 2 rule-reconfiguration validation

### Incremental Delivery

1. Establish the local runtime and worker retrieval boundary
2. Deliver the core BPMN + DMN comparison flow for a single two-object comparison
3. Validate DMN-only rule changes without code or BPMN edits
4. Finalize documentation and cross-cutting quality checks

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is complete:
   - Developer A: User Story 1 comparison flow and validation
   - Developer B: User Story 2 DMN configuration and reconfiguration validation
3. Final polish and documentation pass after story completion

---

## Notes

- [P] tasks = different files or parallelizable work items without dependency conflicts
- [US1]/[US2] labels map each task to the relevant user story for traceability
- Each user story is independently testable using the local Camunda + MongoDB runtime
- The Java worker remains a retrieval-only component; DMN/FEEL owns selection, extraction, and comparison logic
- No UI, no additional transformation layer, and no architecture change are introduced in v1
