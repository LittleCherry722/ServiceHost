scalaVersion := "2.10.0"

// Parameters for Eclipse

retrieveManaged := true

EclipseKeys.relativizeLibs := true

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE16)

// Default = Scala
//EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource