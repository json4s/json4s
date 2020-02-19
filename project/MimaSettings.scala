import sbt._
import Keys._
import com.typesafe.tools.mima.core.{ProblemFilters, ReversedMissingMethodProblem}
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._

object MimaSettings {

  val previousVersions = (0 to 7).toSet[Int].map(patch => s"3.6.$patch")

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    mimaPreviousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) =>
          (previousVersions -- (0 to 5).map("3.6." + _)).map {
            organization.value %% name.value % _
          }
        case Some((2, 12)) =>
          // exclude some versions which build with Scala 2.12.7
          // maybe due to https://github.com/scala/scala/pull/7035 ???
          // https://github.com/json4s/json4s/blob/v3.6.2/project/build.scala#L61
          (previousVersions -- (0 to 2).map("3.6." + _)).map {
            organization.value %% name.value % _
          }
        case Some((2, scalaMajor)) if scalaMajor <= 11 =>
          previousVersions.map {
            organization.value %% name.value % _
          }
        case _ => Set.empty
      }
    },
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[ReversedMissingMethodProblem]("org.json4s.Formats.keyTransformation"),
      ProblemFilters.exclude[ReversedMissingMethodProblem]("org.json4s.Formats.withCamelSnakeTransformation"),
      ProblemFilters.exclude[ReversedMissingMethodProblem]("org.json4s.DefaultFormats.keyTransformation"),
      ProblemFilters.exclude[ReversedMissingMethodProblem]("org.json4s.DefaultFormats.org$json4s$DefaultFormats$_setter_$keyTransformation_=")
    ),
    test in Test := {
      mimaReportBinaryIssues.value
      (test in Test).value
    }
  )

}
