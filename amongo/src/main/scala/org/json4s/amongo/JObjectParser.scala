/*
 * Copyright 2016 VATBox
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

package org.json4s.amongo

import java.util.UUID
import java.util.regex.Pattern

import org.bson._
import org.bson.codecs.{PatternCodec, UuidCodec}
import org.bson.types.ObjectId
import org.json4s.JsonAST._
import org.json4s.ParserUtil.ParseException
import org.json4s.{Formats, JValue}

import scala.math.ScalaNumber

case class JObjectParser(optimizeBigInt: Boolean = true) {

  def parse(jv: JValue)(implicit formats: Formats): BsonValue = jv match {
    case JObject(obj) => parseObject(obj)
    case JArray(arr) => parseArray(arr)
    case primitive => throw new ParseException(s"Unable to parse $primitive to BsonValue", null)
  }

  private def parseObject(obj: List[JField])(implicit formats: Formats): BsonDocument = {
    val out = new BsonDocument()
    obj.filterNot { case (_, v) => v == JNothing }.foreach { case (name, jv) =>
      out.put(name, jValueToBson(jv))
    }
    out
  }

  private def parseArray(arr: List[JValue])(implicit formats: Formats): BsonArray = {
    val out = new BsonArray()
    arr.filterNot(_ == JNothing).foreach { jv =>
      out.add(jValueToBson(jv))
    }
    out
  }

  private def jValueToBson(jValue: JValue)(implicit formats: Formats): BsonValue = jValue match {
    case JObject(JField("$oid", JString(s)) :: Nil) if ObjectId.isValid(s) =>
      new BsonObjectId(new ObjectId(s))
    case JObject(JField("$regex", JString(s)) :: JField("$flags", JInt(f)) :: Nil) =>
      patternToBson(Pattern.compile(s, f.intValue))
    case JObject(JField("$dt", JString(s)) :: Nil) =>
      formats.dateFormat.parse(s).map(date => new BsonDateTime(date.getTime)) match {
        case Some(bsonDT) => bsonDT
        case None => throw new ParseException(s"Failed to parse into Date from $s", null)
      }
    case JObject(JField("$uuid", JString(s)) :: Nil) =>
      UUIDtoBson(UUID.fromString(s))
    case JObject(jvObj) => parseObject(jvObj)
    case JArray(arr) => parseArray(arr)
    case primitiveJValue => parseJValue(primitiveJValue)
  }

  private def parseJValue(jValue: JValue)(implicit formats: Formats): BsonValue = jValue match {
    case JNothing => sys.error("No BsonValue for 'Nothing'")
    case JNull => BsonNull.VALUE
    case JString(s) if s == null => new BsonString("null")
    case JString(s) => new BsonString(s)
    case JDouble(num) => new BsonDouble(num)
    case JLong(num) => new BsonInt64(num)
    case JBool(value) => new BsonBoolean(value)
    case JDecimal(num) => bigNumberToBson(num)
    case JInt(num) => bigNumberToBson(num)
    case fallback => new BsonString("") // Not quite sure if that's a good thing
  }

  private def bigNumberToBson(number: ScalaNumber): BsonValue = number match {
    case bi: BigInt if optimizeBigInt =>
      if (bi <= Int.MaxValue && bi >= Int.MinValue)
        new BsonInt32(bi.intValue())
      else if (bi <= Long.MaxValue && bi >= Long.MinValue) new BsonInt64(bi.longValue())
      else new BsonString(bi.toString)
    case bigNum => new BsonString(bigNum.toString)
  }

  private def patternToBson(pattern: Pattern): BsonValue = {
    val wrapper = new BsonDocumentWrapper[Pattern](pattern, new PatternCodec)
    wrapper
  }

  private def UUIDtoBson(uuid: UUID): BsonValue = {
    val wrapper = new BsonDocumentWrapper[UUID](uuid, new UuidCodec(UuidRepresentation.STANDARD))
    wrapper
  }
}

object JObjectParser {
  def parser = JObjectParser()
}