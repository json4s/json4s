import sbt._
import Keys._

object Dependencies {

  /* Steal from lift build */
  def crossMapped(mappings: (String, String)*): CrossVersion =
    CrossVersion.binaryMapped(Map(mappings: _*) orElse { case v => v })

  def defaultOrMapped(default: String, alternatives: (String, String)*): String => String =
    Map(alternatives: _*) orElse { case _ => default }

  type ModuleMap = String => ModuleID

  /* stop stealing */

  lazy val scalaz_core = "org.scalaz" %% "scalaz-core" % "7.0.2"

  val jodaTime = Seq("joda-time" % "joda-time" % "2.1", "org.joda" % "joda-convert" % "1.2")

  lazy val scalacheck =  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"

  lazy val specs = "org.specs2" %% "specs2"      % "1.14"  % "test"

  val jackson = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2")

  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.5.6"

  lazy val scalap: ModuleMap      = "org.scala-lang"             % "scalap"             % _

  val commonsCodec = "commons-codec"              % "commons-codec"      % "1.7"

  val mockito = "org.mockito"                 % "mockito-all"              % "1.9.5"      % "test"

}