// Don't update to mima 0.8.0
// https://github.com/lightbend/mima/commit/c2d1c317ff9330a1177
addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.7.0")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.10.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.9.4")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "2.0.1")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.5.1")
addSbtPlugin("com.github.gseitz" % "sbt-release"         % "1.0.13")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

