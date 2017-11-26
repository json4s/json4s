import sbt._
import Keys._

object Dependencies {

  lazy val jodaTime     = Seq(
    "joda-time" % "joda-time"    % "2.9.9",
    "org.joda"  % "joda-convert" % "1.8.2"
  )
  lazy val jackson      = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.0"
  )
  lazy val scalaz_core  = "org.scalaz"                   %% "scalaz-core"          % "7.2.14"
  lazy val paranamer    = "com.thoughtworks.paranamer"   %  "paranamer"            % "2.8"
  lazy val specs        = Def.setting{
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 10 =>
        Seq("org.specs2" %% "specs2-scalacheck" % "3.9.4" % "test")
      case _ =>
        Seq("org.specs2" %% "specs2-scalacheck" % "4.0.1" % "test")
    }
  }
  lazy val mockito      = "org.mockito"                  %  "mockito-core"         % "2.8.47" % "test"

  def scalaXml(scalaVersion: String) = {
    PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion)){
      case Some((2, scalaMajor)) if scalaMajor >= 11 =>
        Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.6")
    }.toList.flatten
  }

}
