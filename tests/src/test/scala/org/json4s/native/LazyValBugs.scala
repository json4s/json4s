package org.json4s.native

import org.json4s.{FieldSerializer, Formats, DefaultFormats}
import org.json4s.native.Serialization.{read, write}
import org.scalatest.wordspec.AnyWordSpec

class MovieNode(val id: String, val title: String)

class ActorNode(val id: String, val name: String) {
  val normalMovie: MovieNode = new MovieNode("M1", "Die Hard")
  lazy val lazyMovie: MovieNode = new MovieNode("M2", "Armageddon")
  lazy val lazyMovies: List[MovieNode] = List(new MovieNode("M3", "Sixth Sense"), new MovieNode("M4", "Unbreakable"))
  lazy val rating: Int = 3
  lazy val isBald: Boolean = true
}

/**
 * Test of a fix for failed lazy val serialization from FieldSerializer
 * where the serialized lazy val will get default value result (null, 0, false, etc) instead of the real value.
 */
class LazyValBugs extends AnyWordSpec {

  "LazyValBugs Test" should {

    "Serialize lazy val to json" in {
      implicit val formats: Formats = DefaultFormats + FieldSerializer[ActorNode](includeLazyVal = true)

      val actorNode = new ActorNode("A1", "Bruce Willis")
      val res = write(actorNode)

      assert(!res.contains(""""normalMovie":null"""))
      assert(!res.contains(""""lazyMovie":null"""))
      assert(!res.contains(""""lazyMovies":null"""))
      assert(!res.contains(""""rating":0"""))
      assert(!res.contains(""""isBald":false"""))
    }

    "Don't serialize lazy val to json if includeLazyVal is not set in FieldSerializer" in {
      implicit val formats: Formats = DefaultFormats + FieldSerializer[ActorNode]()

      val actorNode = new ActorNode("A1", "Bruce Willis")
      val res = write(actorNode)

      assert(!res.contains(""""normalMovie":null"""))
      assert(res.contains(""""lazyMovie":null"""))
      assert(res.contains(""""lazyMovies":null"""))
      assert(res.contains(""""rating":0"""))
      assert(res.contains(""""isBald":false"""))
    }

    val jsonStr =
      s"""
         |{
         |  "id": "A2",
         |  "name": "Tom Hanks",
         |  "rating": 5,
         |  "isBald": false,
         |  "normalMovie":
         |  {
         |    "id": "M13",
         |    "title": "Apollo 13"
         |  },
         |  "lazyMovie":
         |  {
         |    "id": "M5",
         |    "title": "Forrest Gump"
         |  },
         |  "lazyMovies": [
         |  {
         |    "id": "M6",
         |    "title": "Cast Away"
         |  },
         |  {
         |    "id": "M7",
         |    "title": "You've Got Mail"
         |  },
         |  {
         |    "id": "M8",
         |    "title": "The Da Vinci Code"
         |  }
         |  ]
         |}
         """.stripMargin

    "Deserialize lazy val value from parsed json" in {
      implicit val formats: Formats = DefaultFormats + FieldSerializer[ActorNode](includeLazyVal = true)

      val actorNode = read[ActorNode](jsonStr)

      assert(actorNode.id == "A2")
      assert(actorNode.name == "Tom Hanks")
      assert(actorNode.normalMovie.id == "M13")
      assert(actorNode.normalMovie.title == "Apollo 13")
      assert(actorNode.lazyMovie.id == "M5")
      assert(actorNode.lazyMovie.title == "Forrest Gump")
      assert(actorNode.lazyMovies.size == 3)
      for (movie <- actorNode.lazyMovies) {
        if (movie.id == "M6") assert(movie.title == "Cast Away")
        else if (movie.id == "M7") assert(movie.title == "You've Got Mail")
        else if (movie.id == "M8") assert(movie.title == "The Da Vinci Code")
        else fail("Invalid movie id found: " + movie.id)
      }
      assert(actorNode.rating == 5)
      assert(actorNode.isBald == false)
    }

    "Don't deserialize lazy val from parsed json if includeLazyVal is not set in FieldSerializer" in {
      implicit val formats: Formats = DefaultFormats + FieldSerializer[ActorNode]()

      val actorNode = read[ActorNode](jsonStr)

      assert(actorNode.id == "A2")
      assert(actorNode.name == "Tom Hanks")
      assert(actorNode.normalMovie.id == "M13")
      assert(actorNode.normalMovie.title == "Apollo 13")
      assert(actorNode.lazyMovie.id == "M2")
      assert(actorNode.lazyMovie.title == "Armageddon")
      assert(actorNode.lazyMovies.size == 2)
      for (movie <- actorNode.lazyMovies) {
        if (movie.id == "M3") assert(movie.title == "Sixth Sense")
        else if (movie.id == "M4") assert(movie.title == "Unbreakable")
        else fail("Invalid movie id found: " + movie.id)
      }
      assert(actorNode.rating == 3)
      assert(actorNode.isBald == true)
    }
  }

}
