import sbt._
import Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  lazy val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.1" % "test"

  lazy val jodaTime = Seq(
    "joda-time" % "joda-time" % "2.10.10",
    "org.joda" % "joda-convert" % "2.2.1"
  )
  lazy val jackson = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.4"
  )
  lazy val scalaz_core = Def.setting(
    "org.scalaz" %%% "scalaz-core" % "7.3.4" cross CrossVersion.for3Use2_13
  )
  lazy val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.8"
  lazy val scalatest = Def.setting(
    Seq("org.scalatest" %%% "scalatest-wordspec" % "3.2.10" % "test")
  )
  lazy val scalatestScalacheck = Def.setting(
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) =>
        Seq("org.scalatestplus" %%% "scalacheck-1-15" % "3.2.4.0-M1" % "test")
      case _ =>
        Seq("org.scalatestplus" %%% "scalacheck-1-15" % "3.2.9.0" % "test")
    }
  )

  lazy val scalaXml = Def.setting {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) =>
        "org.scala-lang.modules" %% "scala-xml" % "1.3.0"
      case _ =>
        "org.scala-lang.modules" %%% "scala-xml" % "2.0.1"
    }
  }
}
