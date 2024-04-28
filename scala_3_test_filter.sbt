val excludeTests = Set(
  "native.LazyValBugs",
).map("org.json4s." + _)

ThisBuild / Test / testOptions ++= {
  if (scalaBinaryVersion.value == "3") {
    Seq(Tests.Exclude(excludeTests))
  } else {
    Nil
  }
}
