addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.1.18")
addSbtPlugin("com.typesafe.sbt" % "sbt-javaversioncheck" % "0.1.0")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.7.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-start-script"     % "0.10.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "2.0")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.1.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.3.1")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

