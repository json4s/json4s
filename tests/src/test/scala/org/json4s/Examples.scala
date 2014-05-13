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

object NativeExamples extends Examples[Document]("Native") with native.JsonMethods
object JacksonExamples extends Examples[JValue]("Jackson") with jackson.JsonMethods

object Examples {
  import JsonDSL._

  val lotto = """
{
  "lotto":{
    "lotto-id":5,
    "winning-numbers":[2,45,34,23,7,5,3],
    "winners":[ {
      "winner-id":23,
      "numbers":[2,45,34,23,3, 5]
    },{
      "winner-id" : 54 ,
      "numbers":[ 52,3, 12,11,18,22 ]
    }]
  }
}
"""

  val person = """
{
  "person": {
    "name": "Joe",
    "age": 35,
    "spouse": {
      "person": {
        "name": "Marilyn",
        "age": 33
      }
    }
  }
}
"""

  val personDSL =
    ("person" ->
      ("name" -> "Joe") ~
      ("age" -> 35) ~
      ("spouse" ->
        ("person" ->
          ("name" -> "Marilyn") ~
          ("age" -> 33)
        )
      )
    )

  val objArray =
"""
{ "name": "joe",
  "address": {
    "street": "Bulevard",
    "city": "Helsinki"
  },
  "children": [
    {
      "name": "Mary",
      "age": 5
    },
    {
      "name": "Mazy",
      "age": 3
    }
  ]
}
"""

  val nulls = ("f1" -> null) ~ ("f2" -> List(null, "s"))
  val quoted = """["foo \" \n \t \r bar"]"""
  val symbols = ("f1" -> 'foo) ~ ("f2" -> 'bar)
}

abstract class Examples[T](mod: String) extends Specification with JsonMethods[T] {

  import JsonAST.concat
  import Examples._
  import JsonDSL._
  (mod + " Examples") should {

    "Lotto example" in {
      val json = parse(lotto)
      val renderedLotto = compact(render(json))
      json must_== parse(renderedLotto)
    }

    "Person example" in {
      val json = parse(person)
      val renderedPerson = pretty(render(json))
      json must_== parse(renderedPerson)
      render(json) must_== render(personDSL)
      compact(render(json \\ "name")) must_== """{"name":"Joe","name":"Marilyn"}"""
      compact(render(json \ "person" \ "name")) must_== "\"Joe\""
    }

    "Transformation example" in {
      val uppercased = parse(person).transformField { case JField(n, v) => JField(n.toUpperCase, v) }
      val rendered = compact(render(uppercased))
      rendered must_==
        """{"PERSON":{"NAME":"Joe","AGE":35,"SPOUSE":{"PERSON":{"NAME":"Marilyn","AGE":33}}}}"""
    }

    "Remove Field example" in {
      val json = parse(person) removeField { _ == JField("name", "Marilyn") }
      (json \\ "name") must_== JString("Joe")
      compact(render(json \\ "name")) must_== "\"Joe\""
    }

    "Remove example" in {
      val json = parse(person) remove { _ == JString("Marilyn") }
      (json \\ "name") must_== JString("Joe")
      compact(render(json \\ "name")) must_== "\"Joe\""
    }

    "XPath operator should behave the same after adding and removing a second field with the same name" in {
      val json = parse(lotto)
      val addition = parse("""{"lotto-two": {"lotto-id": 6}}""")
      val json2 = json merge addition removeField { _ == JField("lotto-id", 6) }

      (json2 \\ "lotto-id") must_== (json \\ "lotto-id")
    }

    "Queries on person example" in {
      val json = parse(person)
      val filtered = json filterField {
        case JField("name", _) => true
        case _ => false
      }
      filtered must_== List(JField("name", JString("Joe")), JField("name", JString("Marilyn")))

      val found = json findField {
        case JField("name", _) => true
        case _ => false
      }
      found must_== Some(JField("name", JString("Joe")))
    }

    "Object array example" in {
      val json = parse(objArray)
      compact(render(json \ "children" \ "name")) must_== """["Mary","Mazy"]"""
      compact(render((json \ "children")(0) \ "name")) must_== "\"Mary\""
      compact(render((json \ "children")(1) \ "name")) must_== "\"Mazy\""
      (for { JObject(o) <- json; JField("name", JString(y)) <- o } yield y) must_== List("joe", "Mary", "Mazy")
    }

    "Unbox values using XPath-like type expression" in {
      parse(objArray) \ "children" \\ classOf[JInt] must_== List(5, 3)
      parse(lotto) \ "lotto" \ "winning-numbers" \ classOf[JInt] must_== List(2, 45, 34, 23, 7, 5, 3)
      parse(lotto) \\ "winning-numbers" \ classOf[JInt] must_== List(2, 45, 34, 23, 7, 5, 3)
    }

    "Quoted example" in {
      val json = parse(quoted)
      List("foo \" \n \t \r bar") must_== json.values
    }

    "Null example" in {
      compact(render(parse(""" {"name": null} """))) must_== """{"name":null}"""
    }

    "Null rendering example" in {
      compact(render(nulls)) must_== """{"f1":null,"f2":[null,"s"]}"""
    }

    "Symbol example" in {
      compact(render(symbols)) must_== """{"f1":"foo","f2":"bar"}"""
    }

    "Unicode example" in {
      parse("[\" \\u00e4\\u00e4li\\u00f6t\"]") must_== JArray(List(JString(" \u00e4\u00e4li\u00f6t")))
    }

    "Exponent example" in {
      parse("""{"num": 2e5 }""") must_== JObject(List(JField("num", JDouble(200000.0))))
      parse("""{"num": -2E5 }""") must_== JObject(List(JField("num", JDouble(-200000.0))))
      parse("""{"num": 2.5e5 }""") must_== JObject(List(JField("num", JDouble(250000.0))))
      parse("""{"num": 2.5e-5 }""") must_== JObject(List(JField("num", JDouble(2.5e-5))))
    }

    "JSON building example" in {
      val json = JObject(("name", JString("joe")), ("age", JInt(34))) ++ JObject(("name", JString("mazy")), ("age", JInt(31)))
      compact(render(json)) must_== """[{"name":"joe","age":34},{"name":"mazy","age":31}]"""
    }

    "JSON building with implicit primitive conversions example" in {
      import DoubleMode._
      val json = JObject(("name", "joe"), ("age", 34)) ++ JObject(("name", "mazy"), ("age", 31))
      compact(render(json)) must_== """[{"name":"joe","age":34},{"name":"mazy","age":31}]"""
    }

    "Example which collects all integers and forms a new JSON" in {
      val json = parse(person)
      val ints = json.fold(JNothing: JValue) { (a, v) => v match {
        case x: JInt => a ++ x
        case _ => a
      }}
      compact(render(ints)) must_== """[35,33]"""
    }

    "Generate JSON with DSL example" in {
      val json: JValue =
        ("id" -> 5) ~
        ("tags" -> Map("a" -> 5, "b" -> 7))
      compact(render(json)) must_== """{"id":5,"tags":{"a":5,"b":7}}"""
    }

    "Tuple2 example" in {
      implicit val fmts = DefaultFormats
      val json = """{"blah":123}"""
      Extraction.extract[(String, Int)](parse(json)) must_== ("blah" -> 123)
    }

    "List[Tuple2] example" in {
      implicit val fmts = DefaultFormats
      val json = parse("""[{"blah1":13939},{"blah2":3948}]""")
      Extraction.extract[List[(String, Int)]](json) must_== "blah1" -> 13939 :: "blah2" -> 3948 :: Nil
    }

    "List[Animal] example" in {
//      case class Dog(name: String) extends Animal
//      case class Fish(weight: Double) extends Animal
      implicit val fmts = DefaultFormats + ShortTypeHints(List[Class[_]](classOf[Dog], classOf[Fish]))
      val json = parse(s"""[{"name":"pluto","${fmts.typeHintFieldName}":"Dog"},{"weight":1.3,"${fmts.typeHintFieldName}":"Fish"}]""")
      Extraction.extract[List[Animal]](json) must_== Dog("pluto") :: Fish(1.3) :: Nil
    }
  }
}
