import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._

object MimaSettings {

  val previousVersions = Set(0, 1, 2).map(patch => s"3.5.$patch")

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    mimaPreviousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 12 =>
          previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ }
        case _ =>
          Set(2).map(patch => s"3.5.$patch").map {
            organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _
          }
      }
    }
  )

}
