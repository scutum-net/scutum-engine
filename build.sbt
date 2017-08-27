name := "scutum-engine"
version := "1.0"
scalaVersion := "2.12.1"

libraryDependencies += "com.google.code.gson" % "gson" % "2.8.1"
libraryDependencies += "com.github.gphat" %% "wabisabi" % "2.2.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "net.codingwell" % "scala-guice_2.12" % "4.1.0"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.10.2.0"
libraryDependencies += "com.typesafe.akka" % "akka-http_2.12" % "10.0.8"
libraryDependencies += "com.typesafe.akka" % "akka-stream_2.12" % "2.5.3"
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.3" % "test"
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.5.0"

assemblyMergeStrategy in assembly := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}