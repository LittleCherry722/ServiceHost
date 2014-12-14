name := "Sbpm Service Host Update"

version := "1.0"

scalaVersion := "2.11.1"


resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.4",
    "com.typesafe.akka" %% "akka-remote" % "2.3.4",
    "com.typesafe.akka" %% "akka-slf4j" % "2.3.4",
    "org.scalaj" %% "scalaj-http" % "0.3.15"
)
