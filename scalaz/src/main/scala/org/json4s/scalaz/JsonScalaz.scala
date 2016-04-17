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

import _root_.scalaz._
import std.option._
import syntax.applicative._
import syntax.validation._
import syntax.contravariant._


trait Types {
  type Result[+A] = ValidationNel[Error, A]

  sealed abstract class Error extends Product with Serializable
  case class UnexpectedJSONError(was: JValue, expected: Class[_ <: JValue]) extends Error
  case class NoSuchFieldError(name: String, json: JValue) extends Error
  case class UncategorizedError(key: String, desc: String, args: List[Any]) extends Error

  case object Fail {
    def apply[A](key: String, desc: String, args: List[Any]): Result[A] = 
      Validation.failureNel(UncategorizedError(key, desc, args))

    def apply[A](key: String, desc: String): Result[A] = 
      Validation.failureNel(UncategorizedError(key, desc, Nil))
  }

  implicit def JValueMonoid: Monoid[JValue] = Monoid.instance(_ ++ _, JNothing)
  implicit def JValueEqual: Equal[JValue] = Equal.equalA

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

  object JSONW {

    def apply[A:JSONW]: JSONW[A] = implicitly[JSONW[A]]

    def instance[A](f: A => JValue): JSONW[A] = new JSONW[A] {
      def write(a: A) = f(a)
    }

  }


  object JSONR {

    def apply[A:JSONR]: JSONR[A] = implicitly[JSONR[A]]

    def instance[A](f: JValue => Result[A]): JSONR[A] = new JSONR[A] {
      def read(json: JValue) = f(json)
    }

    def instanceE[A](f: JValue => Error \/ A): JSONR[A] = new JSONR[A] {
      def read(json: JValue) = f(json).validationNel
    }

  }

  object JSON {

    def apply[A:JSON](implicit jsonA: JSON[A]): JSON[A] = jsonA

    def instance[A](f: JValue => Result[A], g: A => JValue): JSON[A] = new JSON[A] {
      override def read(json: JValue): Result[A] = f(json)
      override def write(value: A): JValue = g(value)
    }

    implicit def JSONfromJSONRW[A](implicit readA: JSONR[A], writeA: JSONW[A]): JSON[A] = new JSON[A] {
      override def read(json: JValue): Result[A] = readA.read(json)
      override def write(value: A): JValue = writeA.write(value)
    }

  }


  implicit val jsonrMonad = new Monad[JSONR] {

    override def point[A](a: => A): JSONR[A] = new JSONR[A] {
      override def read(json: JValue): Result[A] = a.successNel
    }

    override def bind[A, B](fa: JSONR[A])(f: (A) => JSONR[B]): JSONR[B] = new JSONR[B] {
      override def read(json: JValue): Result[B] = {
        fa.read(json) match {
          case Success(a) => f(a).read(json)
          case Failure(error) => Failure(error)
        }
      }
    }

  }

  implicit val jsonwContravariant = new Contravariant[JSONW] {

    override def contramap[A, B](r: JSONW[A])(f: (B) => A): JSONW[B] = new JSONW[B] {
      override def write(value: B): JValue = {
        r.write(f(value))
      }
    }

  }

  implicit class JSONRExt[A](fa: JSONR[A]) {

    def emap[B](f: A => Result[B]): JSONR[B] = new JSONR[B] {
      override def read(json: JValue): Result[B] = {
        fa.read(json) match {
          case Success(a) => f(a)
          case f@Failure(error) => f
        }
      }
    }

    def orElse[B >: A](fa2: JSONR[B]): JSONR[B] = new JSONR[B] {
      override def read(json: JValue): Result[B] = {
        fa.read(json) orElse fa2.read(json)
      }
    }

  }

  implicit class JSONExt[A](fa: JSON[A]) {

    def xmap[B](f1: A => B, f2: B => A): JSON[B] = new JSON[B] {
      override def write(value: B): JValue = (fa:JSONW[A]).contramap(f2).write(value)

      override def read(json: JValue): Result[B] = (fa:JSONR[A]).map(f1).read(json)
    }

    def exmap[B](f1: A => Result[B], f2: B => A): JSON[B] = new JSON[B] {
      override def write(value: B): JValue = (fa:JSONW[A]).contramap(f2).write(value)

      override def read(json: JValue): Result[B] = (fa:JSONR[A]).emap(f1).read(json)
    }

  }

  implicit class JSONROps(json: JValue) {
    def validate[A: JSONR]: ValidationNel[Error, A] = implicitly[JSONR[A]].read(json)
    def read[A: JSONR]: Error \/ A = implicitly[JSONR[A]].read(json).disjunction.leftMap(_.head)
  }

  implicit class JSONWOps[A](a: A) {
    def toJson(implicit w: JSONW[A]): JValue = w.write(a)
  }


  def field[A: JSONR](name: String)(json: JValue): Result[A] = json match {
    case JObject(fs) => 
      fs.find(_._1 == name)
        .map(f => implicitly[JSONR[A]].read(f._2))
        .orElse(implicitly[JSONR[A]].read(JNothing).fold(_ => none, x => some(Success(x))))
        .getOrElse(Validation.failureNel(NoSuchFieldError(name, json)))
    case x =>
      Validation.failureNel(UnexpectedJSONError(x, classOf[JObject]))
  }

  type EitherNel[+a] = NonEmptyList[Error] \/ a
  def validate[A: JSONR](name: String) = Kleisli(field[A](name)).mapK[EitherNel, A](_.disjunction)
  implicit def function2EitherNel[A](f: A => Result[A]): (A => EitherNel[A]) = (a: A) => f(a).disjunction
  implicit def kleisli2Result[A](v: Kleisli[EitherNel, JValue, A]): JValue => Result[A] = v.run.andThen(_.validation)

  def makeObj(fields: Traversable[(String, JValue)]): JObject = 
    JObject(fields.toList.map { case (n, v) => JField(n, v) })
}

object JsonScalaz extends Types with Lifting with Base with org.json4s.scalaz.Tuples
