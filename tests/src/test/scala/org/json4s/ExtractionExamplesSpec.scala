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

import java.util.Date
import org.json4s

import org.scalatest.wordspec.AnyWordSpec
import org.json4s.native.Document
import org.json4s.prefs.ExtractionNullStrategy

class NativeExtractionExamples
  extends ExtractionExamples[Document]("Native", native.Serialization)
  with native.JsonMethods
class JacksonExtractionExamples
  extends ExtractionExamples[JValue]("Jackson", jackson.Serialization)
  with jackson.JsonMethods

abstract class ExtractionExamples[T](mod: String, ser: json4s.Serialization) extends AnyWordSpec with JsonMethods[T] {

  implicit lazy val formats: Formats = DefaultFormats

  val notNullFormats = new DefaultFormats {
    override val extractionNullStrategy = ExtractionNullStrategy.Disallow
  }

  val nullAsAbsentFormats = new DefaultFormats {
    override val extractionNullStrategy = ExtractionNullStrategy.TreatAsAbsent
  }

  val strictFormats = formats.strict

  val nonStrictFormats = formats.nonStrict

  def treeFormats[T] = ser.formats(ShortTypeHints(List(classOf[Node[T]], classOf[Leaf[T]], EmptyLeaf.getClass)))

  (mod + " Extraction Examples Specification") should {
    "Extraction example" in {
      val json = parse(testJson)
      assert(
        json.extract[Person] == Person(
          "joe",
          Address("Bulevard", "Helsinki"),
          List(Child("Mary", 5, Some(date("2004-09-04T18:06:22Z"))), Child("Mazy", 3, None))
        )
      )
    }

    "Extraction with path expression example" in {
      val json = parse(testJson)
      assert((json \ "address").extract[Address] == Address("Bulevard", "Helsinki"))
    }

    "Partial extraction example" in {
      val json = parse(testJson)
      assert(json.extract[SimplePerson] == SimplePerson("joe", Address("Bulevard", "Helsinki")))
    }

    "Extract with a default value" in {
      val json = parse(testJson)
      assert((json \ "address2").extractOrElse(Address("Tie", "Helsinki")) == Address("Tie", "Helsinki"))
    }

    "Extract with default value and secondary constructor" in {
      val json = parse("""{ "a": "A", "b": "B", "int": 5 }""")
      val result = json.extract[SecondaryConstructorCaseClass]
      assert(result == SecondaryConstructorCaseClass(Pair("A", "B"), None, false, 5))
    }

    "Map with primitive values extraction example" in {
      val json = parse(testJson)
      assert(json.extract[PersonWithMap] == PersonWithMap("joe", Map("street" -> "Bulevard", "city" -> "Helsinki")))
    }

    "Map with object values extraction example" in {
      val json = parse(twoAddresses)
      assert(
        json.extract[PersonWithAddresses] == PersonWithAddresses(
          "joe",
          Map("address1" -> Address("Bulevard", "Helsinki"), "address2" -> Address("Soho", "London"))
        )
      )
    }

    "scala.collection.Map extraction example" in {
      val json = parse("""{ "name": "Joe" }""")
      assert(json.extract[scala.collection.Map[String, String]] == scala.collection.Map("name" -> "Joe"))
    }

    "mutable.Map extraction example" in {
      val json = parse("""{ "name": "Joe" }""")
      assert(
        json.extract[scala.collection.mutable.Map[String, String]] == scala.collection.mutable.Map("name" -> "Joe")
      )
    }

    "Simple value extraction example" in {
      val json = parse(testJson)
      assert(json.extract[Name] == Name("joe"))
      assert((json \ "children")(0).extract[Name] == Name("Mary"))
      assert((json \ "children")(1).extract[Name] == Name("Mazy"))
    }

    "Primitive value extraction example" in {
      val json = parse(testJson)
      assert((json \ "name").extract[String] == "joe")
      assert((json \ "name").extractOpt[String] == Some("joe"))
      assert((json \ "name").extractOpt[Int] == None)
      assert(((json \ "children")(0) \ "birthdate").extract[Date] == date("2004-09-04T18:06:22Z"))

      assert(JInt(1).extract[Int] == 1)
      assert(JInt(1).extract[String] == "1")
    }

    "Primitive extraction example" in {
      val json = parse(primitives)
      assert(
        json.extract[Primitives] == Primitives(
          124,
          123L,
          126.5,
          127.5.floatValue,
          "128",
          Symbol("symb"),
          125,
          129.byteValue,
          true
        )
      )
    }

    "Null extraction example" in {
      val json = parse("""{ "name": null, "age": 5, "birthdate": null }""")
      assert(json.extract[Child] == Child(null, 5, None))
    }

    "Date extraction example" in {
      val json = parse("""{"name":"e1","timestamp":"2009-09-04T18:06:22Z"}""")
      assert(json.extract[Event] == Event("e1", date("2009-09-04T18:06:22Z")))
    }

    "Timestamp extraction example" in {
      val json = parse("""{"timestamp":"2009-09-04T18:06:22Z"}""")
      assert(new Date((json \ "timestamp").extract[java.sql.Timestamp].getTime) == date("2009-09-04T18:06:22Z"))
    }

    "Option extraction example" in {
      val json = parse("""{ "name": null, "age": 5, "mother":{"name":"Marilyn"}}""")
      assert(json.extract[OChild] == OChild(None, 5, Some(Parent("Marilyn")), None))
    }

    "Option extraction example with strictOptionParsing" in {
      // JNull should not extract to None
      val fm = notNullFormats.withStrictOptionParsing

      assertThrows[MappingException] {
        parse("""{ "name": null, "age": 5, "mother":{"name":"Marilyn"}}""")
          .extract[OChild](fm, implicitly[Manifest[OChild]])
      }

      val mf = implicitly[Manifest[OptionValue]]
      assertThrows[MappingException] { parse("""{"value": null}""").extract[OptionValue](fm, mf) }
      assert(parse("""{}""").extract[OptionValue](fm, mf) == OptionValue(None))
      assert(parse("""{"value": 1}""").extract[OptionValue](fm, mf) == OptionValue(Some(1)))
    }

    "Missing JSON array extracted as an empty List (no default value) when strictArrayExtraction is false" in {
      assert(
        parse(missingChildren).extract[Person](nonStrictFormats, implicitly[Manifest[Person]]) == Person(
          "joe",
          Address("Bulevard", "Helsinki"),
          Nil
        )
      )
    }

    "Missing JSON array extracted as an empty List (has default value) when strictArrayExtraction is false" in {
      assert(
        parse(missingChildren).extract[PersonNoKids](
          nonStrictFormats,
          implicitly[Manifest[PersonNoKids]]
        ) == PersonNoKids("joe", Address("Bulevard", "Helsinki"), Nil)
      )
    }

    "Missing JSON array fails extraction (no default value) when strictArrayExtraction is true" in {
      assertThrows[MappingException] {
        parse(missingChildren).extract[Person](strictFormats, implicitly[Manifest[Person]])
      }
    }

    "Missing JSON array extracted as an empty List (has default value) when strictArrayExtraction is true" in {
      assert(
        parse(missingChildren).extract[PersonNoKids](
          strictFormats,
          implicitly[Manifest[PersonNoKids]]
        ) == PersonNoKids("joe", Address("Bulevard", "Helsinki"), Nil)
      )
    }

    "Missing JSON object extracted as an empty Map (no default value) when strictMapExtraction is false" in {
      assert(
        parse(noAddress).extract[PersonWithMap](
          nonStrictFormats,
          implicitly[Manifest[PersonWithMap]]
        ) == PersonWithMap("joe", Map())
      )
    }

    "Missing JSON object extracted as an empty Map (has default value) when strictMapExtraction is false" in {
      assert(
        parse(noAddress).extract[PersonWithDefaultEmptyMap](
          nonStrictFormats,
          implicitly[Manifest[PersonWithDefaultEmptyMap]]
        ) == PersonWithDefaultEmptyMap("joe", Map())
      )
    }

    "Missing JSON object fails extraction (no default value) when strictMapExtraction is true" in {
      assertThrows[MappingException] {
        parse(noAddress)
          .extract[PersonWithMap](strictFormats, implicitly[Manifest[PersonWithMap]])
      }
    }

    "Missing JSON object extracted as an empty Map (has default value) when strictMapExtraction is true" in {
      assert(
        parse(noAddress).extract[PersonWithDefaultEmptyMap](
          strictFormats,
          implicitly[Manifest[PersonWithDefaultEmptyMap]]
        ) == PersonWithDefaultEmptyMap("joe", Map())
      )
    }

    "Multidimensional array extraction example" in {
      assert(
        parse(multiDimensionalArrays).extract[MultiDim] == MultiDim(
          List(List(List(1, 2), List(3)), List(List(4), List(5, 6))),
          List(List(Name("joe"), Name("mary")), List(Name("mazy")))
        )
      )
    }

    "Flatten example with simple case class" in {
      val f = Extraction.flatten(Extraction.decompose(SimplePerson("joe", Address("Bulevard", "Helsinki"))))
      val e = Map(".name" -> "\"joe\"", ".address.street" -> "\"Bulevard\"", ".address.city" -> "\"Helsinki\"")

      assert(f == e)
    }

    "Unflatten example with top level string and int" in {
      val m = Map(".name" -> "\"joe\"", ".age" -> "32")

      assert(Extraction.unflatten(m) == JObject(List(JField("name", JString("joe")), JField("age", JInt(32)))))
    }

    "Unflatten example with top level string and double" in {
      val m = Map(".name" -> "\"joe\"", ".age" -> "32.2")

      assert(Extraction.unflatten(m) == JObject(List(JField("name", JString("joe")), JField("age", JDouble(32.2)))))
    }

    "Unflatten example with two-level string properties" in {
      val m = Map(".name" -> "\"joe\"", ".address.street" -> "\"Bulevard\"", ".address.city" -> "\"Helsinki\"")

      assert(
        Extraction.unflatten(m) == JObject(
          List(
            JField("name", JString("joe")),
            JField("address", JObject(List(JField("street", JString("Bulevard")), JField("city", JString("Helsinki")))))
          )
        )
      )
    }

    "Unflatten example with top level array" in {
      val m = Map(".foo[2]" -> "2", ".foo[0]" -> "0", ".foo[1]" -> "1")

      assert(Extraction.unflatten(m) == JObject(List(JField("foo", JArray(List(JInt(0), JInt(1), JInt(2)))))))
    }

    "Unflatten example with field name is prefix of the other field name" in {
      val m = Map(".data" -> "5", ".data_type" -> "6")

      assert(Extraction.unflatten(m) == JObject(JField("data", JInt(5)), JField("data_type", JInt(6))))
    }

    "Flatten and unflatten are symmetric" in {
      val parsed = parse(testJson)

      assert(Extraction.unflatten(Extraction.flatten(parsed)) == parsed)
    }

    "Flatten preserves empty sets" in {
      val s = SetWrapper(Set())

      assert(Extraction.flatten(Extraction.decompose(s)).get(".set") == Some("[]"))
    }

    "Flatten and unflatten are symmetric with empty sets" in {
      val s = SetWrapper(Set())

      assert(Extraction.unflatten(Extraction.flatten(Extraction.decompose(s))).extract[SetWrapper] == s)
    }

    "List extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[List[Name]] == List(Name("Mary"), Name("Mazy")))
    }

    "Map extraction example" in {
      val json = parse(testJson) \ "address"
      assert(json.extract[Map[String, String]] == Map("street" -> "Bulevard", "city" -> "Helsinki"))
    }

    "Set extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[Set[Name]] == Set(Name("Mary"), Name("Mazy")))
    }

    "Seq extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[Seq[Name]] == Seq(Name("Mary"), Name("Mazy")))
    }

    "Mutable set extraction example" in {
      val json = parse(testJson) \ "children"
      assert(
        json.extract[scala.collection.mutable.Set[Name]] == scala.collection.mutable.Set(Name("Mary"), Name("Mazy"))
      )
    }

    "Mutable seq extraction example" in {
      val json = parse(testJson) \ "children"
      assert(
        json.extract[scala.collection.mutable.Seq[Name]] == scala.collection.mutable.Seq(Name("Mary"), Name("Mazy"))
      )
    }

    // https://github.com/json4s/json4s/issues/82
    "Vector extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[Vector[Name]] == Vector(Name("Mary"), Name("Mazy")))
    }

    "Stream extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[Stream[Name]] == Stream(Name("Mary"), Name("Mazy")))
    }

    "Iterable extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[Iterable[Name]] == Iterable(Name("Mary"), Name("Mazy")))
    }

    "Immutable Queue extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[collection.immutable.Queue[Name]] == collection.immutable.Queue(Name("Mary"), Name("Mazy")))
    }

    "Mutable Queue extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[collection.mutable.Queue[Name]] == collection.mutable.Queue(Name("Mary"), Name("Mazy")))
    }

    "Immutable HashSet extraction example" in {
      val json = parse(testJson) \ "children"
      assert(
        json.extract[collection.immutable.HashSet[Name]] == collection.immutable.HashSet(Name("Mary"), Name("Mazy"))
      )
    }

    "Mutable HashSet extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[collection.mutable.HashSet[Name]] == collection.mutable.HashSet(Name("Mary"), Name("Mazy")))
    }

    "ArrayBuffer extraction example" in {
      val json = parse(testJson) \ "children"
      assert(
        json.extract[collection.mutable.ArrayBuffer[Name]] == collection.mutable.ArrayBuffer(
          Name("Mary"),
          Name("Mazy")
        )
      )
    }

    "ListBuffer extraction example" in {
      val json = parse(testJson) \ "children"
      assert(
        json
          .extract[collection.mutable.ListBuffer[Name]] == collection.mutable.ListBuffer(Name("Mary"), Name("Mazy"))
      )
    }

    "Mutable Stack extraction example" in {
      val json = parse(testJson) \ "children"
      assert(json.extract[collection.mutable.Stack[Name]] == collection.mutable.Stack(Name("Mary"), Name("Mazy")))
    }

    "ArraySeq extraction example" in {
      val scalaV = scala.util.Properties.versionNumberString
      if (scalaV.startsWith("2.11") || scalaV.startsWith("2.12")) {
        val json = parse(testJson) \ "children"
        assert(
          json.extract[collection.mutable.ArraySeq[Name]] == collection.mutable.ArraySeq(Name("Mary"), Name("Mazy"))
        )
      } else {
        // Scala 2.13 ArraySeq.apply take ClassTag parameter
        assert(1 == 1)
      }
    }

    "Extraction and decomposition are symmetric" in {
      val person = parse(testJson).extract[Person]
      assert(Extraction.decompose(person).extract[Person] == person)
    }

    "Extraction failure message example" in {
      val json = parse("""{"city":"San Francisco"}""")
      try {
        json.extract[Address]
        fail()
      } catch {
        case e: MappingException =>
          assert(
            e.getMessage == "No usable value for street\nDid not find value which can be converted into java.lang.String"
          )
      }
    }

    "Best matching constructor selection example" in {
      assert(
        parse("""{"name":"john","age":32,"size":"M"}""")
          .extract[MultipleConstructors] == MultipleConstructors("john", 32, Some("M"))
      )

      assert(
        parse("""{"name":"john","age":32}""")
          .extract[MultipleConstructors] == MultipleConstructors("john", 32, Some("S"))
      )

      assert(
        parse("""{"name":"john","foo":"xxx"}""").extract[MultipleConstructors] == MultipleConstructors("john", 30, None)
      )

      assert(
        parse("""{"name":"john","age":32,"size":null}""")
          .extract[MultipleConstructors] == MultipleConstructors("john", 32, None)
      )

      assert(
        parse("""{"birthYear":1990,"name":"john","foo":2}""")
          .extract[MultipleConstructors] == MultipleConstructors("john", 20, None)
      )

      assert(
        parse("""{"foo":2,"age":12,"size":"XS"}""")
          .extract[MultipleConstructors] == MultipleConstructors("unknown", 12, Some("XS"))
      )
    }

    "Partial JSON extraction" in {
      assert(parse(stringField).extract[ClassWithJSON] == ClassWithJSON("one", JString("msg")))
      assert(
        parse(objField).extract[ClassWithJSON] == ClassWithJSON("one", JObject(List(JField("yes", JString("woo")))))
      )
    }

    "Double can be coerced to Int or Long" in {
      assert(JDouble(2.1).extract[Int] == 2)
      assert(JDouble(2.1).extract[Long] == 2L)
    }

    "Map with nested non-polymorphic list extraction example" in {
      assert(parse("""{"a":["b"]}""").extract[Map[String, List[String]]] == Map("a" -> List("b")))
    }

    "List with nested non-polymorphic list extraction example" in {
      assert(parse("""[["a"]]""").extract[List[List[String]]] == List(List("a")))
    }

    "Complex nested non-polymorphic collections extraction example" in {
      assert(
        parse("""{"a":[{"b":"c"}]}""").extract[Map[String, List[Map[String, String]]]] == Map(
          "a" -> List(Map("b" -> "c"))
        )
      )
    }

    "format nullExtractionStrategy set to Disallow should disallow null values in extraction for class types" in {
      try {
        parse("""{"name":"foobar","address":null}""")
          .extract[SimplePerson](notNullFormats, Manifest.classType(classOf[SimplePerson]))
        fail()
      } catch {
        case e: MappingException =>
          assert(
            e.getMessage == "No usable value for address\nDid not find value which can be converted into org.json4s.Address"
          )
      }
    }

    "format nullExtractionStrategy set to TreatAsAbsent should disallow null values in extraction for class types without default values" in {
      try {
        parse("""{"name":"foobar","address":null}""").extract[SimplePerson](
          nullAsAbsentFormats,
          Manifest.classType(classOf[SimplePerson])
        )
        fail()
      } catch {
        case e: MappingException =>
          assert(e.getMessage == "No usable value for address\nExpected value but got null")
      }
    }

    "format nullExtractionStrategy set to Disallow should disallow null values in extraction for primitive types" in {
      try {
        parse("""{"name":null}""").extract[Name](notNullFormats, Manifest.classType(classOf[Name]))
        fail()
      } catch {
        case e: MappingException =>
          assert(
            e.getMessage == "No usable value for name\nDid not find value which can be converted into java.lang.String"
          )
      }
    }

    "format nullExtractionStrategy set to TreatAsAbsent should disallow null values in extraction for primitive types without default values" in {
      try {
        parse("""{"name":null}""").extract[Name](nullAsAbsentFormats, Manifest.classType(classOf[Name]))
        fail()
      } catch {
        case e: MappingException =>
          assert(e.getMessage == "No usable value for name\nExpected value but got null")
      }
    }

    "format nullExtractionStrategy set to Disallow should extract a null Option[T] as None" in {
      assert(
        parse("""{"name":null,"age":22}""")
          .extract[OChild](notNullFormats, Manifest.classType(classOf[OChild])) == new OChild(None, 22, None, None)
      )
    }

    "format nullExtractionStrategy set to TreatAsAbsent should extract a null Option[T] as None" in {
      assert(
        parse("""{"name":null,"age":22}""").extract[OChild](
          nullAsAbsentFormats,
          Manifest.classType(classOf[OChild])
        ) == new OChild(None, 22, None, None)
      )
    }

    "format nullExtractionStrategy set to Disallow should disallow null values in extraction for class types with default values" in {
      try {
        parse("""{"name":"foobar","address":null}""").extract[PersonWithDefaultValues](
          notNullFormats,
          Manifest.classType(classOf[PersonWithDefaultValues])
        )
        fail()
      } catch {
        case e: MappingException =>
          assert(
            e.getMessage == "No usable value for address\nDid not find value which can be converted into org.json4s.Address"
          )
      }
    }

    "format nullExtractionStrategy set to TreatAsAbsent should ignore null values in extraction for class types with default values. 1" in {
      assert(
        parse("""{"name":null}""").extract[PersonWithDefaultValues](
          nullAsAbsentFormats,
          Manifest.classType(classOf[PersonWithDefaultValues])
        ) == PersonWithDefaultValues()
      )
    }

    "format nullExtractionStrategy set to Disallow should disallow null values in extraction for collection types" in {
      try {
        parse("""[1,null,3]""").extract[Seq[Int]](notNullFormats, implicitly)
        fail()
      } catch {
        case e: MappingException =>
          assert(e.getMessage == "Did not find value which can be converted into int")
      }
    }

    "format nullExtractionStrategy set to TreatAsAbsent should ignore null values in extraction for class types with default values. 2" in {
      assert(parse("""[1,null,3]""").extract[Seq[Int]](nullAsAbsentFormats, implicitly) == Seq(1, 3))
    }

    "format nullExtractionStrategy set to Disallow should use custom null serializer to set Option[T] as None" in {
      object CustomNull
        extends CustomSerializer[Null](_ =>
          (
            {
              case JNothing => null
              case JNull => null
              case JString("") => null
            },
            { case _ =>
              JString("")
            }
          )
        )
      assert(
        parse("""{"name":null,"age":22, "mother": ""}""").extract[OChild](
          notNullFormats + CustomNull,
          Manifest.classType(classOf[OChild])
        ) == new OChild(None, 22, None, None)
      )
    }

    "format nullExtractionStrategy set to TreatAsAbsent should use custom null serializer to set Option[T] as None" in {
      object CustomNull
        extends CustomSerializer[Null](_ =>
          (
            {
              case JNothing => null
              case JNull => null
              case JString("") => null
            },
            { case _ =>
              JString("")
            }
          )
        )
      assert(
        parse("""{"name":null,"age":22, "mother": ""}""").extract[OChild](
          nullAsAbsentFormats + CustomNull,
          Manifest.classType(classOf[OChild])
        ) == new OChild(None, 22, None, None)
      )
    }

    "simple case objects should be successfully extracted as a singleton instance" in {
      assert(
        parse(emptyTree)
          .extract[LeafTree[Int]](treeFormats, Manifest.classType(classOf[LeafTree[Int]])) == LeafTree.empty
      )
    }

    "case objects in a complex structure should be successfully extracted as a singleton instance" in {
      assert(
        parse(tree).extract[LeafTree[Int]](treeFormats[Int], Manifest.classType(classOf[LeafTree[Int]])) == Node(
          List[LeafTree[Int]](EmptyLeaf, Node(List.empty), Leaf(1), Leaf(2))
        )
      )
    }

    "#274 Examples with default value should be parsed" in {
      val res = WithDefaultValueHolder(Seq(WithDefaultValue("Bob")))
      assert(
        parse("""{"values":[{"name":"Bob","gender":"male"}]}""").extract[WithDefaultValueHolder](
          DefaultFormats,
          Manifest.classType(classOf[WithDefaultValueHolder])
        ) == res
      )
    }

    "#537 Example with a Seq and default Seq value should be extracted from empty json" in {
      val res = SeqWithDefaultSeq(values = Nil)
      assert(parse("""{ }""").extract[SeqWithDefaultSeq] == res)
    }

  }

  val testJson =
    """
{ "name": "joe",
  "address": {
    "street": "Bulevard",
    "city": "Helsinki"
  },
  "children": [
    {
      "name": "Mary",
      "age": 5,
      "birthdate": "2004-09-04T18:06:22Z"
    },
    {
      "name": "Mazy",
      "age": 3
    }
  ]
}
"""

  val maryChildJson =
    """
      |{
      |  "name": "Mary",
      |  "age": 5,
      |  "birthdate": "2004-09-04T18:06:22Z"
      |}
    """.stripMargin

  val missingChildren =
    """
{
  "name": "joe",
  "address": {
    "street": "Bulevard",
    "city": "Helsinki"
  }
}
"""

  val noAddress =
    """
{
  "name": "joe"
}
"""

  val twoAddresses =
    """
{
  "name": "joe",
  "addresses": {
    "address1": {
      "street": "Bulevard",
      "city": "Helsinki"
    },
    "address2": {
      "street": "Soho",
      "city": "London"
    }
  }
}
"""

  val primitives =
    """
{
  "l": 123,
  "i": 124,
  "sh": 125,
  "d": 126.5,
  "f": 127.5,
  "s": "128",
  "b": 129,
  "bool": true,
  "sym":"symb"
}
"""

  val multiDimensionalArrays =
    """
{
  "ints": [[[1, 2], [3]], [[4], [5, 6]]],
  "names": [[{"name": "joe"}, {"name": "mary"}], [{"name": "mazy"}]]
}
"""

  val stringField =
    """
{
  "name": "one",
  "message": "msg"
}
"""

  val objField =
    """
{
  "name": "one",
  "message": {
    "yes": "woo"
  }
}
"""

  val emptyTree =
    """
      |{
      |  "jsonClass":"EmptyLeaf$"
      |}
    """.stripMargin

  val tree =
    """
      |{
      |  "jsonClass":"Node",
      |  "children":[
      |    {
      |      "jsonClass":"EmptyLeaf$"
      |    },
      |    {
      |      "jsonClass":"Node",
      |      "children":[]
      |    },
      |    {
      |      "jsonClass":"Leaf",
      |      "value":1
      |    },
      |    {
      |      "jsonClass":"Leaf",
      |      "value":2
      |    }
      |  ]
      |}
    """.stripMargin

  def date(s: String) = DefaultFormats.dateFormat.parse(s).get
}

case class SetWrapper(set: Set[String])

case class Person(name: String, address: Address, children: List[Child])
case class PersonNoKids(name: String, address: Address, children: List[Child] = Nil)
case class Address(street: String, city: String)
case class Child(name: String, age: Int, birthdate: Option[java.util.Date])

case class SimplePerson(name: String, address: Address)

case class PersonWithDefaultValues(name: String = "No name", address: Address = Address("No street", "No city"))
case class PersonWithMap(name: String, address: Map[String, String])
case class PersonWithDefaultEmptyMap(name: String, address: Map[String, String] = Map.empty)
case class PersonWithAddresses(name: String, addresses: Map[String, Address])

case class Name(name: String)

case class Primitives(i: Int, l: Long, d: Double, f: Float, s: String, sym: Symbol, sh: Short, b: Byte, bool: Boolean)

case class OChild(name: Option[String], age: Int, mother: Option[Parent], father: Option[Parent])
case class Parent(name: String)

case class Event(name: String, timestamp: Date)

case class MultiDim(ints: List[List[List[Int]]], names: List[List[Name]])

case class MultipleConstructors(name: String, age: Int, size: Option[String]) {
  def this(name: String) = this(name, 30, None)
  def this(age: Int, name: String) = this(name, age, Some("S"))
  def this(name: String, birthYear: Int) = this(name, 2010 - birthYear, None)
  def this(size: Option[String], age: Int) = this("unknown", age, size)
}

case class ClassWithJSON(name: String, message: JValue)

sealed trait LeafTree[+T]
object LeafTree {
  def empty[T]: LeafTree[T] = EmptyLeaf
}

case class Node[T](children: List[LeafTree[T]]) extends LeafTree[T]
case class Leaf[T](value: T) extends LeafTree[T]
case object EmptyLeaf extends LeafTree[Nothing]

case class WithDefaultValueHolder(values: Seq[WithDefaultValue])
case class WithDefaultValue(name: String, gender: String = "male")

case class SeqWithDefaultSeq(values: Seq[String], values2: Seq[String] = Seq("1", "2", "3"))

case class Pair(a: String, b: String)

case class SecondaryConstructorCaseClass(pair: Pair, test: Option[String], createdUsingCtor: Boolean = true, int: Int) {
  def this(a: String, b: String, test: Option[String], int: Int) = this(Pair(a, b), test, false, int)
}
case class OptionValue(value: Option[Int])
