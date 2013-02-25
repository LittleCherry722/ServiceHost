import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      mainClass := Some("de.tkip.sbpm.Boot"),
      name := "sbpm",
      organization := "tudarmstadt",
      version := "1.2",
      scalaVersion := "2.10.0",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "spray repo" at "http://repo.spray.io",
      resolvers += "Nexus Releases" at "http://nexus.thenewmotion.com/content/repositories/releases-public/",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" % "akka-actor_2.10"   % "2.1.0",
        "com.typesafe.akka" % "akka-testkit_2.10" % "2.1.0" % "test",
        "com.typesafe.slick" % "slick_2.10"       % "1.0.0",
        "junit"             % "junit"             % "4.5"        % "test",
        "org.scalatest"     % "scalatest_2.10"    % "1.9.1",
        "io.spray"          % "spray-can"         % "1.1-M7",
        "io.spray"          % "spray-routing"     % "1.1-M7",
        "io.spray"          % "spray-testkit"     % "1.1-M7",
        "io.spray"          % "spray-json_2.10"   % "1.2.3",
        "ua.t3hnar.bcrypt"  % "scala-bcrypt_2.10" % "2.0")
    )
  )
}
