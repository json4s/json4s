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

object NativeXmlBugs extends XmlBugs[Document]("Native") with native.JsonMethods
object JacksonXmlBugs extends XmlBugs[JValue]("Jackson") with jackson.JsonMethods
abstract class XmlBugs[T](mod: String) extends Specification with JsonMethods[T]{
  import Xml._

  (mod+" XML Bugs") should {
    "HarryH's XML parses correctly" in {
      val xml1 = <venue><id>123</id></venue>
      val xml2 = <venue> <id>{"1"}{"23"}</id> </venue>
      Xml.toJson(xml1) must_== Xml.toJson(xml2)
    }

    "HarryH's XML with attributes parses correctly" in {
      val json = toJson(<tips><group type="Nearby"><tip><id>10</id></tip></group></tips>)
      compact(render(json)) must_== """{"tips":{"group":{"type":"Nearby","tip":{"id":"10"}}}}"""
    }

    "Jono's XML with attributes parses correctly" in {
      val example1 = <word term="example" self="http://localhost:8080/word/example" available="true">content</word>
      val expected1 = """{"word":"content","self":"http://localhost:8080/word/example","term":"example","available":"true"}"""

      val example2 = <word term="example" self="http://localhost:8080/word/example" available="true"></word>
      val expected2 = """{"self":"http://localhost:8080/word/example","term":"example","available":"true"}"""

      (toJson(example1) diff parse(expected1)) must_== Diff(JNothing, JNothing, JNothing)
      (toJson(example2) diff parse(expected2)) must_== Diff(JNothing, JNothing, JNothing)
    }

    "Nodes with attributes converted to correct JSON" in {
      val xml =
        <root>
          <n id="10" x="abc" />
          <n id="11" x="bcd" />
        </root>
//<<<<<<< HEAD
//      val expected = if (BuildInfo.scalaVersion.startsWith("2.10"))
//        """{"root":{"n":[{"id":"10","x":"abc"},{"id":"11","x":"bcd"}]}}"""
//      else
//        """{"root":{"n":[{"x":"abc","id":"10"},{"x":"bcd","id":"11"}]}}"""
//      compact(render(toJson(xml))) must_== expected
//    }
//
//    "XML with empty node is converted correctly to JSON" in {
//      val xml =
//        <tips><group type="Foo"></group><group type="Bar"><tip><text>xxx</text></tip><tip><text>yyy</text></tip></group></tips>
//      val expected = """{"tips":{"group":[{"type":"Foo"},{"type":"Bar","tip":[{"text":"xxx"},{"text":"yyy"}]}]}}"""
//      compact(render(toJson(xml))) must_== expected
//    }
//
//=======
      val expected = """{"root":{"n":[{"id":"10","x":"abc"},{"id":"11","x":"bcd"}]}}"""
      compact(render(toJson(xml))) must_== expected
    }

    "XML with empty node is converted correctly to JSON" in {
      val xml =
        <tips><group type="Foo"></group><group type="Bar"><tip><text>xxx</text></tip><tip><text>yyy</text></tip></group></tips>
      val expected = """{"tips":{"group":[{"type":"Foo"},{"type":"Bar","tip":[{"text":"xxx"},{"text":"yyy"}]}]}}"""
      compact(render(toJson(xml))) must_== expected
    }
  }
}
