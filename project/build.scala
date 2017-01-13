import sbt._
import Keys._
import xml.Group
//import sbtscalashim.Plugin._
import sbtbuildinfo.Plugin._
import com.typesafe.sbt.SbtStartScript


object build extends Build {
  import Dependencies._
  import Resolvers._

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

  val publishSetting = publishTo <<= (version) { version: String =>
    if (version.trim.endsWith("SNAPSHOT"))
      Some(sonatypeNexusSnapshots)
    else
      Some(sonatypeNexusStaging)
  }

  val mavenCentralFrouFrou = Seq(
    homepage := Some(new URL("https://github.com/json4s/json4s")),
    startYear := Some(2009),
    licenses := Seq(("ASL", new URL("http://github.com/json4s/json4s/raw/HEAD/LICENSE"))),
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
      </developers>
    )}
  )

  val json4sSettings = Defaults.defaultSettings ++ mavenCentralFrouFrou ++ Seq(
    organization := "org.json4s",
    scalaVersion := "2.9.3",
    crossScalaVersions := Seq("2.9.1", "2.9.1-1", "2.9.2", "2.9.3"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimize"),
    version := "3.2.12-SNAPSHOT",
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6"),
    manifestSetting,
    publishSetting,
    resolvers ++= Seq( Opts.resolver.sonatypeSnapshots, Opts.resolver.sonatypeReleases)
  )

  lazy val root = Project(
    id = "json4s",
    base = file("."),
    settings = json4sSettings
  ) aggregate(core, native, json4sExt, nativeLift, jacksonSupport, scalazExt, json4sTests, mongo, ast)

  lazy val ast = Project(
    id = "json4s-ast",
    base = file("ast"),
    settings = json4sSettings ++ buildInfoSettings ++ Seq(
      sourceGenerators in Compile <+= buildInfo,
      buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
      buildInfoPackage := "org.json4s"
    )
  )

  lazy val core = Project(
    id = "json4s-core",
    base = file("core"),
    settings = json4sSettings ++ Seq(
      libraryDependencies <++= scalaVersion { sv => Seq(paranamer, scalap(sv)) },
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
    settings = json4sSettings
  ) dependsOn(core % "compile;test->test")

  lazy val json4sExt = Project(
    id = "json4s-ext",
    base = file("ext"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= jodaTime)
  ) dependsOn(native % "provided->compile;test->test")

  lazy val nativeLift = Project(
    id = "json4s-native-lift",
    base = file("native-lift"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= Seq(liftCommon, commonsCodec))
  )  dependsOn(native % "provided->compile;test->test")

  lazy val jacksonSupport = Project(
    id = "json4s-jackson",
    base = file("jackson"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= jackson)
  ) dependsOn(core % "compile;test->test")
//
//  lazy val playSupport = Project(
//    id = "json4s-play",
//    base = file("play"),
//    settings = json4sSettings ++ Seq(libraryDependencies ++= jackson)
//  ) dependsOn(core % "compile;test->test")

  lazy val examples = Project(
     id = "json4s-examples",
     base = file("examples"),
     settings = json4sSettings ++ SbtStartScript.startScriptForClassesSettings ++ Seq(
       libraryDependencies += "net.databinder.dispatch" % "dispatch-core_2.9.2" % "0.11.0",
       libraryDependencies += jacksonScala
     )
  ) dependsOn(
    core % "compile;test->test",
    native % "compile;test->test",
    jacksonSupport % "compile;test->test",
    json4sExt,
    mongo)

//
//  lazy val jacksonExt = Project(
//    id = "json4s-jackson-ext",
//    base = file("jackson-ext"),
//    settings = json4sSettings ++ Seq(libraryDependencies ++= jodaTime)
//  ) dependsOn(jacksonSupport % "compile;test->test")
//
  lazy val scalazExt = Project(
    id = "json4s-scalaz",
    base = file("scalaz"),
    settings = json4sSettings ++ Seq(libraryDependencies <+= scalaVersion(scalaz_core))
  ) dependsOn(core % "compile;test->test", native % "provided->compile", jacksonSupport % "provided->compile")

  lazy val mongo = Project(
     id = "json4s-mongo",
     base = file("mongo"),
     settings = json4sSettings ++ Seq(
       libraryDependencies ++= Seq(
         "org.mongodb" % "mongo-java-driver" % "2.11.4"
      )
     )
  ) dependsOn(core % "compile;test->test")

  lazy val json4sTests = Project(
    id = "json4s-tests",
    base = file("tests"),
    settings = json4sSettings ++ Seq(
      libraryDependencies <++= scalaVersion { sv => Seq(specs(sv), scalacheck, mockito) },
      initialCommands in (Test, console) :=
        """
          |import org.json4s._
          |import reflect._
          |import scala.tools.scalap.scalax.rules.scalasig._
        """.stripMargin
    )
  ) dependsOn(core, native, json4sExt, nativeLift, scalazExt, jacksonSupport, mongo)

  lazy val benchmark = Project(
    id = "json4s-benchmark",
    base = file("benchmark"),
    settings = json4sSettings ++ SbtStartScript.startScriptForClassesSettings ++ Seq(
      cancelable := true,
      libraryDependencies ++= Seq(
        "com.google.code.java-allocation-instrumenter" % "java-allocation-instrumenter" % "2.0",
        "com.google.caliper" % "caliper" % "0.5-rc1",
        "com.google.code.gson" % "gson" % "1.7.1"
      ),
      libraryDependencies += jacksonScala,
      runner in Compile in run <<= (thisProject, taskTemporaryDirectory, scalaInstance, baseDirectory, javaOptions, outputStrategy, javaHome, connectInput) map {
        (tp, tmp, si, base, options, strategy, javaHomeDir, connectIn) =>
          new MyRunner(tp.id, ForkOptions(javaHome = javaHomeDir, connectInput = connectIn, outputStrategy = strategy,
            runJVMOptions = options, workingDirectory = Some(base)) )
      }
    )
  ) dependsOn(core, native, jacksonSupport, json4sExt, mongo)


}

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

