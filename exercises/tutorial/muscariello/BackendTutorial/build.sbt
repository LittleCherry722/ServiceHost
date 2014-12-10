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

scalaVersion := "2.11.3"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "io.spray"           %%  "spray-can"     % "1.3.1",
  "io.spray"           %%  "spray-routing" % "1.3.1",
  "io.spray"           %% "spray-json"    % "1.3.1"
  )
