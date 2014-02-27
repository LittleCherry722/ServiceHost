import sbtassembly.Plugin._
import AssemblyKeys._

assemblySettings

jarName in assembly := "sbpm.jar"

test in assembly := {}

mainClass in assembly := Some("de.tkip.sbpm.Boot")

Revolver.settings

mergeStrategy in assembly := {
  case "reference.conf" =>
    MergeStrategy.concat
  // case PathList(ps @ _*) if isReadme(ps.last) || isLicenseFile(ps.last) =>
  //   MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.first
    }
  case _ => MergeStrategy.first
}

scalaVersion := "2.10.2"

// Parameters for Eclipse

retrieveManaged := true

// EclipseKeys.relativizeLibs := true

// EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE16)

// Default = Scala
//EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

// EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

testOptions in Test <+= (target in Test) map {
  t => Tests.Argument(TestFrameworks.ScalaTest, "junitxml(directory=\"%s\")" format (t / "test-reports"))
}

fork in Test := true

atmosSettings
