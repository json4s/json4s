package org.json4s
package scalaz

import _root_.scalaz._
import Scalaz._
import native.JsonMethods._
import org.json4s.native.scalaz._
import JsonScalaz._

import org.scalatest.wordspec.AnyWordSpec

class Example extends AnyWordSpec {

  case class Address(street: String, zipCode: String)
  object Address extends ((String, String) => Address)
  case class Person(name: String, age: Int, address: Address)
  object Person extends ((String, Int, Address) => Person)

  "Parse address in an Applicative style" in {
    val json = parse(""" {"street": "Manhattan 2", "zip": "00223" } """)
    val a1 = field[String]("zip")(json) ap (field[String]("street")(json) map Address.curried)
    val a2 = (field[String]("street")(json) |@| field[String]("zip")(json)) { Address }
    val a3 = Address.applyJSON(field[String]("street"), field[String]("zip"))(json)
    assert(a2 == Success(Address("Manhattan 2", "00223")))
    assert(a3 == a2)
    assert(a1 == a2)
  }

  "Failed address parsing" in {
    val json = parse(""" {"street": "Manhattan 2", "zip": "00223" } """)
    val a = (field[String]("streets")(json) |@| field[String]("zip")(json)) { Address }
    assert(a.swap.toOption.get.list == IList(NoSuchFieldError("streets", json)))
  }

  "Parse Person with Address" in {
    implicit def addrJSON: JSONR[Address] = new JSONR[Address] {
      def read(json: JValue) = Address.applyJSON(field[String]("street"), field[String]("zip"))(json)
    }

    val p = parse(""" {"name":"joe","age":34,"address":{"street": "Manhattan 2", "zip": "00223" }} """)
    val person = Person.applyJSON(field[String]("name"), field[Int]("age"), field[Address]("address"))(p)
    assert(person == Success(Person("joe", 34, Address("Manhattan 2", "00223"))))
  }

  "Format Person with Address" in {
    implicit def addrJSON: JSONW[Address] = new JSONW[Address] {
      def write(a: Address) =
        makeObj(("street" -> toJSON(a.street)) :: ("zip" -> toJSON(a.zipCode)) :: Nil)
    }

    val p = Person("joe", 34, Address("Manhattan 2", "00223"))
    val json = makeObj(
      ("name" -> toJSON(p.name)) ::
      ("age" -> toJSON(p.age)) ::
      ("address" -> toJSON(p.address)) :: Nil
    )
    assert(json.shows == """{"name":"joe","age":34,"address":{"street":"Manhattan 2","zip":"00223"}}""")
  }

  "Parse Map" in {
    val json = parse(""" {"street": "Manhattan 2", "zip": "00223" } """)
    assert(fromJSON[Map[String, String]](json) == Success(Map("street" -> "Manhattan 2", "zip" -> "00223")))
  }

  "Format Map" in {
    assert(
      toJSON(Map("street" -> "Manhattan 2", "zip" -> "00223")) == parse("""{"street":"Manhattan 2","zip":"00223"}""")
    )
  }
}
