package org.json4s

import org.specs2.mutable.Specification
import org.specs2.ScalaCheck

object JsonDSLSpec extends Specification with JValueGen with ScalaCheck {

  ("JSON DSL Specification") should {
    "build Json" in {
      import JsonDSL._
      val buildProp = (intValue: Int, strValue: String) =>
        (("intValue" -> intValue) ~ ("strValue" -> strValue)) must_== JObject("intValue" -> JInt(intValue), ("strValue", JString(strValue)))
      prop(buildProp)
    }

    "customize value type" in {
      object CustomJsonDSL extends JsonDSL with DoubleMode {
        override implicit def int2jvalue(x: Int): JValue = JString(x.toString)
      }
      import CustomJsonDSL._
      val buildProp = (intValue: Int, strValue: String) =>
        (("intValue" -> intValue) ~ ("strValue" -> strValue)) must_== JObject(("intValue", JString(intValue.toString)), ("strValue", JString(strValue)))
      prop(buildProp)
    }

    "short, byte and char to jvalue" in {
      import JsonDSL._
      val buildProp = (shortValue: Short, byteValue: Byte, charValue: Char) => {
        val dslValue = (("shortValue" -> shortValue) ~
          ("byteValue" -> byteValue) ~
          ("charValue" -> charValue) ~
          ("shortArray" -> List[Short](shortValue)) ~
          ("byteArray" -> List[Byte](byteValue)) ~
          ("charArray" -> List[Char](charValue)))
        val expected = JObject(
          ("shortValue", JInt(shortValue)),
          ("byteValue", JInt(byteValue)),
          ("charValue", JInt(charValue)),
          ("shortArray", JArray(List(JInt(shortValue)))),
          ("byteArray", JArray(List(JInt(byteValue)))),
          ("charArray", JArray(List(JInt(charValue)))))
        dslValue must_== expected
      }
      prop(buildProp)
    }
  }

}
