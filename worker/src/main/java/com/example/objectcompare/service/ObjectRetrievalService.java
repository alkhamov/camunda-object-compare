package com.example.objectcompare.service;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@Service
public class ObjectRetrievalService {

    private final MongoDatabase mongoDatabase;

    public ObjectRetrievalService(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public Map<String, Object> fetchObject(String collectionName, String objectId) {
        Document document = mongoDatabase.getCollection(collectionName)
                .find(eq("_id", new ObjectId(objectId)))
                .first();

        if (document == null) {
            throw new IllegalArgumentException("No document found in collection '" + collectionName + "' for id '" + objectId + "'.");
        }

        return document;
    }
}
