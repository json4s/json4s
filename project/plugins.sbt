addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.3.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-start-script"     % "0.10.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "0.5.0")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.0.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.1.9")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

