import sbt._
import Keys._
import xml.Group
import sbtbuildinfo.Plugin._
import com.typesafe.sbt.SbtStartScript
import MimaSettings.mimaSettings
import com.typesafe.tools.mima.plugin.MimaKeys.mimaPreviousArtifacts
import com.typesafe.sbt.JavaVersionCheckPlugin.autoImport._

object build {
  import Dependencies._

  val manifestSetting = packageOptions += {
    val (title, v, vendor) = (name.value, version.value, organization.value)
      Package.ManifestAttributes(
        "Created-By" -> "Simple Build Tool",
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
    homepage := Some(new URL("https://github.com/json4s/json4s")),
    startYear := Some(2009),
    licenses := Seq(("Apache-2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0"))),
    pomExtra := {
      pomExtra.value ++ Group(
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
        <developer>
          <id>seratch</id>
          <name>Kazuhiro Sera</name>
          <url>http://git.io/sera</url>
        </developer>
      </developers>
      )
    }
  )

  val json4sSettings = mavenCentralFrouFrou ++ Seq(
    organization := "org.json4s",
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.2"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps", "-Xfuture"),
    scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
      case Some((2, 10)) => "-optimize"
    }.toList,
    scalacOptions in (Compile, doc) ++= {
      val base = (baseDirectory in LocalRootProject).value.getAbsolutePath
      val hash = sys.process.Process("git rev-parse HEAD").lines_!.head
      Seq("-sourcepath", base, "-doc-source-url", "https://github.com/json4s/json4s/tree/" + hash + "€{FILE_PATH}.scala")
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
          Seq("-Ywarn-unused-import", "-Ywarn-unused")
        case _ =>
          Nil
      }
    },
    version := "3.5.2",
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6"),
    javaVersionPrefix in javaVersionCheck := Some{
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 11 => "1.7"
        case _ => "1.8"
      }
    },
    parallelExecution in Test := false,
    manifestSetting,
    resolvers ++= Seq(Opts.resolver.sonatypeSnapshots, Opts.resolver.sonatypeReleases),
    crossVersion := CrossVersion.binary
  ) ++ mimaSettings

  val noPublish = Seq(
    mimaPreviousArtifacts := Set(),
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
}

// TODO: fix some deprecation warnings
// taken from https://github.com/dcsobral/scala-foreach-benchmark
class MyRunner(subproject: String, config: ForkScalaRun) extends sbt.ScalaRun {
  def run(mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger): Option[String] = {
    log.info("Running " + subproject + " " + mainClass + " " + options.mkString(" "))

    val javaOptions = classpathOption(classpath) ::: mainClass :: options.toList
    val strategy = config.outputStrategy getOrElse LoggedOutput(log)
    val process =  Fork.java.fork(config.javaHome,
                                  config.runJVMOptions ++ javaOptions,
                                  config.workingDirectory,
                                  Map.empty,
                                  config.connectInput,
                                  strategy)
    def cancel() = {
      log.warn("Run canceled.")
      process.destroy()
      1
    }
    val exitCode = try process.exitValue() catch { case e: InterruptedException => cancel() }
    processExitCode(exitCode, "runner")
  }
  private def classpathOption(classpath: Seq[File]) = "-classpath" :: Path.makeString(classpath) :: Nil
  private def processExitCode(exitCode: Int, label: String) = {
    if(exitCode == 0) None
    else Some("Nonzero exit code returned from " + label + ": " + exitCode)
  }
}
