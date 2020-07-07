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

class NativeMergeExamples extends MergeExamples[Document]("Native") with native.JsonMethods
object NativeMergeExamples extends NativeMergeExamples
class JacksonMergeExamples extends MergeExamples[JValue]("Jackson") with jackson.JsonMethods
abstract class MergeExamples[T](mod: String) extends Specification with JsonMethods[T] {

  (mod+" Merge Examples") should {
    "Merge example" in {
      (scala1 merge scala2) must_== expectedMergeResult
    }
  }

  lazy val scala1 = parse("""
    {
      "lang": "scala",
      "year": 2006,
      "tags": ["fp", "oo"],
      "features": {
        "key1":"val1",
        "key2":"val2"
      }
    }""")

  lazy val scala2 = parse("""
    {
      "tags": ["static-typing","fp"],
      "compiled": true,
      "lang": "scala",
      "features": {
        "key2":"newval2",
        "key3":"val3"
      }
    }""")

  lazy val expectedMergeResult = parse("""
    {
      "lang": "scala",
      "year": 2006,
      "tags": ["fp", "oo", "static-typing"],
      "features": {
        "key1":"val1",
        "key2":"newval2",
        "key3":"val3"
      },
      "compiled": true
    }""")

  "Lotto example" in {
    (lotto1 merge lotto2) must_== mergedLottoResult
  }

  lazy val lotto1 = parse("""
    {
      "lotto":{
        "lotto-id":5,
        "winning-numbers":[2,45,34,23,7,5,3],
        "winners":[{
          "winner-id":23,
          "numbers":[2,45,34,23,3,5]
        }]
      }
    }""")

  lazy val lotto2 = parse("""
    {
      "lotto":{
        "winners":[{
          "winner-id":54,
          "numbers":[52,3,12,11,18,22]
        }]
      }
    }""")

  lazy val mergedLottoResult = parse("""
    {
      "lotto":{
        "lotto-id":5,
        "winning-numbers":[2,45,34,23,7,5,3],
        "winners":[{
          "winner-id":23,
          "numbers":[2,45,34,23,3,5]
        },{
          "winner-id":54,
          "numbers":[52,3,12,11,18,22]
        }]
      }
    }""")
}
