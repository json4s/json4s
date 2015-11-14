import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys._

object MimaSettings {

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    previousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 11 =>
          Set("3.3.0").map{ v =>
            organization.value % s"${name.value}_${scalaBinaryVersion.value}" % v
          }
        case _ =>
          Set.empty
      }
    }
  )

}
