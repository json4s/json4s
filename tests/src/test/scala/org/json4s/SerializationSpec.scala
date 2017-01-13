package org.json4s

import org.specs2.mutable.Specification

private case class OptionalFields(optString: Option[String], optInt: Option[Int], optDouble: Option[Double], optObj: Option[OptionalFields])
private case class MyId(id: String) extends AnyVal
private case class MyModel(ids: Seq[MyId])
private case class AnotherModel(id: MyId)

abstract class SerializationSpec(serialization: Serialization, baseFormats: Formats) extends Specification {

  "Serialization with transformation from camelCases to snake_case keys" should {
    implicit val formats = DefaultFormats.withCamelSnakeTransformation

    "map with camelCase key" in {
      val map = Map[String, String]("customKey" -> "hello", "mapWith" -> "world")
      val ser = serialization.write(map)
      println(ser)
      ser must_== """{"custom_key":"hello","map_with":"world"}"""
      serialization.read[Map[String, String]](ser) must_== map
    }
    "case class with camelCase key" in {
      val optFields = OptionalFields(Some("string"), Some(5), None, None)
      val ser = serialization.write(optFields)
      println(ser)
      ser must_== """{"opt_string":"string","opt_int":5}"""
      serialization.read[OptionalFields](ser) must_== optFields
    }
  }
  "Serialization of case class with many Option[T] fields" should {

    implicit val formats = baseFormats.skippingEmptyValues

    "produce valid JSON without empty fields" in {
      "from case class with all fields empty" in {
        val optFields = OptionalFields(None, None, None, None)
        val str = serialization.write(optFields)
        str must_== "{}"
      }

      "object with one string field defined" in {
        val optFields = OptionalFields(Some("hello"), None, None, None)
        val str = serialization.write(optFields)
        str must_== """{"optString":"hello"}"""
      }

      "from case class with two fields defined #1" in {
        val optFields = OptionalFields(Some("hello"), None, None, Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        str must_== """{"optString":"hello","optObj":{}}"""
      }

      "from case class with two fields defined #2" in {
        val optFields = OptionalFields(None, None, Some(1.0), Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        str must_== """{"optDouble":1.0,"optObj":{}}"""
      }

      "from case class with all fields defined" in {
        val optFields = OptionalFields(Some("hello"), Some(42), Some(1.0), Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        str must_== """{"optString":"hello","optInt":42,"optDouble":1.0,"optObj":{}}"""
      }

      "from case class with nested JSON object" in {
        val optFields = OptionalFields(None, None, None,
          Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        str must_== """{"optObj":{}}"""
      }

      "from case class with deeply nested JSON objects" in {
        val optFields = OptionalFields(None, None, None,
          Some(OptionalFields(None, None, None,
            Some(OptionalFields(None, None, None,
              Some(OptionalFields(None, None, None,
                Some(OptionalFields(None, None, None, None)))))))))
        val str = serialization.write(optFields)
        str must_== """{"optObj":{"optObj":{"optObj":{"optObj":{}}}}}"""
      }

    }

    "produce valid JSON with preserved empty fields" in {
      implicit val formats = baseFormats.preservingEmptyValues

      "from case class with all fields empty" in {
        val optFields = OptionalFields(None, None, None, None)
        val str = serialization.write(optFields)
        str must_== """{"optString":null,"optInt":null,"optDouble":null,"optObj":null}"""
      }

      "from case class with one string field defined" in {
        val optFields = OptionalFields(Some("hello"), None, None, None)
        val str = serialization.write(optFields)
        str must_== """{"optString":"hello","optInt":null,"optDouble":null,"optObj":null}"""
      }

      "from case class with two fields defined #1" in {
        val optFields = OptionalFields(Some("hello"), None, None, Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        str must_== """{"optString":"hello","optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}"""
      }

      "from case class with two fields defined #2" in {
        val optFields = OptionalFields(None, None, Some(1.0), Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        str must_== """{"optString":null,"optInt":null,"optDouble":1.0,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}"""
      }

      "from case class with all fields defined" in {
        val optFields = OptionalFields(Some("hello"), Some(42), Some(1.0), Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        str must_== """{"optString":"hello","optInt":42,"optDouble":1.0,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}"""
      }

      "from case class with nested JSON object" in {
        val optFields = OptionalFields(None, None, None,
          Some(OptionalFields(None, None, None, None)))
        val str = serialization.write(optFields)
        str must_== """{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}"""
      }

      "from case class with deeply nested JSON objects" in {
        val optFields = OptionalFields(None, None, None,
          Some(OptionalFields(None, None, None,
            Some(OptionalFields(None, None, None,
              Some(OptionalFields(None, None, None,
                Some(OptionalFields(None, None, None, None)))))))))
        val str = serialization.write(optFields)
        str must_== """{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":{"optString":null,"optInt":null,"optDouble":null,"optObj":null}}}}}"""
      }

      "#270 Issue serialising sequences of AnyVal" in {
        val expected = MyModel(Seq(MyId("alice")))
        val json = serialization.write(expected)
        // FIXME: looks invalid JSON string
        json must_== """{"ids":[{"id":"alice"}]}"""
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
        json must_== """{"id":"alice"}"""
        val actual = Extraction.extract[AnotherModel](jackson.parseJson(json))
        actual must_== expected
      }
    }

  }

}
