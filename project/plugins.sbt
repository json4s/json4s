addSbtPlugin("ch.epfl.lamp"     % "sbt-dotty"            % "0.5.3")
addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.8.1")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.10.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.9.6")
addSbtPlugin("com.github.sbt"   % "sbt-pgp"              % "2.1.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.5.2")
addSbtPlugin("com.github.sbt"   % "sbt-release"          % "1.0.15")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

