package com.example.objectcompare.delegate;

import com.example.objectcompare.service.ObjectRetrievalService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("retrieveObjectADelegate")
public class RetrieveObjectADelegate implements JavaDelegate {

    private final ObjectRetrievalService objectRetrievalService;

    public RetrieveObjectADelegate(ObjectRetrievalService objectRetrievalService) {
        this.objectRetrievalService = objectRetrievalService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String objectId = (String) execution.getVariable("objectAId");
        if (objectId == null || objectId.isBlank()) {
            objectId = "64d1f3d9d9b0ea002f000001";
        }

        Map<String, Object> objectA = objectRetrievalService.fetchObject("objects", objectId);
        execution.setVariable("objectA", objectA);
    }
}
