import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.*

object Dependencies {
  lazy val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.1" % "test"

  lazy val jodaTime = Seq(
    "joda-time" % "joda-time" % "2.14.0",
    "org.joda" % "joda-convert" % "3.0.1"
  )
  lazy val jackson = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.20.1"
  )
  lazy val scalaz_core = Def.setting(
    "org.scalaz" %%% "scalaz-core" % "7.3.8"
  )
  lazy val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.8.3"
  lazy val scalatest = Def.setting(
    Seq("org.scalatest" %%% "scalatest-wordspec" % "3.2.19" % "test")
  )
  lazy val scalatestScalacheck = Def.setting(
    Seq("org.scalatestplus" %%% "scalacheck-1-18" % "3.2.19.0" % "test")
  )

  lazy val scalaXml = Def.setting {
    "org.scala-lang.modules" %%% "scala-xml" % "2.4.0"
  }
}
