import sbt._
import Keys._

object Dependencies {

  lazy val jodaTime     = Seq(
    "joda-time" % "joda-time"    % "2.6", 
    "org.joda"  % "joda-convert" % "1.7"
  )
  lazy val jackson      = Seq(
    // TODO 2.5 has breaking API changes
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.3.4"
  )
  lazy val jacksonScala = "com.fasterxml.jackson.module" %  "jackson-module-scala" % "2.3.1" 
  lazy val liftCommon   = "net.liftweb"                  %% "lift-common"          % "2.5.1" 
  // TODO: 7.1 has breaking API changes
  lazy val scalaz_core  = "org.scalaz"                   %% "scalaz-core"          % "7.0.6"
  lazy val paranamer    = "com.thoughtworks.paranamer"   %  "paranamer"            % "2.7"
  lazy val commonsCodec = "commons-codec"                %  "commons-codec"        % "1.9"
  // TODO: combination of specs2 2.4 and scalacheck 1.11 has breaking API changes
  lazy val scalacheck   = "org.scalacheck"               %% "scalacheck"           % "1.10.1"    % "test"
  lazy val specs        = "org.specs2"                   %% "specs2"               % "2.3.13"    % "test"
  lazy val mockito      = "org.mockito"                  %  "mockito-all"          % "1.10.19"   % "test"

  def scalaXml(scalaVersion: String) = {
    if (scalaVersion.startsWith("2.11")) Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.3")
    else Nil
  }

}
