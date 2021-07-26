val excludeTests = Set(
  "CustomTypeHintFieldNameExample",
  "FieldSerializerBugs",
  "FieldSerializerExamples",
  "FullTypeHintExamples",
  "JacksonEitherTest",
  "JacksonExtractionBugs",
  "JacksonExtractionExamples",
  "JacksonIgnoreCompanionCtorSpec",
  "JacksonJsonFormatsSpec",
  "JacksonLottoExample",
  "JacksonRichSerializerTest",
  "JacksonStrictOptionParsingModeSpec",
  "MappedHintExamples",
  "NativeEitherTest",
  "NativeExtractionBugs",
  "NativeExtractionExamples",
  "NativeIgnoreCompanionCtorSpec",
  "NativeJsonFormatsSpec",
  "NativeLottoExample",
  "NativeRichSerializerTest",
  "NativeStrictOptionParsingModeSpec",
  "SerializationBugs",
  "SerializationExamples",
  "ShortTypeHintExamples",
  "jackson.JacksonSerializationSpec",
  "native.LazyValBugs",
  "native.NativeSerializationSpec",
  "reflect.ReflectorSpec",
).map("org.json4s." + _)

ThisBuild / Test / testOptions ++= {
  if (scalaBinaryVersion.value == "3") {
    Seq(Tests.Exclude(excludeTests))
  } else {
    Nil
  }
}
