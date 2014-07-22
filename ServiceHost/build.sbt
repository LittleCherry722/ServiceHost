name := "Sbpm Service Host"

version := "1.0"

scalaVersion := "2.11.1"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "spray repo" at "http://repo.spray.io"
)

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.4",
    "com.typesafe.akka" %% "akka-remote" % "2.3.4",
    "com.typesafe.akka" %% "akka-slf4j" % "2.3.4",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe.slick" %% "slick" % "2.1.0-M2",
    "org.scalaj" %% "scalaj-http" % "0.3.15",
    "io.spray" %% "spray-routing" % "1.3.1",
    "io.spray" %% "spray-json" % "1.2.6"
)

unmanagedSourceDirectories in Compile += baseDirectory.value / ".." / "eventbus"
