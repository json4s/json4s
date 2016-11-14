import xml.Group
import sbtbuildinfo.Plugin._
import com.typesafe.sbt.SbtStartScript
import Dependencies._
import build._

lazy val root = Project(
  id = "json4s",
  base = file("."),
  settings = json4sSettings ++ noPublish
) aggregate(core, native, json4sExt, jacksonSupport, scalazExt, json4sTests, mongo, ast, scalap, examples, benchmark)

lazy val ast = Project(
  id = "json4s-ast",
  base = file("ast"),
  settings = json4sSettings ++ buildInfoSettings ++ Seq(
    sourceGenerators in Compile += buildInfo.taskValue,
    buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.json4s"
  )
)

lazy val scalap = Project(
  id = "json4s-scalap",
  base = file("scalap"),
  settings = json4sSettings
)

lazy val core = Project(
  id = "json4s-core",
  base = file("core"),
  settings = json4sSettings ++ Seq(
    libraryDependencies ++= Seq(paranamer) ++ scalaXml(scalaVersion.value),
    initialCommands in (Test, console) := """
        |import org.json4s._
        |import reflect._
      """.stripMargin
  )
) dependsOn(ast % "compile;test->test", scalap)

lazy val native = Project(
  id = "json4s-native",
  base = file("native"),
  settings = json4sSettings
) dependsOn(core % "compile;test->test")

lazy val json4sExt = Project(
  id = "json4s-ext",
  base = file("ext"),
  settings = json4sSettings ++ Seq(libraryDependencies ++= jodaTime)
) dependsOn(native % "provided->compile;test->test")

lazy val jacksonSupport = Project(
  id = "json4s-jackson",
  base = file("jackson"),
  settings = json4sSettings ++ Seq(libraryDependencies ++= jackson)
) dependsOn(core % "compile;test->test")

lazy val examples = Project(
   id = "json4s-examples",
   base = file("examples"),
   settings = json4sSettings ++ SbtStartScript.startScriptForClassesSettings ++ noPublish
) dependsOn(
  core % "compile;test->test",
  native % "compile;test->test",
  jacksonSupport % "compile;test->test",
  json4sExt,
  mongo)

lazy val scalazExt = Project(
  id = "json4s-scalaz",
  base = file("scalaz"),
  settings = json4sSettings ++ Seq(libraryDependencies += scalaz_core)
) dependsOn(core % "compile;test->test", native % "provided->compile", jacksonSupport % "provided->compile")

lazy val mongo = Project(
   id = "json4s-mongo",
   base = file("mongo"),
   settings = json4sSettings ++ Seq(
     libraryDependencies ++= Seq(
       "org.mongodb" % "mongo-java-driver" % "3.3.0"
    )
)) dependsOn(core % "compile;test->test")

lazy val json4sTests = Project(
  id = "json4s-tests",
  base = file("tests"),
  settings = json4sSettings ++ Seq(
    libraryDependencies ++= (specs.value :+ mockito),
    initialCommands in (Test, console) :=
      """
        |import org.json4s._
        |import reflect._
      """.stripMargin
  ) ++ noPublish
) dependsOn(core, native, json4sExt, scalazExt, jacksonSupport, mongo)

lazy val benchmark = Project(
  id = "json4s-benchmark",
  base = file("benchmark"),
  settings = json4sSettings ++ SbtStartScript.startScriptForClassesSettings ++ Seq(
    cancelable := true,
    libraryDependencies ++= Seq(
      "com.google.code.java-allocation-instrumenter" % "java-allocation-instrumenter" % "3.0.1",
      "com.google.caliper" % "caliper" % "0.5-rc1",
      "com.google.code.gson" % "gson" % "2.8.0"
    ),
    runner in Compile in run := {
      val (tp, tmp, si, base, options, strategy, javaHomeDir, connectIn) =
        (thisProject.value, taskTemporaryDirectory.value, scalaInstance.value, baseDirectory.value, javaOptions.value, outputStrategy.value, javaHome.value, connectInput.value)
        new MyRunner(tp.id, ForkOptions(javaHome = javaHomeDir, connectInput = connectIn, outputStrategy = strategy,
          runJVMOptions = options, workingDirectory = Some(base)) )
    }
  ) ++ noPublish
) dependsOn(core, native, jacksonSupport, json4sExt, mongo)
