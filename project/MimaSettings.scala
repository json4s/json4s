import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._
import sbtcrossproject.CrossPlugin.autoImport.crossProjectPlatform
import sbtcrossproject.JVMPlatform
import scalajscrossproject.JSPlatform
import scalanativecrossproject.NativePlatform

object MimaSettings {

  val previousVersions = Set[Int]().map(patch => s"4.1.$patch")

  val mimaSettings = Def.settings(
    MimaPlugin.globalSettings,
    MimaPlugin.buildSettings,
    MimaPlugin.projectSettings,
    mimaPreviousArtifacts := {
      val platform = (crossProjectPlatform.?.value: @unchecked) match {
        case None | Some(JVMPlatform) => ""
        case Some(JSPlatform) => "_sjs1"
        case Some(NativePlatform) => "_native0.4"
      }
      previousVersions.map {
        organization.value % s"${name.value}${platform}_${scalaBinaryVersion.value}" % _
      }
    },
    Test / test := {
      mimaReportBinaryIssues.value
      (Test / test).value
    }
  )

}
