import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._

object MimaSettings {

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    previousArtifact := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 11 =>
          Some(organization.value % s"${name.value}_${scalaBinaryVersion.value}" % "3.3.0.RC4")
        case _ =>
          None
      }
    }
  )

}
