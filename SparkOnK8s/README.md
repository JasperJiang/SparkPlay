# Spark on Kubernetes

## Get Start

document - <https://spark.apache.org/docs/latest/running-on-kubernetes.html>

## Build spark docker image

bin/docker-image-tool.sh -r docker.io/jasperjiang -t v2.4.5-v1 build

bin/docker-image-tool.sh -r docker.io/jasperjiang -p /Users/jjiang153/Documents/Softwares/spark-3.0.0-preview2-bin-hadoop3.2/kubernetes/dockerfiles/spark/bindings/python/Dockerfile -t v3.0.0-v1 build

## Create Service Account

kubectl create serviceaccount spark

## bind role

kubectl create clusterrolebinding spark-role --clusterrole=edit --serviceaccount=default:spark --namespace=default

## submit spark

bin/spark-submit \
    --master k8s://https://192.168.64.3:8443 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.authenticate=false \
    --conf spark.authenticate.secret=1234 \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:v2.4.5-v1 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
    --conf spark.kubernetes.driver.volumes.hostPath.testscript.mount.path=/testscript \
    --conf spark.kubernetes.driver.volumes.hostPath.testscript.mount.readOnly=true \
    --conf spark.kubernetes.driver.volumes.hostPath.testscript.options.path=/Spark \
    --conf spark.kubernetes.driver.pod.name=testpod-driver \
    --conf spark.kubernetes.memoryOverheadFactor=0.1 \
    local:///opt/spark/examples/pi.py

## Spark Application Management

Spark 3.0 only

```bash
spark-submit --kill spark:spark-pi-1547948636094-driver --master k8s://https://192.168.64.3:8443
```

```bash
spark-submit --status spark:spark-pi-1547948636094-driver --master k8s://https://192.168.64.3:8443
```

## Jupyter

```python
import pyspark
conf = pyspark.SparkConf()
conf.setMaster("k8s://https://192.168.64.3:8443") \
conf.set("spark.kubernetes.authenticate.driver.serviceAccountName", "spark") 
conf.set("spark.executor.instances", "2") 
conf.set("spark.kubernetes.container.image","jasperjiang/spark-py:v2.4.5-v1")
conf.set("spark.kubernetes.executor.request.cores","0.1")
onf.set("spark.kubernetes.executor.limit.cores", "0.1")
sc = pyspark.SparkContext(conf=conf)
```

wget https://archive.apache.org/dist/spark/spark-2.4.5/spark-2.4.5-bin-without-hadoop.tgz
wget https://archive.apache.org/dist/spark/spark-3.0.0-preview2/spark-3.0.0-preview2-bin-without-hadoop.tgz
mkdir /usr/local/spark-2.4.5
mkdir /usr/local/spark-3.0.0
tar -xvf spark-2.4.5-bin-without-hadoop.tgz -C /usr/local/spark-2.4.5 --strip 1

tar -xvf spark-3.0.0-preview2-bin-without-hadoop.tgz -C /usr/local/spark-3.0.0 --strip 1

mkdir /opt/conda/share/jupyter/kernels/pySparkOnK8s
vi /opt/conda/share/jupyter/kernels/pySparkOnK8s/kernel.json

mkdir /opt/conda/share/jupyter/kernels/pySparkOnK8sV3
vi /opt/conda/share/jupyter/kernels/pySparkOnK8sV3/kernel.json

pip install py4j

{
 "display_name": "SparkOnK8sV3",
 "language": "python",
 "argv": [
  "/opt/conda/bin/python",
  "-m",
  "ipykernel",
  "-f",
  "{connection_file}"
 ],
"env": {
 "PYSPARK_PYTHON": "python3",
 "PYSPARK_DRIVER_PYTHON": "/opt/conda/bin/python",
 "JAVA_HOME": "/usr/lib/jvm/java-8-openjdk-amd64",
 "SPARK_HOME": "/usr/local/spark-3.0.0",
 "HADOOP_USER_NAME" : "hdfs",
 "HADOOP_CONF_DIR": "/etc/hadoop/conf",
 "PYTHONPATH": "/usr/local/spark-3.0.0/python/lib/py4j-0.10.8.1-src.zip:/usr/local/spark-3.0.0/python/",
 "PYTHONSTARTUP": "/usr/local/spark-3.0.0/python/pyspark/shell.py",
 "PYSPARK_SUBMIT_ARGS": "pyspark-shell",
 "SPARK_DIST_CLASSPATH": "/usr/local/hadoop/etc/hadoop:/usr/local/hadoop/share/hadoop/common/lib/*:/usr/local/hadoop/share/hadoop/common/*:/usr/local/hadoop/share/hadoop/hdfs:/usr/local/hadoop/share/hadoop/hdfs/lib/*:/usr/local/hadoop/share/hadoop/hdfs/*:/usr/local/hadoop/share/hadoop/mapreduce/*:/usr/local/hadoop/share/hadoop/yarn:/usr/local/hadoop/share/hadoop/yarn/lib/*:/usr/local/hadoop/share/hadoop/yarn/*"
 }
}


{
 "display_name": "SparkOnK8s",
 "language": "python",
 "argv": [
  "/opt/conda/bin/python",
  "-m",
  "ipykernel",
  "-f",
  "{connection_file}"
 ],
"env": {
 "PYSPARK_PYTHON": "python3",
 "PYSPARK_DRIVER_PYTHON": "/opt/conda/bin/python",
 "JAVA_HOME": "/usr/lib/jvm/java-8-openjdk-amd64",
 "SPARK_HOME": "/usr/local/spark-2.4.5",
 "HADOOP_USER_NAME" : "hdfs",
 "HADOOP_CONF_DIR": "/etc/hadoop/conf",
 "PYTHONPATH": "/usr/local/spark/python/lib/py4j-0.10.7-src.zip:/usr/local/spark/python/",
 "PYTHONSTARTUP": "/usr/local/spark-2.4.5/python/pyspark/shell.py",
 "PYSPARK_SUBMIT_ARGS": "pyspark-shell",
 "SPARK_DIST_CLASSPATH": "/usr/local/hadoop/etc/hadoop:/usr/local/hadoop/share/hadoop/common/lib/*:/usr/local/hadoop/share/hadoop/common/*:/usr/local/hadoop/share/hadoop/hdfs:/usr/local/hadoop/share/hadoop/hdfs/lib/*:/usr/local/hadoop/share/hadoop/hdfs/*:/usr/local/hadoop/share/hadoop/mapreduce/*:/usr/local/hadoop/share/hadoop/yarn:/usr/local/hadoop/share/hadoop/yarn/lib/*:/usr/local/hadoop/share/hadoop/yarn/*"
 }
}

import pyspark
conf = pyspark.SparkConf()
conf.setMaster("k8s://http://10.0.0.27:30000/")
conf.set("spark.kubernetes.namespace", "spark")
conf.set("spark.executor.instances", "1") 
conf.set("spark.kubernetes.container.image","jasperjiang/spark-py:v2.4.5-v1")
conf.set("spark.driver.host","jupyter-jasperdotjiang111521362418.jhub")
conf.set("spark.driver.bindAddress", "jupyter-jasperdotjiang111521362418")
conf.set("spark.driver.port", "45523")
conf.set("spark.blockManager.port","41494")
conf.set("spark.authenticate", "true")
conf.set("spark.network.crypto.enabled,"true")
conf.set("spark.io.encryption.enabled", "true")
# conf.set("spark.kubernetes.executor.request.cores","0.1")
# conf.set("spark.kubernetes.executor.limit.cores", "1")
sc = pyspark.SparkContext(conf=conf)


kubectl get secret docker-registry-secret --namespace=default --export -o yaml |\
   kubectl apply --namespace=spark-default-workload -f -

spark-2.4.5-bin-without-hadoop/bin/spark-submit \
    --master k8s://https://insights-aks-dev-ed747aec.a618ff7b-a54d-423d-b3c2-1ccf85523a12.privatelink.eastus.azmk8s.io:443 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:v2.4.5-v4 \
    --conf spark.kubernetes.authenticate.submission.caCertFile=/Users/jjiang153/Documents/Projects/XIP/pySparkOnK8s/apiserver.crt \
    --conf spark.kubernetes.authenticate.submission.clientKeyFile=/Users/jjiang153/Documents/Projects/XIP/pySparkOnK8s/clientKey \
    --conf spark.kubernetes.authenticate.submission.clientCertFile=/Users/jjiang153/Documents/Projects/XIP/pySparkOnK8s/clientCertificate \
    --conf spark.kubernetes.authenticate.submission.oauthToken=xxxxxx \
    local:///opt/spark/examples/src/main/python/pi.py

spark-2.4.5-bin-without-hadoop/bin/spark-submit \
    --master k8s://http://10.0.0.27:30000 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-test \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:v2.4.5-v5 \
    --conf spark.kubernetes.namespace=spark-default-workload \
    --conf spark.kubernetes.pyspark.pythonVersion=3 \
    --conf spark.kubernetes.driverEnv.SPARK_PRINT_LAUNCH_COMMAND=1 \
    --conf spark.kubernetes.container.image.pullPolicy=Always \
    file:///Users/jjiang153/Documents/Softwares/Spark/spark-2.4.5-bin-without-hadoop/src/main/python/pi.py

spark-2.4.5-bin-without-hadoop/bin/spark-submit \
    --master k8s://https://kubernetes.docker.internal:6443 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.kubernetes.file.upload.path=wasbs://wasbjasdev@wasbtestdev.blob.core.windows.net/sparkrun \
    --conf spark.hadoop.fs.azure.sas.wasbjasdev.wasbtestdev.blob.core.windows.net='' \
    --conf spark.hadoop.fs.wasbs.impl=org.apache.hadoop.fs.azure.NativeAzureFileSystem \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-default-workload-service-account \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:v2.4.5-v5 \
    --conf spark.kubernetes.namespace=spark-default-workload \
    --conf spark.kubernetes.pyspark.pythonVersion=3 \
    --conf spark.kubernetes.driverEnv.SPARK_PRINT_LAUNCH_COMMAND=1 \
    --conf spark.kubernetes.container.image.pullPolicy=Always \
    file:///Users/jjiang153/Documents/Softwares/Spark/spark-3.0.0-preview2-bin-hadoop3.2/examples/jars/spark-examples_2.12-3.0.0-preview2.jar

spark-3.0.0-preview2-bin-hadoop3.2/bin/spark-submit \
    --master k8s://https://insights-aks-dev-ed747aec.a618ff7b-a54d-423d-b3c2-1ccf85523a12.privatelink.eastus.azmk8s.io:443 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.kubernetes.file.upload.path=wasbs://wasbjasdev@wasbtestdev.blob.core.windows.net/sparkrun \
    --conf spark.hadoop.fs.azure.sas.wasbjasdev.wasbtestdev.blob.core.windows.net='' \
    --conf spark.hadoop.fs.wasbs.impl=org.apache.hadoop.fs.azure.NativeAzureFileSystem \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-default-workload-service-account \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:v3.0.0-v5 \
    --conf spark.kubernetes.namespace=spark-default-workload \
    --conf spark.kubernetes.pyspark.pythonVersion=3 \
    --conf spark.kubernetes.driverEnv.SPARK_PRINT_LAUNCH_COMMAND=1 \
    --conf spark.kubernetes.container.image.pullPolicy=Always \
    local:///opt/spark/examples/src/main/python/pi.py

spark-3.0.0-preview2-bin-hadoop3.2/bin/spark-submit \
    --master k8s://https://kubernetes.docker.internal:6443 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.kubernetes.file.upload.path=wasbs://wasbjasdev@wasbtestdev.blob.core.windows.net/sparkrun \
    --conf spark.hadoop.fs.azure.sas.wasbjasdev.wasbtestdev.blob.core.windows.net='' \
    --conf spark.hadoop.fs.wasbs.impl=org.apache.hadoop.fs.azure.NativeAzureFileSystem \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-default-workload-service-account \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:v3.0.0-v5 \
    --conf spark.kubernetes.namespace=spark-default-workload \
    --conf spark.kubernetes.pyspark.pythonVersion=3 \
    --conf spark.kubernetes.driverEnv.SPARK_PRINT_LAUNCH_COMMAND=1 \
    --conf spark.kubernetes.container.image.pullPolicy=Always \
    file:///Users/jjiang153/Documents/Softwares/Spark/spark-3.0.0-preview2-bin-hadoop3.2/examples/jars/spark-examples_2.12-3.0.0-preview2.jar

spark-3.0.0-preview2-bin-hadoop3.2/bin/spark-submit \
    --master k8s://https://kubernetes.docker.internal:6443 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.kubernetes.file.upload.path=wasbs://wasbjasdev@wasbtestdev.blob.core.windows.net/sparkrun \
    --conf spark.hadoop.fs.azure.sas.wasbjasdev.wasbtestdev.blob.core.windows.net='' \
    --conf spark.hadoop.fs.wasbs.impl=org.apache.hadoop.fs.azure.NativeAzureFileSystem \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-default-workload-service-account \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:v3.0.0-v5 \
    --conf spark.kubernetes.namespace=spark-default-workload \
    --conf spark.kubernetes.pyspark.pythonVersion=3 \
    --conf spark.kubernetes.driverEnv.SPARK_PRINT_LAUNCH_COMMAND=1 \
    --conf spark.kubernetes.container.image.pullPolicy=Always \
    wasbs://wasbjasdev@wasbtestdev.blob.core.windows.net/sparkrun/spark-upload-dbe3fae2-6cfb-4f0a-93ec-fec50b16669c/pi.py

spark-2.4.5-bin-without-hadoop/bin/spark-submit \
    --master k8s://https://kubernetes.docker.internal:6443 \
    --deploy-mode cluster \
    --name spark-pi \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.hadoop.fs.azure.sas.wasbjasdev.wasbtestdev.blob.core.windows.net='' \
    --conf spark.hadoop.fs.wasbs.impl=org.apache.hadoop.fs.azure.NativeAzureFileSystem \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-default-workload-service-account \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:v2.4.5-v5 \
    --conf spark.kubernetes.namespace=spark-default-workload \
    --conf spark.kubernetes.pyspark.pythonVersion=3 \
    --conf spark.kubernetes.driverEnv.SPARK_PRINT_LAUNCH_COMMAND=1 \
    --conf spark.kubernetes.container.image.pullPolicy=Always \
    --conf spark.eventLog.enabled=true \
    --conf spark.eventLog.dir=wasbs://wasbjasdev@wasbtestdev.blob.core.windows.net/sparklog \
    --conf spark.kerberos.keytab=/etc/security/keytabs/xip_services.keytab \
    --conf spark.kerberos.principal=hdfs/nn.${NAMESPACE}.svc.cluster.local@CLUSTER.LOCAL \
    --conf spark.kubernetes.kerberos.krb5.path=/etc/krb5.conf \
    wasbs://wasbjasdev@wasbtestdev.blob.core.windows.net/sparkrun/spark-upload-dbe3fae2-6cfb-4f0a-93ec-fec50b16669c/pi.py
