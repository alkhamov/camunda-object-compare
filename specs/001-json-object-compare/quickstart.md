# Quickstart Guide

This is the shortest reproducible procedure to run the current V2 PoC and verify the V2 comparison result.

## 1. Prerequisites

- Docker Desktop installed and running
- Docker Compose available
- `curl` available

## 2. Start the runtime

```bash
docker compose up --build -d
```

## 3. Verify containers

```bash
docker compose ps
```

Expected: `object-compare-camunda`, `object-compare-mongo`, and `object-compare-worker` are running.

## 4. Verify MongoDB seed data

```bash
docker exec object-compare-mongo mongosh --quiet \
  --eval 'db.getSiblingDB("object_compare").objects.find({}, {_id:1, documentType:1, serviceCharacteristic:1}).pretty()'
```

Confirm these IDs exist:

- Object A: `64d1f3d9d9b0ea002f000001`
- Object B mismatch: `64d1f3d9d9b0ea002f000003`

The seeded mismatch is on `characteristicB`.

## 5. Deploy V2 DMN (manual)

```bash
curl -X POST \
  -F "deployment-name=object-comparison-v2-dmn" \
  -F "enable-duplicate-filtering=false" \
  -F "data=@camunda/v2/object-comparison-v2.dmn" \
  http://localhost:8080/engine-rest/deployment/create
```

## 6. Deploy V2 BPMN (manual)

```bash
curl -X POST \
  -F "deployment-name=object-comparison-v2-bpmn" \
  -F "enable-duplicate-filtering=false" \
  -F "data=@camunda/v2/object-comparison-v2.bpmn" \
  http://localhost:8080/engine-rest/deployment/create
```

Camunda uses in-memory engine persistence in this PoC. If the Camunda container is recreated, redeploy BPMN/DMN.

## 7. Start a V2 comparison process

```bash
curl -X POST \
  http://localhost:8080/engine-rest/process-definition/key/object-comparison-v2/start \
  -H "Content-Type: application/json" \
  -d '{
    "variables": {
      "objectAId": {
        "value": "64d1f3d9d9b0ea002f000001",
        "type": "String"
      },
      "objectBId": {
        "value": "64d1f3d9d9b0ea002f000003",
        "type": "String"
      }
    }
  }'
```

The response contains the process instance `"id"`. Save it as `<PROCESS_INSTANCE_ID>`.

## 8. Verify process completion

```bash
curl -s \
  "http://localhost:8080/engine-rest/history/process-instance/<PROCESS_INSTANCE_ID>"
```

Expected relevant result:

- `"state":"COMPLETED"`

## 9. Verify comparisonResult

```bash
curl -s -G \
  "http://localhost:8080/engine-rest/history/variable-instance" \
  --data-urlencode "processInstanceId=<PROCESS_INSTANCE_ID>" \
  --data-urlencode "variableName=comparisonResult"
```

Expected relevant result:

- `"type":"Boolean"`
- `"value":true`

Why `true`: `characteristicB` is deliberately different, but current V2 Comparison Configuration is `["characteristicA", "characteristicC"]`, so only those two configured characteristics are compared and both match.

## 10. Stop/reset the environment

Stop runtime:

```bash
docker compose down
```

Optional full Mongo reset (destroys persisted local Mongo data):

```bash
docker volume rm camunda-object-compare_mongo-data
```

After volume removal, the next runtime start initializes fresh seed data from `mongo/init/seed-data.js`.
