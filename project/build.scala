import sbt._
import Keys._
import xml.Group
import MimaSettings.mimaSettings
import com.typesafe.tools.mima.plugin.MimaKeys.mimaPreviousArtifacts
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import xerial.sbt.Sonatype.autoImport._

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
    publishTo := sonatypePublishToBundle.value,
    homepage := Some(url("https://github.com/json4s/json4s")),
    startYear := Some(2009),
    licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
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

  val Scala212 = "2.12.18"
  val Scala213 = "2.13.11"
  val Scala3 = "3.3.1"

  def json4sSettings(cross: Boolean) = mavenCentralFrouFrou ++ Def.settings(
    organization := "org.json4s",
    scalaVersion := Scala212,
    crossScalaVersions := Seq(Scala212, Scala213, Scala3),
    addCommandAlias("SetScala212", s"++ ${Scala212}!"),
    addCommandAlias("SetScala213", s"++ ${Scala213}!"),
    addCommandAlias("SetScala3", s"++ ${Scala3}!"),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:existentials",
      "-language:implicitConversions",
      "-language:higherKinds",
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
          Seq("-Xfuture", "-Ypartial-unification")
        case _ =>
          Nil
      }
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq("-Xsource:3")
        case _ =>
          Seq(
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
        val base = if (cross) {
          baseDirectory.value.getParentFile / "shared" / "src" / Defaults.nameForSrc(scope.name)
        } else {
          baseDirectory.value / "src" / Defaults.nameForSrc(scope.name)
        }
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, v)) if v <= 12 =>
            base / s"scala-2.13-"
          case _ =>
            base / s"scala-2.13+"
        }
      }
    },
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommandAndRemaining("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    ),
    Test / parallelExecution := false,
    manifestSetting,
  )

  val noPublish = Seq(
    mimaPreviousArtifacts := Set(),
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )

  val scalajsProjectSettings = Def.settings(
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
    }
  )

}
