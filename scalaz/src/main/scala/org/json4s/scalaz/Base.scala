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
import std.list._
import std.option._
import syntax.validation._
import syntax.std.option._
import syntax.traverse._
import syntax.std.list._
import scala.collection.breakOut

trait Base { this: Types =>
  implicit def boolJSON: JSON[Boolean] = new JSON[Boolean] {
    def read(json: JValue) = json match {
      case JBool(b) => Validation.success(b)
      case x => UnexpectedJSONError(x, classOf[JBool]).failureNel
    }

    def write(value: Boolean) = JBool(value)
  }

  implicit def intJSON: JSON[Int] = new JSON[Int] {
    def read(json: JValue) = json match {
      case JInt(x) => Validation.success(x.intValue)
      case x => UnexpectedJSONError(x, classOf[JInt]).failureNel
    }

    def write(value: Int) = JInt(BigInt(value))
  }

  implicit def longJSON: JSON[Long] = new JSON[Long] {
    def read(json: JValue) = json match {
      case JInt(x) => Validation.success(x.longValue)
      case x => UnexpectedJSONError(x, classOf[JInt]).failureNel
    }

    def write(value: Long) = JInt(BigInt(value))
  }

  implicit def doubleJSON: JSON[Double] = new JSON[Double] {
    def read(json: JValue) = json match {
      case JDouble(x) => Validation.success(x)
      case x => UnexpectedJSONError(x, classOf[JDouble]).failureNel
    }

    def write(value: Double) = JDouble(value)
  }

  implicit def stringJSON: JSON[String] = new JSON[String] {
    def read(json: JValue) = json match {
      case JString(x) => Validation.success(x)
      case x => UnexpectedJSONError(x, classOf[JString]).failureNel
    }

    def write(value: String) = JString(value)
  }

  implicit def bigintJSON: JSON[BigInt] = new JSON[BigInt] {
    def read(json: JValue) = json match {
      case JInt(x) => Validation.success(x)
      case x => UnexpectedJSONError(x, classOf[JInt]).failureNel
    }

    def write(value: BigInt) = JInt(value)
  }

  implicit def jvalueJSON: JSON[JValue] = new JSON[JValue] {
    def read(json: JValue) = Validation.success(json)
    def write(value: JValue) = value
  }

  implicit def listJSONR[A: JSONR]: JSONR[List[A]] = new JSONR[List[A]] {
    def read(json: JValue) = json match {
      case JArray(xs) => 
        xs.map(fromJSON[A]).sequence[({type λ[t] = ValidationNel[Error, t]})#λ, A]
      case x => UnexpectedJSONError(x, classOf[JArray]).failureNel
    }
  }
  implicit def listJSONW[A: JSONW]: JSONW[List[A]] = new JSONW[List[A]] {
    def write(values: List[A]) = JArray(values.map(x => toJSON(x)))
  }

  implicit def optionJSONR[A: JSONR]: JSONR[Option[A]] = new JSONR[Option[A]] {
    def read(json: JValue) = json match {
      case JNothing | JNull => Validation.success(None)
      case x => fromJSON[A](x).map(some)
    }
  }
  implicit def optionJSONW[A: JSONW]: JSONW[Option[A]] = new JSONW[Option[A]] {
    def write(value: Option[A]) = value.map(x => toJSON(x)).getOrElse(JNull)
  }

  implicit def mapJSONR[A: JSONR]: JSONR[Map[String, A]] = new JSONR[Map[String, A]] {
    def read(json: JValue) = json match {
      case JObject(fs) =>
        val m = fs.map(f => fromJSON[A](f._2) map (f._1 -> _))
        val mm = m.sequence[({type λ[t] = ValidationNel[Error, t]})#λ, (String, A)]
        mm.map(_.toMap)
//        val r = m.sequence[PartialApply1Of2[ValidationNEL, Error]#Apply, (String, A)]
//        r.map(_.toMap)
//        val r = fs.map(f => fromJSON[A](f._2).map(v => (f._1, v))).sequence[PartialApply1Of2[ValidationNEL, Error]#Apply, (String, A)]
//        r.map(_.toMap)
      case x => UnexpectedJSONError(x, classOf[JObject]).failureNel
    }
  }
  implicit def mapJSONW[A: JSONW]: JSONW[Map[String, A]] = new JSONW[Map[String, A]] {
    def write(values: Map[String, A]) = JObject(values.map { case (k, v) => JField(k, toJSON(v)) }(breakOut).toList)
  }
}
