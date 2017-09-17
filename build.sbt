import java.nio.file.{CopyOption, Files, StandardCopyOption}

enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

name := "BFInterpreter"
version := "0.1"
scalaVersion := "2.12.3"

scalaJSUseMainModuleInitializer := true

libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0"
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"

npmDependencies in Compile ++= Seq(
  "react" -> "15.6.1",
  "react-dom" -> "15.6.1"
)

lazy val runJS = taskKey[Unit]("Compiles and runs fast JS")

runJS := {
  val bundleName = (webpack in (Compile, fastOptJS)).value.head.data
  val target = file("./dist/bf-interpreter.js")

  Files.copy(bundleName.toPath, target.toPath, StandardCopyOption.REPLACE_EXISTING)
  println("Finished copying bundle")
}


