conflictWarning in ThisBuild := {
  if (scalaBinaryVersion.value == "2.13") {
    ConflictWarning.disable
  } else {
    (conflictWarning in ThisBuild).value
  }
}
