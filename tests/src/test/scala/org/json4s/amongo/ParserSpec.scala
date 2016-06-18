package org.json4s.amongo

import java.util.Date

import org.bson.BsonDocument
import org.json4s.amongo.gens.Generators
import org.json4s.amongo.model.{EmbeddedObject, SimpleObject}
import org.json4s.{DefaultFormats, Extraction}
import org.scalacheck.Prop
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object ParserSpec extends Specification with ScalaCheck {
  implicit val formats = DefaultFormats.lossless + new ObjectIdSerializer + new DateSerializer
  "New Bson Objects support" should {
    "Convert simple object into BsonValue and back" ! Prop.forAll(Generators.simpleObjectGen) { simple =>
      val jValue = Extraction.decompose(simple)
      val doc: BsonDocument = JObjectParser().parse(jValue).asDocument()
      val deserialized = JObjectSerializer.serialize(doc)
      val simpleObjectFromDB = Extraction.extract[SimpleObject](deserialized)
      simpleObjectFromDB must_== simple
    }

    "Convert embedded object into BsonValue and back" ! Prop.forAll(Generators.embeddedObjectGen) { emb =>
      val jValue = Extraction.decompose(emb)
      val doc = JObjectParser().parse(jValue).asDocument()
      val deserialized = JObjectSerializer.serialize(doc)
      val embFromDB = Extraction.extract[EmbeddedObject](deserialized)
      embFromDB must_== emb
    }

    "Convert object that contains many different lists" ! Prop.forAll(Generators.embeddedObjectGen) { emb =>
      val jValue = Extraction.decompose(emb)
      val doc = JObjectParser().parse(jValue).asDocument()
      val deserialized = JObjectSerializer.serialize(doc)
      val embFromDB = Extraction.extract[EmbeddedObject](deserialized)
      embFromDB must_== emb
    }

    "Date conversion" in {
      val date = new Date()
      val jV = Extraction.decompose("date" -> date)
      val doc = JObjectParser().parse(jV).asDocument
      val deserialized = JObjectSerializer.serialize(doc)
      val pair = Extraction.extract[(String, Date)](deserialized)
      date.getTime must_== pair._2.getTime
    }

    /**
      * As you can see there are no tests for Regex and UUID since both of them are serialized using a BsonWrapperDocument
      * In that case only when they are written to the DB they are treated properly.
      *
      * However I made an integration test that uses MongoDB but I can't commit it
      * since it will require a DB to exist on the Test machine
      *
      */
  }
}
