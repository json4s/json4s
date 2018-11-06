import sbt._
import Keys._

object Dependencies {
  lazy val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.1" % "test"

  lazy val jodaTime     = Seq(
    "joda-time" % "joda-time"    % "2.10.1",
    "org.joda"  % "joda-convert" % "2.1.2"
  )
  lazy val jackson      = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.7"
  )
  lazy val scalaz_core  = "org.scalaz"                   %% "scalaz-core"          % "7.2.27"
  lazy val paranamer    = "com.thoughtworks.paranamer"   %  "paranamer"            % "2.8"
  lazy val specs        = "org.specs2"                   %% "specs2-scalacheck"    % "4.3.5" % "test"
  lazy val mockito      = "org.mockito"                  %  "mockito-core"         % "2.23.0" % "test"

  lazy val scalaXml     = "org.scala-lang.modules"       %% "scala-xml"            % "1.1.1"
}
