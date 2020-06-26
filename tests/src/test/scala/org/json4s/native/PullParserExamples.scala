package org.json4s


import org.specs2.mutable.Specification


/**
* System under specification for JSON Pull Parser.
*/
class PullParserExamples extends Specification {

  import native.JsonParser
  import JsonParser._

  "A JSON Pull Parser" should {
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

      val postalCode = JsonParser.parse(json, parser)
      postalCode must_== 10021
    }
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
