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

package org.json4s
package mongo

import BsonDSL._

import scala.util.matching.Regex

import java.util.{Date, UUID}
import java.util.regex.Pattern

import org.bson.types.ObjectId
import org.specs2.mutable.Specification

import com.mongodb.{BasicDBList, DBObject}
import scala.collection.JavaConverters._

class BsonDSLSpec extends Specification  {

  "BsonDSL" should {
    "Convert ObjectId properly" in {
      val oid: ObjectId = ObjectId.get
      val qry: JObject = ("id" -> oid)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)

      dbo.get("id") must_== oid
    }

    "Convert List[ObjectId] properly" in {
      val oidList = ObjectId.get :: ObjectId.get :: ObjectId.get :: Nil
      val qry: JObject = ("ids" -> oidList)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val oidList2: List[ObjectId] = dbo.get("ids").asInstanceOf[BasicDBList].asScala.toList.map(_.asInstanceOf[ObjectId])

      oidList2 must_== oidList
    }

    "Convert Pattern properly" in {
      val ptrn: Pattern = Pattern.compile("^Mongo", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
      val qry: JObject = ("ptrn" -> ptrn)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrn2: Pattern = dbo.get("ptrn").asInstanceOf[Pattern]

      ptrn2.pattern must_== ptrn.pattern
      ptrn2.flags must_== ptrn.flags
    }

    "Convert List[Pattern] properly" in {
      val ptrnList =
        Pattern.compile("^Mongo1", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE) ::
        Pattern.compile("^Mongo2", Pattern.CASE_INSENSITIVE) ::
        Pattern.compile("^Mongo3") :: Nil
      val qry: JObject = ("ptrns" -> ptrnList)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrnList2: List[Pattern] = dbo.get("ptrns").asInstanceOf[BasicDBList].asScala.toList.map(_.asInstanceOf[Pattern])

      ptrnList.map(_.pattern) must_== ptrnList2.map(_.pattern)
      ptrnList.map(_.flags) must_== ptrnList2.map(_.flags)
    }

    "Convert Regex properly" in {
      val regex: Regex = "^Mongo".r
      val qry: JObject = ("regex" -> regex)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrn: Pattern = dbo.get("regex").asInstanceOf[Pattern]

      regex.pattern.pattern must_== ptrn.pattern
      regex.pattern.flags must_== ptrn.flags
    }

    "Convert UUID properly" in {
      val uuid: UUID = UUID.randomUUID
      val qry: JObject = ("uuid" -> uuid)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)

      dbo.get("uuid") must_== uuid
    }

    "Convert List[UUID] properly" in {
      val uuidList = UUID.randomUUID :: UUID.randomUUID :: UUID.randomUUID :: Nil
      val qry: JObject = ("ids" -> uuidList)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val uuidList2: List[UUID] = dbo.get("ids").asInstanceOf[BasicDBList].asScala.toList.map(_.asInstanceOf[UUID])

      uuidList2 must_== uuidList
    }

    "Convert Date properly" in {
      implicit val formats = DefaultFormats.lossless
      val dt: Date = new Date
      val qry: JObject = ("now" -> dt)
      val dbo: DBObject = JObjectParser.parse(qry)

      dbo.get("now") must_== dt
    }

    "Convert List[Date] properly" in {
      implicit val formats = DefaultFormats.lossless
      val dateList = new Date :: new Date :: new Date :: Nil
      val qry: JObject = ("dts" -> dateList)
      val dbo: DBObject = JObjectParser.parse(qry)
      val dateList2: List[Date] = dbo.get("dts").asInstanceOf[BasicDBList].asScala.toList.map(_.asInstanceOf[Date])

      dateList2 must_== dateList
    }
  }
}
