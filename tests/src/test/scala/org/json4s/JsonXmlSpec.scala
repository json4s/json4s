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

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.scalacheck.Arbitrary
import org.json4s.native.Document

//import NativeImports._
//import JsonMethods._

class NativeJsonXmlSpec extends JsonXmlSpec[Document]("Native") with native.JsonMethods
class JacksonXmlSpec extends JsonXmlSpec[JValue]("Jackson") with jackson.JsonMethods

/**
* System under specification for JSON XML.
*/
abstract class JsonXmlSpec[T](mod: String) extends Specification with NodeGen with JValueGen with ScalaCheck with JsonMethods[T] {
  import Xml._
  import scala.xml.Node

  (mod+" JSON XML Specification") should {
    "Valid XML can be converted to JSON and back (symmetric op)" in {
      val conversion = (xml: Node) => { toXml(toJson(xml)).head must_== xml }
      prop(conversion)
    }

    "JSON can be converted to XML, and back to valid JSON (non symmetric op)" in {
      val conversion = (json: JValue) => { parse(compact(render(toJson(toXml(json))))); true must beTrue}
      prop(conversion)
    }
  }

  implicit def arbXml: Arbitrary[Node] = Arbitrary(genXml)
  implicit def arbJValue: Arbitrary[JValue] = Arbitrary(genObject)
}
