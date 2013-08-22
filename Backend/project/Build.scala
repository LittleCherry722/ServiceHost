import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      mainClass := Some("de.tkip.sbpm.Boot"),
      name := "S-BPM Groupware",
      organization := "TU Darmstadt Telecooperation Group",
      version := "1.2",
      scalaVersion := "2.10.2",
      resolvers ++= Seq(
        "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
        "spray repo" at "http://repo.spray.io",
        "spray nightly repo" at "http://nightlies.spray.io",
        "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
        "google-api-services" at "http://google-api-client-libraries.appspot.com/mavenrepo"
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % "2.10.2",
        "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
        "com.typesafe.akka" %% "akka-actor" % "2.2.0",
        "com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test",
        "com.typesafe.akka" %% "akka-remote" % "2.2.0",
        "com.typesafe.slick" %% "slick" % "1.0.1",
        "com.typesafe.slick" %% "slick-testkit" % "1.0.1" % "test",

        "com.typesafe.akka" % "akka-slf4j_2.10" % "2.2.0",
        "ch.qos.logback" % "logback-classic" % "1.0.7",

        "org.xerial" % "sqlite-jdbc" % "3.7.2",
        "com.mchange" % "c3p0" % "0.9.2.1",
        "io.spray" % "spray-can" % "1.2-20130710",
        "io.spray" % "spray-routing" % "1.2-20130710",
        "io.spray" % "spray-testkit" % "1.2-20130710" % "test",
        "io.spray" %% "spray-json" % "1.2.5",
        "net.virtual-void" %%  "json-lenses" % "0.5.3",
        "com.github.t3hnar" % "scala-bcrypt_2.10" % "2.1",
        "com.fasterxml.jackson.core" % "jackson-core" % "2.2.0",

        "com.google.guava" % "guava" % "14.0.1",
        "com.google.code.findbugs" % "jsr305" % "2.0.1",
        "com.google.http-client" % "google-http-client-jackson2" % "1.15.0-rc",
        "com.google.api-client" % "google-api-client-java6" % "1.16.0-rc",
        "com.google.apis"       % "google-api-services-oauth2" % "v2-rev38-1.15.0-rc",
        "com.google.apis"       % "google-api-services-drive" % "v2-rev77-1.15.0-rc",
        "com.google.apis"       % "google-api-services-calendar" % "v3-rev55-1.16.0-rc"
        // "com.google.oauth-client" % "google-oauth-client-java6" % "1.15.0-rc"
      )
    )
  )
}
