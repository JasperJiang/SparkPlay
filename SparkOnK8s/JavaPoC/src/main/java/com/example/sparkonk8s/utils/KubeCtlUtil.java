package com.example.sparkonk8s.utils;

import com.example.sparkonk8s.config.AppProps;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class KubeCtlUtil {

    @Autowired
    AppProps appProps;

    public Pod getPod(String podName, String namespace){
        try(KubernetesClient client = new DefaultKubernetesClient()) {
            return client.pods().inNamespace(namespace).withName(podName).get();
        }catch (Exception e){
            log.error("", e);
            throw new RuntimeException("Error create secret");
        }
    }

    public void createSecretFromFiles(Set<String> filesPath, String execId, String namespace){
        filesPath.forEach(filePathStr -> {
            if (!new File(filePathStr).exists()){
                throw new RuntimeException("file " + filePathStr + " not found");
            }
        });
        Map<String, String> data = new HashMap<>();
        for (String filePathStr : filesPath) {
            File file = new File(filePathStr);
            if (file.exists()){
                String fileName = FilenameUtils.getName(filePathStr);
                try {
                    data.put(fileName, Base64.encode(IOUtils.toByteArray(new FileInputStream(file))));
                } catch (IOException e) {
                    log.error("", e);
                    throw new RuntimeException("IO issue");
                }
            }else {
                throw new RuntimeException("file " + filePathStr + " not found");
            }
        }

        if (!data.isEmpty()) {
            Map<String, String> labels = new HashMap<>();
            labels.put("exec-id", execId);

            try(KubernetesClient client = new DefaultKubernetesClient()) {
                Secret secret = new SecretBuilder()
                        .withNewMetadata()
                        .withName("resource-" + execId)
                        .withLabels(labels)
                        .endMetadata()
                        .withData(data)
                        .build();

                client.secrets()
                        .inNamespace(namespace)
                        .createOrReplace(secret);
            }catch (Exception e){
                log.error("", e);
                throw new RuntimeException("Error create secret");
            }

        }
    }
}
