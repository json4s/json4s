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

trait Tuples { this: Types =>
  implicit def Tuple2JSON[A: JSON, B: JSON]: JSON[(A, B)] = new JSON[(A, B)] {
    def read(json: JValue) = json match {
      case JArray(a :: b :: _) => 
        (fromJSON[A](a) |@| fromJSON[B](b)) { (a, b) => (a, b)  }
      case x => UnexpectedJSONError(x, classOf[JArray]).fail.liftFailNel
    }

    def write(value: (A, B)) = JArray(toJSON(value._1) :: toJSON(value._2) :: Nil)
  }

  implicit def Tuple3JSON[A: JSON, B: JSON, C: JSON]: JSON[(A, B, C)] = new JSON[(A, B, C)] {
    def read(json: JValue) = json match {
      case JArray(a :: b :: c :: _) => 
        (fromJSON[A](a) |@| fromJSON[B](b) |@| fromJSON[C](c)) { (a, b, c) => (a, b, c)  }
      case x => UnexpectedJSONError(x, classOf[JArray]).fail.liftFailNel
    }

    def write(value: (A, B, C)) = JArray(toJSON(value._1) :: toJSON(value._2) :: toJSON(value._3) :: Nil)
  }

  implicit def Tuple4JSON[A: JSON, B: JSON, C: JSON, D: JSON]: JSON[(A, B, C, D)] = new JSON[(A, B, C, D)] {
    def read(json: JValue) = json match {
      case JArray(a :: b :: c :: d :: _) => 
        (fromJSON[A](a) |@| fromJSON[B](b) |@| fromJSON[C](c) |@| fromJSON[D](d)) { (a, b, c, d) => (a, b, c, d)  }
      case x => UnexpectedJSONError(x, classOf[JArray]).fail.liftFailNel
    }

    def write(value: (A, B, C, D)) = JArray(toJSON(value._1) :: toJSON(value._2) :: toJSON(value._3) :: toJSON(value._4) :: Nil)
  }

  implicit def Tuple5JSON[A: JSON, B: JSON, C: JSON, D: JSON, E: JSON]: JSON[(A, B, C, D, E)] = new JSON[(A, B, C, D, E)] {
    def read(json: JValue) = json match {
      case JArray(a :: b :: c :: d :: e :: _) => 
        (fromJSON[A](a) |@| fromJSON[B](b) |@| fromJSON[C](c) |@| fromJSON[D](d) |@| fromJSON[E](e)) { (a, b, c, d, e) => (a, b, c, d, e)  }
      case x => UnexpectedJSONError(x, classOf[JArray]).fail.liftFailNel
    }

    def write(value: (A, B, C, D, E)) = JArray(toJSON(value._1) :: toJSON(value._2) :: toJSON(value._3) :: toJSON(value._4) :: toJSON(value._5) :: Nil)
  }

  implicit def Tuple6JSON[A: JSON, B: JSON, C: JSON, D: JSON, E: JSON, F: JSON]: JSON[(A, B, C, D, E, F)] = new JSON[(A, B, C, D, E, F)] {
    def read(json: JValue) = json match {
      case JArray(a :: b :: c :: d :: e :: f :: _) => 
        (fromJSON[A](a) |@| fromJSON[B](b) |@| fromJSON[C](c) |@| fromJSON[D](d) |@| fromJSON[E](e) |@| fromJSON[F](f)) { (a, b, c, d, e, f) => (a, b, c, d, e, f)  }
      case x => UnexpectedJSONError(x, classOf[JArray]).fail.liftFailNel
    }

    def write(value: (A, B, C, D, E, F)) = JArray(toJSON(value._1) :: toJSON(value._2) :: toJSON(value._3) :: toJSON(value._4) :: toJSON(value._5) :: toJSON(value._6) :: Nil)
  }
}
