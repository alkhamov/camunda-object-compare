package com.example.objectcompare.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalTaskConfig {

    @Bean
    public ExternalTaskClient externalTaskClient(@Value("${camunda.client.base-url}") String baseUrl,
                                                @Value("${camunda.client.worker-id}") String workerId) {
        return ExternalTaskClient.create()
                .baseUrl(baseUrl)
                .workerId(workerId)
                .build();
    }
}
