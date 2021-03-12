package org.json4s
package mongo

import org.scalatest.wordspec.AnyWordSpec
import org.json4s.{Extraction, DefaultFormats}
import java.util.Date

object DateSerializerSpec {
  case class WithDate(id: Int, name: String, createdAt: Date)
  case class EventWithDate(
    timestamp: Date,
    index: Int,
    event: String,
    description: String,
    version: String,
    platform: String,
    device: String,
    userId: String,
    params: List[String] = Nil
  )
}
class DateSerializerSpec extends AnyWordSpec {

  import DateSerializerSpec._
  implicit val formats: Formats = DefaultFormats.lossless + new DateSerializer("$date")
  val js =
    """{ "_id" : { "$oid" : "51523bc0036433e0ce323ca6"} , "timestamp" : { "$date" : "2013-03-26T11:19:00.000Z"} , "index" : 1 , "event" : "uAppLaunch" , "description" : "" , "version" : "370" , "platform" : "iPad" , "device" : "Apple iPad 3rd Gen (Wi-Fi Only)" , "userId" : "89B59046-A6F1-4E13-B5B9-055FF2D2BBF1" , "params" : [ ""]}"""

  "A DateSerializer" should {
    "serialize a date" in {
      val d = new Date
      val obj = WithDate(2, "Alice", d)
      val jv = Extraction.decompose(obj)
      println(jv)
      assert(jv.extract[WithDate] == obj)
    }

    "serialize a mongo json string" in {
      val d = formats.dateFormat.parse("2013-03-26T11:19:00.000Z")
      val obj = jackson.JsonMethods.parse(js).extract[EventWithDate]
      assert(
        obj == EventWithDate(
          d.get,
          1,
          "uAppLaunch",
          "",
          "370",
          "iPad",
          "Apple iPad 3rd Gen (Wi-Fi Only)",
          "89B59046-A6F1-4E13-B5B9-055FF2D2BBF1",
          List("")
        )
      )
    }
  }

}
