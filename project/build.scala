import sbt._
import Keys._
import xml.Group
import sbtscalashim.Plugin._
import sbtbuildinfo.Plugin._


object Json4sBuild extends Build {
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
    version := "3.1.0-SNAPSHOT",
    scalaVersion := "2.10.0-RC5",
    crossScalaVersions := Seq("2.9.2", "2.10.0-RC5"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimize"),
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6"),
    manifestSetting,
    publishSetting,
    resolvers ++= Seq( sonatypeNexusSnapshots, sonatypeNexusReleases),
    crossVersion := CrossVersion.full,
    artifact in (Compile, packageBin) <<= (artifact in Compile, scalaVersion) { (art: Artifact, sv) =>
      sv match {
        case "2.9.2" => art.copy(classifier = Some("scalaz7"))
        case _ => art
      }
    }
  )

  lazy val root = Project(
    id = "json4s",
    base = file("."),
    settings = json4sSettings
  ) aggregate(core, native, json4sExt, jacksonSupport, scalazExt, json4sTests, mongo, ast)

  lazy val ast = Project(
    id = "json4s-ast",
    base = file("ast"),
    settings = json4sSettings ++ scalaShimSettings ++ buildInfoSettings ++ Seq(
      sourceGenerators in Compile <+= scalaShim,
      sourceGenerators in Compile <+= buildInfo,
      buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
      buildInfoPackage := "org.json4s"
    )
  )

  lazy val core = Project(
    id = "json4s-core",
    base = file("core"),
    settings = json4sSettings ++ Seq(libraryDependencies <++= scalaVersion { sv => Seq(paranamer, scalap(sv)) } )
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

//
//  lazy val nativeLift = Project(
//    id = "json4s-native-lift",
//    base = file("native-lift"),
//    settings = json4sSettings ++ Seq(libraryDependencies ++= Seq(liftCommon, commonsCodec))
//  )  dependsOn(native % "compile;test->test")

  lazy val jacksonSupport = Project(
    id = "json4s-jackson",
    base = file("jackson"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= jackson)
  ) dependsOn(core % "compile;test->test")

  lazy val examples = Project(
     id = "json4s-examples",
     base = file("examples"),
     settings = json4sSettings ++ Seq(
       libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.9.4"
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
         "org.mongodb" % "mongo-java-driver" % "2.9.1"
      )
     )
  ) dependsOn(core % "compile;test->test")

  lazy val json4sTests = Project(
    id = "json4s-tests",
    base = file("tests"),
    settings = json4sSettings ++ Seq(libraryDependencies <++= scalaVersion { sv => Seq(specs(sv), scalacheck(sv), mockito) })
  ) dependsOn(core, native, json4sExt, scalazExt, jacksonSupport, mongo)


}
