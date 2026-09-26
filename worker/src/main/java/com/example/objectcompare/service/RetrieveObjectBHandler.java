package com.example.objectcompare.service;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ExternalTaskHandler for topic "retrieve-object-b".
 */
@ExternalTaskSubscription(topicName = "retrieve-object-b")
@Component
public class RetrieveObjectBHandler implements ExternalTaskHandler {

    private final ObjectRetrievalService retrievalService;

    public RetrieveObjectBHandler(ObjectRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String objectId = (String) externalTask.getVariable("objectBId");
        if (objectId == null || objectId.isBlank()) {
            // preserve previous behavior: fallback test id
            objectId = "64d1f3d9d9b0ea002f000002";
        }

        // Retrieve the full document without any filtering or mapping
        var document = retrievalService.fetchObject("objects", objectId);

        // Complete the external task and set the process variable objectB
        externalTaskService.complete(externalTask, Map.of("objectB", document));
    }
}
