package org.json4s

import org.specs2.mutable.Specification
import org.scalacheck._
import org.scalacheck.Prop.forAllNoShrink
import org.specs2.ScalaCheck
import org.specs2.matcher.MatchResult

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
  }

}
