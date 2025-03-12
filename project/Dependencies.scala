import sbt._
import Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  lazy val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.1" % "test"

  lazy val jodaTime = Seq(
    "joda-time" % "joda-time" % "2.10.14",
    "org.joda" % "joda-convert" % "2.2.2"
  )
  lazy val jackson = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.7.2"
  )
  lazy val scalaz_core = Def.setting(
    "org.scalaz" %%% "scalaz-core" % "7.3.8"
  )
  lazy val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.8.3"
  private def scalatestVersion = "3.2.19"
  lazy val scalatest = Def.setting(
    Seq("org.scalatest" %%% "scalatest-wordspec" % scalatestVersion % "test")
  )
  lazy val scalatestScalacheck = Def.setting(
    Seq("org.scalatestplus" %%% "scalacheck-1-18" % s"${scalatestVersion}.0" % "test")
  )

  lazy val scalaXml = Def.setting {
    "org.scala-lang.modules" %%% "scala-xml" % "2.3.0"
  }
}
