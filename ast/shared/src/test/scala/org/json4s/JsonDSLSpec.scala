package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.Checkers

class JsonDSLSpec extends AnyWordSpec with JValueGen with Checkers {

  "JSON DSL Specification" should {
    "build Json" in {
      import JsonDSL._
      check { (intValue: Int, strValue: String) =>
        (("intValue" -> intValue) ~ ("strValue" -> strValue)) == JObject(
          "intValue" -> JInt(intValue),
          ("strValue", JString(strValue))
        )
      }
    }

    "customize value type" in {
      object CustomJsonDSL extends JsonDSL with DoubleMode {
        override implicit def int2jvalue(x: Int): JValue = JString(x.toString)
      }
      import CustomJsonDSL._
      check { (intValue: Int, strValue: String) =>
        (("intValue" -> intValue) ~ ("strValue" -> strValue)) == JObject(
          ("intValue", JString(intValue.toString)),
          ("strValue", JString(strValue))
        )
      }
    }

    "short, byte and char to jvalue" in {
      import JsonDSL._
      check { (shortValue: Short, byteValue: Byte, charValue: Char) =>
        val dslValue = ("shortValue" -> shortValue) ~
          ("byteValue" -> byteValue) ~
          ("charValue" -> charValue) ~
          ("shortArray" -> List[Short](shortValue)) ~
          ("byteArray" -> List[Byte](byteValue)) ~
          ("charArray" -> List[Char](charValue))
        val expected = JObject(
          ("shortValue", JInt(shortValue)),
          ("byteValue", JInt(byteValue)),
          ("charValue", JInt(charValue)),
          ("shortArray", JArray(List(JInt(shortValue)))),
          ("byteArray", JArray(List(JInt(byteValue)))),
          ("charArray", JArray(List(JInt(charValue))))
        )
        dslValue == expected
      }
    }
  }

}
