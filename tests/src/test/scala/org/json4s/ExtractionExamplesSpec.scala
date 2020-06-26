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

import org.specs2.mutable.Specification
import org.json4s.native.Document

class NativeExtractionExamples extends ExtractionExamples[Document]("Native", native.Serialization) with native.JsonMethods
class JacksonExtractionExamples extends ExtractionExamples[JValue]("Jackson", jackson.Serialization) with jackson.JsonMethods

abstract class ExtractionExamples[T](mod: String, ser : json4s.Serialization) extends Specification with JsonMethods[T] {

  implicit lazy val formats = DefaultFormats

  val notNullFormats = new DefaultFormats {
    override val allowNull = false
  }

  val strictFormats = formats.strict

  val nonStrictFormats = formats.nonStrict

  def treeFormats[T] = ser.formats(ShortTypeHints(List(classOf[Node[T]], classOf[Leaf[T]], EmptyLeaf.getClass)))

  (mod+" Extraction Examples Specification") should {
    "Extraction example" in {
      val json = parse(testJson)
      json.extract[Person] must_== Person("joe", Address("Bulevard", "Helsinki"), List(Child("Mary", 5, Some(date("2004-09-04T18:06:22Z"))), Child("Mazy", 3, None)))
    }

    "Extraction with path expression example" in {
      val json = parse(testJson)
      (json \ "address").extract[Address] must_== Address("Bulevard", "Helsinki")
    }

    "Partial extraction example" in {
      val json = parse(testJson)
      json.extract[SimplePerson] must_== SimplePerson("joe", Address("Bulevard", "Helsinki"))
    }

    "Extract with a default value" in {
      val json = parse(testJson)
      (json \ "address2").extractOrElse(Address("Tie", "Helsinki")) must_== Address("Tie", "Helsinki")
    }

    "Extract with default value and secondary constructor" in {
      val json = parse("""{ "a": "A", "b": "B", "int": 5 }""")
      val result = json.extract[SecondaryConstructorCaseClass]
      result must_== SecondaryConstructorCaseClass(Pair("A", "B"), None, false, 5)
    }

    "Map with primitive values extraction example" in {
      val json = parse(testJson)
      json.extract[PersonWithMap] must_==
        PersonWithMap("joe", Map("street" -> "Bulevard", "city" -> "Helsinki"))
    }

    "Map with object values extraction example" in {
      val json = parse(twoAddresses)
      json.extract[PersonWithAddresses] must_==
        PersonWithAddresses("joe", Map("address1" -> Address("Bulevard", "Helsinki"),
                                       "address2" -> Address("Soho", "London")))
    }

    "scala.collection.Map extraction example" in {
      val json = parse("""{ "name": "Joe" }""")
      json.extract[scala.collection.Map[String, String]] must_== scala.collection.Map("name" -> "Joe")
    }

    "mutable.Map extraction example" in {
      val json = parse("""{ "name": "Joe" }""")
      json.extract[scala.collection.mutable.Map[String, String]] must_== scala.collection.mutable.Map("name" -> "Joe")
    }

    "Simple value extraction example" in {
      val json = parse(testJson)
      json.extract[Name] must_== Name("joe")
      (json \ "children")(0).extract[Name] must_== Name("Mary")
      (json \ "children")(1).extract[Name] must_== Name("Mazy")
    }

    "Primitive value extraction example" in {
      val json = parse(testJson)
      (json \ "name").extract[String] must_== "joe"
      (json \ "name").extractOpt[String] must_== Some("joe")
      (json \ "name").extractOpt[Int] must_== None
      ((json \ "children")(0) \ "birthdate").extract[Date] must_== date("2004-09-04T18:06:22Z")

      JInt(1).extract[Int] must_== 1
      JInt(1).extract[String] must_== "1"
    }

    "Primitive extraction example" in {
      val json = parse(primitives)
      json.extract[Primitives] must_== Primitives(124, 123L, 126.5, 127.5.floatValue, "128", Symbol("symb"), 125, 129.byteValue, true)
    }

    "Null extraction example" in {
      val json = parse("""{ "name": null, "age": 5, "birthdate": null }""")
      json.extract[Child] must_== Child(null, 5, None)
    }

    "Date extraction example" in {
      val json = parse("""{"name":"e1","timestamp":"2009-09-04T18:06:22Z"}""")
      json.extract[Event] must_== Event("e1", date("2009-09-04T18:06:22Z"))
    }

    "Timestamp extraction example" in {
      val json = parse("""{"timestamp":"2009-09-04T18:06:22Z"}""")
      new Date((json \ "timestamp").extract[java.sql.Timestamp].getTime) must_== date("2009-09-04T18:06:22Z")
    }

    "Option extraction example" in {
      val json = parse("""{ "name": null, "age": 5, "mother":{"name":"Marilyn"}}""")
      json.extract[OChild] must_== OChild(None, 5, Some(Parent("Marilyn")), None)
    }

    "Option extraction example with strictOptionParsing" in {
      // JNull should not extract to None
      val fm = notNullFormats.withStrictOptionParsing

      parse("""{ "name": null, "age": 5, "mother":{"name":"Marilyn"}}""")
        .extract[OChild](fm, implicitly[Manifest[OChild]]) must throwA[MappingException]
      
      val mf   = implicitly[Manifest[OptionValue]]
      parse("""{"value": null}""").extract[OptionValue](fm, mf) must throwA[MappingException]
      parse("""{}""").extract[OptionValue](fm, mf) must_== OptionValue(None)
      parse("""{"value": 1}""").extract[OptionValue](fm, mf) must_== OptionValue(Some(1))
    }

    "Missing JSON array extracted as an empty List (no default value) when strictArrayExtraction is false" in {
      parse(missingChildren).extract[Person](nonStrictFormats, implicitly[Manifest[Person]]) must_== Person("joe", Address("Bulevard", "Helsinki"), Nil)
    }

    "Missing JSON array extracted as an empty List (has default value) when strictArrayExtraction is false" in {
      parse(missingChildren).extract[PersonNoKids](nonStrictFormats, implicitly[Manifest[PersonNoKids]]) must_== PersonNoKids("joe", Address("Bulevard", "Helsinki"), Nil)
    }

    "Missing JSON array fails extraction (no default value) when strictArrayExtraction is true" in {
      parse(missingChildren).extract[Person](strictFormats, implicitly[Manifest[Person]]) must throwA[MappingException]
    }

    "Missing JSON array extracted as an empty List (has default value) when strictArrayExtraction is true" in {
      parse(missingChildren).extract[PersonNoKids](strictFormats, implicitly[Manifest[PersonNoKids]]) must_== PersonNoKids("joe", Address("Bulevard", "Helsinki"), Nil)
    }

    "Multidimensional array extraction example" in {
      parse(multiDimensionalArrays).extract[MultiDim] must_== MultiDim(
        List(List(List(1, 2), List(3)), List(List(4), List(5, 6))),
        List(List(Name("joe"), Name("mary")), List(Name("mazy"))))
    }

    "Flatten example with simple case class" in {
      val f = Extraction.flatten(Extraction.decompose(SimplePerson("joe", Address("Bulevard", "Helsinki"))))
      val e = Map(".name" -> "\"joe\"", ".address.street" -> "\"Bulevard\"", ".address.city"   -> "\"Helsinki\"")

      f must_== e
    }

    "Unflatten example with top level string and int" in {
      val m = Map(".name" -> "\"joe\"", ".age" -> "32")

      Extraction.unflatten(m) must_== JObject(List(JField("name",JString("joe")), JField("age",JInt(32))))
    }

    "Unflatten example with top level string and double" in {
      val m = Map(".name" -> "\"joe\"", ".age" -> "32.2")

      Extraction.unflatten(m) must_== JObject(List(JField("name",JString("joe")), JField("age",JDouble(32.2))))
    }

    "Unflatten example with two-level string properties" in {
      val m = Map(".name" -> "\"joe\"", ".address.street" -> "\"Bulevard\"", ".address.city"   -> "\"Helsinki\"")

      Extraction.unflatten(m) must_== JObject(List(JField("name", JString("joe")), JField("address", JObject(List(JField("street", JString("Bulevard")), JField("city", JString("Helsinki")))))))
    }

    "Unflatten example with top level array" in {
      val m = Map(".foo[2]" -> "2", ".foo[0]" -> "0", ".foo[1]" -> "1")

      Extraction.unflatten(m) must_== JObject(List(JField("foo", JArray(List(JInt(0), JInt(1), JInt(2))))))
    }

    "Unflatten example with field name is prefix of the other field name" in {
      val m = Map(".data" -> "5", ".data_type" -> "6")

      Extraction.unflatten(m) must_== JObject(JField("data", JInt(5)), JField("data_type", JInt(6)))
    }

    "Flatten and unflatten are symmetric" in {
      val parsed = parse(testJson)

      Extraction.unflatten(Extraction.flatten(parsed)) must_== parsed
    }

    "Flatten preserves empty sets" in {
      val s = SetWrapper(Set())

      Extraction.flatten(Extraction.decompose(s)).get(".set") must_== Some("[]")
    }

    "Flatten and unflatten are symmetric with empty sets" in {
      val s = SetWrapper(Set())

      Extraction.unflatten(Extraction.flatten(Extraction.decompose(s))).extract[SetWrapper] must_== s
    }

    "List extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[List[Name]] must_== List(Name("Mary"), Name("Mazy"))
    }

    "Map extraction example" in {
      val json = parse(testJson) \ "address"
      json.extract[Map[String, String]] must_== Map("street" -> "Bulevard", "city" -> "Helsinki")
    }

    "Set extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[Set[Name]] must_== Set(Name("Mary"), Name("Mazy"))
    }

    "Seq extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[Seq[Name]] must_== Seq(Name("Mary"), Name("Mazy"))
    }

    "Mutable set extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[scala.collection.mutable.Set[Name]] must_==
        scala.collection.mutable.Set(Name("Mary"), Name("Mazy"))
    }

    "Mutable seq extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[scala.collection.mutable.Seq[Name]] must_==
        scala.collection.mutable.Seq(Name("Mary"), Name("Mazy"))
    }

    // https://github.com/json4s/json4s/issues/82
    "Vector extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[Vector[Name]] must_== Vector(Name("Mary"), Name("Mazy"))
    }

    "Stream extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[Stream[Name]] must_== Stream(Name("Mary"), Name("Mazy"))
    }

    "Iterable extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[Iterable[Name]] must_== Iterable(Name("Mary"), Name("Mazy"))
    }

    "Immutable Queue extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[collection.immutable.Queue[Name]] must_== collection.immutable.Queue(Name("Mary"), Name("Mazy"))
    }

    "Mutable Queue extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[collection.mutable.Queue[Name]] must_== collection.mutable.Queue(Name("Mary"), Name("Mazy"))
    }

    "Immutable HashSet extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[collection.immutable.HashSet[Name]] must_== collection.immutable.HashSet(Name("Mary"), Name("Mazy"))
    }

    "Mutable HashSet extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[collection.mutable.HashSet[Name]] must_== collection.mutable.HashSet(Name("Mary"), Name("Mazy"))
    }

    "ArrayBuffer extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[collection.mutable.ArrayBuffer[Name]] must_== collection.mutable.ArrayBuffer(Name("Mary"), Name("Mazy"))
    }

    "ListBuffer extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[collection.mutable.ListBuffer[Name]] must_== collection.mutable.ListBuffer(Name("Mary"), Name("Mazy"))
    }

    "Mutable Stack extraction example" in {
      val json = parse(testJson) \ "children"
      json.extract[collection.mutable.Stack[Name]] must_== collection.mutable.Stack(Name("Mary"), Name("Mazy"))
    }

    "ArraySeq extraction example" in {
      val scalaV = scala.util.Properties.versionNumberString
      if (scalaV.startsWith("2.11") || scalaV.startsWith("2.12")) {
        val json = parse(testJson) \ "children"
        json.extract[collection.mutable.ArraySeq[Name]] must_== collection.mutable.ArraySeq(Name("Mary"), Name("Mazy"))
      } else {
        // Scala 2.13 ArraySeq.apply take ClassTag parameter
        1 must_=== 1
      }
    }

    "Extraction and decomposition are symmetric" in {
      val person = parse(testJson).extract[Person]
      Extraction.decompose(person).extract[Person] must_== person
    }

    "Extraction failure message example" in {
      val json = parse("""{"city":"San Francisco"}""")
      json.extract[Address] must throwA(MappingException("No usable value for street\nDid not find value which can be converted into java.lang.String", null))
    }

    "Best matching constructor selection example" in {
      parse("""{"name":"john","age":32,"size":"M"}""").extract[MultipleConstructors] must_==
        MultipleConstructors("john", 32, Some("M"))

      parse("""{"name":"john","age":32}""").extract[MultipleConstructors] must_==
        MultipleConstructors("john", 32, Some("S"))

      parse("""{"name":"john","foo":"xxx"}""").extract[MultipleConstructors] must_==
        MultipleConstructors("john", 30, None)

      parse("""{"name":"john","age":32,"size":null}""").extract[MultipleConstructors] must_==
        MultipleConstructors("john", 32, None)

      parse("""{"birthYear":1990,"name":"john","foo":2}""").extract[MultipleConstructors] must_==
        MultipleConstructors("john", 20, None)

      parse("""{"foo":2,"age":12,"size":"XS"}""").extract[MultipleConstructors] must_==
        MultipleConstructors("unknown", 12, Some("XS"))
    }

    "Partial JSON extraction" in {
      parse(stringField).extract[ClassWithJSON] must_== ClassWithJSON("one", JString("msg"))
      parse(objField).extract[ClassWithJSON] must_== ClassWithJSON("one", JObject(List(JField("yes", JString("woo")))))
    }

    "Double can be coerced to Int or Long" in {
      JDouble(2.1).extract[Int] must_== 2
      JDouble(2.1).extract[Long] must_== 2L
    }

    "Map with nested non-polymorphic list extraction example" in {
      parse("""{"a":["b"]}""").extract[Map[String, List[String]]] must_== Map("a" -> List("b"))
    }

    "List with nested non-polymorphic list extraction example" in {
      parse("""[["a"]]""").extract[List[List[String]]] must_== List(List("a"))
    }

    "Complex nested non-polymorphic collections extraction example" in {
      parse("""{"a":[{"b":"c"}]}""").extract[Map[String, List[Map[String, String]]]] must_== Map("a" -> List(Map("b" -> "c")))
    }

    "allowNull format set to false should disallow null values in extraction for class types" in {
      parse("""{"name":"foobar","address":null}""").extract[SimplePerson](notNullFormats, Manifest.classType(classOf[SimplePerson])) must throwA(MappingException("No usable value for address\nDid not find value which can be converted into org.json4s.Address", null))
    }

    "allowNull format set to false should disallow null values in extraction for primitive types" in {
      parse("""{"name":null}""").extract[Name](notNullFormats, Manifest.classType(classOf[Name])) must throwA(MappingException("No usable value for name\nDid not find value which can be converted into java.lang.String", null))
    }

    "allowNull format set to false should extract a null Option[T] as None" in {
      parse("""{"name":null,"age":22}""").extract[OChild](notNullFormats, Manifest.classType(classOf[OChild])) must_== new OChild(None, 22, None, None)
    }

    "allowNull format set to false should use custom null serializer to set Option[T] as None" in {
      object CustomNull extends CustomSerializer[Null](_ => ( {
        case JNothing => null
        case JNull => null
        case JString("") => null
      }, {
        case _ => JString("")
      }))
      parse("""{"name":null,"age":22, "mother": ""}""").extract[OChild](notNullFormats + CustomNull, Manifest.classType(classOf[OChild])) must_== new OChild(None, 22, None, None)
    }

    "simple case objects should be successfully extracted as a singleton instance" in {
      parse(emptyTree).extract[LeafTree[Int]](treeFormats, Manifest.classType(classOf[LeafTree[Int]])) must_== LeafTree.empty
    }

    "case objects in a complex structure should be successfully extracted as a singleton instance" in {
      parse(tree).extract[LeafTree[Int]](treeFormats[Int], Manifest.classType(classOf[LeafTree[Int]])) must_== Node(List[LeafTree[Int]](EmptyLeaf, Node(List.empty), Leaf(1), Leaf(2)))
    }

    "#274 Examples with default value should be parsed" in {
      val res = WithDefaultValueHolder(Seq(WithDefaultValue("Bob")))
      parse("""{"values":[{"name":"Bob","gender":"male"}]}""").extract[WithDefaultValueHolder](
        DefaultFormats, Manifest.classType(classOf[WithDefaultValueHolder])) must_== (res)
    }

    "#537 Example with a Seq and default Seq value should be extracted from empty json" in {
      val res = SeqWithDefaultSeq(values = Nil)
      parse("""{ }""").extract[SeqWithDefaultSeq] must_== res
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

case class PersonWithMap(name: String, address: Map[String, String])
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
  def empty[T] : LeafTree[T] = EmptyLeaf
}

case class Node[T](children : List[LeafTree[T]]) extends LeafTree[T]
case class Leaf[T](value : T) extends LeafTree[T]
case object EmptyLeaf extends LeafTree[Nothing]

case class WithDefaultValueHolder(values: Seq[WithDefaultValue])
case class WithDefaultValue(name: String, gender: String = "male")

case class SeqWithDefaultSeq(values:Seq[String], values2:Seq[String] = Seq("1", "2", "3"))

case class Pair(a: String, b: String)

case class SecondaryConstructorCaseClass(pair: Pair, test: Option[String], createdUsingCtor: Boolean = true, int: Int) {
  def this(a: String, b: String, test: Option[String], int: Int) = this(Pair(a, b), test, false, int)
}
case class OptionValue(value: Option[Int])
