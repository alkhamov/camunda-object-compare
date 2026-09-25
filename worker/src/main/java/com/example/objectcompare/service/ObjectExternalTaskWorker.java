package com.example.objectcompare.service;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@Component
public class ObjectExternalTaskWorker {

    private final MongoDatabase mongoDatabase;

    public ObjectExternalTaskWorker(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @ExternalTaskSubscription(topicName = "retrieve-object-a")
    public void retrieveObjectA(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String objectId = (String) externalTask.getVariable("objectAId");
        if (objectId == null || objectId.isBlank()) {
            objectId = "64d1f3d9d9b0ea002f000001";
        }

        Map<String, Object> objectA = fetchObject("objects", objectId);
        externalTaskService.complete(externalTask, Map.of("objectA", objectA));
    }

    @ExternalTaskSubscription(topicName = "retrieve-object-b")
    public void retrieveObjectB(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String objectId = (String) externalTask.getVariable("objectBId");
        if (objectId == null || objectId.isBlank()) {
            objectId = "64d1f3d9d9b0ea002f000002";
        }

        Map<String, Object> objectB = fetchObject("objects", objectId);
        externalTaskService.complete(externalTask, Map.of("objectB", objectB));
    }

    private Map<String, Object> fetchObject(String collectionName, String objectId) {
        Document document = mongoDatabase.getCollection(collectionName)
                .find(eq("_id", new ObjectId(objectId)))
                .first();

        if (document == null) {
            throw new IllegalArgumentException("No document found in collection '" + collectionName + "' for id '" + objectId + "'.");
        }

        return document;
    }
}
