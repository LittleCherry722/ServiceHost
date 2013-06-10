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
      scalaVersion := "2.10.1",
      resolvers ++= Seq(
        "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
        "spray repo" at "http://repo.spray.io",
        // "Nexus Releases" at "http://nexus.thenewmotion.com/content/repositories/releases-public/",
        "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "google-api-services" at "http://google-api-client-libraries.appspot.com/mavenrepo"
      ),
      libraryDependencies ++= Seq(
        // "org.scala-lang"     % "scala-actors"   % "2.10.0",
        "org.scala-lang"     % "scala-reflect"  % "2.10.0",
        // "com.typesafe.akka"  %% "akka-actor"    % "2.1.2",
        "com.typesafe.akka"  %% "akka-actor"    % "2.1.4",
        // "com.typesafe.akka"  %% "akka-testkit"  % "2.1.2"   % "test",
        "com.typesafe.akka"  %% "akka-testkit"  % "2.1.4"   % "test",
        // "com.typesafe.slick" %% "slick"         % "1.0.0",
        "com.typesafe.slick" %% "slick"         % "1.0.1",
        // "com.typesafe.slick" %% "slick-testkit" % "1.0.0"   % "test",
        "com.typesafe.slick" %% "slick-testkit" % "1.0.1"   % "test",
        "org.xerial"         %  "sqlite-jdbc"   % "3.7.2",
        // "junit"              %  "junit"         % "4.5"     % "test",
        "org.scalatest"      %% "scalatest"     % "1.9.1"   % "test",
        "io.spray"           %  "spray-can"     % "1.1-M7",
        // "com.mchange"        %  "c3p0"          % "0.9.2",
        "com.mchange"        %  "c3p0"          % "0.9.2.1",

        "io.spray"           %  "spray-routing" % "1.1-M7",
        "io.spray"           %  "spray-testkit" % "1.1-M7"  % "test",
        // "io.spray"           %% "spray-json"    % "1.2.3",
		"io.spray"           %% "spray-json"    % "1.2.5",
        // "ua.t3hnar.bcrypt"   %% "scala-bcrypt"  % "2.0",
		"com.github.t3hnar" % "scala-bcrypt_2.10" % "2.1"
        // "com.fasterxml.jackson.core" % "jackson-core"                 % "2.0.5",
		"com.fasterxml.jackson.core" % "jackson-core"                 % "2.2.0",
        // "com.google.guava"           % "guava"                        % "13.0",
		"com.google.guava"           % "guava"                        % "14.0.1"
        // "com.google.oauth-client"    % "google-oauth-client-java6"    % "1.13.1-beta",
		"com.google.oauth-client"    % "google-oauth-client-java6"    % "1.15.0-rc",
        // "com.google.api-client"      % "google-api-client-java6"      % "1.13.2-beta",
		"com.google.api-client"      % "google-api-client-java6"      % "1.15.0-rc",
        // "com.google.apis"            % "google-api-services-drive"    % "v2-rev47-1.13.2-beta",
		"com.google.apis"            % "google-api-services-drive"    % "v2-rev77-1.15.0-rc",
        // "com.google.http-client"     % "google-http-client-jackson2"  % "1.13.1-beta",
		"com.google.http-client"     % "google-http-client-jackson2"  % "1.15.0-rc",
        // "com.google.code.findbugs"   % "jsr305"                       % "1.3.9",
		"com.google.code.findbugs"   % "jsr305"                       % "2.0.1",
        // "com.google.apis"            % "google-api-services-oauth2"   % "v2-rev30-1.13.2-beta"
		"com.google.apis"            % "google-api-services-oauth2"   % "v2-rev38-1.15.0-rc"
      )
    )
  )
  
}
