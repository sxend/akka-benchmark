import sbt._

object Dependencies {
  lazy val AkkaVersion = "2.6.9"
  lazy val AkkaHttpVersion = "10.2.0"
  lazy val scalaTest = Seq("org.scalatest" %% "scalatest" % "3.2.0" % Test)
  lazy val loggers = Seq(
    "org.slf4j" % "slf4j-api" % "1.7.30",
      "org.apache.logging.log4j" % "log4j-api" % "2.13.3",
      "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.3",
      "com.conversantmedia" % "disruptor" % "1.2.17",
  )
  lazy val akka = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
    "io.netty" % "netty" % "3.10.6.Final"
  )
  lazy val utils = Seq(
    "org.apache.commons" % "commons-lang3" % "3.11"
  )
}
