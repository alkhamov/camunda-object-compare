package com.example.objectcompare;

import org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableExternalTaskClient
public class ObjectCompareApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObjectCompareApplication.class, args);
    }
}
