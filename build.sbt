import Dependencies._
import build._

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = Project(
  id = "json4s",
  base = file("."),
).settings(
  json4sSettings,
  noPublish,
) aggregate (core, xml, native, json4sExt, jacksonSupport, scalazExt, json4sTests, mongo, ast, scalap, examples)

lazy val ast = Project(
  id = "json4s-ast",
  base = file("ast"),
).settings(
  json4sSettings,
  buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
  buildInfoPackage := "org.json4s",
).enablePlugins(BuildInfoPlugin)

lazy val scalap = Project(
  id = "json4s-scalap",
  base = file("scalap"),
).settings(
  json4sSettings,
)

lazy val xml = Project(
  id = "json4s-xml",
  base = file("xml"),
).settings(
  json4sSettings,
  libraryDependencies += scalaXml.value,
).dependsOn(core)

lazy val core = Project(
  id = "json4s-core",
  base = file("core"),
).settings(
  json4sSettings,
  libraryDependencies ++= Seq(paranamer),
  Test / console / initialCommands := """
      |import org.json4s._
      |import reflect._
    """.stripMargin,
).dependsOn(ast % "compile;test->test", scalap)

lazy val native = Project(
  id = "json4s-native",
  base = file("native"),
).settings(
  json4sSettings,
).dependsOn(core % "compile;test->test")

lazy val json4sExt = Project(
  id = "json4s-ext",
  base = file("ext"),
).settings(
  json4sSettings,
  libraryDependencies ++= jodaTime,
).dependsOn(native % "provided->compile;test->test")

lazy val jacksonSupport = Project(
  id = "json4s-jackson",
  base = file("jackson"),
).settings(
  json4sSettings,
  libraryDependencies ++= jackson,
).dependsOn(core % "compile;test->test")

lazy val examples = Project(
  id = "json4s-examples",
  base = file("examples"),
).settings(
  json4sSettings,
  noPublish,
).dependsOn(
  core % "compile;test->test",
  native % "compile;test->test",
  jacksonSupport % "compile;test->test",
  json4sExt,
  mongo
)

lazy val scalazExt = Project(
  id = "json4s-scalaz",
  base = file("scalaz"),
).settings(
  json4sSettings,
  libraryDependencies += scalaz_core,
).dependsOn(core % "compile;test->test", native % "provided->compile", jacksonSupport % "provided->compile")

lazy val mongo = Project(
  id = "json4s-mongo",
  base = file("mongo"),
).settings(
  json4sSettings,
  libraryDependencies ++= Seq(
    "org.mongodb" % "mongo-java-driver" % "3.12.8"
  ),
).dependsOn(core % "compile;test->test")

lazy val json4sTests = Project(
  id = "json4s-tests",
  base = file("tests"),
).settings(
  json4sSettings,
  noPublish,
  libraryDependencies ++= Seq(specs.value, scalatest, mockito, jaxbApi, scalatestScalacheck),
  Test / console / initialCommands :=
    """
      |import org.json4s._
      |import reflect._
    """.stripMargin,
).dependsOn(core, xml, native, json4sExt, scalazExt, jacksonSupport, mongo)
