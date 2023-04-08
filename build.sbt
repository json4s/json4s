import sbtcrossproject.CrossProject
import build._

Global / onChangedBuildSource := ReloadOnSourceChanges

json4sSettings(cross = false)
noPublish

lazy val nativeSettings = Def.settings(
)

lazy val ast = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("ast"))
  .settings(
    name := "json4s-ast",
    json4sSettings(cross = true),
    Compile / sourceGenerators += task {
      val v = CrossVersion.partialVersion(scalaVersion.value)
      Seq(
        ("ReaderFunctions.scala", CodeGen.reader),
        ("WriterFunctions.scala", CodeGen.writer(v == Some((2, 13)) || v.exists(_._1 == 3))),
        ("FormatFunctions.scala", CodeGen.format)
      ).map { case (fileName, src) =>
        val f = (Compile / sourceManaged).value / "org" / "json4s" / fileName
        IO.write(f, src)
        f
      }
    },
    buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.json4s",
  )
  .enablePlugins(BuildInfoPlugin)
  .nativeSettings(
    nativeSettings
  )
  .jsSettings(
    scalajsProjectSettings
  )
  .platformsSettings(JSPlatform, NativePlatform)(
    Compile / unmanagedSourceDirectories += {
      baseDirectory.value.getParentFile / "js_native/src/main/scala"
    }
  )
  .platformsSettings(JVMPlatform, NativePlatform)(
    Seq(Compile, Test).map { c =>
      c / unmanagedSourceDirectories += {
        baseDirectory.value.getParentFile / "jvm_native/src" / Defaults.nameForSrc(c.name) / "scala"
      }
    }
  )

lazy val astJVM = ast.jvm

lazy val scalap = project
  .in(file("scalap"))
  .settings(
    name := "json4s-scalap",
    json4sSettings(cross = false),
    libraryDependencies ++= Seq(Dependencies.jaxbApi),
  )

val isScala3 = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value).exists(_._1 == 3)
)

lazy val xml = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("xml"))
  .settings(
    name := "json4s-xml",
    json4sSettings(cross = true),
    libraryDependencies += Dependencies.scalaXml.value,
  )
  .nativeSettings(
    nativeSettings,
  )
  .jsSettings(
    scalajsProjectSettings
  )
  .dependsOn(
    ast % "compile;test->test",
    nativeCore % "test",
  )

lazy val xmlJVM = xml.jvm

lazy val core = project
  .in(file("core"))
  .settings(
    name := "json4s-core",
    json4sSettings(cross = false),
    libraryDependencies ++= {
      scalaBinaryVersion.value match {
        case "2.12" =>
          Seq(Dependencies.paranamer)
        case _ =>
          Nil
      }
    },
    Test / console / initialCommands := """
        |import org.json4s._
        |import reflect._
      """.stripMargin,
  )
  .dependsOn(astJVM % "compile;test->test", scalap)

lazy val nativeCore = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("native-core"))
  .settings(
    name := "json4s-native-core",
    json4sSettings(cross = true),
    Test / unmanagedResourceDirectories += baseDirectory.value.getParentFile / "shared/src/test/resources",
  )
  .nativeSettings(
    nativeSettings
  )
  .jsSettings(
    scalajsProjectSettings
  )
  .dependsOn(ast % "compile;test->test")

lazy val nativeCoreJVM = nativeCore.jvm

lazy val native = project
  .in(file("native"))
  .settings(
    name := "json4s-native",
    json4sSettings(cross = false),
  )
  .dependsOn(
    core % "compile;test->test",
    nativeCoreJVM % "compile;test->test",
  )

lazy val ext = project
  .in(file("ext"))
  .settings(
    name := "json4s-ext",
    json4sSettings(cross = false),
    libraryDependencies ++= Dependencies.jodaTime,
  )
  .dependsOn(core)

lazy val jacksonCore = project
  .in(file("jackson-core"))
  .settings(
    name := "json4s-jackson-core",
    json4sSettings(cross = false),
    libraryDependencies ++= Dependencies.jackson,
  )
  .dependsOn(astJVM % "compile;test->test")

lazy val jackson = project
  .in(file("jackson"))
  .settings(
    name := "json4s-jackson",
    json4sSettings(cross = false),
  )
  .dependsOn(
    core % "compile;test->test",
    native % "test->test",
    jacksonCore % "compile;test->test",
  )

lazy val examples = project
  .in(file("examples"))
  .settings(
    name := "json4s-examples",
    json4sSettings(cross = false),
    noPublish,
  )
  .dependsOn(
    core % "compile;test->test",
    native % "compile;test->test",
    jackson % "compile;test->test",
    ext,
    mongo
  )

lazy val scalaz = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("scalaz"))
  .settings(
    name := "json4s-scalaz",
    json4sSettings(cross = true),
    libraryDependencies += Dependencies.scalaz_core.value,
  )
  .nativeSettings(
    nativeSettings,
  )
  .jsSettings(
    scalajsProjectSettings
  )
  .dependsOn(
    ast % "compile;test->test",
    nativeCore % "provided->compile",
  )
  .configurePlatform(JVMPlatform)(
    _.dependsOn(
      jacksonCore % "provided->compile",
    )
  )

lazy val mongo = project
  .in(file("mongo"))
  .settings(
    name := "json4s-mongo",
    json4sSettings(cross = false),
    libraryDependencies ++= Seq(
      "org.mongodb" % "mongo-java-driver" % "3.12.13"
    ),
  )
  .dependsOn(
    core % "compile;test->test",
    jackson % "test",
  )

lazy val tests = project
  .in(file("tests"))
  .settings(
    json4sSettings(cross = false),
    noPublish,
    Test / console / initialCommands :=
      """
        |import org.json4s._
        |import reflect._
      """.stripMargin,
  )
  .dependsOn(
    core % "compile;test->test",
    xmlJVM % "compile;test->test",
    native % "compile;test->test",
    ext,
    jackson
  )
