package org.json4s
import native.JsonMethods._
import org.specs2.mutable.Specification
import scala.util.Try

class FieldSerializerExamples extends Specification {
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
    dog2.name must_== dog.name
    dog2.color must_== dog.color
    dog2.owner must_== dog.owner
    dog2.size must_== dog.size
  }

  "Fields can be ignored and renamed" in {
    val dogSerializer = FieldSerializer[WildDog](
      renameTo("name", "animalname") orElse ignore("owner"),
      renameFrom("animalname", "name")
    )

    implicit val formats = DefaultFormats + dogSerializer

    val ser = swrite(dog)
    val dog2 = read[WildDog](ser)
    dog2.name must_== dog.name
    dog2.color must_== dog.color
    dog2.owner must beNull
    dog2.size must_== dog.size
    (parse(ser) \ "animalname") must_== JString("pluto")
  }

  "Selects best matching serializer" in {
    val dogSerializer = FieldSerializer[WildDog](ignore("name"))
    implicit val formats = DefaultFormats + FieldSerializer[AnyRef]() + dogSerializer

    val dog2 = read[WildDog](swrite(dog))
    val cat2 = read[WildCat](swrite(cat))

    dog2.name must_== ""
    cat2.name must_== "tommy"
  }


  "Renames a property name to/from" in {
    val dudeSerializer = FieldSerializer[Dude](renameTo("name", "nm"), renameFrom("nm", "name"))
    implicit val formats = DefaultFormats + dudeSerializer
    val dude = Dude("Jeffrey")

    val jv = Extraction.decompose(dude)
    jv \ "nm" must_== JString("Jeffrey")

    val result = Extraction.extract[Dude](jv)
    result must_== dude
  }

  "Renames a property name to/from in subproperties" in {
    val dudeSerializer = FieldSerializer[Dude](renameTo("name", "nm"), renameFrom("nm", "name"))
    implicit val formats = DefaultFormats + dudeSerializer
    val dude = Dude("Jeffrey", Dude("Angel") :: Dude("Constantin") :: Nil)

    val jv = Extraction.decompose(dude)
    jv \ "nm" must_== JString("Jeffrey")
    jv \ "friends" \\ "nm" must_== JObject(List("nm" -> JString("Angel"), "nm" -> JString("Constantin")))

    val result = Extraction.extract[Dude](jv)
    result must_== dude
  }

  "Extract should fail when undefined fields are provided with strictFieldSerialialization on" in {
    val customFormats = new DefaultFormats {
      override val strictFieldDeserialization: Boolean = true
    }

    val dudeSerializer = FieldSerializer[Dude]()
    implicit val formats = customFormats + dudeSerializer

    val ser = parse("""{"name":"John", "friends":[], "lastName": "Smith"}""")

    Try { Extraction.extract[Dude](ser) } must beFailedTry
  }

  "rename functionality should not break strictFieldSerialialization" in {
    val customFormats = new DefaultFormats {
      override val strictFieldDeserialization: Boolean = true
    }

    val dudeSerializer = FieldSerializer[Dude](renameTo("name", "nm"), renameFrom("nm", "name"))
    implicit val formats = customFormats + dudeSerializer

    val ser = parse("""{"nm":"John", "friends":[]}""")

    Try { Extraction.extract[Dude](ser) } must beSuccessfulTry
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
case class Dude(name: String,  friends: List[Dude] = Nil)

