package org.json4s

import org.scalatest.wordspec.AnyWordSpec

private case class OptionalFields(
  optString: Option[String],
  optInt: Option[Int],
  optDouble: Option[Double],
  optObj: Option[OptionalFields]
)
private case class MyId(id: String) extends AnyVal
private case class MyModel(ids: Seq[MyId])
private case class AnotherModel(id: MyId)

abstract class SerializationSpec(serialization: Serialization, baseFormats: Formats) extends AnyWordSpec {

  "Serialization of case class with many Option[T] fields" should {

    implicit val formats: Formats = baseFormats.skippingEmptyValues

    "produce valid JSON without empty fields" should {
      "from case class with all fields empty" in {
        val optFields = OptionalFields(None, None, None, None)
        val str = serialization.write(optFields)
        assert(str == "{}")
      }

      "object with one string field defined" in {
        val optFields = OptionalFields(Some("hello"), None, None, None)
        val str = serialization.write(optFields)
        assert(str == """{"optString":"hello"}""")
      }

      "from case class with two fields defined #1" in {
        val optFields = OptionalFields(Some("hello"), None, None, Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        assert(str == """{"optString":"hello","optObj":{}}""")
      }

      "from case class with two fields defined #2" in {
        val optFields = OptionalFields(None, None, Some(1.0), Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        assert(str == """{"optDouble":1.0,"optObj":{}}""")
      }

      "from case class with all fields defined" in {
        val optFields = OptionalFields(Some("hello"), Some(42), Some(1.0), Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        assert(str == """{"optString":"hello","optInt":42,"optDouble":1.0,"optObj":{}}""")
      }

      "from case class with nested JSON object" in {
        val optFields = OptionalFields(None, None, None, Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        assert(str == """{"optObj":{}}""")
      }

      "from case class with deeply nested JSON objects" in {
        val optFields = OptionalFields(
          None,
          None,
          None,
          Some(
            OptionalFields(
              None,
              None,
              None,
              Some(
                OptionalFields(
                  None,
                  None,
                  None,
                  Some(OptionalFields(None, None, None, Some(OptionalFields(None, None, None, None))))
                )
              )
            )
          )
        )
        val str = serialization.write(optFields)
        assert(str == """{"optObj":{"optObj":{"optObj":{"optObj":{}}}}}""")
      }

    }

    "produce valid JSON with preserved empty fields" should {
      implicit val formats: Formats = baseFormats.preservingEmptyValues

      "from case class with all fields empty" in {
        val optFields = OptionalFields(None, None, None, None)
        val str = serialization.write(optFields)
        assert(str == """{"optString":null,"optInt":null,"optDouble":null,"optObj":null}""")
      }

      "from case class with one string field defined" in {
        val optFields = OptionalFields(Some("hello"), None, None, None)
        val str = serialization.write(optFields)
        assert(str == """{"optString":"hello","optInt":null,"optDouble":null,"optObj":null}""")
      }

      "from case class with two fields defined #1" in {
        val optFields = OptionalFields(Some("hello"), None, None, Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        assert(
          str == """{"optString":"hello","optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}"""
        )
      }

      "from case class with two fields defined #2" in {
        val optFields = OptionalFields(None, None, Some(1.0), Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        assert(
          str == """{"optString":null,"optInt":null,"optDouble":1.0,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}"""
        )
      }

      "from case class with all fields defined" in {
        val optFields = OptionalFields(Some("hello"), Some(42), Some(1.0), Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        assert(
          str == """{"optString":"hello","optInt":42,"optDouble":1.0,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}"""
        )
      }

      "from case class with nested JSON object" in {
        val optFields = OptionalFields(None, None, None, Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        assert(
          str == """{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}"""
        )
      }

      "from case class with deeply nested JSON objects" in {
        val optFields = OptionalFields(
          None,
          None,
          None,
          Some(
            OptionalFields(
              None,
              None,
              None,
              Some(
                OptionalFields(
                  None,
                  None,
                  None,
                  Some(OptionalFields(None, None, None, Some(OptionalFields(None, None, None, None))))
                )
              )
            )
          )
        )
        val str = serialization.write(optFields)
        assert(
          str == """{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}}}}"""
        )
      }

      "#270 Issue serialising sequences of AnyVal" in {
        val expected = MyModel(Seq(MyId("alice")))
        val json = serialization.write(expected)
        // FIXME: looks invalid JSON string
        assert(json == """{"ids":[{"id":"alice"}]}""")
        // FIXME: package$MappingException
        // val actual = Extraction.extract[MyModel](jackson.parseJson(json))
        // actual must_== expected
        /*
        [error] Caused by org.json4s.package$MappingException: Do not know how to convert JObject(List((id,JString(alice)))) into class java.lang.String
        [error] org.json4s.reflect.package$.fail(package.scala:93)
        [error] org.json4s.Extraction$.convert(Extraction.scala:676)
        [error] org.json4s.Extraction$.extract(Extraction.scala:388)
         */
      }

//      "#270 with expected json" in {
//        val expected = MyModel(Seq(MyId("alice")))
//        val json = """{"ids":["alice"]}"""
//        val actual = Extraction.extract[MyModel](jackson.parseJson(json))
//        actual must_== expected
//        /*
//[info]   x #270 with expected json
//[error]      'MyModel(List(alice))' is not equal to 'MyModel(List(MyId(alice)))' (SerializationSpec.scala:153)
//[info]
//[error] Expected: ...List([MyId(]al...))[)]
//[info]
//[error] Actual:   ...List([]al...))[]
//      */
//      }

      "#270 works with single AnyVal" in {
        val expected = AnotherModel(MyId("alice"))
        val json = serialization.write(expected)
        assert(json == """{"id":"alice"}""")
        val actual = Extraction.extract[AnotherModel](jackson.parseJson(json))
        assert(actual == expected)
      }

      "#661 Matching algorithm picks least correct ctor" in {
        assert(serialization.read[BadSpec](s"""{"item2": 789, "item3": 123}""") == BadSpec(789, 123))
      }

      "#674 serializes a boolean in a map from a trait in Scala 2.13" in {
        implicit val formats: Formats = DefaultFormats.skippingEmptyValues + FieldSerializer[AttributesT]()

        val expected = Foo("test")
        val json = org.json4s.native.Serialization.writePretty(expected)

        val actual = Extraction.extract[Foo](jackson.parseJson(json))
        assert(actual == expected)
      }

    }
  }
}

case class BadSpec(item2: Int, item3: Int, isVisited: Boolean = false)
case object BadSpec {
  def apply(item1: Int, item2: Int, item3: Int): BadSpec = BadSpec(item2, item3)
}

case class Foo(msg: String) extends AttributesT
trait AttributesT {
  val attributes: Map[String, Boolean] = Map("bar" -> true, "baz" -> false)
}
