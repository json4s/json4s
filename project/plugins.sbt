addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.6.1")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.9.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.8.1")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "2.0.1")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.5.0")
addSbtPlugin("com.github.gseitz" % "sbt-release"         % "1.0.13")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

