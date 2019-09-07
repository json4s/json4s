addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.6.0")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.9.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.6")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.1.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.4.2")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

