name := "Sbpm Service Host"

version := "1.0"

scalaVersion := "2.10.3"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.2.3",
    "com.typesafe.akka" %% "akka-remote" % "2.2.3",
    "org.scalaj" %% "scalaj-http" % "0.3.12"
)

unmanagedSourceDirectories in Compile += file("../eventbus")
