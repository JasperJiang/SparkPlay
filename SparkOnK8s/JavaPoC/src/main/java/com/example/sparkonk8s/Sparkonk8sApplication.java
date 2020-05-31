package com.example.sparkonk8s;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Sparkonk8sApplication {

    public static void main(String[] args) {
        SpringApplication.run(Sparkonk8sApplication.class, args);
    }

}
