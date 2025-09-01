import sbt._
import Keys._
import xml.Group
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import sbtprojectmatrix.ProjectMatrixKeys.*

object build {
  import Dependencies._

  val manifestSetting = packageOptions += {
    val (title, v, vendor) = (name.value, version.value, organization.value)
    Package.ManifestAttributes(
      "Created-By" -> "sbt",
      "Built-By" -> System.getProperty("user.name"),
      "Build-Jdk" -> System.getProperty("java.version"),
      "Specification-Title" -> title,
      "Specification-Version" -> v,
      "Specification-Vendor" -> vendor,
      "Implementation-Title" -> title,
      "Implementation-Version" -> v,
      "Implementation-Vendor-Id" -> vendor,
      "Implementation-Vendor" -> vendor
    )
  }

  val mavenCentralFrouFrou = Seq(
    publishTo := (if (isSnapshot.value) None else localStaging.value),
    homepage := Some(url("https://github.com/json4s/json4s")),
    startYear := Some(2009),
    licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
    organization := "io.github.json4s",
    pomExtra := {
      pomExtra.value ++ Group(
        <scm>
        <url>https://github.com/json4s/json4s</url>
        <connection>scm:git:git://github.com/json4s/json4s.git</connection>
      </scm>
      <developers>
        <developer>
          <id>casualjim</id>
          <name>Ivan Porto Carrero</name>
          <url>http://flanders.co.nz/</url>
        </developer>
        <developer>
          <id>seratch</id>
          <name>Kazuhiro Sera</name>
          <url>http://git.io/sera</url>
        </developer>
      </developers>
      )
    }
  )

  val Scala212 = "2.12.20"
  val Scala213 = "2.13.16"
  val Scala3 = "3.3.6"

  def scalaVersions = Seq(Scala212, Scala213, Scala3)

  def json4sSettings = mavenCentralFrouFrou ++ Def.settings(
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:existentials",
      "-language:implicitConversions",
    ),
    Seq(Compile, Test).map(c =>
      c / unmanagedSourceDirectories += {
        projectMatrixBaseDirectory.value.getAbsoluteFile / "shared" / "src" / Defaults.nameForSrc(
          c.name
        ) / s"scala-${scalaBinaryVersion.value}"
      }
    ),
    Seq(Compile, Test).map(c =>
      c / unmanagedSourceDirectories ++= {
        projectMatrixBaseDirectory.?.value.toSeq.flatMap { x =>
          val d = x.getAbsoluteFile / "shared" / "src" / Defaults.nameForSrc(c.name)
          val d1 = d / "scala"

          d1 +: (
            CrossVersion.partialVersion(scalaVersion.value) match {
              case Some((n, _)) =>
                Seq(d / s"scala-${n}")
              case _ =>
                Nil
            }
          )
        }
      },
    ),
    Compile / packageSrc / mappings ++= (Compile / managedSources).value.map { f =>
      // to merge generated sources into sources.jar as well
      (f, f.relativeTo((Compile / sourceManaged).value).get.getPath)
    },
    libraryDependencies ++= Seq(scalatest.value, scalatestScalacheck.value).flatten,
    (Compile / doc / scalacOptions) ++= {
      val base = (LocalRootProject / baseDirectory).value.getAbsolutePath
      val hash = sys.process.Process("git rev-parse HEAD").lineStream_!.head
      Seq(
        "-sourcepath",
        base,
        "-doc-source-url",
        "https://github.com/json4s/json4s/tree/" + hash + "€{FILE_PATH}.scala"
      )
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v <= 12 =>
          Seq("-Xfuture", "-Ypartial-unification", "-language:higherKinds")
        case _ =>
          Nil
      }
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) =>
          Seq("-Xsource:3")
        case Some((2, 13)) =>
          Seq("-Xsource:3-cross")
        case _ =>
          Seq(
            "-Wconf:msg=with as a type operator has been deprecated:error"
          )
      }
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq("-Ywarn-unused:imports")
        case _ =>
          Nil
      }
    },
    javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
    Seq(Compile, Test).map { scope =>
      (scope / unmanagedSourceDirectories) += {
        val base = projectMatrixBaseDirectory.value.getAbsoluteFile / "shared" / "src" / Defaults.nameForSrc(scope.name)
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, v)) if v <= 12 =>
            base / s"scala-2.13-"
          case _ =>
            base / s"scala-2.13+"
        }
      }
    },
    Test / parallelExecution := false,
    manifestSetting,
  )

  val noPublish = Seq(
    publish / skip := true,
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )

  val jsSettings = Def.settings(
    if (sys.props.isDefinedAt("scala_js_wasm")) {
      println("enable wasm")
      Def.settings(
        scalaJSLinkerConfig ~= (
          _.withExperimentalUseWebAssembly(true).withModuleKind(ModuleKind.ESModule)
        ),
        jsEnv := {
          import org.scalajs.jsenv.nodejs.NodeJSEnv
          val config = NodeJSEnv
            .Config()
            .withArgs(
              List(
                "--experimental-wasm-exnref",
                "--experimental-wasm-imported-strings",
                "--turboshaft-wasm",
              )
            )
          new NodeJSEnv(config)
        },
      )
    } else {
      Def.settings()
    },
    scalacOptions += {
      val hash = sys.process.Process("git rev-parse HEAD").lineStream_!.head
      val a = (LocalRootProject / baseDirectory).value.toURI.toString
      val g = "https://raw.githubusercontent.com/json4s/json4s/" + hash
      val key = CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) =>
          "-scalajs-mapSourceURI"
        case _ =>
          "-P:scalajs:mapSourceURI"
      }
      s"${key}:$a->$g/"
    },
    Seq(Compile, Test).map(c =>
      c / unmanagedSourceDirectories ++= Seq(
        projectMatrixBaseDirectory.value.getAbsoluteFile / "js" / "src" / Defaults.nameForSrc(c.name) / "scala",
        projectMatrixBaseDirectory.value.getAbsoluteFile / "js-native" / "src" / Defaults.nameForSrc(c.name) / "scala"
      ),
    ),
  )

  val jvmSettings = Def.settings(
    Seq(Compile, Test).map(c =>
      c / unmanagedSourceDirectories ++= Seq(
        projectMatrixBaseDirectory.value.getAbsoluteFile / "jvm" / "src" / Defaults.nameForSrc(c.name) / "scala",
        projectMatrixBaseDirectory.value.getAbsoluteFile / "jvm-native" / "src" / Defaults.nameForSrc(c.name) / "scala",
      ),
    ),
  )

  val nativeSettings = Def.settings(
    Seq(Compile, Test).map(c =>
      c / unmanagedSourceDirectories ++= Seq(
        projectMatrixBaseDirectory.value.getAbsoluteFile / "jvm-native" / "src" / Defaults.nameForSrc(c.name) / "scala",
        projectMatrixBaseDirectory.value.getAbsoluteFile / "js-native" / "src" / Defaults.nameForSrc(c.name) / "scala",
      )
    ),
  )

}
