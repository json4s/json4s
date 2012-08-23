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

import org.specs.{ScalaCheck, Specification}
import org.scalacheck._
import org.scalacheck.Prop.{forAll, forAllNoShrink}


/**
 * System under specification for JSON AST.
 */
object JsonAstSpec extends Specification("JSON AST Specification") with JValueGen with ScalaCheck {
  "Functor identity" in {
    val identityProp = (json: JValue) => json == (json map identity)
    forAll(identityProp) must pass
  }

  "Functor composition" in {
    val compositionProp = (json: JValue, fa: JValue => JValue, fb: JValue => JValue) =>
      json.map(fb).map(fa) == json.map(fa compose fb)

    forAll(compositionProp) must pass
  }

  "Monoid identity" in {
    val identityProp = (json: JValue) => (json ++ JNothing == json) && (JNothing ++ json == json)
    forAll(identityProp) must pass
  }

  "Monoid associativity" in {
    val assocProp = (x: JValue, y: JValue, z: JValue) => x ++ (y ++ z) == (x ++ y) ++ z
    forAll(assocProp) must pass
  }

  "Merge identity" in {
    val identityProp = (json: JValue) => (json merge JNothing) == json && (JNothing merge json) == json
    forAll(identityProp) must pass
  }

  "Merge idempotency" in {
    val idempotencyProp = (x: JValue) => (x merge x) == x
    forAll(idempotencyProp) must pass
  }

  "Diff identity" in {
    val identityProp = (json: JValue) =>
      (json diff JNothing) == Diff(JNothing, JNothing, json) &&
      (JNothing diff json) == Diff(JNothing, json, JNothing)

    forAll(identityProp) must pass
  }

  "Diff with self is empty" in {
    val emptyProp = (x: JValue) => (x diff x) == Diff(JNothing, JNothing, JNothing)
    forAll(emptyProp) must pass
  }

  "Diff is subset of originals" in {
    val subsetProp = (x: JObject, y: JObject) => {
      val Diff(c, a, d) = x diff y
      y == (y merge (c merge a))
    }
    forAll(subsetProp) must pass
  }

  "Diff result is same when fields are reordered" in {
    val reorderProp = (x: JObject) => (x diff reorderFields(x)) == Diff(JNothing, JNothing, JNothing)
    forAll(reorderProp) must pass
  }

  "Remove all" in {
    val removeAllProp = (x: JValue) => (x remove { _ => true }) == JNothing
    forAll(removeAllProp) must pass
  }

  "Remove nothing" in {
    val removeNothingProp = (x: JValue) => (x remove { _ => false }) == x
    forAll(removeNothingProp) must pass
  }

  "Remove removes only matching elements (in case of a field, its value is set to JNothing)" in {
    forAllNoShrink(genJValue, genJValueClass) { (json: JValue, x: Class[_ <: JValue]) => {
      val removed = json remove typePredicate(x)
      val Diff(c, a, d) = json diff removed
      val elemsLeft = removed filter {
        case JField(_, JNothing) => false
        case _ => true
      }
      c == JNothing && a == JNothing && elemsLeft.forall(_.getClass != x)
    }} must pass
  }

  "Replace one" in {
    val anyReplacement = (x: JValue, replacement: JObject) => {
      def findOnePath(jv: JValue, l: List[String]): List[String] = jv match {
        case JField(name, value) => findOnePath(value, name :: l)
        case JObject(fl) => fl match {
          case field :: xs => findOnePath(field, l)
          case Nil => l
        }
        case _ => l
      }

      val path = findOnePath(x, Nil).reverse
      val result = x.replace(path, replacement)

      def replaced(path: List[String], in: JValue): Boolean = {
        path match {
          case Nil => x == in

          case name :: Nil => (in \ name) match {
            case `replacement` => true
            case _ => false
          }

          case name :: xs => (in \ name) match {
            case JNothing => false
            case value => replaced(xs, value)
          }
        }
      }

      replaced(path, result)
    }

    // ensure that we test some JObject instances
    val fieldReplacement = (x: JObject, replacement: JObject) => anyReplacement(x, replacement)

    forAll(fieldReplacement) must pass
    forAll(anyReplacement) must pass
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
