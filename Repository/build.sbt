import sbtassembly.Plugin._
import AssemblyKeys._

assemblySettings

jarName in assembly := "sbpm.jar" 

test in assembly := {} 

mainClass in assembly := Some("de.tkip.sbpm.repo.Boot")

// mergeStrategy in assembly := { 
//   case "reference.conf" =>
//     MergeStrategy.concat
//   // case PathList(ps @ _*) if isReadme(ps.last) || isLicenseFile(ps.last) =>
//   //   MergeStrategy.rename
//   case PathList("META-INF", xs @ _*) =>
//     (xs map {_.toLowerCase}) match {
//       case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
//         MergeStrategy.discard
//       case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
//         MergeStrategy.discard
//       case "plexus" :: xs =>
//         MergeStrategy.discard
//       case "services" :: xs =>
//         MergeStrategy.filterDistinctLines
//       case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
//         MergeStrategy.filterDistinctLines
//       case _ => MergeStrategy.first
//     }
//   case _ => MergeStrategy.first
// }
