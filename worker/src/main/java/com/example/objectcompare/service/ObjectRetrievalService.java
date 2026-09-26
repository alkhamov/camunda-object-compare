package com.example.objectcompare.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@Service
public class ObjectRetrievalService {

    private final MongoDatabase mongoDatabase;
    private final ObjectMapper objectMapper;

    public ObjectRetrievalService(
            MongoDatabase mongoDatabase,
            ObjectMapper objectMapper) {
        this.mongoDatabase = mongoDatabase;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> fetchObject(
            String collectionName,
            String objectId) {

        Document document = mongoDatabase.getCollection(collectionName)
                .find(eq("_id", new ObjectId(objectId)))
                .first();

        if (document == null) {
            throw new IllegalArgumentException(
                    "No document found in collection '" +
                    collectionName +
                    "' for id '" +
                    objectId +
                    "'.");
        }

        return objectMapper.convertValue(
                document,
                new TypeReference<Map<String, Object>>() {}
        );
    }
}