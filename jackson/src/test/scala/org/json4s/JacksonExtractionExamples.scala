package org.json4s

class JacksonExtractionExamples
    extends ExtractionExamples[JValue]("Jackson", jackson.Serialization)
    with jackson.JsonMethods
