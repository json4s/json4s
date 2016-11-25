package org.json4s
package scalaz

import _root_.scalaz._
import Scalaz._
import native.JsonMethods._
import org.json4s.native.scalaz._
import JsonScalaz._

import org.specs2.mutable.Specification

object Example extends Specification {

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
    implicit def addrJSON: JSONR[Address] = JSONR.instance[Address] {
      json => Address.applyJSON(field[String]("street"), field[String]("zip"))(json)
    }

    val p = parse(""" {"name":"joe","age":34,"address":{"street": "Manhattan 2", "zip": "00223" }} """)
    val person = Person.applyJSON(field[String]("name"), field[Int]("age"), field[Address]("address"))(p)
    person mustEqual Success(Person("joe", 34, Address("Manhattan 2", "00223")))
  }

  "Format Person with Address" in {
    implicit def addrJSON: JSONW[Address] = JSONW.instance[Address] {
      a =>
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
    toJSON(Map("street" -> "Manhattan 2", "zip" -> "00223")).shows mustEqual
      """{"street":"Manhattan 2","zip":"00223"}"""
  }

  "Parse item in Monadic style" in {

    case class Item(label: String, amount: Int, price: Double)

    val json: JValue =
      parse("""
        |[
        |  {
        |    "label": "foo item",
        |    "amount": { "value": 200 },
        |    "price": { "value": 1.99 }
        |  },
        |  {
        |    "label": "bar item",
        |    "amount": { "value": 100 },
        |    "price": { "value": 2.50 }
        |  }
        |]
      """.stripMargin)

    implicit val itemJSONR: JSONR[Item] = JSONR.instanceE[Item] { json =>
      for {
        label <- (json \ "label").read[String]
        amount <- (json \ "amount" \ "value").read[Int]
        price <- (json \ "price" \ "value").read[Double]
      } yield Item(label, amount, price)
    }

    implicit val itemJSONW: JSONW[Item] = JSONW.instance[Item] { item =>
      makeObj(
        ("label" -> toJSON(item.label)) ::
        ("amount" -> makeObj(("value" -> toJSON(item.amount)) :: Nil)) ::
        ("price" -> makeObj(("value" -> toJSON(item.price)) :: Nil)) :: Nil
      )
    }

    json.validate[List[Item]] must beLike[Result[List[Item]]] {
      case Success(xs) =>
        xs must haveSize(2)

        (xs.toJson === json) must beTrue
    }

  }

  "DynamicJValue" in {

    val text =
      """
        |{
        |  "street" : "Manhattan 2",
        |  "zip" : "00223",
        |  "info" : { "verified": true }
        |}
      """.stripMargin

    val json: JValue = parse(text)

    case class AddressInfo(street: String, zip: String, info: DynamicJValue)

    implicit val dynamicJValueJson = JSON.instance[DynamicJValue](
      json => DynamicJValue.dyn(json).successNel,
      _.raw
    )

    implicit val adressInfoJSONR: JSONR[AddressInfo] = AddressInfo.applyJSON(
      field[String]("street"),
      field[String]("zip"),
      field[DynamicJValue]("info")
    )

    implicit val addressInfoJSONW: JSONW[AddressInfo] = JSONW.instance[AddressInfo] { info =>
      makeObj(
        ("street" -> toJSON(info.street)) ::
        ("zip" -> toJSON(info.zip)) ::
        ("info" -> toJSON(info.info)) :: Nil
      )
    }

    json.validate[AddressInfo] must beLike[Result[AddressInfo]] {
      case Success(info) =>

        info.street must_== "Manhattan 2"
        info.zip must_== "00223"
        info.info.raw must_== JObject("verified" -> JBool(true))
    }


  }
}
