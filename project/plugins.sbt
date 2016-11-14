addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.1.11")
addSbtPlugin("com.typesafe.sbt" % "sbt-javaversioncheck" % "0.1.0")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.6.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-start-script"     % "0.10.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "1.0")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.0.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.1.10")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

