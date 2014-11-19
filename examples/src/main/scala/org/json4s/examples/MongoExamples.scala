package org.json4s
package examples

import com.ning.http.client.Response
import dispatch._, Defaults._
import com.mongodb._
import mongo.JObjectParser

object MongoExamples extends App with jackson.JsonMethods {

  import Api.formats
  object read {
    object Json extends (Response => JValue) {
      def apply(r: Response) =
        (dispatch.as.String andThen (parse(_)))(r)
    }
  }


  val mongo = com.mongodb.Mongo.connect(new DBAddress("127.0.0.1", "json4s_examples"))
  val coll = mongo.getCollection("swagger_data")

  val f = for {
    listing <- Http(url("http://petstore.swagger.wordnik.com/api/api-docs") OK read.Json)
    api <- Http(url("http://petstore.swagger.wordnik.com/api/api-docs/pet") OK read.Json)
  } yield {
    println("Listing received, storing...")
    coll.save(JObjectParser.parse(listing))

    println("Api description received, storing...")
    coll save {
      JObjectParser parse {
        api transformField {
          case JField(nm, v) if nm.startsWith("$") => JField(s"#${nm.substring(1)}", v)
        }
      }
    }

    println("Swagger api has been harvested.")
  }

  f onFailure { case r =>
    println(r)
  }

  f onComplete { r =>
    Http.shutdown()
  }
}