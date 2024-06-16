import build._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

Global / onChangedBuildSource := ReloadOnSourceChanges

noPublish

lazy val ast = projectMatrix
  .in(file("ast"))
  .defaultAxes()
  .settings(
    name := "json4s-ast",
    json4sSettings,
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
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings,
  )
  .nativePlatform(
    scalaVersions = scalaVersions,
    settings = nativeSettings
  )
  .jsPlatform(
    scalaVersions = scalaVersions,
    settings = jsSettings
  )

lazy val scalap = projectMatrix
  .in(file("scalap"))
  .defaultAxes()
  .settings(
    name := "json4s-scalap",
    json4sSettings,
    libraryDependencies ++= Seq(Dependencies.jaxbApi),
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings,
  )

lazy val xml = projectMatrix
  .in(file("xml"))
  .defaultAxes()
  .settings(
    name := "json4s-xml",
    json4sSettings,
    libraryDependencies += Dependencies.scalaXml.value,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings,
  )
  .nativePlatform(
    scalaVersions = scalaVersions,
    settings = nativeSettings,
  )
  .jsPlatform(
    scalaVersions = scalaVersions,
    settings = jsSettings
  )
  .dependsOn(
    ast % "compile;test->test",
    nativeCore % "test",
  )

lazy val core = projectMatrix
  .in(file("core"))
  .defaultAxes()
  .settings(
    name := "json4s-core",
    json4sSettings,
    libraryDependencies ++= {
      scalaBinaryVersion.value match {
        case "2.12" =>
          Seq(Dependencies.paranamer)
        case "3" =>
          // Since this dependency requires the compiler version,
          // it's the safest if users provide one depending on which scala
          // they are compiling on
          Seq("org.scala-lang" %% "scala3-staging" % scalaVersion.value % "test,provided")
        case _ =>
          Nil
      }
    },
    Test / console / initialCommands := """
        |import org.json4s._
        |import reflect._
      """.stripMargin,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings ++ overwriteScala3version,
  )
  .dependsOn(ast % "compile;test->test", scalap)

lazy val nativeCore = projectMatrix
  .in(file("native-core"))
  .defaultAxes()
  .settings(
    name := "json4s-native-core",
    json4sSettings,
    Test / unmanagedResourceDirectories += baseDirectory.value.getParentFile / "shared/src/test/resources",
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings,
  )
  .nativePlatform(
    scalaVersions = scalaVersions,
    settings = nativeSettings,
  )
  .jsPlatform(
    scalaVersions = scalaVersions,
    settings = jsSettings
  )
  .dependsOn(ast % "compile;test->test")

lazy val native = projectMatrix
  .in(file("native"))
  .defaultAxes()
  .settings(
    name := "json4s-native",
    json4sSettings,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings ++ overwriteScala3version,
  )
  .dependsOn(
    core % "compile;test->test",
    nativeCore % "compile;test->test",
  )

lazy val ext = projectMatrix
  .in(file("ext"))
  .defaultAxes()
  .settings(
    name := "json4s-ext",
    json4sSettings,
    libraryDependencies ++= Dependencies.jodaTime,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings ++ overwriteScala3version,
  )
  .dependsOn(core)

lazy val jacksonCore = projectMatrix
  .in(file("jackson-core"))
  .defaultAxes()
  .settings(
    name := "json4s-jackson-core",
    json4sSettings,
    libraryDependencies ++= Dependencies.jackson,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings,
  )
  .nativePlatform(
    scalaVersions = scalaVersions,
    settings = Def.settings(
      noPublish,
      Compile / sources := Nil,
      Test / sources := Nil,
    ),
  )
  .jsPlatform(
    scalaVersions = scalaVersions,
    settings = Def.settings(
      noPublish,
      Compile / sources := Nil,
      Test / sources := Nil,
    )
  )
  .dependsOn(ast % "compile;test->test")

lazy val jackson = projectMatrix
  .in(file("jackson"))
  .defaultAxes()
  .settings(
    name := "json4s-jackson",
    json4sSettings,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings ++ overwriteScala3version,
  )
  .dependsOn(
    core % "compile;test->test",
    native % "test->test",
    jacksonCore % "compile;test->test",
  )

lazy val examples = projectMatrix
  .in(file("examples"))
  .defaultAxes()
  .settings(
    name := "json4s-examples",
    json4sSettings,
    noPublish,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings ++ overwriteScala3version,
  )
  .dependsOn(
    core % "compile;test->test",
    native % "compile;test->test",
    jackson % "compile;test->test",
    ext,
    mongo
  )

lazy val scalaz = projectMatrix
  .in(file("scalaz"))
  .defaultAxes()
  .settings(
    name := "json4s-scalaz",
    json4sSettings,
    libraryDependencies += Dependencies.scalaz_core.value,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings,
  )
  .nativePlatform(
    scalaVersions = scalaVersions,
    settings = nativeSettings,
  )
  .jsPlatform(
    scalaVersions = scalaVersions,
    settings = jsSettings
  )
  .dependsOn(
    ast % "compile;test->test",
    nativeCore % "provided->compile",
    jacksonCore % "provided->compile"
  )

lazy val mongo = projectMatrix
  .in(file("mongo"))
  .defaultAxes()
  .settings(
    name := "json4s-mongo",
    json4sSettings,
    libraryDependencies ++= Seq(
      "org.mongodb" % "mongo-java-driver" % "3.12.14"
    ),
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings ++ overwriteScala3version,
  )
  .dependsOn(
    core % "compile;test->test",
    jackson % "test",
  )

lazy val tests = projectMatrix
  .in(file("tests"))
  .defaultAxes()
  .settings(
    json4sSettings,
    noPublish,
    Test / console / initialCommands :=
      """
        |import org.json4s._
        |import reflect._
      """.stripMargin,
  )
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = jvmSettings ++ overwriteScala3version,
  )
  .dependsOn(
    core % "compile;test->test",
    xml % "compile;test->test",
    native % "compile;test->test",
    ext,
    jackson
  )

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publishSigned"),
  releaseStepCommandAndRemaining("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
