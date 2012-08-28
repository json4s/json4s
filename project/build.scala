import sbt._
import Keys._
import xml.Group

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
    version := "3.0.0-SNAPSHOT",
    scalaVersion := "2.9.2",
    crossScalaVersions := Seq("2.8.1", "2.8.2", "2.9.0", "2.9.0-1", "2.9.1", "2.9.2", "2.10.0-M7"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimize"),
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6"),
    manifestSetting,
    publishSetting,
    crossPaths := false,
    resolvers ++= Seq( sonatypeNexusSnapshots, sonatypeNexusReleases)    
  )

  lazy val root = Project(
    id = "json4s",
    base = file("."),
    settings = json4sSettings
  ) aggregate(core, native, nativeExt, nativeLift, jacksonSupport, jacksonExt, scalazExt, json4sTests)

  lazy val core = Project(
    id = "json4s-core",
    base = file("core"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= Seq(paranamer, scalap))
  )

  lazy val native = Project(
    id = "json4s-native",
    base = file("native"),
    settings = json4sSettings
  ) dependsOn(core % "compile;test->test")

  lazy val nativeExt = Project(
    id = "json4s-native-ext",
    base = file("native-ext"),
    settings = json4sSettings ++ Seq(
      libraryDependencies ++= jodaTime)
  ) dependsOn(native % "compile;test->test")

  lazy val nativeLift = Project(
    id = "json4s-native-lift",
    base = file("native-lift"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= Seq(liftCommon, commonsCodec))
  )  dependsOn(native % "compile;test->test")

  lazy val jacksonSupport = Project(
    id = "json4s-jackson",
    base = file("jackson"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= jackson)
  ) dependsOn(core % "compile;test->test")

  lazy val jacksonExt = Project(
    id = "json4s-jackson-ext",
    base = file("jackson-ext"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= jodaTime)
  ) dependsOn(jacksonSupport % "compile;test->test")

  lazy val scalazExt = Project(
    id = "json4s-scalaz",
    base = file("scalaz"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= Seq(scalaz))
  ) dependsOn(core % "compile;test->test", native, jacksonSupport)

  lazy val json4sTests = Project(
    id = "json4s-tests",
    base = file("tests"),
    settings = json4sSettings ++ Seq(libraryDependencies ++= Seq(specs2, scalaCheck, mockito))
  ) dependsOn(core, native, nativeExt, nativeLift, scalazExt, jacksonSupport, jacksonExt)
}