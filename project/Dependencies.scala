import sbt._
import Keys._

object Dependencies {

  lazy val jodaTime     = Seq(
    "joda-time" % "joda-time"    % "2.9.5",
    "org.joda"  % "joda-convert" % "1.8.1"
  )
  lazy val jackson      = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.4"
  )
  lazy val scalaz_core  = "org.scalaz"                   %% "scalaz-core"          % "7.2.10"
  lazy val paranamer    = "com.thoughtworks.paranamer"   %  "paranamer"            % "2.8"
  lazy val commonsCodec = "commons-codec"                %  "commons-codec"        % "1.9"
  lazy val specs        = Def.setting{
    Seq("org.specs2" %% "specs2-scalacheck" % "3.8.6" % "test")
  }
  lazy val mockito      = "org.mockito"                  %  "mockito-all"          % "1.10.19" % "test"

  def scalaXml(scalaVersion: String) = {
    PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion)){
      case Some((2, scalaMajor)) if scalaMajor >= 11 =>
        Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.6")
    }.toList.flatten
  }

}
