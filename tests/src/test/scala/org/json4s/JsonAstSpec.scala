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

import org.specs2.mutable.Specification
import org.scalacheck._
import org.scalacheck.Prop.forAllNoShrink
import org.specs2.ScalaCheck
import org.specs2.matcher.MatchResult

object JsonAstSpec extends Specification with JValueGen with ScalaCheck {

  ("JSON AST Specification") should {
    "Functor identity" in {
      val identityProp = (json: JValue) => json must_== (json map identity)
      prop(identityProp)
    }

    "Monoid identity" in {
      val identityProp = (json: JValue) => (json ++ JNothing must_== json) and (JNothing ++ json must_== json)
      prop(identityProp)
    }

    "Monoid associativity" in {
      val assocProp = (x: JValue, y: JValue, z: JValue) => x ++ (y ++ z) must_== (x ++ y) ++ z
      prop(assocProp)
    }

    "Merge identity" in {
      val identityProp = (json: JValue) => ((json merge JNothing) must_== json) and ((JNothing merge json) must_== json)
      prop(identityProp)
    }

    "Merge idempotency" in {
      val idempotencyProp = (x: JValue) => (x merge x) must_== x
      prop(idempotencyProp)
    }

    "Diff identity" in {
      val identityProp = (json: JValue) =>
        ((json diff JNothing) must_== Diff(JNothing, JNothing, json)) and
        ((JNothing diff json) must_== Diff(JNothing, json, JNothing))

      prop(identityProp)
    }

    "Diff with self is empty" in {
      val emptyProp = (x: JValue) => (x diff x) must_== Diff(JNothing, JNothing, JNothing)
      prop(emptyProp)
    }

    "Diff is subset of originals" in {
      val subsetProp = (x: JObject, y: JObject) => {
        val Diff(c, a, d) = x diff y
        y must_== (y merge (c merge a))
      }
      prop(subsetProp)
    }

    "Diff result is same when fields are reordered" in {
      val reorderProp = (x: JObject) => (x diff reorderFields(x)) must_== Diff(JNothing, JNothing, JNothing)
      prop(reorderProp)
    }

    "Remove all" in {
      val removeAllProp = (x: JValue) => (x remove { _ => true }) must_== JNothing
      prop(removeAllProp)
    }

    "Remove nothing" in {
      val removeNothingProp = (x: JValue) => (x remove { _ => false }) must_== x
      prop(removeNothingProp)
    }

    "Remove removes only matching elements" in {
      forAllNoShrink(genJValue, genJValueClass) { (json: JValue, x: Class[_ <: JValue]) => {
        val removed = json remove typePredicate(x)
        val elemsLeft = removed filter {
          case _ => true
        }
        (elemsLeft.forall(_.getClass != x) must beTrue)
      }}
    }

    "noNulls removes JNulls and JNothings" in {
      forAllNoShrink(genJValue, genJValueClass) { (json: JValue, x: Class[_ <: JValue]) => {
        val noNulls = json.noNulls
        val elemsLeft = noNulls filter {
          case _ => true
        }
        //noNulls can remove everything in which case we get a JNothing, otherwise there should be no JNulls or JNothings
        (noNulls must_== JNothing) or
        (elemsLeft.forall(e => e != JNull && e != JNothing) must beTrue)
      }}
    }

    "Replace one" in {
      val anyReplacement = (x: JValue, replacement: JObject) => {
        def findOnePath(jv: JValue, l: List[String]): List[String] = jv match {
          case JObject(fl) => fl match {
            case field :: xs => findOnePath(field._2, l)
            case Nil => l
          }
          case _ => l
        }

        val path = findOnePath(x, Nil).reverse
        val result = x.replace(path, replacement)

        def replaced(path: List[String], in: JValue): MatchResult[_] = {
          path match {
            case Nil => x must_== in

            case name :: Nil => (in \ name) must_== `replacement`

            case name :: xs =>
              val value = (in \ name)
              (value must_!= JNothing) and replaced(xs, value)
          }
        }

        replaced(path, result)
      }

      // ensure that we test some JObject instances
      val fieldReplacement = (x: JObject, replacement: JObject) => anyReplacement(x, replacement)

      prop(fieldReplacement)
      prop(anyReplacement)
    }

    "equals hashCode" in prop{ x: JObject =>
      val y = JObject(scala.util.Random.shuffle(x.obj))

      x must_== y
      x.## must_== y.##
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
}
