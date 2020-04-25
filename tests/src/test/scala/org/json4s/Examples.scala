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

import org.json4s.prefs.EmptyValueStrategy
import org.specs2.mutable.Specification
import org.json4s.native.Document

class NativeExamples extends Examples[Document]("Native") with native.JsonMethods {
  import JsonDSL._

  "issue 482 Infinity" in {
    val value = Map("a" -> Double.PositiveInfinity, "b" -> Double.NegativeInfinity)
    val json = compact(render(value))
    parse(json) must_== JObject(List(
      ("a", JDouble(Double.PositiveInfinity)),
      ("b", JDouble(Double.NegativeInfinity))
    ))
  }
}

object JacksonExamples extends Examples[JValue]("Jackson") with jackson.JsonMethods

object Examples {
  import JsonDSL._

  val lotto = """
{
  "lotto":{
    "lotto-id":5,
    "winning-numbers":[2,45,34,23,7,5,3],
    "lucky-number":[7],
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

  val objArray2 =
"""
{ "name": "joe",
  "address": {
    "street": "Bulevard",
    "city": "Helsinki"
  },
  "children": [
    {
      "name": "Mary",
      "age": 2
    }
  ]
}
"""

  val nulls = JObject("f1" -> null) ~ ("f2" -> List(null, "s"))
  val quoted = """["foo \" \n \t \r bar"]"""
  val symbols = ("f1" -> Symbol("foo")) ~ ("f2" -> Symbol("bar"))
}

abstract class Examples[T](mod: String) extends Specification with JsonMethods[T] {

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

    "Object array example 2" in {
      compact(render(parse(lotto) \ "lotto" \ "lucky-number")) must_== """[7]"""
      compact(render(parse(objArray2) \ "children" \ "name")) must_== """["Mary"]"""
    }

    // https://github.com/json4s/json4s/issues/562
    "Object array example 3" in {
      parse("{\"a\" : []}") \ "a" \ "c" must_== JNothing
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

    // ------------------------------------------------------------
    // Working examples for GitHub issues
    // ------------------------------------------------------------

    "#278 strange behavior of filterField" in {
      val jString = """{
  "longt" : "-0.000000",
  "latt" : "0.00000",
  "error" : {
    "description" : "Your request did not produce any results. Check your spelling and try again.",
    "code" : "008"
  }
}"""
      val jAst = parse(jString)
      val filtered1: List[(String, JValue)] = jAst.filterField{
        case JField("longt", _) => false
        case _ => true
      }
      filtered1.exists(_._1 == "longt") must_== false
      filtered1.exists(_._1 == "error") must_== true
      // These assertions fail
      // filtered1.exists(_._1 == "description") must_== false
      // filtered1.exists(_._1 == "code") must_== false

      val filtered2: List[(String, JValue)] = jAst.filterField{
        case JField("error", _) => false
        case _ => true
      }
      filtered2.exists(_._1 == "longt") must_== true
      filtered2.exists(_._1 == "error") must_== false
      // These assertions fail
      // filtered2.exists(_._1 == "description") must_== false
      // filtered2.exists(_._1 == "code") must_== false
    }

    "#23 Change serialization of Seq[Option[T]] so it doesn't remove elements" in {
      val a: List[Option[Int]] = List(Some(1), None, None, Some(1))
      compact(render(a)) must_== "[1,1]"

      // #131 Strategies for empty value treatment
      // https://github.com/json4s/json4s/pull/131
      val preserve = new DefaultFormats {
        override val emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.preserve
      }
      compact(render(a)(preserve)) must_== "[1,null,null,1]"
    }

    "#146 Snake support with case classes" in {
      implicit val f = DefaultFormats
      val json = """{"full_name": "Kazuhiro Sera", "github_account_name": "seratch"}"""
      val expected = Issue146CamelCaseClass("Kazuhiro Sera", Some("seratch"))
      val actual = Extraction.extract[Issue146CamelCaseClass](jackson.parseJson(json).camelizeKeys)
      actual must_== expected
    }

    "#545 snake support with case classes and uuid keys" in {
      implicit val f = DefaultFormats
      val caseClass = Issue545CamelCaseClassWithUUID(Map("565b2803-fdb9-4359-8c39-da1a347d76ca" -> "awesome"))
      val expected = """{"my_map":{"565b2803-fdb9-4359-8c39-da1a347d76ca":"awesome"}}"""
      val unexpected = """{"my_map":{"565b2803_fdb9_4359_8c39_da1a347d76ca":"awesome"}}"""
      val future = compact(render(Extraction.decompose(caseClass).underscoreCamelCaseKeysOnly))
      val actual = compact(render(Extraction.decompose(caseClass).underscoreKeys))
      actual must_== unexpected
      future must_== expected
    }

    "Camelize should not fail on json with empty keys." in {
      implicit val f = DefaultFormats
      val json = """{"full_name": "Kazuhiro Sera", "github_account_name": "seratch", "" : ""}"""
      val expected = Issue146CamelCaseClass("Kazuhiro Sera", Some("seratch"))
      val actual = Extraction.extract[Issue146CamelCaseClass](jackson.parseJson(json).camelizeKeys)
      actual must_== expected
    }
  }
}
private case class Issue545CamelCaseClassWithUUID(myMap: Map[String, String])
private case class Issue146CamelCaseClass(fullName: String, githubAccountName: Option[String])
