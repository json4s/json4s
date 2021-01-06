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

object JsonAST {

  /**
   * Concatenates a sequence of <code>JValue</code>s.
   * <p>
   * Example:<pre>
   * concat(JInt(1), JInt(2)) == JArray(List(JInt(1), JInt(2)))
   * </pre>
   */
  def concat(xs: JValue*): JValue = xs.foldLeft(JNothing: JValue)(_ ++ _)

  object JValue extends Merge.Mergeable

  /**
   * Data type for JSON AST.
   */
  sealed abstract class JValue extends Diff.Diffable with Product with Serializable {
    type Values


    /**
     * Return unboxed values from JSON
     * <p>
     * Example:<pre>
     * JObject(JField("name", JString("joe")) :: Nil).values == Map("name" -> "joe")
     * </pre>
     */
    def values: Values

    /**
     * Return direct child elements.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil).children == List(JInt(1), JInt(2))
     * </pre>
     */
    def children: List[JValue] = this match {
      case JObject(l) => l map (_._2)
      case JArray(l) => l
      case _ => Nil
    }


    /**
     * Return nth element from JSON.
     * Meaningful only to JArray, JObject and JField. Returns JNothing for other types.
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil)(1) == JInt(2)
     * </pre>
     */
    def apply(i: Int): JValue = JNothing


    /**
     * Concatenate with another JSON.
     * This is a concatenation monoid: (JValue, ++, JNothing)
     * <p>
     * Example:<pre>
     * JArray(JInt(1) :: JInt(2) :: Nil) ++ JArray(JInt(3) :: Nil) ==
     * JArray(List(JInt(1), JInt(2), JInt(3)))
     * </pre>
     */
    def ++(other: JValue) = {
      def append(value1: JValue, value2: JValue): JValue = (value1, value2) match {
        case (JNothing, x) => x
        case (x, JNothing) => x
        case (JArray(xs), JArray(ys)) => JArray(xs ::: ys)
        case (JArray(xs), v: JValue) => JArray(xs ::: List(v))
        case (v: JValue, JArray(xs)) => JArray(v :: xs)
        case (x, y) => JArray(x :: y :: Nil)
      }
      append(this, other)
    }

    /**
     * When this [[org.json4s.JsonAST.JValue]] is a [[org.json4s.JsonAST.JNothing]] or a [[org.json4s.JsonAST.JNull]], this method returns [[scala.None]]
     * When it has a value it will return [[scala.Some]]
     */
    def toOption: Option[JValue] = this match {
      case JNothing | JNull => None
      case json => Some(json)
    }

    /**
     * When this [[org.json4s.JsonAST.JValue]] is a [[org.json4s.JsonAST.JNothing]], this method returns [[scala.None]]
     * When it has a value it will return [[scala.Some]]
     */
    def toSome: Option[JValue] = this match {
      case JNothing => None
      case json => Some(json)
    }
  }

  case object JNothing extends JValue {
    type Values = None.type
    def values = None
  }
  case object JNull extends JValue {
    type Values = Null
    def values = null
  }
  case class JString(s: String) extends JValue {
    type Values = String
    def values = s
  }
  trait JNumber
  case class JDouble(num: Double) extends JValue with JNumber {
    type Values = Double
    def values = num
  }
  case class JDecimal(num: BigDecimal) extends JValue with JNumber {
    type Values = BigDecimal
    def values = num
  }
  case class JLong(num: Long) extends JValue with JNumber {
    type Values = Long
    def values = num
  }
  case class JInt(num: BigInt) extends JValue with JNumber {
    type Values = BigInt
    def values = num
  }
  case class JBool(value: Boolean) extends JValue {
    type Values = Boolean
    def values = value
  }
  object JBool {
    def apply(value: Boolean): JBool = if (value) True else False
    val True = new JBool(true)
    val False = new JBool(false)
  }

  case class JObject(obj: List[JField]) extends JValue {
    type Values = Map[String, Any]
    def values: Map[String,Any] = obj.iterator.map { case (n, v) => (n, v.values) }.toMap

    override def equals(that: Any): Boolean = that match {
      case o: JObject => obj.toSet == o.obj.toSet
      case _ => false
    }

    override def hashCode: Int = obj.toSet[JField].hashCode
  }
  case object JObject {
    def apply(fs: JField*): JObject = JObject(fs.toList)
  }

  case class JArray(arr: List[JValue]) extends JValue {
    type Values = List[Any]
    def values: Values = arr.map(_.values)
    override def apply(i: Int): JValue = arr(i)
  }

  // JSet is set implementation for JValue.
  // It supports basic set operations, like intersection, union and difference.
  case class JSet(set: Set[JValue]) extends JValue {
    type Values = Set[JValue]
    def values = set

    override def equals(o: Any): Boolean = o match {
      case o: JSet => o.values == values
      case _ => false
    }

    def intersect(o: JSet): JSet = JSet(o.values.intersect(values))
    def union(o: JSet): JSet = JSet(o.values.union(values))
    def difference(o: JSet): JSet = JSet(values.diff(o.values))

  }

  type JField = (String, JValue)
  object JField {
    def apply(name: String, value: JValue): (String, JValue) = (name, value)

    final class OptionStringJValue(val get: JField) extends AnyVal {
      def isEmpty: Boolean = false
    }
    def unapply(f: JField): OptionStringJValue = new OptionStringJValue(f)
  }
}

