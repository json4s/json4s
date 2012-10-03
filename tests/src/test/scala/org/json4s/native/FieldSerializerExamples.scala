package org.json4s
import native.JsonMethods._
import org.specs2.mutable.Specification

object FieldSerializerExamples extends Specification {
  import native.Serialization.{read, write => swrite}
  import FieldSerializer._

  val dog = new WildDog("black")
  dog.name = "pluto"
  dog.owner = Owner("joe", 35)

  val cat = new WildCat(100)
  cat.name = "tommy"

  "All fields are serialized by default" in {
    implicit val formats = DefaultFormats + FieldSerializer[WildDog]()
    val ser = swrite(dog)
    val dog2 = read[WildDog](ser)
    dog2.name mustEqual dog.name
    dog2.color mustEqual dog.color
    dog2.owner mustEqual dog.owner
    dog2.size mustEqual dog.size
  }

  "Fields can be ignored and renamed" in {
    val dogSerializer = FieldSerializer[WildDog](
      renameTo("name", "animalname") orElse ignore("owner"),
      renameFrom("animalname", "name")
    )

    implicit val formats = DefaultFormats + dogSerializer

    val ser = swrite(dog)
    val dog2 = read[WildDog](ser)
    dog2.name mustEqual dog.name
    dog2.color mustEqual dog.color
    dog2.owner must beNull
    dog2.size mustEqual dog.size
    (parseJson(ser) \ "animalname") mustEqual JString("pluto")
  }

  "Selects best matching serializer" in {
    val dogSerializer = FieldSerializer[WildDog](ignore("name"))
    implicit val formats = DefaultFormats + FieldSerializer[AnyRef]() + dogSerializer

    val dog2 = read[WildDog](swrite(dog))
    val cat2 = read[WildCat](swrite(cat))

    dog2.name mustEqual ""
    cat2.name mustEqual "tommy"
  }
}

abstract class Mammal {
  var name: String = ""
  var owner: Owner = null
  val size = List(10, 15)
}

class WildDog(val color: String) extends Mammal
class WildCat(val cuteness: Int) extends Mammal

case class Owner(name: String, age: Int)
