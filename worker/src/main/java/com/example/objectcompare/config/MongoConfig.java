package com.example.objectcompare.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient(@Value("${app.mongo.uri:mongodb://localhost:27017}") String mongoUri) {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient,
                                      @Value("${app.mongo.database:object_compare}") String databaseName) {
        return mongoClient.getDatabase(databaseName);
    }
}
