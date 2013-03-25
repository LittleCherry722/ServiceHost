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
      scalaVersion := "2.10.0",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "spray repo" at "http://repo.spray.io",
      resolvers += "Nexus Releases" at "http://nexus.thenewmotion.com/content/repositories/releases-public/",
      resolvers += "google-api-services" at "http://google-api-client-libraries.appspot.com/mavenrepo",
      libraryDependencies ++= Seq(
        "org.scala-lang"     % "scala-actors"   % "2.10.0",
        "org.scala-lang"     % "scala-reflect"  % "2.10.0",
        "com.typesafe.akka"  %% "akka-actor"    % "2.1.2",
        "com.typesafe.akka"  %% "akka-testkit"  % "2.1.2"   % "test",
        "com.typesafe.slick" %% "slick"         % "1.0.0",
        "com.typesafe.slick" %% "slick-testkit" % "1.0.0"   % "test",
        "org.xerial"         %  "sqlite-jdbc"   % "3.7.2",
        "junit"              %  "junit"         % "4.5"     % "test",
        "org.scalatest"      %% "scalatest"     % "1.9.1"   % "test",
        "io.spray"           %  "spray-can"     % "1.1-M7",
        "io.spray"           %  "spray-routing" % "1.1-M7",
        "io.spray"           %  "spray-testkit" % "1.1-M7"  % "test",
        "io.spray"           %% "spray-json"    % "1.2.3",
        "ua.t3hnar.bcrypt"   %% "scala-bcrypt"  % "2.0",
        "com.fasterxml.jackson.core" % "jackson-core"                 % "2.0.5",
        "com.google.guava"           % "guava"                        % "13.0",
        "com.google.oauth-client"    % "google-oauth-client-java6"    % "1.13.1-beta",
        "com.google.api-client"      % "google-api-client-java6"      % "1.13.2-beta",
        "com.google.apis"            % "google-api-services-drive"    % "v2-rev47-1.13.2-beta",
        "com.google.http-client"     % "google-http-client-jackson2"  % "1.13.1-beta",
        "com.google.code.findbugs"   % "jsr305"                       % "1.3.9",
        "com.google.apis"            % "google-api-services-oauth2"   % "v2-rev30-1.13.2-beta"
        )
    )
  )
  
}
