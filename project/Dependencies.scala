import sbt._
import Keys._

object Dependencies {

  lazy val jodaTime     = Seq(
    "joda-time" % "joda-time"    % "2.9.2",
    "org.joda"  % "joda-convert" % "1.8.1"
  )
  lazy val jackson      = Seq(
    // TODO: 2.7
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5"
  )
  lazy val scalaz_core  = "org.scalaz"                   %% "scalaz-core"          % "7.2.2"
  lazy val paranamer    = "com.thoughtworks.paranamer"   %  "paranamer"            % "2.8"
  lazy val commonsCodec = "commons-codec"                %  "commons-codec"        % "1.9"
  lazy val specs        = Def.setting{
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor <= 11 => Seq(
        "org.specs2" %% "specs2-scalacheck" % "3.7.3" % "test"
      )
      case _ => Seq(
        "org.specs2" %% "specs2-scalacheck" % "3.7.3.1" % "test" exclude("org.scalacheck", "scalacheck_2.12.0-M3"),
        "org.scalacheck" %% "scalacheck" % "1.13.1" % "test"
      )
    }
  }
  lazy val mockito      = "org.mockito"                  %  "mockito-all"          % "1.10.19" % "test"

  def scalaXml(scalaVersion: String) = {
    PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion)){
      case Some((2, scalaMajor)) if scalaMajor >= 11 =>
        Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.5")
    }.toList.flatten
  }

}
