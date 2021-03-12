package org.json4s

import native.JsonMethods._
import org.scalatest.wordspec.AnyWordSpec

class FieldSerializerExamples extends AnyWordSpec {
  import native.Serialization.{read, write => swrite}
  import FieldSerializer._

  val dog = new WildDog("black")
  dog.name = "pluto"
  dog.owner = Owner("joe", 35)

  val cat = new WildCat(100)
  cat.name = "tommy"

  "All fields are serialized by default" in {
    implicit val formats: Formats = DefaultFormats + FieldSerializer[WildDog]()
    val ser = swrite(dog)
    val dog2 = read[WildDog](ser)
    assert(dog2.name == dog.name)
    assert(dog2.color == dog.color)
    assert(dog2.owner == dog.owner)
    assert(dog2.size == dog.size)
  }

  "Fields can be ignored and renamed" in {
    val dogSerializer = FieldSerializer[WildDog](
      renameTo("name", "animalname") orElse ignore("owner"),
      renameFrom("animalname", "name")
    )

    implicit val formats: Formats = DefaultFormats + dogSerializer

    val ser = swrite(dog)
    val dog2 = read[WildDog](ser)
    assert(dog2.name == dog.name)
    assert(dog2.color == dog.color)
    assert(dog2.owner == null)
    assert(dog2.size == dog.size)
    assert((parse(ser) \ "animalname") == JString("pluto"))
  }

  "Selects best matching serializer" in {
    val dogSerializer = FieldSerializer[WildDog](ignore("name"))
    implicit val formats: Formats = DefaultFormats + FieldSerializer[AnyRef]() + dogSerializer

    val dog2 = read[WildDog](swrite(dog))
    val cat2 = read[WildCat](swrite(cat))

    assert(dog2.name == "")
    assert(cat2.name == "tommy")
  }

  "Renames a property name to/from" in {
    val dudeSerializer = FieldSerializer[Dude](renameTo("name", "nm"), renameFrom("nm", "name"))
    implicit val formats: Formats = DefaultFormats + dudeSerializer
    val dude = Dude("Jeffrey")

    val jv = Extraction.decompose(dude)
    assert({ jv \ "nm" } == JString("Jeffrey"))

    val result = Extraction.extract[Dude](jv)
    assert(result == dude)
  }

  "Renames a property name to/from in subproperties" in {
    val dudeSerializer = FieldSerializer[Dude](renameTo("name", "nm"), renameFrom("nm", "name"))
    implicit val formats: Formats = DefaultFormats + dudeSerializer
    val dude = Dude("Jeffrey", Dude("Angel") :: Dude("Constantin") :: Nil)

    val jv = Extraction.decompose(dude)
    assert({ jv \ "nm" } == JString("Jeffrey"))
    assert({ jv \ "friends" \\ "nm" } == JObject(List("nm" -> JString("Angel"), "nm" -> JString("Constantin"))))

    val result = Extraction.extract[Dude](jv)
    assert(result == dude)
  }

  "Extract should fail when undefined fields are provided with strictFieldSerialialization on" in {
    val customFormats = new DefaultFormats {
      override val strictFieldDeserialization: Boolean = true
    }

    val dudeSerializer = FieldSerializer[Dude]()
    implicit val formats: Formats = customFormats + dudeSerializer

    val ser = parse("""{"name":"John", "friends":[], "lastName": "Smith"}""")

    assertThrows[MappingException] {
      Extraction.extract[Dude](ser)
    }
  }

  "rename functionality should not break strictFieldSerialialization" in {
    val customFormats = new DefaultFormats {
      override val strictFieldDeserialization: Boolean = true
    }

    val dudeSerializer = FieldSerializer[Dude](renameTo("name", "nm"), renameFrom("nm", "name"))
    implicit val formats: Formats = customFormats + dudeSerializer

    val ser = parse("""{"nm":"John", "friends":[]}""")

    Extraction.extract[Dude](ser)
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
case class Dude(name: String, friends: List[Dude] = Nil)
