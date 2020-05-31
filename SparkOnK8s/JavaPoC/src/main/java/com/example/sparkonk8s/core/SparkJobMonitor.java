package com.example.sparkonk8s.core;

import com.example.sparkonk8s.config.AppProps;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SparkJobMonitor {

    private KubernetesClient kubernetesClient;

    private static final Integer INITIAL_DELAY = 0;

    private static final Integer DEFAULT_PERIOD_IN_MINUTES = 0;

    private static final String NAMESPACE_LABEL = "namespace-type=spark-workload";

    private static final String SPARK_DRIVER_LABEL = "pod-type=spark-driver";

    private static final String SPARK_EXECUTOR_LABEL = "pod-type=spark-executor";

    @Autowired
    public SparkJobMonitor(AppProps appProps) {

        kubernetesClient = new DefaultKubernetesClient();

        final Integer period = 2;

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(this::processJob, INITIAL_DELAY,
                (period == null) ? DEFAULT_PERIOD_IN_MINUTES : period,
                TimeUnit.MINUTES);
    }

    private void processJob(){
        log.info("Started kube Pod monitoring thread.......");
        PodList podList = kubernetesClient.pods().inAnyNamespace().withLabel(SPARK_DRIVER_LABEL).list();
        podList.getItems().stream().filter(pod -> {
            final String podStatus = pod.getStatus().getPhase();
            return "succeeded".equalsIgnoreCase(podStatus) || "failed".equalsIgnoreCase(podStatus);
        }).forEach(pod -> {
            final String execId = pod.getMetadata().getLabels().get("exec-id");
            final String nameSpace = pod.getMetadata().getNamespace();
            log.info("Deleting the pod - {}", pod.getMetadata().getName());
            kubernetesClient.pods().delete(pod);
            kubernetesClient.secrets().inNamespace(nameSpace).withLabel("exec-id="+execId).delete();
        });
    }
}

