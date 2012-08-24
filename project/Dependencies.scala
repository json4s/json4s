import sbt._
import Keys._

object Dependencies {
  val scalaz = "org.scalaz" %% "scalaz-core" % "6.0.4"

  val jodaTime = Seq("joda-time" % "joda-time" % "2.1", "org.joda" % "joda-convert" % "1.2")

  val specs2 = "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"

  val jackson = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.5",
      "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.0.2")

  val scalaCheck = "org.scalacheck" % "scalacheck_2.9.2" % "1.10.0" % "test"

  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.4.1"

  val scalap = "org.scala-lang"             % "scalap"             % "2.9.2"

  val commonsCodec = "commons-codec"              % "commons-codec"      % "1.6"

  val mockito = "org.mockito"                 % "mockito-all"              % "1.8.5"      % "test"

  val liftCommon = "net.liftweb" %% "lift-common" % "2.5-SNAPSHOT"
}