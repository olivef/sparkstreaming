# sparkstreaming
mvn package
spark-submit --master yarn --class com.amazonaws.dags.spark.SparkStreamingExample --driver-memory 1G --num-executors 4 target/SparkExamples-1.0-SNAPSHOT-jar-with-dependencies.jar  AccessLogStream  kinesis.eu-west-1.amazonaws.com s3://olivefemr/output-`date +%Y-%m-%d-%H-%M-%S`/
