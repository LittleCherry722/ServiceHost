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
      scalaVersion := "2.10.2",
      resolvers ++= Seq(
        "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
        "spray repo" at "http://repo.spray.io",
        "spray nightly repo" at "http://nightlies.spray.io",
        "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
        "google-api-services" at "http://google-api-client-libraries.appspot.com/mavenrepo"),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % "2.10.2",
        "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
        "com.typesafe.akka" %% "akka-actor" % "2.2.0",
        "com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test",

        "io.spray" % "spray-can" % "1.2-20130710",
        "io.spray" % "spray-routing" % "1.2-20130710",
        "io.spray" % "spray-testkit" % "1.2-20130710" % "test",
        "io.spray" %% "spray-json" % "1.2.5")))
}
