package org.json4s
package scalaz

import _root_.scalaz._
import Scalaz._
import native.JsonMethods._
import org.json4s.native.scalaz._
import JsonScalaz._

import org.specs2.mutable.Specification

class Example extends Specification {

  case class Address(street: String, zipCode: String)
  case class Person(name: String, age: Int, address: Address)

  "Parse address in an Applicative style" in {
    val json = parse(""" {"street": "Manhattan 2", "zip": "00223" } """)
    val a1 = field[String]("zip")(json) ap (field[String]("street")(json) map Address.curried)
    val a2 = (field[String]("street")(json) |@| field[String]("zip")(json)) { Address }
    val a3 = Address.applyJSON(field[String]("street"), field[String]("zip"))(json)
    a2 mustEqual Success(Address("Manhattan 2", "00223"))
    a3 mustEqual a2
    a1 mustEqual a2
  }

  "Failed address parsing" in {
    val json = parse(""" {"street": "Manhattan 2", "zip": "00223" } """)
    val a = (field[String]("streets")(json) |@| field[String]("zip")(json)) { Address }
    a.swap.toOption.get.list mustEqual IList(NoSuchFieldError("streets", json))
  }

  "Parse Person with Address" in {
    implicit def addrJSON: JSONR[Address] = new JSONR[Address] {
      def read(json: JValue) = Address.applyJSON(field[String]("street"), field[String]("zip"))(json)
    }

    val p = parse(""" {"name":"joe","age":34,"address":{"street": "Manhattan 2", "zip": "00223" }} """)
    val person = Person.applyJSON(field[String]("name"), field[Int]("age"), field[Address]("address"))(p)
    person mustEqual Success(Person("joe", 34, Address("Manhattan 2", "00223")))
  }

  "Format Person with Address" in {
    implicit def addrJSON: JSONW[Address] = new JSONW[Address] {
      def write(a: Address) =
        makeObj(("street" -> toJSON(a.street)) :: ("zip" -> toJSON(a.zipCode)) :: Nil)
    }

    val p = Person("joe", 34, Address("Manhattan 2", "00223"))
    val json = makeObj(("name" -> toJSON(p.name)) ::
                       ("age" -> toJSON(p.age)) ::
                       ("address" -> toJSON(p.address)) :: Nil)
    json.shows mustEqual
      """{"name":"joe","age":34,"address":{"street":"Manhattan 2","zip":"00223"}}"""
  }

  "Parse Map" in {
    val json = parse(""" {"street": "Manhattan 2", "zip": "00223" } """)
    fromJSON[Map[String, String]](json) mustEqual Success(Map("street" -> "Manhattan 2", "zip" -> "00223"))
  }

  "Format Map" in {
    toJSON(Map("street" -> "Manhattan 2", "zip" -> "00223")) mustEqual
      parse("""{"street":"Manhattan 2","zip":"00223"}""")
  }
}
