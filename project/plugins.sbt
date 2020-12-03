addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.8.1")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.10.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.9.5")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "2.1.1")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.5.1")
addSbtPlugin("com.github.gseitz" % "sbt-release"         % "1.0.13")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

