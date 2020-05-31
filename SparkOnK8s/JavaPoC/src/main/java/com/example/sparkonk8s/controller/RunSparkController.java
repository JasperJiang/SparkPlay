package com.example.sparkonk8s.controller;

import com.example.sparkonk8s.config.AppProps;
import com.example.sparkonk8s.services.SparkExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RunSparkController {

    @Autowired
    AppProps appProps;

    @Autowired
    SparkExecutorService sparkExecutorService;

    @PostMapping(value = "/run")
    public void run(@RequestParam("filePath") String filePath){
        sparkExecutorService.run(filePath);
    }
}
