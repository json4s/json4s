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

package net.liftweb.json.scalaz

import scalaz._
import Scalaz._
import net.liftweb.json._

trait Types {
  type Result[A] = ValidationNEL[Error, A]

  sealed trait Error
  case class UnexpectedJSONError(was: JValue, expected: Class[_ <: JValue]) extends Error
  case class NoSuchFieldError(name: String, json: JValue) extends Error
  case class UncategorizedError(key: String, desc: String, args: List[Any]) extends Error

  case object Fail {
    def apply[A](key: String, desc: String, args: List[Any]): Result[A] = 
      UncategorizedError(key, desc, args).fail.liftFailNel

    def apply[A](key: String, desc: String): Result[A] = 
      UncategorizedError(key, desc, Nil).fail.liftFailNel
  }

  implicit def JValueShow[A <: JValue]: Show[A] = new Show[A] {
    def show(json: A) = compact(render(json)).toList
  }

  implicit def JValueZero: Zero[JValue] = zero(JNothing)
  implicit def JValueSemigroup: Semigroup[JValue] = semigroup(_ ++ _)
  implicit def JValueEqual: Equal[JValue] = equalA

  trait JSONR[A] {
    def read(json: JValue): Result[A]
  }

  trait JSONW[A] {
    def write(value: A): JValue
  }

  trait JSON[A] extends JSONR[A] with JSONW[A]

  implicit def Result2JSONR[A](f: JValue => Result[A]): JSONR[A] = new JSONR[A] {
    def read(json: JValue) = f(json)
  }

  def fromJSON[A: JSONR](json: JValue): Result[A] = implicitly[JSONR[A]].read(json)
  def toJSON[A: JSONW](value: A): JValue = implicitly[JSONW[A]].write(value)

  def field[A: JSONR](name: String)(json: JValue): Result[A] = json match {
    case JObject(fs) => 
      fs.find(_.name == name)
        .map(f => implicitly[JSONR[A]].read(f.value))
        .orElse(implicitly[JSONR[A]].read(JNothing).fold(_ => none, x => some(Success(x))))
        .getOrElse(NoSuchFieldError(name, json).fail.liftFailNel)
    case x => UnexpectedJSONError(x, classOf[JObject]).fail.liftFailNel
  }

  def validate[A: JSONR](name: String): Kleisli[Result, JValue, A] = kleisli(field[A](name))

  def makeObj(fields: Traversable[(String, JValue)]): JObject = 
    JObject(fields.toList.map { case (n, v) => JField(n, v) })
}

object JsonScalaz extends Types with Lifting with Base with Tuples
