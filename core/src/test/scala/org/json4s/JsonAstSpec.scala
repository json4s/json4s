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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.Checkers
import org.scalacheck._
import org.scalacheck.Prop.forAllNoShrink

class JsonAstSpec extends AnyWordSpec with JValueGen with Checkers {

  "JSON AST Specification" should {
    "Functor identity" in check { (json: JValue) =>
      json == (json map identity)
    }

    "Monoid identity" in check { (json: JValue) =>
      ({ json ++ JNothing } == json) && ({ JNothing ++ json } == json)
    }

    "Monoid associativity" in check { (x: JValue, y: JValue, z: JValue) =>
      (x ++ (y ++ z)) == ((x ++ y) ++ z)
    }

    "Merge identity" in check { (json: JValue) =>
      ((json merge JNothing) == json) && ((JNothing merge json) == json)
    }

    "Merge idempotency" in check { (x: JValue) =>
      ((x merge x) == x)
    }

    "Diff identity" in check { (json: JValue) =>
      ((json diff JNothing) == Diff(JNothing, JNothing, json)) &&
      ((JNothing diff json) == Diff(JNothing, json, JNothing))
    }

    "Diff with self is empty" in check { (x: JValue) =>
      (x diff x) == Diff(JNothing, JNothing, JNothing)
    }

    "Diff is subset of originals" in check { (x: JObject, y: JObject) =>
      {
        val Diff(c, a, d @ _) = x diff y
        (y == (y merge (c merge a)))
      }
    }

    "Diff result is same when fields are reordered" in check { (x: JObject) =>
      ((x diff reorderFields(x)) == Diff(JNothing, JNothing, JNothing))
    }

    "Remove all" in check { (x: JValue) =>
      ((x remove { _ => true }) == JNothing)
    }

    "Remove nothing" in check { (x: JValue) =>
      ((x remove { _ => false }) == x)
    }

    "Remove removes only matching elements" in {
      forAllNoShrink(genJValue, genJValueClass) { (json: JValue, x: Class[_ <: JValue]) =>
        {
          val removed = json remove typePredicate(x)
          val elemsLeft = removed filter { case _ =>
            true
          }
          elemsLeft.forall(_.getClass != x)
        }
      }
    }

    "noNulls removes JNulls and JNothings" in {
      forAllNoShrink(genJValue, genJValueClass) { (json: JValue, x: Class[_ <: JValue]) =>
        {
          val noNulls = json.noNulls
          val elemsLeft = noNulls filter { case _ =>
            true
          }
          //noNulls can remove everything in which case we get a JNothing, otherwise there should be no JNulls or JNothings
          (noNulls == JNothing) || (elemsLeft.forall(e => e != JNull && e != JNothing))
        }
      }
    }

    val anyReplacement = (x: JValue, replacement: JObject) => {
      def findOnePath(jv: JValue, l: List[String]): List[String] = jv match {
        case JObject((fn, fv) :: _) => findOnePath(fv, fn :: l)
        case _ => l
      }

      val path = findOnePath(x, Nil).reverse

      val result = x.replace(path, replacement)

      def replaced(path: List[String], in: JValue): Boolean = {
        path match {
          case Nil =>
            x == in
          case name :: Nil =>
            (in \ name) == `replacement`
          case name :: xs =>
            val value = in \ name
            (value != JNothing) && replaced(xs, value)
        }
      }

      replaced(path, result)
    }

    "Replace one. any" in check {
      anyReplacement
    }
    "Replace one. field" in check {
      // ensure that we test some JObject instances
      (x: JObject, replacement: JObject) => anyReplacement(x, replacement)
    }

    "Replace each element in JArray" in {

      implicit val arbArray: Arbitrary[JField] = Arbitrary(genFieldArray)

      check { (field: JField, replacement: JObject) =>
        val JField(fn, JArray(_)) = field

        // ensure that we test a JObject with a JArray Field
        val obj = JObject(field)

        val result = obj.replace(s"$fn[]" :: Nil, replacement)

        // checks that each element was replaced
        result match {
          case JObject((_, JArray(xs)) :: _) =>
            xs.forall(_ == replacement)
        }

      }

    }

    "Replace one element in JArray" in {

      implicit def arbArray: Arbitrary[JField] = Arbitrary(genFieldArray)

      check { (field: JField, replacement: JObject) =>
        val JField(fn, JArray(arr)) = field

        // ensure that we test a JObject with a JArray Field
        val obj = JObject(field)

        val index = scala.util.Random.nextInt(arr.length)

        val result = obj.replace(s"$fn[$index]" :: Nil, replacement)

        // checks that only one element was replaced
        result match {
          case JObject((_, JArray(xs)) :: _) => {
            xs.indices.forall(i =>
              if (i == index) {
                (xs(i) == replacement)
              } else {
                (xs(i) == arr(i))
              }
            )
          }
        }

      }

    }

    "equals hashCode" in check { (x: JObject) =>
      val y = JObject(scala.util.Random.shuffle(x.obj))

      (x == y) && (x.## == y.##)
    }
  }

  private def reorderFields(json: JValue) = json map {
    case JObject(xs) => JObject(xs.reverse)
    case x => x
  }

  private def typePredicate(clazz: Class[_])(json: JValue) = json match {
    case x if x.getClass == clazz => true
    case _ => false
  }

  implicit def arbJValue: Arbitrary[JValue] = Arbitrary(genJValue)
  implicit def arbJObject: Arbitrary[JObject] = Arbitrary(genObject)
  implicit def arbJArray: Arbitrary[JArray] = Arbitrary(genArray)

}
