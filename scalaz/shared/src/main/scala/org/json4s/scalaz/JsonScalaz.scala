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
package scalaz

import _root_.scalaz.*
import std.option.*

trait Types {
  type Result[A] = ValidationNel[Error, A]

  sealed abstract class Error extends Product with Serializable
  case class UnexpectedJSONError(was: JValue, expected: Class[? <: JValue]) extends Error
  case class NoSuchFieldError(name: String, json: JValue) extends Error
  case class UncategorizedError(key: String, desc: String, args: List[Any]) extends Error

  case object Fail {
    def apply[A](key: String, desc: String, args: List[Any]): Result[A] =
      Validation.failureNel(UncategorizedError(key, desc, args))

    def apply[A](key: String, desc: String): Result[A] =
      Validation.failureNel(UncategorizedError(key, desc, Nil))
  }

  implicit val JValueMonoid: Monoid[JValue] = Monoid.instance(_ ++ _, JNothing)
  implicit val JValueEqual: Equal[JValue] = Equal.equalA

  trait JSONR[A] {
    def read(json: JValue): Result[A]
  }

  trait JSONW[A] {
    def write(value: A): JValue
  }

  trait JSON[A] extends JSONR[A] with JSONW[A]

  implicit def Result2JSONR[A](f: JValue => Result[A]): JSONR[A] =
    (json: JValue) => f(json)

  def fromJSON[A: JSONR](json: JValue): Result[A] = implicitly[JSONR[A]].read(json)
  def toJSON[A: JSONW](value: A): JValue = implicitly[JSONW[A]].write(value)

  def field[A: JSONR](name: String)(json: JValue): Result[A] = json match {
    case JObject(fs) =>
      fs.find(_._1 == name)
        .map(f => implicitly[JSONR[A]].read(f._2))
        .orElse(implicitly[JSONR[A]].read(JNothing).fold(_ => none, x => some(Success(x): Result[A])))
        .getOrElse(Validation.failureNel(NoSuchFieldError(name, json)))
    case x =>
      Validation.failureNel(UnexpectedJSONError(x, classOf[JObject]))
  }

  type EitherNel[a] = NonEmptyList[Error] \/ a
  def validate[A: JSONR](name: String): Kleisli[EitherNel, JValue, A] =
    Kleisli(field[A](name)).mapK[EitherNel, A](_.toDisjunction)
  implicit def function2EitherNel[A](f: A => Result[A]): A => EitherNel[A] = (a: A) => f(a).toDisjunction
  implicit def kleisli2Result[A](v: Kleisli[EitherNel, JValue, A]): JValue => Result[A] = v.run.andThen(_.toValidation)

  def makeObj(fields: Iterable[(String, JValue)]): JObject =
    JObject(fields.toList.map { case (n, v) => JField(n, v) })
}

object JsonScalaz extends Types with Lifting with Base with org.json4s.scalaz.Tuples
