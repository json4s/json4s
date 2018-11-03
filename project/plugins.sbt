addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.3.0")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.9.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "2.3")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.1.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.3.4")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

