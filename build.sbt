import xml.Group
import sbtbuildinfo.Plugin._
import com.typesafe.sbt.pgp.PgpKeys
import Dependencies._

val manifestSetting = packageOptions += {
    Package.ManifestAttributes(
      "Created-By" -> "Simple Build Tool",
      "Built-By" -> System.getProperty("user.name"),
      "Build-Jdk" -> System.getProperty("java.version"),
      "Specification-Title" -> name.value,
      "Specification-Version" -> version.value,
      "Specification-Vendor" -> organization.value,
      "Implementation-Title" -> name.value,
      "Implementation-Version" -> version.value,
      "Implementation-Vendor-Id" -> organization.value,
      "Implementation-Vendor" -> organization.value
    )
}

val publishSetting = publishTo := {
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some(Opts.resolver.sonatypeSnapshots)
  else
    Some(Opts.resolver.sonatypeStaging)
}

val mavenCentralFrouFrou = Seq(
  homepage := Some(new URL("https://github.com/json4s/json4s")),
  startYear := Some(2009),
  licenses := Seq(("ASL", new URL("http://github.com/json4s/json4s/raw/HEAD/LICENSE"))),
  pomExtra := (pomExtra.value ++ Group(
    <scm>
      <url>http://github.com/json4s/json4s</url>
      <connection>scm:git:git://github.com/json4s/json4s.git</connection>
    </scm>
    <developers>
      <developer>
        <id>casualjim</id>
        <name>Ivan Porto Carrero</name>
        <url>http://flanders.co.nz/</url>
      </developer>
    </developers>
  ))
)

val Scala212 = "2.12.3"

val json4sSettings = mavenCentralFrouFrou ++ Seq(
  organization := "org.json4s",
  scalaVersion := Scala212,
  version := "3.2.12-SNAPSHOT",
  crossScalaVersions := Seq("2.11.11", Scala212),
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-language:implicitConversions", "-language:higherKinds", "-language:reflectiveCalls", "-language:postfixOps"),
  javacOptions ++= Seq("-target", "1.6", "-source", "1.6"),
  manifestSetting,
  publishSetting,
  parallelExecution in Test := false,
  resolvers ++= Seq(Opts.resolver.sonatypeReleases),
  crossVersion := CrossVersion.binary
)

val json4sMimaSettings = Seq(
  mimaPreviousArtifacts := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        Set("11").map { v =>
          organization.value %% name.value % s"3.2.$v"
        }
      case _ =>
        Set.empty
    }
  }
)

lazy val root = Project(
  id = "json4s",
  base = file("."),
  settings = json4sSettings ++ noPublish
) aggregate(core, native, json4sExt, jacksonSupport, json4sTests, ast)

lazy val ast = Project(
  id = "json4s-ast",
  base = file("ast"),
  settings = json4sSettings ++ buildInfoSettings ++ json4sMimaSettings ++ Seq(
    sourceGenerators in Compile += buildInfo.taskValue,
    buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.json4s"
  )
)

lazy val core = Project(
  id = "json4s-core",
  base = file("core"),
  settings = json4sSettings ++ json4sMimaSettings ++ Seq(
    libraryDependencies ++= Seq(paranamer, scalap(scalaVersion.value)),
    initialCommands in (Test, console) := """
        |import org.json4s._
        |import reflect._
        |import scala.tools.scalap.scalax.rules.scalasig._
      """.stripMargin
  )
) dependsOn(ast % "compile;test->test")

lazy val native = Project(
  id = "json4s-native",
  base = file("native"),
  settings = json4sSettings ++ json4sMimaSettings
) dependsOn(core % "compile;test->test")

lazy val json4sExt = Project(
  id = "json4s-ext",
  base = file("ext"),
  settings = json4sSettings ++ json4sMimaSettings ++ Seq(libraryDependencies ++= jodaTime)
) dependsOn(native % "provided->compile;test->test")

lazy val jacksonSupport = Project(
  id = "json4s-jackson",
  base = file("jackson"),
  settings = json4sSettings ++ json4sMimaSettings ++ Seq(libraryDependencies ++= jackson)
) dependsOn(core % "compile;test->test")

lazy val examples = Project(
   id = "json4s-examples",
   base = file("examples"),
   settings = json4sSettings ++ noPublish ++ Seq(
   )
) dependsOn(
  core % "compile;test->test",
  native % "compile;test->test",
  jacksonSupport % "compile;test->test",
  json4sExt)

lazy val json4sTests = Project(
  id = "json4s-tests",
  base = file("tests"),
  settings = json4sSettings ++ noPublish ++ Seq(
    libraryDependencies ++= Seq(specs, scalacheck),
    initialCommands in (Test, console) :=
      """
        |import org.json4s._
        |import reflect._
        |import scala.tools.scalap.scalax.rules.scalasig._
      """.stripMargin
  )
) dependsOn(core, native, json4sExt, jacksonSupport)

lazy val noPublish = Seq(
  mimaPreviousArtifacts := Set(),
  publishArtifact := false,
  PgpKeys.publishSigned := {},
  PgpKeys.publishLocalSigned := {},
  publish := {},
  publishLocal := {}
)
