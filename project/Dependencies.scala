import sbt._
import Keys._

object Dependencies {
  lazy val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.1" % "test"

  lazy val jodaTime     = Seq(
    "joda-time" % "joda-time"    % "2.10.14",
    "org.joda"  % "joda-convert" % "2.2.2"
  )
  lazy val jackson      = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.10.8"
  )
  lazy val scalaz_core  = "org.scalaz"                   %% "scalaz-core"          % "7.2.30"
  lazy val paranamer    = "com.thoughtworks.paranamer"   %  "paranamer"            % "2.8"
  lazy val specs        = Def.setting{
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 10 =>
        Seq("org.specs2" %% "specs2-scalacheck" % "3.10.0" % "test")
      case _ =>
        Seq("org.specs2" %% "specs2-scalacheck" % "4.10.6" % "test")
    }
  }
  lazy val mockito      = "org.mockito"                  %  "mockito-core"         % "4.11.0" % "test"

  def scalaXml(scalaVersion: String) = {
    PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion)){
      case Some((2, scalaMajor)) if scalaMajor >= 13 =>
        Seq("org.scala-lang.modules" %% "scala-xml" % "1.3.0")
      case Some((2, scalaMajor)) if scalaMajor >= 11 =>
        Seq("org.scala-lang.modules" %% "scala-xml" % "1.1.1")
    }.toList.flatten
  }

}
