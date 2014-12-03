import sbt._
import Process._
import Keys._

name := "AkkaTutorial"

scalaVersion := "2.11.4"

mainClass := Some("de.tkip.akkatutorial.Main")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.7"
