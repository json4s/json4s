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
  lazy val scalaz_core  = "org.scalaz"                   %% "scalaz-core"          % "7.2.12"
  lazy val paranamer    = "com.thoughtworks.paranamer"   %  "paranamer"            % "2.8"
  lazy val specs        = Def.setting{
    // TODO enable if specs2 for Scala 2.13 released
    // https://github.com/etorreborre/specs2/issues/573
    PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
      case Some((2, v)) if v <= 12 =>
        Seq("org.specs2" %% "specs2-scalacheck" % "3.8.9" % "test")
    }.toList.flatten
  }
  lazy val mockito      = "org.mockito"                  %  "mockito-core"         % "2.7.22" % "test"

  def scalaXml(scalaVersion: String) = {
    PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion)){
      case Some((2, scalaMajor)) if scalaMajor >= 11 =>
        Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.6")
    }.toList.flatten
  }

}
