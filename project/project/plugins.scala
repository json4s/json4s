import sbt._
import Keys._

object Json4sPluginsBuild extends Build {

  lazy val root = (Project("plugins", file("."))
                    dependsOn uri("git://github.com/sbt/xsbt-gpg-plugin.git#sbt-0.12"))
}
