import sbt._
import Process._
import Keys._

name := "akkatutorial"

scalaVersion := "2.10.0"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.2"
