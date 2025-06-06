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
import org.scalatest.wordspec.AnyWordSpec
import com.mongodb.DBObject
import scala.util.control.Exception._

class JObjectParserSpec extends AnyWordSpec {

  def buildTestData: (ObjectId, DBObject) = {
    val oid = ObjectId.get
    val dbo = JObjectParser.parse("x" -> oid.toString)(using DefaultFormats)
    (oid, dbo)
  }

  "JObjectParser" should {
    "convert strings to ObjectId by default" in {
      val (oid, dbo) = buildTestData
      val xval = allCatch.opt(dbo.get("x").asInstanceOf[ObjectId])

      assert(xval.isDefined)
      xval.foreach { x =>
        assert(x == oid)
      }
    }
    "not convert strings to ObjectId when configured not to" in {
      JObjectParser.stringProcessor.set((s: String) => s)

      val (oid, dbo) = buildTestData
      val xval = allCatch.opt(dbo.get("x").asInstanceOf[String])

      assert(xval.isDefined)
      xval.foreach { x =>
        assert(x == oid.toString)
      }
    }
  }
}
