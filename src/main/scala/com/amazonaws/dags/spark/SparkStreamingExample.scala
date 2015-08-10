package com.amazonaws.dags.spark

import org.apache.spark.Logging
import org.apache.spark.SparkConf
import org.apache.spark.streaming.Milliseconds
import org.apache.spark.streaming.StreamingContext
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kinesis.KinesisUtils

object SparkStreamingExample extends Logging {
  def main(args: Array[String]) {
    // Syntax: 
    // ./bin/spark-submit --master yarn \
    // --class com.amazonaws.dags.spark.SparkStreamingExample \
    // --driver-memory 4G \
    // SparkExamples-1.0-SNAPSHOT.jar KinesisTest kinesis.us-east-1.amazonaws.com hdfs:///output-`date +%Y-%m-%d-%H-%M-%S`/ >spark.log 2> error.log

    val Array(kinesisStream, endpointUrl, output) = args

    val conf = new SparkConf()
    conf.setAppName("KinesisTest") // This also identifies the DynamoDB table the KCL will use
    conf.set("spark.shuffle.consolidateFiles", "true")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.storage.memoryFraction", "0.5")

    val batchInterval = Milliseconds(20000)
    val sc = new StreamingContext(conf, batchInterval)
    val kinesisCheckpointInterval = batchInterval
    val kinesisClient = new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain())
    kinesisClient.setEndpoint(endpointUrl)
    val numShards = kinesisClient.describeStream(kinesisStream).getStreamDescription().getShards().size()
    val numStreams = numShards
    //val numStreams = 1

    val kinesisStreams = (0 until numStreams).map(i =>
      KinesisUtils.createStream(
        sc,
        kinesisStream,
        endpointUrl,
        kinesisCheckpointInterval,
        InitialPositionInStream.LATEST,
        StorageLevel.MEMORY_AND_DISK_2))

    val unionStreams = sc.union(kinesisStreams)
    val accessLogRegex = """^(\S+) (\S+) (\S+) \[([\w:/]+\s[+\-]\d{4})\] "(.+?)" (\S+) (\S+) "([^"]*)" "([^"]*)"""".r
    val wordCounts = unionStreams map { byteArray =>
      val accessLogRegex(ip, user, unknown1, date, request, rc, cl, unknown2, ua) = new String(byteArray)
      (rc, 1)
    } reduceByKey (_ + _)

    wordCounts.print()
    wordCounts.saveAsTextFiles(output, kinesisStream)
    sc.start()
    sc.awaitTermination()
  }
}
