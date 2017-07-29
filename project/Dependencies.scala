import sbt._

object Dependencies {

  type ModuleMap = String => ModuleID

  val jodaTime = Seq("joda-time" % "joda-time" % "2.3", "org.joda" % "joda-convert" % "1.6")

  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.6" % "test"

  lazy val specs = "org.specs2" %% "specs2-scalacheck" % "2.4.17" % "test"

  val jackson = Seq("com.fasterxml.jackson.core" % "jackson-databind" % "2.3.1")

  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.6"

  lazy val scalap: ModuleMap      = "org.scala-lang"             % "scalap"             % _
}
