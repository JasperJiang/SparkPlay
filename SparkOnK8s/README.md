# Spark on Kubernetes

## Get Start

document - <https://spark.apache.org/docs/latest/running-on-kubernetes.html>

## Build spark docker image

bin/docker-image-tool.sh -r docker.io/jasperjiang -t my-spark-docker build

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
    --conf spark.authenticate=true \
    --conf spark.authenticate.secret=1234 \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.container.image=jasperjiang/spark-py:my-spark-docker \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
    local:///opt/spark/examples/src/main/python/pi.py
