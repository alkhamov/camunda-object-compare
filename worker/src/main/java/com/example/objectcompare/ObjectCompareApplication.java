package com.example.objectcompare;

import org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableExternalTaskClient(
        baseUrl = "${APP_CAMUNDA_BASE_URL:http://localhost:8080/engine-rest}",
        workerId = "${APP_CAMUNDA_WORKER_ID:object-compare-worker}",
        lockDuration = 10000,
        maxTasks = 1,
        asyncResponseTimeout = 10000
)
public class ObjectCompareApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObjectCompareApplication.class, args);
    }
}
