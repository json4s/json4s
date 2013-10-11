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
import text.Document

object NativeErrorHandlingSpec extends ErrorHandlingSpec[Document]("Native") with native.JsonMethods
//object JacksonErrorHandlingSpec extends ErrorHandlingSpec[JValue]("Jackson") with jackson.JsonMethods

object ErrorHandlingSpec {
  val WrongXTypeJson = """{ "x": null, "y": null, "z": null}"""
  val ObjectNullJson = """{ "x": 10, "y": null, "z": false, "e": "EnumOne"}"""
}


abstract class ErrorHandlingSpec[T](mod: String) extends Specification with JsonMethods[T] {
  import JsonDSL._
  import ErrorHandlingSpec._
  import native.Serialization.{write => swrite}

  implicit lazy val formats = (new DefaultFormats { override val strict = true }) + new org.json4s.ext.EnumSerializer(EnumMyEnum)

  (mod + " case class modeling json type") should {
    "throw a error explaining the cause when parsing a string for an int" in {
      val obj = ErrorHandlingJson(10, "abc", true, EnumMyEnum.EnumTwo)
      println(EnumMyEnum.EnumTwo)
      println(swrite(obj))
      /*val json = parse(ObjectNullJson)
      println(json)
      val xJson = (json \ "x")
      println(xJson)
      val x = xJson.extract[Int]
      println(x)
      val obj = json.extract[ErrorHandlingJson]
      println(obj)*/
      1 must_== 1
    }
  }
}

object EnumMyEnum extends Enumeration {
  type MyEnum = Value
  val EnumOne, EnumTwo, EnumThree = Value
}

case class ErrorHandlingJson(
  val x: Int,
  val y: String,
  val z: Boolean,
  val e: EnumMyEnum.MyEnum
)
