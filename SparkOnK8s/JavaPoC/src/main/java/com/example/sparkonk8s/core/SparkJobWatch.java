package com.example.sparkonk8s.core;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class SparkJobWatch{

    private boolean hasCompleted(Pod pod){
        String phase = pod.getStatus().getPhase();
        return phase.equalsIgnoreCase("Succeeded") || phase.equalsIgnoreCase("Failed");
    }

    @Async
    public void startWatch(String podName, String nameSpace)  {
        final CountDownLatch closeLatch = new CountDownLatch(1);
            try (final KubernetesClient client = new DefaultKubernetesClient();
                 Watch watch = client.pods().inNamespace(nameSpace).withName(podName).watch(new SparkPodWatcher(closeLatch))) {
                closeLatch.await();
            } catch (KubernetesClientException e) {
                log.error("Could not watch resources ", e);
            } catch (InterruptedException e){
                log.error("thread interrupted ", e);
                Thread.currentThread().interrupt();
            }
    }

    class SparkPodWatcher implements Watcher<Pod>{
        CountDownLatch closeLatch;

        public SparkPodWatcher(CountDownLatch closeLatch) {
            this.closeLatch = closeLatch;
        }
        @Override
        public void eventReceived(Action action, Pod resource) {
            switch (action.name().toUpperCase()){
                case "DELETED":
                case "ERROR":
                    closeLatch.countDown();
                    break;
                default:
                    log.info("========== watcher podName:{}, action:{}, phase:{}", resource.getMetadata().getName(), action, resource.getStatus().getPhase());
                    if (hasCompleted(resource)){
                        closeLatch.countDown();
                    }
                    break;
            }
        }

        @Override
        public void onClose(KubernetesClientException e) {
            log.info("Watcher onClose");
            if (e != null) {
                log.error(e.getMessage(), e);
                closeLatch.countDown();
            }
        }
    }
}
