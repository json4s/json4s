import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._

object MimaSettings {

  val previousVersions = Set[Int]().map(patch => s"3.7.$patch")

  val mimaSettings = Def.settings(
    MimaPlugin.globalSettings,
    MimaPlugin.buildSettings,
    MimaPlugin.projectSettings,
    mimaPreviousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 12 =>
          previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ }
        case _ => Set.empty
      }
    },
    (Test / test) := {
      mimaReportBinaryIssues.value
      (Test / test).value
    }
  )

}
