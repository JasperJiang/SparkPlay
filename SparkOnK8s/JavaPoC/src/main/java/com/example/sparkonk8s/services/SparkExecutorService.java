package com.example.sparkonk8s.services;

import com.example.sparkonk8s.config.AppProps;
import com.example.sparkonk8s.core.SparkJobWatch;
import com.example.sparkonk8s.utils.KubeCtlUtil;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class SparkExecutorService {

    @Autowired
    KubeCtlUtil kubeCtlUtil;

    @Autowired
    AppProps appProps;

    @Autowired
    SparkJobWatch sparkJobWatch;

    public void run(String filePath){

        String execId = UUID.randomUUID().toString();
        log.info("execId = {}", execId);
        kubeCtlUtil.createSecretFromFiles(Collections.singleton(filePath), execId, appProps.getSparkDefaultWorkLoadNamespace());
        SparkLauncher sparkLauncher = getSparkLauncher(Paths.get(filePath), execId);
        try {
            SparkAppHandle appHandle = sparkLauncher.startApplication();
//            for (int i = 0; i < 50; i++) {
//                if (kubeCtlUtil.getPod("spark-driver-"+execId, appProps.getSparkDefaultWorkLoadNamespace()) != null);
//
//            }
//            String appId = appHandle.getAppId();
//            log.info("appId: {}", appId);
            sparkJobWatch.startWatch("spark-driver-"+execId, appProps.getSparkDefaultWorkLoadNamespace());
        } catch (Exception e) {
            log.error("Error occurred while launching the spark application", e);
        }
    }

    private SparkLauncher getSparkLauncher(Path sparkResPath, String execId) {
        KubernetesClient kubernetesClient = new DefaultKubernetesClient();
        String kubeUrl = kubernetesClient.getMasterUrl().toString();
        kubernetesClient.close();

        SparkLauncher sparkLauncher = new SparkLauncher(setEnvVariables());
        sparkLauncher
                .setDeployMode("cluster")
//                .setMaster("k8s://http://10.0.0.27:30000")
                .setMaster("k8s://" + kubeUrl)
//                .setMaster("k8s://https://insights-aks-dev-ed747aec.a618ff7b-a54d-423d-b3c2-1ccf85523a12.privatelink.eastus.azmk8s.io:443")
                .setAppName("MyTestApp")
//                .setSparkHome("/Users/jjiang153/Documents/Softwares/Spark/spark-2.4.5-bin-without-hadoop")
                .setSparkHome("/Users/jjiang153/Documents/Softwares/Spark/spark-2.4.5-bin-hadoop2.7")
                .setAppResource("/tmp/resources/" + FilenameUtils.getName(sparkResPath.toString()))
                .setVerbose(true)
                .setMainClass("org.apache.spark.examples.SparkPi")
                .setConf("spark.executor.instances", "1")
                .setConf("spark.kubernetes.driver.pod.name", "spark-driver-"+execId)
                .setConf("spark.kubernetes.driver.label.pod-type", "spark-driver")
                .setConf("spark.kubernetes.executor.label.pod-type", "spark-executor")
                .setConf("spark.kubernetes.driver.label.exec-id", execId)
                .setConf("spark.kubernetes.executor.label.exec-id", execId)
                .setConf("spark.kubernetes.authenticate.driver.serviceAccountName", "spark-default-workload-service-account")
                .setConf("spark.kubernetes.container.image", "jasperjiang/spark-py:v3.0.0-v5")
                .setConf("spark.kubernetes.namespace", appProps.getSparkDefaultWorkLoadNamespace())
                .setConf("spark.kubernetes.pyspark.pythonVersion", "3")
                .setConf("spark.kubernetes.driverEnv.SPARK_PRINT_LAUNCH_COMMAND", "1")
                .setConf("spark.kubernetes.container.image.pullPolicy", "Always")
                .setConf("spark.kubernetes.driver.secrets.resource-"+execId, "/tmp/resources");
        return sparkLauncher;
    }

    private SparkAppHandle.Listener[] addSparkMonitorAsListener() {
        List<SparkAppHandle.Listener> appListeners = new ArrayList<>();
//        Collections.addAll(appListeners, listeners);
        appListeners.add(new SparkAppHandle.Listener() {

            @Override
            public void stateChanged(SparkAppHandle handle) {
//                SparkEvent event = null;

                String appId = handle.getAppId();
                //state can be changed even during processing the switch
                SparkAppHandle.State state = handle.getState();

                log.info("Spark State Changed for App {}.Changed State to {} ", appId, state);

                switch (state) {

                    case FINISHED:
//                        if (isLocalMode) {
//                            //The reported state is always FINISHED in local mode before real FAILED state appears from the process.
//                            //It is due the fact that Spark is always sending FINISHED when it is stopping, but child process is not finished yet.
//                            //Spark is executed as that child process.
//                            //FAILED state is fired later with ChildProcAppHandle::monitorChild, but the handle is already disconnected.
//                            int exitCode = getLocalSparkChildProcessRealExitValue(handle);
//                            log.info("Got process exit code for App {} as {} ", appId, exitCode);
//                            if (exitCode == 0) {
//                                event = new JobCompleteEvent(appId, jobArgs);
//                            }
//                        } else {
//
//                            String yarnState = getState(appId);
//                            //The reported state is sometimes finished. Query the yarn to verify it is same
//                            log.info("Got yarn state for App {} as {} ", appId, yarnState);
//                            if ("FAILED".equalsIgnoreCase(yarnState)) {
//                                event = new JobFailureEvent(appId, jobArgs);
//                            } else if ("SUCCEEDED".equalsIgnoreCase(yarnState)) {
//                                event = new JobCompleteEvent(appId, jobArgs);
//                            }
//                        }
                        log.info("FINISHED");
                        break;
                    case FAILED:
                        log.info("FAILED");
//                        event = new JobFailureEvent(appId, jobArgs);
                        break;
                    case RUNNING:
                        log.info("RUNNING");
//                        event = new JobRunningEvent(appId, jobArgs);
                        break;

                    case KILLED:
                        log.info("KILLED");
//                        event = new JobKillEvent(appId, false, jobArgs);
                        //Killing a spark job can be either via timeout or manual.*/
                        // The events gets generated @ those place
                }

//                if (JOB_DONE_STATES.contains(state) && appId != null) {
//                    sparkMonitor.removeFromQueue(appId);
//                    handle.disconnect();
//                }
            }

            @Override
            public void infoChanged(SparkAppHandle handle) {
                //Do nothing...
            }
        });

        return appListeners.toArray(new SparkAppHandle.Listener[0]);
    }

    private HashMap<String, String> setEnvVariables() {
        HashMap<String, String> map = new HashMap<>();
        map.put("SPARK_PRINT_LAUNCH_COMMAND", "1");
//        map.put("SPARK_DIST_CLASSPATH", "/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/etc/hadoop:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/common/lib/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/common/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/hdfs:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/hdfs/lib/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/hdfs/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/mapreduce/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/yarn:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/yarn/lib/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/yarn/*");
//        map.put("SPARK_DIST_CLASSPATH", "/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/etc/hadoop:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/common/lib/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/common/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/hdfs:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/hdfs/lib/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/hdfs/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/mapreduce/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/yarn:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/yarn/lib/*:/Users/jjiang153/Documents/Softwares/Spark/hadoop-3.0.0/share/hadoop/yarn/*");
        return map;
    }
}
