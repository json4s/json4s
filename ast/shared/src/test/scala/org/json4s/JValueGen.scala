/*
 * Copyright 2009-2010 WorldWide Conferencing, LLC
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

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary
import org.json4s.JsonAST.JField

trait JValueGen {
  def genJValue: Gen[JValue] = frequency((5, genSimple), (1, delay(genArray)), (1, delay(genObject)))
  def genSimple: Gen[JValue] = oneOf(
    const(JNull),
    arbitrary[Int].map(JInt(_)),
    arbitrary[Long].map(JLong(_)),
    arbitrary[Double].map(JDouble(_)),
    arbitrary[Boolean].map(JBool(_)),
    arbitrary[String].map(JString(_))
  )

  def genArray: Gen[JArray] = for (l <- genList) yield JArray(l)
  def genObject: Gen[JObject] = for (l <- genFieldList) yield JObject(l)

  def genList = Gen.containerOfN[List, JValue](listSize, genJValue)
  def genFieldList = Gen.containerOfN[List, JField](fieldsSize, genField)

  def genField(genValue: Gen[JValue]): Gen[JField] = for {
    name <- identifier
    value <- genValue
    id <- choose(0, 1000000)
  } yield JField(name + id, value)
  def genField: Gen[JField] = genField(genJValue)
  def genFieldArray: Gen[JField] = genField(genArray)

  def genJValueClass: Gen[Class[_ <: JValue]] = oneOf(
    JNull.getClass.asInstanceOf[Class[JValue]],
    JNothing.getClass.asInstanceOf[Class[JValue]],
    classOf[JInt],
    classOf[JDouble],
    classOf[JBool],
    classOf[JString],
    classOf[JArray],
    classOf[JObject],
    classOf[JSet]
  )

  def listSize = choose(1, 5).sample.get
  def fieldsSize = choose(0, 5).sample.get

  implicit val jValueCogen: Cogen[JValue] =
    Cogen[JValue] {
      new runtime.AbstractFunction2[rng.Seed, JValue, rng.Seed] {
        def apply(seed: rng.Seed, j: JValue) = j match {
          case JString(a) => Cogen.perturb(seed, a)
          case JBool(a) => Cogen.perturb(seed, a)
          case JArray(a) => Cogen.perturb(seed, a)
          case JSet(a) => Cogen.perturb(seed, a.toList)
          case JDecimal(a) => Cogen.perturb(seed, a.bigDecimal.unscaledValue.toByteArray)
          case JDouble(a) => Cogen.perturb(seed, a)
          case JInt(a) => Cogen.perturb(seed, a.toByteArray)
          case JLong(a) => Cogen.perturb(seed, a)
          case JObject(a) => Cogen.perturb(seed, a)
          case JNothing => seed.reseed(1)
          case JNull => seed.reseed(2)
        }
      }
    }
}
