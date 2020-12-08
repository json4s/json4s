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
trait BigDecimalMode { self: Implicits =>

  implicit def double2jvalue(x: Double): JValue = JDecimal(x)
  implicit def float2jvalue(x: Float): JValue = JDecimal(x.toDouble)
  implicit def bigdecimal2jvalue(x: BigDecimal): JValue = JDecimal(x)

}
object BigDecimalMode extends Implicits with BigDecimalMode
trait DoubleMode { self: Implicits =>
  implicit def double2jvalue(x: Double): JValue = JDouble(x)
  implicit def float2jvalue(x: Float): JValue = JDouble(x.toDouble)
  implicit def bigdecimal2jvalue(x: BigDecimal): JValue = JDouble(x.doubleValue)

}
object DoubleMode extends Implicits with DoubleMode
trait Implicits {
  implicit def short2jvalue(x: Short): JValue = JInt(x: Int)
  implicit def byte2jvalue(x: Byte): JValue = JInt(x: Int)
  implicit def char2jvalue(x: Char): JValue = JInt(x: Int)
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

  implicit def seq2jvalue[A](s: Iterable[A])(implicit ev: A => JValue): JArray =
    JArray(s.toList.map(ev))

  implicit def map2jvalue[A](m: Map[String, A])(implicit ev: A => JValue): JObject =
    JObject(m.toList.map { case (k, v) => JField(k, ev(v)) })

  implicit def option2jvalue[A](opt: Option[A])(implicit ev: A => JValue): JValue = opt match {
    case Some(x) => ev(x)
    case None => JNothing
  }

  implicit def symbol2jvalue(x: Symbol): JString = JString(x.name)
  implicit def pair2jvalue[A](t: (String, A))(implicit ev: A => JValue): JObject = JObject(List(JField(t._1, ev(t._2))))
  implicit def list2jvalue(l: List[JField]): JObject = JObject(l)
  implicit def jobject2assoc(o: JObject): JsonListAssoc = new JsonListAssoc(o.obj)
  implicit def pair2Assoc[A](t: (String, A))(implicit ev: A => JValue): JsonAssoc[A] = new JsonAssoc(t)
}

final class JsonAssoc[A](private val left: (String, A)) extends AnyVal {
  def ~[B](right: (String, B))(implicit ev1: A => JValue, ev2: B => JValue): JObject = {
    val l: JValue = ev1(left._2)
    val r: JValue = ev2(right._2)
    JObject(JField(left._1, l) :: JField(right._1, r) :: Nil)
  }

  def ~(right: JObject)(implicit ev: A => JValue): JObject = {
    val l: JValue = ev(left._2)
    JObject(JField(left._1, l) :: right.obj)
  }
  def ~~[B](right: (String, B))(implicit ev1: A => JValue, ev2: B => JValue): JObject = this.~(right)
  def ~~(right: JObject)(implicit ev: A => JValue): JObject = this.~(right)
}

final class JsonListAssoc(private val left: List[JField]) extends AnyVal {
  def ~(right: (String, JValue)): JObject = JObject(left ::: List(JField(right._1, right._2)))
  def ~(right: JObject): JObject = JObject(left ::: right.obj)
  def ~~(right: (String, JValue)): JObject = this.~(right)
  def ~~(right: JObject): JObject = this.~(right)
}
