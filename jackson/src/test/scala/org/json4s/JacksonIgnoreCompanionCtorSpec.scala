package org.json4s

class JacksonIgnoreCompanionCtorSpec
    extends IgnoreCompanionConstructors[JValue]("Jackson", jackson.Serialization)
    with jackson.JsonMethods
