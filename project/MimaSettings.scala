import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaKeys._

object MimaSettings {

  val previousVersions = (0 to 11).toSet[Int].map(patch => s"3.6.$patch")

  val mimaSettings = Seq(
    ThisBuild / mimaReportSignatureProblems := true,
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
    (Test / test) := {
      mimaReportBinaryIssues.value
      (Test / test).value
    }
  )

}
