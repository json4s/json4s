/*
 * Copyright 2011 WorldWide Conferencing, LLC
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

package com.tt.json4s.mongo

import BsonDSL.*

import scala.util.matching.Regex
import java.util.{Date, UUID}
import java.util.regex.Pattern
import org.bson.types.ObjectId
import org.scalatest.wordspec.AnyWordSpec
import com.mongodb.{BasicDBList, DBObject}
import com.tt.json4s.{DefaultFormats, Formats, JObject}

import scala.collection.JavaConverters.*

class BsonDSLSpec extends AnyWordSpec {

  "BsonDSL" should {
    "Convert ObjectId properly" in {
      val oid: ObjectId = ObjectId.get
      val qry: JObject = "id" -> oid
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)

      assert(dbo.get("id") == oid)
    }

    "Convert List[ObjectId] properly" in {
      val oidList = ObjectId.get :: ObjectId.get :: ObjectId.get :: Nil
      val qry: JObject = "ids" -> oidList
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val oidList2: List[ObjectId] =
        dbo.get("ids").asInstanceOf[BasicDBList].asScala.toList.map(_.asInstanceOf[ObjectId])

      assert(oidList2 == oidList)
    }

    "Convert Pattern properly" in {
      val ptrn: Pattern = Pattern.compile("^Mongo", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
      val qry: JObject = "ptrn" -> ptrn
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrn2: Pattern = dbo.get("ptrn").asInstanceOf[Pattern]

      assert(ptrn2.pattern == ptrn.pattern)
      assert(ptrn2.flags == ptrn.flags)
    }

    "Convert List[Pattern] properly" in {
      val ptrnList =
        Pattern.compile("^Mongo1", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE) ::
        Pattern.compile("^Mongo2", Pattern.CASE_INSENSITIVE) ::
        Pattern.compile("^Mongo3") :: Nil
      val qry: JObject = "ptrns" -> ptrnList
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrnList2: List[Pattern] =
        dbo.get("ptrns").asInstanceOf[BasicDBList].asScala.toList.map(_.asInstanceOf[Pattern])

      assert(ptrnList.map(_.pattern) == ptrnList2.map(_.pattern))
      assert(ptrnList.map(_.flags) == ptrnList2.map(_.flags))
    }

    "Convert Regex properly" in {
      val regex: Regex = "^Mongo".r
      val qry: JObject = "regex" -> regex
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrn: Pattern = dbo.get("regex").asInstanceOf[Pattern]

      assert(regex.pattern.pattern == ptrn.pattern)
      assert(regex.pattern.flags == ptrn.flags)
    }

    "Convert UUID properly" in {
      val uuid: UUID = UUID.randomUUID
      val qry: JObject = "uuid" -> uuid
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)

      assert(dbo.get("uuid") == uuid)
    }

    "Convert List[UUID] properly" in {
      val uuidList = UUID.randomUUID :: UUID.randomUUID :: UUID.randomUUID :: Nil
      val qry: JObject = "ids" -> uuidList
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val uuidList2: List[UUID] = dbo.get("ids").asInstanceOf[BasicDBList].asScala.toList.map(_.asInstanceOf[UUID])

      assert(uuidList2 == uuidList)
    }

    "Convert Date properly" in {
      implicit val formats: Formats = DefaultFormats.lossless
      val dt: Date = new Date
      val qry: JObject = "now" -> dt
      val dbo: DBObject = JObjectParser.parse(qry)

      assert(dbo.get("now") == dt)
    }

    "Convert List[Date] properly" in {
      implicit val formats: Formats = DefaultFormats.lossless
      val dateList = new Date :: new Date :: new Date :: Nil
      val qry: JObject = "dts" -> dateList
      val dbo: DBObject = JObjectParser.parse(qry)
      val dateList2: List[Date] = dbo.get("dts").asInstanceOf[BasicDBList].asScala.toList.map(_.asInstanceOf[Date])

      assert(dateList2 == dateList)
    }
  }
}
