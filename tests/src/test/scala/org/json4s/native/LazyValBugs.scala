package org.json4s.native

import org.json4s.{FieldSerializer, Formats, DefaultFormats}
import org.json4s.native.Serialization.{read, write}
import org.specs2.mutable.Specification

class MovieNode(val id: String, val title: String)

class ActorNode(val id: String, val name: String) {
  val normalMovie: MovieNode = new MovieNode("M1", "Die Hard")
  lazy val lazyMovie: MovieNode = new MovieNode("M2", "Armageddon")
  lazy val lazyMovies: List[MovieNode] = List(new MovieNode("M3", "Sixth Sense"), new MovieNode("M4", "Unbreakable"))
  lazy val rating: Int = 3
  lazy val isBald: Boolean = true
}

/** Test of a fix for failed lazy val serialization from FieldSerializer
  * where the serialized lazy val will get default value result (null, 0, false, etc) instead of the real value.
  *
  */
class LazyValBugs extends Specification {

  "LazyValBugs Test" should {

    "Serialize lazy val to json" in {
      implicit val formats: Formats = DefaultFormats + FieldSerializer[ActorNode](includeLazyVal = true)

      val actorNode = new ActorNode("A1", "Bruce Willis")
      val res = write(actorNode)

      res.contains(""""normalMovie":null""") must beFalse
      res.contains(""""lazyMovie":null""") must beFalse
      res.contains(""""lazyMovies":null""") must beFalse
      res.contains(""""rating":0""") must beFalse
      res.contains(""""isBald":false""") must beFalse
    }

    "Don't serialize lazy val to json if includeLazyVal is not set in FieldSerializer" in {
      implicit val formats: Formats = DefaultFormats + FieldSerializer[ActorNode]()

      val actorNode = new ActorNode("A1", "Bruce Willis")
      val res = write(actorNode)

      res.contains(""""normalMovie":null""") must beFalse
      res.contains(""""lazyMovie":null""") must beTrue
      res.contains(""""lazyMovies":null""") must beTrue
      res.contains(""""rating":0""") must beTrue
      res.contains(""""isBald":false""") must beTrue
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

      actorNode.id === "A2"
      actorNode.name === "Tom Hanks"
      actorNode.normalMovie.id === "M13"
      actorNode.normalMovie.title === "Apollo 13"
      actorNode.lazyMovie.id === "M5"
      actorNode.lazyMovie.title === "Forrest Gump"
      actorNode.lazyMovies must have size 3
      for (movie <- actorNode.lazyMovies) {
        if (movie.id == "M6") movie.title === "Cast Away"
        else if (movie.id == "M7") movie.title === "You've Got Mail"
        else if (movie.id == "M8") movie.title === "The Da Vinci Code"
        else failure("Invalid movie id found: " + movie.id)
      }
      actorNode.rating === 5
      actorNode.isBald === false
    }

    "Don't deserialize lazy val from parsed json if includeLazyVal is not set in FieldSerializer" in {
      implicit val formats: Formats = DefaultFormats + FieldSerializer[ActorNode]()

      val actorNode = read[ActorNode](jsonStr)

      actorNode.id === "A2"
      actorNode.name === "Tom Hanks"
      actorNode.normalMovie.id === "M13"
      actorNode.normalMovie.title === "Apollo 13"
      actorNode.lazyMovie.id === "M2"
      actorNode.lazyMovie.title === "Armageddon"
      actorNode.lazyMovies must have size 2
      for (movie <- actorNode.lazyMovies) {
        if (movie.id == "M3") movie.title === "Sixth Sense"
        else if (movie.id == "M4") movie.title === "Unbreakable"
        else failure("Invalid movie id found: " + movie.id)
      }
      actorNode.rating === 3
      actorNode.isBald === true
    }
  }

}
