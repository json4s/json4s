/*
 * Copyright 2010-2011 WorldWide Conferencing, LLC
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

import java.util.UUID
import java.util.regex.Pattern
import com.mongodb.{BasicDBObject, BasicDBList, DBObject}
import org.bson.types.ObjectId
import java.util.concurrent.atomic.AtomicReference
import org.json4s.ParserUtil.ParseException
import collection.JavaConverters._

object JObjectParser  {
  /**
    * Set this to override JObjectParser turning strings that are valid
    * ObjectIds into actual ObjectIds. For example, place the following in Boot.boot:
    *
    * <code>JObjectParser.stringProcessor.default.set((s: String) => s)</code>
    */
  val stringProcessor = new AtomicReference[String => Object](defaultStringProcessor _) {}

  def defaultStringProcessor(s: String): Object = {
    if (ObjectId.isValid(s)) new ObjectId(s)
    else s
  }

  /*
  * Parse a JObject into a DBObject
  */
  def parse(jo: JValue)(implicit formats: Formats): DBObject =
    Parser.parse(jo, formats)

  /*
  * Serialize a DBObject into a JObject
  */
  def serialize(a: Any)(implicit formats: Formats): JValue = serialize0(a, formats)

  private[this] def serialize0(a: Any, formats: Formats): JValue = {
    import Meta.Reflection._
    a.asInstanceOf[AnyRef] match {
      case null => JNull
      case x if isPrimitive(x.getClass) => primitive2jvalue(x)
      case x if isDateType(x.getClass) => datetype2jvalue(x)(formats)
      case x if isMongoType(x.getClass) => mongotype2jvalue(x)(formats)
      case x: BasicDBList => JArray(x.asScala.toList.map( x => serialize0(x, formats)))
      case x: BasicDBObject => JObject(
        x.keySet.asScala.toList.map { f =>
          JField(f.toString, serialize0(x.get(f.toString), formats))
        }
      )
      case x => {
        JNothing
      }
    }
  }

  object Parser {

    def parse(jv: JValue, formats: Formats): DBObject = jv match {
      case jo: JObject =>
        parseObject(jo.obj, formats)
      case ja: JArray =>
        parseArray(ja.arr, formats)
      case x => throw new ParseException(s"Couldn't parse $x to a DBObject", null)
    }

    private def parseArray(arr: List[JValue], formats: Formats): BasicDBList = {
      val dbl = new BasicDBList
      trimArr(arr) foreach { a =>
        a match {
          case JObject(JField("$oid", JString(s)) :: Nil) if (ObjectId.isValid(s)) =>
            dbl.add(new ObjectId(s))
          case JObject(JField("$regex", JString(s)) :: JField("$flags", JInt(f)) :: Nil) =>
            dbl.add(Pattern.compile(s, f.intValue))
          case JObject(JField("$dt", JString(s)) :: Nil) =>
            formats.dateFormat.parse(s) foreach { d => dbl.add(d) }
          case JObject(JField("$uuid", JString(s)) :: Nil) =>
            dbl.add(UUID.fromString(s))
          case JArray(arr) => dbl.add(parseArray(arr, formats))
          case JObject(jo) => dbl.add(parseObject(jo, formats))
          case jv: JValue => dbl.add(renderValue(jv, formats))
        }
      }
      dbl
    }

    private def parseObject(obj: List[JField], formats: Formats): BasicDBObject = {
      val dbo = new BasicDBObject
      trimObj(obj) foreach { jf =>
        jf._2 match {
          case JObject(JField("$oid", JString(s)) :: Nil) if (ObjectId.isValid(s)) =>
            dbo.put(jf._1, new ObjectId(s))
          case JObject(JField("$regex", JString(s)) :: JField("$flags", JInt(f)) :: Nil) =>
            dbo.put(jf._1, Pattern.compile(s, f.intValue))
          case JObject(JField("$dt", JString(s)) :: Nil) =>
            formats.dateFormat.parse(s) foreach { d => dbo.put(jf._1, d) }
          case JObject(JField("$uuid", JString(s)) :: Nil) =>
            dbo.put(jf._1, UUID.fromString(s))
          case JArray(arr) => dbo.put(jf._1, parseArray(arr, formats))
          case JObject(jo) => dbo.put(jf._1, parseObject(jo, formats))
          case jv: JValue => dbo.put(jf._1, renderValue(jv, formats))
        }
      }
      dbo
    }

    private def renderValue(jv: JValue, formats: Formats): Object = jv match {
      case JBool(b) => java.lang.Boolean.valueOf(b)
      case JInt(n) => renderInteger(n)
      case JDouble(n) => new java.lang.Double(n)
      case JNull => null
      case JNothing => sys.error("can't render 'nothing'")
      case JString(null) => "null"
      case JString(s) => stringProcessor.get()(s)
      case _ =>  ""
    }

    // FIXME: This is not ideal.
    private def renderInteger(i: BigInt): Object = {
      if (i <= java.lang.Integer.MAX_VALUE && i >= java.lang.Integer.MIN_VALUE) {
        new java.lang.Integer(i.intValue)
      }
      else if (i <= java.lang.Long.MAX_VALUE && i >= java.lang.Long.MIN_VALUE) {
        new java.lang.Long(i.longValue)
      }
      else {
        i.toString
      }
    }

    private def trimArr(xs: List[JValue]) = xs.filterNot(_ == JNothing)
    private def trimObj(xs: List[JField]) = xs.filterNot(_._2 == JNothing)
  }
}
