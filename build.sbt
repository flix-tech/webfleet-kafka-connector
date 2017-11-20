name := "webfleedKafkaConnector"

version := "0.0.1"

scalaVersion := "2.12.4"

organization := "com.flixtech"

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

resolvers += "confluent" at "http://packages.confluent.io/maven/"

fork in Test := true

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.1",
  "co.wrisk.jcredstash" % "jcredstash" % "0.0.4",
  "joda-time" % "joda-time" % "2.9.9",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.5",
  "org.apache.kafka" % "connect-api" % "0.11.0.0" % "provided",
  "io.confluent" % "kafka-connect-avro-converter" % "3.2.1"  % "provided",
  "com.amazonaws" % "aws-java-sdk-cloudwatch" % "1.11.86",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "io.dropwizard.metrics" % "metrics-core" % "3.2.5",
  "org.mockito" % "mockito-core" % "2.9.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

assemblyMergeStrategy in assembly := {
  case PathList("org", "slf4j", "impl", "StaticLoggerBinder.class")  => MergeStrategy.discard
  case PathList("org", "slf4j", "impl", "StaticMDCBinder.class")  => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

publishTo := {
  val artifactory = "https://flixbus.jfrog.io/flixbus/"
  if (isSnapshot.value)
    Some("Artifactory Realm" at artifactory + "libs-snapshot-local")
  else
    Some("Artifactory Realm" at artifactory + "libs-release-local")
}

//credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
credentials += Credentials(new File(".credentials"))
