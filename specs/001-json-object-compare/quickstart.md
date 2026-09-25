# Quickstart Guide

This guide describes the validation flow for the local proof-of-concept runtime before implementation begins.

## Prerequisites

- macOS developer machine
- Docker Desktop installed and running
- Docker Compose available
- Optional: Camunda Modeler for BPMN and DMN editing
- Optional: MongoDB Compass for inspection of sample documents

## Planned runtime

The final local environment will consist of:

- Camunda 7 engine and webapps
- MongoDB 7
- a small Java worker application that fetches Object A and Object B from MongoDB

## Startup

```bash
docker compose up -d
```

Expected result:
- Camunda 7 is reachable on the local browser endpoint configured in Docker Compose.
- MongoDB is running with a local persistent volume.
- The worker is running and ready to participate in external task handling or equivalent BPMN integration.

## Validation scenarios

1. Seed MongoDB with two JSON documents that share the same relevant `serviceCharacteristic` entries.
2. Start the BPMN process.
3. Confirm the process retrieves both objects and exposes them as process variables.
4. Confirm the DMN decision evaluates and returns `true` when the configured relevant characteristics match.
5. Change the DMN configuration by adding or removing a characteristic name and redeploy the DMN model.
6. Rerun the process without changing BPMN or worker code; confirm the result changes according to the updated DMN rules.

## Expected outcomes

- The process completes successfully.
- The final process variable `comparisonResult` is a boolean.
- The decision result reflects all configured relevant characteristics, not a hardcoded code path.
- Changing the DMN configuration alone changes the behavior.

## Shutdown

```bash
docker compose down
```

Use `docker volume rm` only if a full reset of the local MongoDB data is required for a clean re-seed.
