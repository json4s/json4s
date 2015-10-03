import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._

object MimaSettings {

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    previousArtifact := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 11 =>
          // NOTE: when we start 3.4.0 release, enable this mima setting
          // previousArtifact must always be the previous version (e.g.) verify 3.4.2 for 3.4.3 build
          // Some(organization.value % s"${name.value}_${scalaBinaryVersion.value}" % "3.4.0")
          None
        case _ =>
          None
      }
    }
  )

}
