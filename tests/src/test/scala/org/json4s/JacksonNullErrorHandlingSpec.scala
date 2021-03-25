package org.json4s

class JacksonNullErrorHandlingSpec extends NullErrorHandlingSpec[JValue]("Jackson") with jackson.JsonMethods
