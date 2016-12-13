/*
 * Copyright 2012 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.json4s
package mongo

import JsonDSL._

import org.bson.types.ObjectId
import org.specs2.mutable.Specification
import com.mongodb.{BasicDBObject, DBObject}
import scala.util.control.Exception._

object JObjectParserSpec extends Specification  {

  title("JObjectParser Specification")

  sequential

  def buildTestData: (ObjectId, DBObject) = {
    val oid = ObjectId.get
    val dbo = JObjectParser.parse("x" -> oid.toString)(DefaultFormats)
    (oid, dbo)
  }

  "JObjectParser" should {
    "convert strings to ObjectId by default" in {
      val (oid, dbo) = buildTestData
      val xval = allCatch.opt(dbo.get("x").asInstanceOf[ObjectId])

      xval.isDefined must_== true
      xval.toList map { x =>
        x must_== oid
      } reduce (_ and _)
    }

    "not convert strings to ObjectId when configured not to" in {
      JObjectParser.stringProcessor.set((s: String) => s)

      val (oid, dbo) = buildTestData
      val xval = allCatch.opt(dbo.get("x").asInstanceOf[String])

      xval.isDefined must_== true
      xval.toList map { x =>
        x must_== oid.toString
      } reduce (_ and _)
    }

    "convert JLong to Long, JDecimal to String" in {
      val l: Long = 1481520538344L
      val bd = "1.234"
      val jObj: JObject =
        ("l" -> JLong(l)) ~
        ("bd" -> JDecimal(BigDecimal(bd)))

      val jDBObj = new BasicDBObject()
      jDBObj.put("l", java.lang.Long.valueOf(l))
      jDBObj.put("bd", bd)

      implicit val format = DefaultFormats

      JObjectParser.parse(jObj) must_== jDBObj
    }

    "convert JInt to Different types according to size" in {
      val i: Int = 123
      val l: Long = 1481520538344L
      val bi = BigInt("10000000000000000000")
      val jObj: JObject =
        ("i" -> JInt(i)) ~
        ("l" -> JInt(l)) ~
        ("s" -> JInt(bi))

      implicit val format = DefaultFormats

      val jDBObj = JObjectParser.parse(jObj)
      jDBObj.get("i") must haveClass[java.lang.Integer]
      jDBObj.get("l") must haveClass[java.lang.Long]
      jDBObj.get("s") must haveClass[java.lang.String]

    }
  }
}

