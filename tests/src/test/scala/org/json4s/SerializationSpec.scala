package org.json4s

import org.specs2.mutable.Specification

case class OptionalFields(optString: Option[String], optInt: Option[Int], optDouble: Option[Double], optObj: Option[OptionalFields])

abstract class SerializationSpec(serialization: Serialization, baseFormats: Formats) extends Specification {

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
    }

  }

}