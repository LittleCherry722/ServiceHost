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

scalaVersion := "2.10.3"

scalaHome := Some(file("/usr/share/scala"))

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.1.2",
  //"org.parboiled"      % "parboiled-scala_2.10" % "1.1.6" exclude("org.scala-stm", "scala-stm_2.10.0"),
  "io.spray"           %  "spray-can"     % "1.1-M7" excludeAll(
    ExclusionRule("org.parboiled", "parboiled-scala_2.10.0-RC5")//,
    //ExclusionRule("com.typesafe.akka", "akka-actor", "2.1.0")
  ),
  "io.spray"           %  "spray-routing" % "1.1-M7",
  "io.spray"           %% "spray-json"    % "1.2.3"
  )
