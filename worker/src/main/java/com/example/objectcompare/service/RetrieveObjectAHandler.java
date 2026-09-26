package com.example.objectcompare.service;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ExternalTaskHandler for topic "retrieve-object-a".
 */
@ExternalTaskSubscription(topicName = "retrieve-object-a")
@Component
public class RetrieveObjectAHandler implements ExternalTaskHandler {

    private final ObjectRetrievalService retrievalService;

    public RetrieveObjectAHandler(ObjectRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String objectId = (String) externalTask.getVariable("objectAId");
        if (objectId == null || objectId.isBlank()) {
            // preserve previous behavior: fallback test id
            objectId = "64d1f3d9d9b0ea002f000001";
        }

        // Retrieve the full document without any filtering or mapping
        var document = retrievalService.fetchObject("objects", objectId);

        // Complete the external task and set the process variable objectA
        externalTaskService.complete(externalTask, Map.of("objectA", document));
    }
}
