import pyspark
conf = pyspark.SparkConf()
conf.setMaster("k8s://https://192.168.64.3:8443")
conf.set("spark.kubernetes.authenticate.driver.serviceAccountName", "spark") 
conf.set("spark.executor.instances", "2") 
conf.set("spark.kubernetes.container.image","jasperjiang/spark-py:v2.4.5-v1")
conf.set("spark.kubernetes.executor.request.cores","0.1")
conf.set("spark.kubernetes.executor.limit.cores", "0.1")
sc = pyspark.SparkContext(conf=conf)