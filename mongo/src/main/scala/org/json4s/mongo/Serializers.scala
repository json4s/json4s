/*
* Copyright 2010-2011 WorldWide Conferencing, LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.json4s
package mongo

import java.util.{Date, UUID}
import java.util.regex.Pattern

import org.bson.types.ObjectId

/*
* Provides a way to serialize/de-serialize ObjectIds.
*
* Queries for a ObjectId (oid) using the lift-json DSL look like:
* ("_id" -> ("$oid" -> oid.toString))
*/
class ObjectIdSerializer extends Serializer[ObjectId] {
  private val ObjectIdClass = classOf[ObjectId]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ObjectId] = {
    case (TypeInfo(ObjectIdClass, _), json) => json match {
      case JObject(JField("$oid", JString(s)) :: Nil) if (ObjectId.isValid(s)) =>
        new ObjectId(s)
      case x => throw new MappingException(s"Can't convert $x to ObjectId")
    }
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case x: ObjectId => Meta.objectIdAsJValue(x)
  }
}

/*
* Provides a way to serialize/de-serialize Patterns.
*
* Queries for a Pattern (pattern) using the lift-json DSL look like:
* ("pattern" -> (("$regex" -> pattern.pattern) ~ ("$flags" -> pattern.flags)))
* ("pattern" -> (("$regex" -> "^Mo") ~ ("$flags" -> Pattern.CASE_INSENSITIVE)))
*/
class PatternSerializer extends Serializer[Pattern] {
  private val PatternClass = classOf[Pattern]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Pattern] = {
    case (TypeInfo(PatternClass, _), json) => json match {
      case JObject(JField("$regex", JString(s)) :: JField("$flags", JInt(f)) :: Nil) =>
        Pattern.compile(s, f.intValue)
      case x => throw new MappingException(s"Can't convert $x to Pattern")
    }
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case x: Pattern => Meta.patternAsJValue(x)
  }
}

/*
* Provides a way to serialize/de-serialize Dates.
*
* Queries for a Date (dt) using the lift-json DSL look like:
* ("dt" -> ("$dt" -> formats.dateFormat.format(dt)))
*/
class DateSerializer(fieldName: String = "$dt") extends Serializer[Date] {
  private val DateClass = classOf[Date]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Date] = {
    case (TypeInfo(DateClass, _), json) => json match {
      case JObject(JField(`fieldName`, JString(s)) :: Nil) =>
        format.dateFormat.parse(s).getOrElse(throw new MappingException(s"Can't parse $s to Date"))
      case x => throw new MappingException(s"Can't convert $x to Date")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case d: Date => JObject(JField(fieldName, JString(format.dateFormat.format(d))) :: Nil)
  }
}

/*
* Provides a way to serialize/de-serialize UUIDs.
*
* Queries for a UUID (u) using the lift-json DSL look like:
* ("uuid" -> ("$uuid" -> u.toString))
*/
class UUIDSerializer extends Serializer[UUID] {
  private val UUIDClass = classOf[UUID]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), UUID] = {
    case (TypeInfo(UUIDClass, _), json) => json match {
      case JObject(JField("$uuid", JString(s)) :: Nil) => UUID.fromString(s)
      case x => throw new MappingException(s"Can't convert $x to UUID")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: UUID => Meta.uuidAsJValue(x)
  }
}

