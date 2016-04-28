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

object NativeNullErrorHandlingSpec extends NullErrorHandlingSpec[Document]("Native") with native.JsonMethods
object JacksonNullErrorHandlingSpec extends NullErrorHandlingSpec[JValue]("Jackson") with jackson.JsonMethods

object NullErrorHandlingSpec {
  val NullIntTypeJson = """{"x": null, "y": true, "z": "abc"}"""
  val NullBooleanTypeJson = """{"x": 10, "y": null, "z": "abc"}"""
  val NullStringTypeJson = """{"x": 10, "y": true, "z": null}"""
}

abstract class NullErrorHandlingSpec[T](mod: String) extends Specification with JsonMethods[T] {
  import NullErrorHandlingSpec._

  implicit lazy val formats = DefaultFormats

  (mod + " case class modeling json type") should {
    "throw an error explaining the cause when parsing null int" in {
      val json = parse(NullIntTypeJson)
      json.extract[NullErrorHandlingJson] must throwA(new MappingException(
        "No usable value for x\nNull invalid value for a sub-type of AnyVal"))
    }
    "throw an error explaining the cause when parsing null boolean" in {
      val json = parse(NullBooleanTypeJson)
      json.extract[NullErrorHandlingJson] must throwA(new MappingException(
        "No usable value for y\nNull invalid value for a sub-type of AnyVal"))
    }
    "returns a correct result when parsing a null string" in {
      val json = parse(NullStringTypeJson)
      val obj = json.extract[NullErrorHandlingJson]
      obj.x must_== 10
      obj.y must_== true
      obj.z must_== null
    }
  }
}

case class NullErrorHandlingJson(
  val x: Int,
  val y: Boolean,
  val z: String
)
