package org.json4s
package examples

import dispatch._, Defaults._
import com.ning.http.client.Response


object DispatchExamples extends App with native.JsonMethods {

  import Api.formats
  object read {
    object Json extends (Response => JValue) {
      def apply(r: Response) =
        (dispatch.as.String andThen (parse(_)))(r)
    }

    def resources(jv: JValue) = jv.extract[org.json4s.examples.ApiListing]
    def apiDefinition(jv: JValue) = jv.extract[org.json4s.examples.Api]

    object ApiListing extends (Response => org.json4s.examples.ApiListing) {
      def apply(v1: Response): org.json4s.examples.ApiListing = (Json andThen resources)(v1)
    }

    object Api extends (Response => org.json4s.examples.Api) {
      def apply(v1: Response): org.json4s.examples.Api = (Json andThen apiDefinition)(v1)
    }
  }

  val f = for {
    listing <- Http(url("http://petstore.swagger.wordnik.com/api/api-docs") OK read.ApiListing)
    api <- Http(url("http://petstore.swagger.wordnik.com/api/api-docs/pet") OK read.Api)
  } yield {
    println("The listing: ")
    println(listing)
    println("The json for the listing")
    println(jackson.Serialization.writePretty(listing))

    println("The api: ")
    println(api)
    println("The json for the api")
    println(jackson.Serialization.writePretty(api))
  }

  f onFailure { case r =>
    println(r)
  }

  f onComplete { r =>
    Http.shutdown()
  }
}