import sbt._
import Keys._
import xml.Group
import sbtbuildinfo.Plugin._
import com.typesafe.sbt.SbtStartScript
import MimaSettings.mimaSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifacts
import com.typesafe.sbt.JavaVersionCheckPlugin.autoImport._

object build extends Build {
  import Dependencies._

  val manifestSetting = packageOptions <+= (name, version, organization) map {
    (title, version, vendor) =>
      Package.ManifestAttributes(
        "Created-By" -> "Simple Build Tool",
        "Built-By" -> System.getProperty("user.name"),
        "Build-Jdk" -> System.getProperty("java.version"),
        "Specification-Title" -> title,
        "Specification-Version" -> version,
        "Specification-Vendor" -> vendor,
        "Implementation-Title" -> title,
        "Implementation-Version" -> version,
        "Implementation-Vendor-Id" -> vendor,
        "Implementation-Vendor" -> vendor
      )
  }

  val mavenCentralFrouFrou = Seq(
    homepage := Some(new URL("https://github.com/json4s/json4s")),
    startYear := Some(2009),
    licenses := Seq(("Apache-2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0"))),
    pomExtra <<= (pomExtra, name, description) {(pom, name, desc) => pom ++ Group(
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
    )}
  )

  val json4sSettings = mavenCentralFrouFrou ++ Seq(
    organization := "org.json4s",
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0-M4"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimize", "-feature", "-language:existentials", "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps"),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
          Seq("-Ywarn-unused-import", "-Ywarn-unused")
        case _ =>
          Nil
      }
    },
    version := "3.4.0-SNAPSHOT",
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6"),
    javaVersionPrefix in javaVersionCheck := Some{
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 11 => "1.7"
        case _ => "1.8"
      }
    },
    manifestSetting,
    resolvers ++= Seq(Opts.resolver.sonatypeSnapshots, Opts.resolver.sonatypeReleases),
    crossVersion := CrossVersion.binary
  ) ++ mimaSettings

  val noPublish = Seq(
    previousArtifacts := Set(),
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )

  lazy val root = Project(
    id = "json4s",
    base = file("."),
    settings = json4sSettings ++ noPublish
  ) aggregate(core, native, json4sExt, jacksonSupport, scalazExt, json4sTests, mongo, ast, scalap, examples, benchmark)

  lazy val ast = Project(
    id = "json4s-ast",
    base = file("ast"),
    settings = json4sSettings ++ buildInfoSettings ++ Seq(
      sourceGenerators in Compile <+= buildInfo,
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
      libraryDependencies <++= scalaVersion { sv => Seq(paranamer) ++ scalaXml(sv) },
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
         "org.mongodb" % "mongo-java-driver" % "3.2.1"
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
        "com.google.code.java-allocation-instrumenter" % "java-allocation-instrumenter" % "3.0",
        "com.google.caliper" % "caliper" % "0.5-rc1",
        "com.google.code.gson" % "gson" % "2.6"
      ),
      runner in Compile in run <<= (thisProject, taskTemporaryDirectory, scalaInstance, baseDirectory, javaOptions, outputStrategy, javaHome, connectInput) map {
        (tp, tmp, si, base, options, strategy, javaHomeDir, connectIn) =>
          new MyRunner(tp.id, ForkOptions(javaHome = javaHomeDir, connectInput = connectIn, outputStrategy = strategy,
            runJVMOptions = options, workingDirectory = Some(base)) )
      }
    ) ++ noPublish
  ) dependsOn(core, native, jacksonSupport, json4sExt, mongo)


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
