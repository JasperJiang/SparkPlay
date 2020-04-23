# Spark on Kubernetes

## Get Start

document - <https://spark.apache.org/docs/latest/running-on-kubernetes.html>

## Build spark docker image

bin/docker-image-tool.sh -r docker.io/jasperjiang -t v2.4.5-v1 build

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

import pyspark
conf = pyspark.SparkConf()
conf.setMaster("k8s://https://192.168.64.3:8443") 
conf.set("spark.kubernetes.authenticate.driver.serviceAccountName", "spark") 
conf.set("spark.executor.instances", "2") 
conf.set("spark.kubernetes.container.image","jasperjiang/spark-py:v2.4.5-v1")
sc = pyspark.SparkContext(conf=conf)