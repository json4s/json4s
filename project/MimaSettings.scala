import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._

object MimaSettings {

  val previousVersions = Set[Int]().map(patch => s"3.7.$patch")

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    mimaPreviousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) =>
          (previousVersions -- (0 to 5).map("3.6." + _)).map {
            organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _
          }
        case Some((2, scalaMajor)) if scalaMajor <= 12 =>
          previousVersions.map {
            organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _
          }
        case _ => Set.empty
      }
    },
    test in Test := {
      mimaReportBinaryIssues.value
      (test in Test).value
    }
  )

}
