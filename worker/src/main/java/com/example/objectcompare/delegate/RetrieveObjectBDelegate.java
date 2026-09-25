package com.example.objectcompare.delegate;

import com.example.objectcompare.service.ObjectRetrievalService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("retrieveObjectBDelegate")
public class RetrieveObjectBDelegate implements JavaDelegate {

    private final ObjectRetrievalService objectRetrievalService;

    public RetrieveObjectBDelegate(ObjectRetrievalService objectRetrievalService) {
        this.objectRetrievalService = objectRetrievalService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String objectId = (String) execution.getVariable("objectBId");
        if (objectId == null || objectId.isBlank()) {
            objectId = "64d1f3d9d9b0ea002f000002";
        }

        Map<String, Object> objectB = objectRetrievalService.fetchObject("objects", objectId);
        execution.setVariable("objectB", objectB);
    }
}
