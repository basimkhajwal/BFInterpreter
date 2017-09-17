enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

name := "BFInterpreter"
version := "0.1"
scalaVersion := "2.12.3"

scalaJSUseMainModuleInitializer := true

libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0"

npmDependencies in Compile ++= Seq(
  "react" -> "15.6.1",
  "react-dom" -> "15.6.1"
)

crossTarget in fastOptJS := file("dist")
crossTarget in fullOptJS := file("dist")
