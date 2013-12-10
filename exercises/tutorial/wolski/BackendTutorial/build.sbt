import sbt._
import Process._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

assemblySettings

unmanagedBase <<= baseDirectory { base => base / "managed_lib" }

name := "sbpmtutorial"

jarName in assembly := "sbpm_tutorial.jar" 

mainClass in assembly := Some("de.tkip.sbpm.Boot")

scalaVersion := "2.10.0"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.1.2",
  "io.spray"           %  "spray-can"     % "1.1-M7",
  "io.spray"           %  "spray-routing" % "1.1-M7",
  "io.spray"           %% "spray-json"    % "1.2.3"
  )