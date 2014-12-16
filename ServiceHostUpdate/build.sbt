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

// not sure if this dependency should be defined here -- ServiceHost seems to
// work without it
libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5",
                            "org.slf4j" % "slf4j-simple" % "1.7.5")
