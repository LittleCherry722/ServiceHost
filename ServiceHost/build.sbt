name := "Sbpm Service Host"

version := "1.0"

scalaVersion := "2.10.4"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "spray repo" at "http://repo.spray.io",
    "spray nightly repo" at "http://nightlies.spray.io"
)

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.2.4",
    "com.typesafe.akka" %% "akka-remote" % "2.2.4",
    "org.scalaj" %% "scalaj-http" % "0.3.12",
    "io.spray" %% "spray-json" % "1.2.5" // A signature in GraphJsonProtocol.class refers to term json
)

unmanagedSourceDirectories in Compile += baseDirectory.value / ".." / "eventbus"
