import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      mainClass := Some("de.tkip.sbpm.repo.Boot"),
      name := "S-BPM Repository",
      organization := "TU Darmstadt Telecooperation Group",
      version := "0.1",
      scalaVersion := "2.10.1",
      resolvers ++= Seq(
        "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
        "spray repo" at "http://repo.spray.io",
        "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
        "google-api-services" at "http://google-api-client-libraries.appspot.com/mavenrepo"),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % "2.10.0",
        "org.scalatest" %% "scalatest" % "1.9.1" % "test",
        "com.typesafe.akka" %% "akka-actor" % "2.1.4",
        "com.typesafe.akka" %% "akka-testkit" % "2.1.4" % "test",

        "io.spray" % "spray-can" % "1.1-M7",
        "io.spray" % "spray-routing" % "1.1-M7",
        "io.spray" % "spray-testkit" % "1.1-M7" % "test",
        "io.spray" %% "spray-json" % "1.2.5")))

}
