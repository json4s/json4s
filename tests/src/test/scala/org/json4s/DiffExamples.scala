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
import org.json4s.native.Document

class NativeDiffExamples extends DiffExamples[Document]("Native") with native.JsonMethods
class JacksonDiffExamples extends DiffExamples[JValue]("Jackson") with jackson.JsonMethods

abstract class DiffExamples[T](mod: String) extends Specification with JsonMethods[T]  {

  title(mod + " Diff Examples")
  import NativeMergeExamples.{scala1, scala2, lotto1, lotto2, mergedLottoResult}

  "Diff example" in {
    val Diff(changed, added, deleted) = scala1 diff scala2
    changed must_== expectedChanges
    added must_== expectedAdditions
    deleted must_== expectedDeletions
  }


  lazy val expectedChanges = parse("""
    {
      "tags": ["static-typing","fp"],
      "features": {
        "key2":"newval2"
      }
    }""")

  lazy val expectedAdditions = parse("""
    {
      "features": {
        "key3":"val3"
      },
      "compiled": true
    }""")

  lazy val expectedDeletions = parse("""
    {
      "year":2006,
      "features":{ "key1":"val1" }
    }""")

  "Lotto example" in {
    val Diff(changed, added, deleted) = mergedLottoResult diff lotto1
    changed must_== JNothing
    added must_== JNothing
    deleted must_== lotto2
  }

  "Example from http://tlrobinson.net/projects/js/jsondiff/" in {
    val json1 = read("/diff-example-json1.json")
    val json2 = read("/diff-example-json2.json")
    val expectedChanges = read("/diff-example-expected-changes.json")
    val expectedAdditions = read("/diff-example-expected-additions.json")
    val expectedDeletions = read("/diff-example-expected-deletions.json")

    json1 diff json2 must_== Diff(expectedChanges, expectedAdditions, expectedDeletions)
  }

  "After adding and removing a field, there should be no difference" in {
    import JsonDSL._
    val addition = parse("""{"author":"Martin"}""")
    val scala2 = scala1 merge addition removeField { _ == JField("author", "Martin") }
    scala1 diff scala2 must_== Diff(JNothing, JNothing, JNothing)
  }

  "Changing value type results in a change diff" in {
    val original = JObject("a" -> JInt(1))
    val changed = JObject("a" -> JString("different"))
    original diff changed must_== Diff(changed, JNothing, JNothing)
  }

  private def read(resource: String) =
    parse(getClass.getResourceAsStream(resource))

}
