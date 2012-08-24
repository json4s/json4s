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

import util.control.Exception._

import org.specs.Specification
import native._

object ParserBugs extends Specification {
  "Unicode ffff is a valid char in string literal" in {
    parseOpt(""" {"x":"\uffff"} """).isDefined mustEqual true
  }

  "Does not hang when parsing 2.2250738585072012e-308" in {
    allCatch.opt(parse(""" [ 2.2250738585072012e-308 ] """)) mustEqual None
    allCatch.opt(parse(""" [ 22.250738585072012e-309 ] """)) mustEqual None
  }

  "Does not allow colon at start of array (1039)" in {
    parseOpt("""[:"foo", "bar"]""") mustEqual None
  }

  "Does not allow colon instead of comma in array (1039)" in {
    parseOpt("""["foo" : "bar"]""") mustEqual None
  }

  "Solo quote mark should fail cleanly (not StringIndexOutOfBoundsException) (1041)" in {
    JsonParser.parse("\"", discardParser) must throwA(new Exception()).like {
      case e: JsonParser.ParseException => e.getMessage.startsWith("unexpected eof")
    }
  }

  "Field names must be quoted" in {
    val json = JObject(List(JField("foo\nbar", JInt(1))))
    val s = compact(render(json))
    s mustEqual """{"foo\nbar":1}"""
    parse(s) mustEqual json
  }

  private val discardParser = (p : JsonParser.Parser) => {
     var token: JsonParser.Token = null
     do {
       token = p.nextToken
     } while (token != JsonParser.End)
   }
}
