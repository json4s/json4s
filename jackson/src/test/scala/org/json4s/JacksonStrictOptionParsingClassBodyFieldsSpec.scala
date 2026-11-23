package org.json4s

class JacksonStrictOptionParsingClassBodyFieldsSpec
  extends StrictOptionParsingClassBodyFieldsSpec[JValue]("Jackson")
  with jackson.JsonMethods
