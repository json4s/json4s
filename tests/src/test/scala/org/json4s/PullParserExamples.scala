/*
 * Copyright 2010-2011 WorldWide Conferencing, LLC
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

import org.specs.Specification
import native._


/**
 * System under specification for JSON Pull Parser.
 */
object PullParserExamples extends Specification("JSON Pull Parser Examples") {
  import JsonParser._

  "Pull parsing example" in {
    val parser = (p: Parser) => {
      def parse: BigInt = p.nextToken match {
        case FieldStart("postalCode") => p.nextToken match {
          case IntVal(code) => code
          case _ => p.fail("expected int")
        }
        case End => p.fail("no field named 'postalCode'")
        case _ => parse
      }

      parse
    }

    val postalCode = parse(json, parser)
    postalCode mustEqual 10021
  }

  val json = """
  {
     "firstName": "John",
     "lastName": "Smith",
     "address": {
         "streetAddress": "21 2nd Street",
         "city": "New York",
         "state": "NY",
         "postalCode": 10021
     },
     "phoneNumbers": [
         { "type": "home", "number": "212 555-1234" },
         { "type": "fax", "number": "646 555-4567" }
     ],
     "newSubscription": false,
     "companyName": null
 }"""
}
