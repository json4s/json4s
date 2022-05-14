import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._
import sbtcrossproject.CrossPlugin.autoImport.crossProjectPlatform
import sbtcrossproject.JVMPlatform
import scalajscrossproject.JSPlatform
import scalanativecrossproject.NativePlatform

object MimaSettings {

  val previousVersions = settingKey[Seq[String]]("")

  val mimaSettings = Def.settings(
    MimaPlugin.globalSettings,
    MimaPlugin.buildSettings,
    MimaPlugin.projectSettings,
    previousVersions := (0 to 5).map(patch => s"4.0.$patch"),
    mimaPreviousArtifacts := {
      val platform = (crossProjectPlatform.?.value: @unchecked) match {
        case None | Some(JVMPlatform) => ""
        case Some(JSPlatform) => "_sjs1"
        case Some(NativePlatform) => "_native0.4"
      }
      previousVersions.value.map {
        organization.value % s"${name.value}${platform}_${scalaBinaryVersion.value}" % _
      }.toSet
    },
    Test / test := {
      mimaReportBinaryIssues.value
      (Test / test).value
    }
  )

}
