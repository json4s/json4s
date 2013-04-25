/*
 * Copyright 2009-2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json4s

import JsonAST._

/**
 * Basic implicit conversions from primitive types into JSON.
 * Example:<pre>
 * import org.json4s.Implicits._
 * JObject(JField("name", "joe") :: Nil) == JObject(JField("name", JString("joe")) :: Nil)
 * </pre>
 */
trait BigDecimalMode { self: Implicits ⇒

  implicit def double2jvalue(x: Double): JValue = JDecimal(x)
  implicit def float2jvalue(x: Float): JValue = JDecimal(x.toDouble)
  implicit def bigdecimal2jvalue(x: BigDecimal): JValue = JDecimal(x)

}
object BigDecimalMode extends Implicits with BigDecimalMode
trait DoubleMode { self: Implicits ⇒
  implicit def double2jvalue(x: Double): JValue = JDouble(x)
  implicit def float2jvalue(x: Float): JValue = JDouble(x.toDouble)
  implicit def bigdecimal2jvalue(x: BigDecimal): JValue = JDouble(x.doubleValue())

}
object DoubleMode extends Implicits with DoubleMode
trait Implicits {
  implicit def int2jvalue(x: Int): JValue = JInt(x)
  implicit def long2jvalue(x: Long): JValue = JInt(x)
  implicit def bigint2jvalue(x: BigInt): JValue = JInt(x)
  implicit def double2jvalue(x: Double): JValue
  implicit def float2jvalue(x: Float): JValue
  implicit def bigdecimal2jvalue(x: BigDecimal): JValue
  implicit def boolean2jvalue(x: Boolean): JValue = JBool(x)
  implicit def string2jvalue(x: String): JValue = JString(x)
}

/**
 * A DSL to produce valid JSON.
 * Example:<pre>
 * import org.json4s.JsonDSL._
 * ("name", "joe") ~ ("age", 15) == JObject(JField("name",JString("joe")) :: JField("age",JInt(15)) :: Nil)
 * </pre>
 */
object JsonDSL extends JsonDSL with DoubleMode {
  object WithDouble extends JsonDSL with DoubleMode
  object WithBigDecimal extends JsonDSL with BigDecimalMode
}
trait JsonDSL extends Implicits {

  implicit def seq2jvalue[A <% JValue](s: Traversable[A]) =
    JArray(s.toList.map { a ⇒ val v: JValue = a; v })

  implicit def map2jvalue[A <% JValue](m: Map[String, A]) =
    JObject(m.toList.map { case (k, v) ⇒ JField(k, v) })

  implicit def option2jvalue[A <% JValue](opt: Option[A]): JValue = opt match {
    case Some(x) ⇒ x
    case None ⇒ JNothing
  }

  implicit def symbol2jvalue(x: Symbol) = JString(x.name)
  implicit def pair2jvalue[A <% JValue](t: (String, A)) = JObject(List(JField(t._1, t._2)))
  implicit def list2jvalue(l: List[JField]) = JObject(l)
  implicit def jobject2assoc(o: JObject) = new JsonListAssoc(o.obj)
  implicit def pair2Assoc[A <% JValue](t: (String, A)) = new JsonAssoc(t)

  class JsonAssoc[A <% JValue](left: (String, A)) {
    def ~[B <% JValue](right: (String, B)) = {
      val l: JValue = left._2
      val r: JValue = right._2
      JObject(JField(left._1, l) :: JField(right._1, r) :: Nil)
    }

    def ~(right: JObject) = {
      val l: JValue = left._2
      JObject(JField(left._1, l) :: right.obj)
    }
  }

  class JsonListAssoc(left: List[JField]) {
    def ~(right: (String, JValue)) = JObject(left ::: List(JField(right._1, right._2)))
    def ~(right: JObject) = JObject(left ::: right.obj)
  }
}