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

import java.util.regex.Pattern
import java.util.{Calendar, Date, UUID}

import org.bson._
import org.bson.codecs.UuidCodec
import org.bson.types.ObjectId
import org.json4s.JsonAST.JNull
import org.json4s._

import scala.collection.JavaConversions._

object JObjectSerializer {
  def serialize(a: Any)(implicit formats: Formats): JValue = {
    a.asInstanceOf[AnyRef] match {
      case null => JNull
      case b: BsonNull => JNull
      case b: BsonArray => JArray(b.toList.map(serialize))
      case b: BsonBoolean => JBool(b.getValue)
      case b: BsonDateTime => dateAsJValue(new Date(b.getValue)) // UTC Epoch [[https://docs.mongodb.com/manual/reference/bson-types/#date]]
      case b: BsonDocument => JObject(
        b.entrySet.toList.map(entry => JField(entry.getKey, serialize(entry.getValue)))
      )
      case b: BsonBinary if b.getType == BsonBinarySubType.UUID_STANDARD.getValue => uuidAsJValue(binaryUUID(b))
      case b: BsonDouble => JDouble(b.getValue)
      case b: BsonInt32 => JInt(BigInt(b.getValue))
      case b: BsonInt64 => JLong(b.getValue)
      case b: BsonObjectId if isObjectIdSerializerUsed => objectIdAsJValue(b.getValue)
      case b: BsonObjectId => JString(b.getValue.toString)
      case b: BsonString => JString(b.getValue)
      case x => typeToJValue(x)
    }
  }

  /*
    * This is used to convert DBObjects into JObjects, it's here just in case mongo will decide to return one of these objects
    */
  private def typeToJValue(a: Any)(implicit formats: Formats): JValue = a match {
    case null => JNull
    case x: String => JString(x)
    case x: Int => JInt(x)
    case x: Long => JLong(x)
    case x: Double => JDouble(x)
    case x: Float => JDouble(x)
    case x: Byte => JInt(BigInt(x))
    case x: BigInt => JInt(x)
    case x: BigDecimal => JDecimal(x)
    case x: Boolean => JBool(x)
    case x: Short => JInt(BigInt(x))
    case x: java.lang.Integer => JInt(BigInt(x.asInstanceOf[Int]))
    case x: java.lang.Long => JLong(x)
    case x: java.lang.Double => JDouble(x.asInstanceOf[Double])
    case x: java.lang.Float => JDouble(x.asInstanceOf[Float])
    case x: java.lang.Byte => JInt(BigInt(x.asInstanceOf[Byte]))
    case x: java.lang.Boolean => JBool(x.asInstanceOf[Boolean])
    case x: java.lang.Short => JInt(BigInt(x.asInstanceOf[Short]))
    case x: Calendar => dateAsJValue(x.getTime)
    case x: Date => dateAsJValue(x)
    case x: ObjectId if isObjectIdSerializerUsed => objectIdAsJValue(x)
    case x: ObjectId => JString(x.toString)
    case x: Pattern => patternAsJValue(x)
    case x: UUID => uuidAsJValue(x)
    case _ => JNothing
  }

  private def binaryUUID(bin: BsonBinary) = {
    /**
      * This code was copied from [[UuidCodec]] since no better elegant solution was found
      */
    def readLongFromArrayBigEndian(bytes: Array[Byte], offset: Int): Long = {
      var x: Long = 0
      x |= (0xFFL & bytes(offset + 7))
      x |= (0xFFL & bytes(offset + 6)) << 8
      x |= (0xFFL & bytes(offset + 5)) << 16
      x |= (0xFFL & bytes(offset + 4)) << 24
      x |= (0xFFL & bytes(offset + 3)) << 32
      x |= (0xFFL & bytes(offset + 2)) << 40
      x |= (0xFFL & bytes(offset + 1)) << 48
      x |= (0xFFL & bytes(offset)) << 56
      x
    }
    if (bin.getData.length == 16) {
      new UUID(readLongFromArrayBigEndian(bin.getData,0),readLongFromArrayBigEndian(bin.getData,8))
    } else throw new BSONException("Unexpected UUID representation")

  }

  private[amongo] def dateAsJValue(d: Date)(implicit formats: Formats) = JObject(JField("$dt", JString(formats.dateFormat.format(d))) :: Nil)

  private[amongo] def patternAsJValue(p: Pattern) = JObject(JField("$regex", JString(p.pattern)) :: JField("$flags", JInt(p.flags)) :: Nil)

  private[amongo] def objectIdAsJValue(oid: ObjectId) = JObject(JField("$oid", JString(oid.toString)) :: Nil)

  private[amongo] def uuidAsJValue(u: UUID): JValue = JObject(JField("$uuid", JString(u.toString)) :: Nil)

  private def isObjectIdSerializerUsed(implicit formats: Formats): Boolean = {
    formats.customSerializers.exists(_.getClass == classOf[ObjectIdSerializer])
  }
}
