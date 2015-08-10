name := "SparkStreamingExample" 

version := "1.0" 

scalaVersion := "2.10.4" 

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.1.0" 

resolvers += "Scala-tools Maven2 Repository" at "http://scala-tools.org/repo-releases"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.1.0"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.4.1"

libraryDependencies += "com.amazonaws" % "amazon-kinesis-client" % "1.6.0"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.10.10"

libraryDependencies += "org.apache.spark" % "spark-streaming-kinesis-asl_2.10" % "1.4.1"


