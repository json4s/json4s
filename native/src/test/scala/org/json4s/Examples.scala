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
import org.scalatest.wordspec.AnyWordSpec
import org.json4s.native.Document

class NativeExamples extends Examples[Document]("Native") with native.JsonMethods {
  import JsonDSL._

  "issue 482 Infinity" in {
    val value = Map("a" -> Double.PositiveInfinity, "b" -> Double.NegativeInfinity)
    val json = compact(render(value))
    assert(
      parse(json) == JObject(
        List(
          ("a", JDouble(Double.PositiveInfinity)),
          ("b", JDouble(Double.NegativeInfinity))
        )
      )
    )
  }
}

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
    "person" ->
    ("name" -> "Joe") ~
    ("age" -> 35) ~
    ("spouse" ->
    ("person" ->
    ("name" -> "Marilyn") ~
    ("age" -> 33)))

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

abstract class Examples[T](mod: String) extends AnyWordSpec with JsonMethods[T] {

  import Examples._
  import JsonDSL._

  (mod + " Examples") should {

    "Lotto example" in {
      val json = parse(lotto)
      val renderedLotto = compact(render(json))
      assert(json == parse(renderedLotto))
    }

    "Person example" in {
      val json = parse(person)
      val renderedPerson = pretty(render(json))
      assert(json == parse(renderedPerson))
      assert(render(json) == render(personDSL))
      assert(compact(render(json \\ "name")) == """{"name":"Joe","name":"Marilyn"}""")
      assert(compact(render(json \ "person" \ "name")) == "\"Joe\"")
    }

    "Transformation example" in {
      val uppercased = parse(person).transformField { case JField(n, v) => JField(n.toUpperCase, v) }
      val rendered = compact(render(uppercased))
      assert(rendered == """{"PERSON":{"NAME":"Joe","AGE":35,"SPOUSE":{"PERSON":{"NAME":"Marilyn","AGE":33}}}}""")
    }

    "Remove Field example" in {
      val json = parse(person) removeField { _ == JField("name", "Marilyn") }
      assert((json \\ "name") == JString("Joe"))
      assert(compact(render(json \\ "name")) == "\"Joe\"")
    }

    "Remove example" in {
      val json = parse(person) remove { _ == JString("Marilyn") }
      assert((json \\ "name") == JString("Joe"))
      assert(compact(render(json \\ "name")) == "\"Joe\"")
    }

    "XPath operator should behave the same after adding and removing a second field with the same name" in {
      val json = parse(lotto)
      val addition = parse("""{"lotto-two": {"lotto-id": 6}}""")
      val json2 = json merge addition removeField { _ == JField("lotto-id", 6) }

      assert((json2 \\ "lotto-id") == (json \\ "lotto-id"))
    }

    "Queries on person example" in {
      val json = parse(person)
      val filtered = json filterField {
        case JField("name", _) => true
        case _ => false
      }
      assert(filtered == List(JField("name", JString("Joe")), JField("name", JString("Marilyn"))))

      val found = json findField {
        case JField("name", _) => true
        case _ => false
      }
      assert(found == Some(JField("name", JString("Joe"))))
    }

    "Object array example" in {
      val json = parse(objArray)
      assert(compact(render(json \ "children" \ "name")) == """["Mary","Mazy"]""")
      assert(compact(render((json \ "children")(0) \ "name")) == "\"Mary\"")
      assert(compact(render((json \ "children")(1) \ "name")) == "\"Mazy\"")
      assert((for { JObject(o) <- json; JField("name", JString(y)) <- o } yield y) == List("joe", "Mary", "Mazy"))
    }

    "Object array example 2" in {
      assert(compact(render(parse(lotto) \ "lotto" \ "lucky-number")) == """[7]""")
      assert(compact(render(parse(objArray2) \ "children" \ "name")) == """["Mary"]""")
    }

    // https://github.com/json4s/json4s/issues/562
    "Object array example 3" in {
      assert({ parse("{\"a\" : []}") \ "a" \ "c" } == JNothing)
    }

    "Unbox values using XPath-like type expression" in {
      assert({ parse(objArray) \ "children" \\ classOf[JInt] } == List(5, 3))
      assert({ parse(lotto) \ "lotto" \ "winning-numbers" \ classOf[JInt] } == List(2, 45, 34, 23, 7, 5, 3))
      assert({ parse(lotto) \\ "winning-numbers" \ classOf[JInt] } == List(2, 45, 34, 23, 7, 5, 3))
    }

    "Quoted example" in {
      val json = parse(quoted)
      assert(List("foo \" \n \t \r bar") == json.values)
    }

    "Null example" in {
      assert(compact(render(parse(""" {"name": null} """))) == """{"name":null}""")
    }

    "Null rendering example" in {
      assert(compact(render(nulls)) == """{"f1":null,"f2":[null,"s"]}""")
    }

    "Symbol example" in {
      assert(compact(render(symbols)) == """{"f1":"foo","f2":"bar"}""")
    }

    "Unicode example" in {
      assert(parse("[\" \\u00e4\\u00e4li\\u00f6t\"]") == JArray(List(JString(" \u00e4\u00e4li\u00f6t"))))
    }

    "Exponent example" in {
      assert(parse("""{"num": 2e5 }""") == JObject(List(JField("num", JDouble(200000.0)))))
      assert(parse("""{"num": -2E5 }""") == JObject(List(JField("num", JDouble(-200000.0)))))
      assert(parse("""{"num": 2.5e5 }""") == JObject(List(JField("num", JDouble(250000.0)))))
      assert(parse("""{"num": 2.5e-5 }""") == JObject(List(JField("num", JDouble(2.5e-5)))))
    }

    "JSON building example" in {
      val json =
        JObject(("name", JString("joe")), ("age", JInt(34))) ++ JObject(("name", JString("mazy")), ("age", JInt(31)))
      assert(compact(render(json)) == """[{"name":"joe","age":34},{"name":"mazy","age":31}]""")
    }

    "JSON building with implicit primitive conversions example" in {
      import DoubleMode._
      val json = JObject(("name", "joe"), ("age", 34)) ++ JObject(("name", "mazy"), ("age", 31))
      assert(compact(render(json)) == """[{"name":"joe","age":34},{"name":"mazy","age":31}]""")
    }

    "Example which collects all integers and forms a new JSON" in {
      val json = parse(person)
      val ints = json.fold(JNothing: JValue) { (a, v) =>
        v match {
          case x: JInt => a ++ x
          case _ => a
        }
      }
      assert(compact(render(ints)) == """[35,33]""")
    }

    "Generate JSON with DSL example" in {
      val json: JValue =
        ("id" -> 5) ~
        ("tags" -> Map("a" -> 5, "b" -> 7))
      assert(compact(render(json)) == """{"id":5,"tags":{"a":5,"b":7}}""")
    }

    "Tuple2 example" in {
      implicit val fmts: Formats = DefaultFormats
      val json = """{"blah":123}"""
      assert(Extraction.extract[(String, Int)](parse(json)) == ("blah" -> 123))
    }

    "List[Tuple2] example" in {
      implicit val fmts: Formats = DefaultFormats
      val json = parse("""[{"blah1":13939},{"blah2":3948}]""")
      assert(Extraction.extract[List[(String, Int)]](json) == { "blah1" -> 13939 :: "blah2" -> 3948 :: Nil })
    }

    "List[Animal] example" in {
//      case class Dog(name: String) extends Animal
//      case class Fish(weight: Double) extends Animal
      val typeHints = ShortTypeHints(List[Class[_]](classOf[Dog], classOf[Fish]))
      implicit val fmts: Formats = DefaultFormats + typeHints
      val json = parse(
        s"""[{"name":"pluto","${typeHints.typeHintFieldName}":"Dog"},{"weight":1.3,"${typeHints.typeHintFieldName}":"Fish"}]"""
      )
      assert(Extraction.extract[List[Animal]](json) == { Dog("pluto") :: Fish(1.3) :: Nil })
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
      val filtered1: List[(String, JValue)] = jAst.filterField {
        case JField("longt", _) => false
        case _ => true
      }
      assert(filtered1.exists(_._1 == "longt") == false)
      assert(filtered1.exists(_._1 == "error") == true)
      // These assertions fail
      // filtered1.exists(_._1 == "description") must_== false
      // filtered1.exists(_._1 == "code") must_== false

      val filtered2: List[(String, JValue)] = jAst.filterField {
        case JField("error", _) => false
        case _ => true
      }
      assert(filtered2.exists(_._1 == "longt") == true)
      assert(filtered2.exists(_._1 == "error") == false)
      // These assertions fail
      // filtered2.exists(_._1 == "description") must_== false
      // filtered2.exists(_._1 == "code") must_== false
    }

    "#23 Change serialization of Seq[Option[T]] so it doesn't remove elements" in {
      val a: List[Option[Int]] = List(Some(1), None, None, Some(1))
      assert(compact(render(a)) == "[1,1]")

      // #131 Strategies for empty value treatment
      // https://github.com/json4s/json4s/pull/131
      assert(compact(render(a, emptyValueStrategy = EmptyValueStrategy.preserve)) == "[1,null,null,1]")
    }

    "#146 Snake support with case classes" in {
      implicit val f: Formats = DefaultFormats
      val json = """{"full_name": "Kazuhiro Sera", "github_account_name": "seratch"}"""
      val expected = Issue146CamelCaseClass("Kazuhiro Sera", Some("seratch"))
      val actual = Extraction.extract[Issue146CamelCaseClass](parse(json).camelizeKeys)
      assert(actual == expected)
    }

    "#714 Camelize double underscores" in {
      implicit val f: Formats = DefaultFormats
      val json = """{"full__name": "Kazuhiro Sera", "github___________________account___name": "seratch"}"""
      val expected = Issue146CamelCaseClass("Kazuhiro Sera", Some("seratch"))
      val actual = Extraction.extract[Issue146CamelCaseClass](parse(json).camelizeKeys)
      assert(actual == expected)
    }

    "#545 snake support with case classes and uuid keys" in {
      implicit val f: Formats = DefaultFormats
      val caseClass = Issue545CamelCaseClassWithUUID(Map("565b2803-fdb9-4359-8c39-da1a347d76ca" -> "awesome"))
      val expected = """{"my_map":{"565b2803-fdb9-4359-8c39-da1a347d76ca":"awesome"}}"""
      val unexpected = """{"my_map":{"565b2803_fdb9_4359_8c39_da1a347d76ca":"awesome"}}"""
      val future = compact(render(Extraction.decompose(caseClass).underscoreCamelCaseKeysOnly))
      val actual = compact(render(Extraction.decompose(caseClass).underscoreKeys))
      assert(actual == unexpected)
      assert(future == expected)
    }

    "Camelize should not fail on json with empty keys." in {
      implicit val f: Formats = DefaultFormats
      val json = """{"full_name": "Kazuhiro Sera", "github_account_name": "seratch", "" : ""}"""
      val expected = Issue146CamelCaseClass("Kazuhiro Sera", Some("seratch"))
      val actual = Extraction.extract[Issue146CamelCaseClass](parse(json).camelizeKeys)
      assert(actual == expected)
    }

    "Multiple type hint field names should be possible" in {
      implicit val f: Formats = DefaultFormats +
        ShortTypeHints(classOf[Cherry] :: classOf[Oak] :: Nil, "wood") +
        ShortTypeHints(classOf[Iron] :: classOf[IronMaiden] :: Nil, "metal")

      val json =
        """
          |{
          |  "woods": [
          |    {
          |      "wood": "Cherry",
          |      "hardness": 3
          |    },
          |    {
          |      "wood": "Oak",
          |      "hardness": 8
          |    }
          |  ],
          |  "metals": [
          |    {
          |      "metal": "Iron",
          |      "origin": "USA"
          |    },
          |    {
          |      "metal": "IronMaiden",
          |      "origin": "UK"
          |    }
          |  ]
          |}
        """.stripMargin

      val actual = parse(json).extract[Materials]
      val expected = Materials(List(Cherry(3), Oak(8)), List(Iron("USA"), IronMaiden("UK")))

      assert(actual == expected)
    }

  }
}
private case class Issue545CamelCaseClassWithUUID(myMap: Map[String, String])
private case class Issue146CamelCaseClass(fullName: String, githubAccountName: Option[String])

trait Wood
case class Cherry(hardness: Int) extends Wood
case class Oak(hardness: Int) extends Wood

trait Metal
case class Iron(origin: String) extends Metal
case class IronMaiden(origin: String) extends Metal

case class Materials(woods: List[Wood], metals: List[Metal])
