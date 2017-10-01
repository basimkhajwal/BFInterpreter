import java.nio.file.{CopyOption, Files, StandardCopyOption}

enablePlugins(ScalaJSPlugin)

name := "BFInterpreter"
version := "0.1"
scalaVersion := "2.12.3"

libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0"
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"
libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "10.0.2"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

jsDependencies ++= Seq(
  "org.webjars.bower" % "react" % "15.6.1"
    /        "react-with-addons.js"
    minified "react-with-addons.min.js"
    commonJSName "React",

  "org.webjars.bower" % "react" % "15.6.1"
    /         "react-dom.js"
    minified  "react-dom.min.js"
    dependsOn "react-with-addons.js"
    commonJSName "ReactDOM",

  "org.webjars.bower" % "react" % "15.6.1"
    /         "react-dom-server.js"
    minified  "react-dom-server.min.js"
    dependsOn "react-dom.js"
    commonJSName "ReactDOMServer"
)

lazy val runJS = taskKey[Unit]("Compiles and runs fast JS")
runJS := {
  val bundleName = (fastOptJS in Compile).value.data
  val deps = file(bundleName.toString.replace("-fastopt.js", "-jsdeps.js"))
  val target = file("./dist/bf-interpreter.js")
  val targetDeps = file("./dist/jsdeps.js")

  Files.copy(bundleName.toPath, target.toPath, StandardCopyOption.REPLACE_EXISTING)
  Files.copy(deps.toPath, targetDeps.toPath, StandardCopyOption.REPLACE_EXISTING)
  println("RunJS - Finished copying bundle")
}
