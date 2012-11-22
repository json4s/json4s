import sbt._
import Keys._

object Dependencies {

  /* Steal from lift build */
  def crossMapped(mappings: (String, String)*): CrossVersion =
    CrossVersion.binaryMapped(Map(mappings: _*) orElse { case v => v })

  def defaultOrMapped(default: String, alternatives: (String, String)*): String => String =
    Map(alternatives: _*) orElse { case _ => default }

  type ModuleMap = String => ModuleID

  lazy val CVMapping282  = crossMapped("2.8.2" -> "2.8.1")
  lazy val CVMapping292  = crossMapped("2.9.2" -> "2.9.1")
  lazy val CVMapping2911 = crossMapped("2.9.2" -> "2.9.1", "2.9.1-1" -> "2.9.1")
  lazy val CVMapping292All = crossMapped("2.9.0" -> "2.9.2", "2.9.0-1" -> "2.9.2", "2.9.1" -> "2.9.2", "2.9.1-1" -> "2.9.2", "2.10.0-RC3" -> "2.10.0-RC2")
  // lazy val CVMapping292All = crossMapped("2.9.0" -> "2.9.2", "2.9.0-1" -> "2.9.2", "2.9.1" -> "2.9.2", "2.9.1-1" -> "2.9.2")
  lazy val CVMappingAll  = crossMapped("2.9.2" -> "2.9.1", "2.9.1-1" -> "2.9.1", "2.8.2" -> "2.8.1")

  lazy val slf4jVersion = "1.6.4"

  lazy val scalazGroup       = defaultOrMapped("org.scalaz", "2.8.0" -> "com.googlecode.scalaz")
  lazy val scalazVersion     = defaultOrMapped("7.0.0-M6", "2.8.0" -> "5.0")
  lazy val scalacheckGroup   = defaultOrMapped("org.scalacheck", "2.8.0" -> "org.scala-tools.testing")
  lazy val scalacheckVersion = defaultOrMapped("1.10.0", "2.8.0" -> "1.7", "2.8.1" -> "1.8", "2.8.2" -> "1.8")
  lazy val specsVersion      = defaultOrMapped("1.12.3", "2.8.0" -> "1.5", "2.8.1" -> "1.5", "2.8.2" -> "1.5", "2.9.0" -> "1.7.1", "2.9.0-1" -> "1.8.2")
  /* stop stealing */

  lazy val scalaz_core: ModuleMap = sv => scalazGroup(sv) % "scalaz-core" % scalazVersion(sv) cross CVMapping292All

  val jodaTime = Seq("joda-time" % "joda-time" % "2.1", "org.joda" % "joda-convert" % "1.2")

  lazy val scalacheck: ModuleMap =  sv => scalacheckGroup(sv) % "scalacheck" % scalacheckVersion(sv) % "test" cross crossMapped("2.8.2" -> "2.8.1", "2.10.0-RC3" -> "2.10.0-RC2")

  lazy val specs: ModuleMap      = "org.specs2" % "specs2"      % specsVersion(_)      % "test" cross CrossVersion.full

  val jackson = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.5")

  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.4.1"

  lazy val scalap: ModuleMap      = "org.scala-lang"             % "scalap"             % _

  val commonsCodec = "commons-codec"              % "commons-codec"      % "1.6"

  val mockito = "org.mockito"                 % "mockito-all"              % "1.8.5"      % "test"

  val liftCommon = "net.liftweb" %% "lift-common" % "2.4" cross CVMapping2911

  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.2" cross CVMappingAll
}
