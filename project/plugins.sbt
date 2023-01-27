// Don't update to mima 0.8.0
// https://github.com/lightbend/mima/commit/c2d1c317ff9330a1177
addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.7.0")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.11.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.9.16")
addSbtPlugin("com.github.sbt"   % "sbt-pgp"              % "2.2.1")
addSbtPlugin("com.github.sbt"   % "sbt-release"          % "1.1.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

