import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "sbpm",
      organization := "tudarmstadt",
      version := "1.2",
      scalaVersion := "2.9.2",
    	scalacOptions := Seq("-Ydependent-method-types", "-unchecked", "-deprecation", "-encoding", "utf8"),
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "spray repo" at "http://repo.spray.io",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" % "akka-actor"       % "2.0.4",
        "com.typesafe.akka" % "akka-testkit"     % "2.0.4"           % "test",
        "junit"             % "junit"            % "4.5"             % "test",
        "org.scalatest"     % "scalatest_2.9.0"  % "1.6.1"           % "test",
        "io.spray"          % "spray-can"        % "1.0-M6",
        "io.spray"          % "spray-routing"    % "1.0-M6",
        "io.spray"          % "spray-json_2.9.2" % "1.2.3",
        "com.typesafe"      % "slick_2.10.0-RC1" % "0.11.2")
    )
  )
}
