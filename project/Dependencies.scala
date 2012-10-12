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
  lazy val CVMappingAll  = crossMapped("2.9.2" -> "2.9.1", "2.9.1-1" -> "2.9.1", "2.8.2" -> "2.8.1")

  lazy val slf4jVersion = "1.6.4"

  lazy val scalazGroup       = defaultOrMapped("org.scalaz", "2.8.0" -> "com.googlecode.scalaz")
  lazy val scalazVersion     = defaultOrMapped("6.0.4", "2.8.0" -> "5.0", "2.9.0" -> "6.0.RC2")
  lazy val scalacheckGroup   = defaultOrMapped("org.scalacheck", "2.8.0" -> "org.scala-tools.testing")
  lazy val scalacheckVersion = defaultOrMapped("1.10.0", "2.8.0" -> "1.7", "2.8.1" -> "1.8", "2.8.2" -> "1.8")
  lazy val specsVersion      = defaultOrMapped("1.6.8", "2.8.0" -> "1.6.5", "2.9.1" -> "1.6.9", "2.9.1-1" -> "1.6.9", "2.9.2" -> "1.6.9")
  /* stop stealing */

  lazy val scalaz_core: ModuleMap = sv => scalazGroup(sv)        % "scalaz-core"        % scalazVersion(sv) cross crossMapped("2.8.2" -> "2.8.1", "2.9.1-1" -> "2.9.1")

  val jodaTime = Seq("joda-time" % "joda-time" % "2.1", "org.joda" % "joda-convert" % "1.2")

  lazy val scalacheck: ModuleMap =  sv => scalacheckGroup(sv) % "scalacheck" % scalacheckVersion(sv) % "test" cross CVMappingAll

  lazy val specs: ModuleMap      = "org.scala-tools.testing" % "specs"      % specsVersion(_)      % "test" cross CVMappingAll

  val jackson = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.5")

  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.4.1"

  lazy val scalap: ModuleMap      = "org.scala-lang"             % "scalap"             % _

  val commonsCodec = "commons-codec"              % "commons-codec"      % "1.6"

  val mockito = "org.mockito"                 % "mockito-all"              % "1.8.5"      % "test"

  val liftCommon = "net.liftweb" %% "lift-common" % "2.4" cross CVMapping2911

  val inflector = "io.backchat.inflector" %% "scala-inflector" % "1.3.5"

  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.2" cross CVMappingAll
}