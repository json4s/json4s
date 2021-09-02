package org.json4s

trait JValueParent { self: JValue.type =>
  type ValuesType[A <: JValue] = A match {
    case JNothing.type => None.type
    case JNull.type => Null
    case JString => String
    case JDouble => Double
    case JDecimal => BigDecimal
    case JLong => Long
    case JInt => Int
    case JBool => Boolean
    case JObject => Map[String, Any]
    case JArray => List[Any]
    case JSet => Set[JValue]
  }
}
