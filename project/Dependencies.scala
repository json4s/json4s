import sbt._
import Keys._

object Dependencies {

  /* Steal from lift build */
  def crossMapped(mappings: (String, String)*): CrossVersion =
    CrossVersion.binaryMapped(Map(mappings: _*) orElse { case v => v })

  def defaultOrMapped(default: String, alternatives: (String, String)*): String => String =
    Map(alternatives: _*) orElse { case _ => default }

  type ModuleMap = String => ModuleID
//
//  lazy val CVMapping282  = crossMapped("2.8.2" -> "2.8.1")
//  lazy val CVMapping292  = crossMapped("2.9.2" -> "2.9.1")
//  lazy val CVMapping2911 = crossMapped("2.9.2" -> "2.9.1", "2.9.1-1" -> "2.9.1")
  lazy val CVMapping292All = crossMapped("2.10.0" -> "2.10.0-RC5")
//  lazy val CVMapping292All = crossMapped("2.9.0" -> "2.9.2", "2.9.0-1" -> "2.9.2", "2.9.1" -> "2.9.2", "2.9.1-1" -> "2.9.2", "2.10.0" -> "2.10.0-RC5")
  // lazy val CVMapping292All = crossMapped("2.9.0" -> "2.9.2", "2.9.0-1" -> "2.9.2", "2.9.1" -> "2.9.2", "2.9.1-1" -> "2.9.2")
  lazy val CVMappingAll  = crossMapped("2.9.2" -> "2.9.1", "2.9.1-1" -> "2.9.1", "2.8.2" -> "2.8.1")

  lazy val slf4jVersion = "1.7.2"

//  lazy val scalazGroup       = defaultOrMapped("org.scalaz", "2.8.0" -> "com.googlecode.scalaz")
//  lazy val scalazVersion     = defaultOrMapped("7.0.0-M6", "2.8.0" -> "5.0")
//  lazy val scalacheckGroup   = defaultOrMapped("org.scalacheck", "2.8.0" -> "org.scala-tools.testing")
//  lazy val scalacheckVersion = defaultOrMapped("1.10.0", "2.8.0" -> "1.7", "2.8.1" -> "1.8", "2.8.2" -> "1.8")
//  lazy val specsVersion      = defaultOrMapped("1.13", "2.8.0" -> "1.5", "2.8.1" -> "1.5", "2.8.2" -> "1.5", "2.9.0" -> "1.7.1", "2.9.0-1" -> "1.8.2")
  /* stop stealing */

  lazy val scalaz_core = "org.scalaz" %% "scalaz-core" % "7.0.0-M7"

  val jodaTime = Seq("joda-time" % "joda-time" % "2.1", "org.joda" % "joda-convert" % "1.2")

  lazy val scalacheck =  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"

  lazy val specs = "org.specs2" %% "specs2"      % "1.13"  % "test"

  val jackson = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.2")

  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.5.2"

  lazy val scalap: ModuleMap      = "org.scala-lang"             % "scalap"             % _

  val commonsCodec = "commons-codec"              % "commons-codec"      % "1.7"

  val mockito = "org.mockito"                 % "mockito-all"              % "1.9.5"      % "test"

//  val liftCommon = "net.liftweb" %% "lift-common" % "2.4" cross CVMapping2911
//
//  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.2" cross CVMappingAll
}
