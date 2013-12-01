import sbt._
import Keys._

object Dependencies {

  /* Steal from lift build */
  def crossMapped(mappings: (String, String)*): CrossVersion =
    CrossVersion.binaryMapped(Map(mappings: _*) orElse { case v => v })

  def defaultOrMapped(default: String, alternatives: (String, String)*): String => String =
    Map(alternatives: _*).withDefaultValue(default)

  type ModuleMap = String => ModuleID

  lazy val CVMapping292  = crossMapped("2.9.2" -> "2.9.1")
  lazy val CVMapping2911 = crossMapped("2.9.2" -> "2.9.1", "2.9.1-1" -> "2.9.1", "2.9.3" -> "2.9.1")
  lazy val CVMappingAll  = crossMapped("2.9.2" -> "2.9.1", "2.9.1-1" -> "2.9.1", "2.8.2" -> "2.8.1")

  lazy val slf4jVersion = "1.6.4"

  lazy val scalazGroup       = defaultOrMapped("org.scalaz", "2.8.0" -> "com.googlecode.scalaz")
  lazy val scalazVersion     = defaultOrMapped("6.0.4", "2.9.0" -> "6.0.RC2")
  lazy val specs2Version      = defaultOrMapped("1.12.4", "2.9.2" -> "1.12.4.1", "2.9.3" -> "1.12.4.1")
  /* stop stealing */

  lazy val scalaz_core: ModuleMap = sv => scalazGroup(sv)        % "scalaz-core"        % scalazVersion(sv) cross crossMapped("2.8.2" -> "2.8.1", "2.9.1-1" -> "2.9.1", "2.9.3" -> "2.9.2")

  val jodaTime = Seq("joda-time" % "joda-time" % "2.1", "org.joda" % "joda-convert" % "1.2")

  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"

  lazy val specs: ModuleMap      = "org.specs2" % "specs2" % specs2Version(_) % "test" cross crossMapped("2.9.0" -> "2.9.1", "2.9.0-1" -> "2.9.1")

  val jackson = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2")

  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.5.6"

  lazy val scalap: ModuleMap      = "org.scala-lang"             % "scalap"             % _

  val commonsCodec = "commons-codec"              % "commons-codec"      % "1.7"

  val mockito = "org.mockito"                 % "mockito-all"              % "1.9.5"      % "test"

  val liftCommon = "net.liftweb" %% "lift-common" % "2.4" cross CVMapping2911

//  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.2" cross CVMappingAll
}