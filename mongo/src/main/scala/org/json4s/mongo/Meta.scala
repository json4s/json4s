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

import java.util.{Calendar, Date, GregorianCalendar, UUID}
import java.util.regex.Pattern
import org.bson.types.ObjectId

object Meta {

  /*
  * For converting scala objects into DBObject values
  */
  object Reflection {
    import com.mongodb.DBRef

    /*
    * These don't require a conversion and can be put directly into a DBObject
    */
    val primitives = Set[Class[_]](classOf[String], classOf[Int], classOf[Long], classOf[Double],
                                  classOf[Float], classOf[Byte], classOf[BigInt], classOf[Boolean],
                                  classOf[Short], classOf[java.lang.Integer], classOf[java.lang.Long],
                                  classOf[java.lang.Double], classOf[java.lang.Float],
                                  classOf[java.lang.Byte], classOf[java.lang.Boolean],
                                  classOf[java.lang.Short])

    def isPrimitive(clazz: Class[_]) = primitives contains clazz

    /*
    * This is used to convert DBObjects into JObjects
    */
    def primitive2jvalue(a: Any) = a match {
      case x: String => JString(x)
      case x: Int => JInt(x)
      case x: Long => JInt(x)
      case x: Double => JDouble(x)
      case x: Float => JDouble(x)
      case x: Byte => JInt(BigInt(x))
      case x: BigInt => JInt(x)
      case x: Boolean => JBool(x)
      case x: Short => JInt(BigInt(x))
      case x: java.lang.Integer => JInt(BigInt(x.asInstanceOf[Int]))
      case x: java.lang.Long => JInt(BigInt(x.asInstanceOf[Long]))
      case x: java.lang.Double => JDouble(x.asInstanceOf[Double])
      case x: java.lang.Float => JDouble(x.asInstanceOf[Float])
      case x: java.lang.Byte => JInt(BigInt(x.asInstanceOf[Byte]))
      case x: java.lang.Boolean => JBool(x.asInstanceOf[Boolean])
      case x: java.lang.Short => JInt(BigInt(x.asInstanceOf[Short]))
      case _ => sys.error(s"not a primitive ${a.asInstanceOf[AnyRef].getClass.getName}")
    }

    /*
    * Date types require formatting
    */
    val datetypes = Set[Class[_]](classOf[Calendar], classOf[Date], classOf[GregorianCalendar])

    @deprecated("Use `isDateType` instead", "3.1.0")
    def datetype_?(clazz: Class[_]) = isDateType(clazz)
    def isDateType(clazz: Class[_]) = datetypes contains clazz

    def datetype2jvalue(a: Any)(implicit formats: Formats) = a match {
      case x: Calendar => dateAsJValue(x.getTime, formats)
      case x: Date => dateAsJValue(x, formats)
    }

    def datetype2dbovalue(a: Any) = a match {
      case x: Calendar => x.getTime
      case x: Date => x
    }

    /*
    * Extended Mongo types.
    */
    val mongotypes = Set[Class[_]](
      classOf[DBRef], classOf[ObjectId], classOf[Pattern], classOf[UUID])

    @deprecated("Use `isMongoType` instead", "3.1.0")
    def mongotype_?(clazz: Class[_]) = isMongoType(clazz)
    def isMongoType(clazz: Class[_]) = mongotypes contains clazz

    /*
    * Definitive place for JValue conversion of mongo types
    */
    def mongotype2jvalue(a: Any)(implicit formats: Formats) = a match {
      case x: ObjectId => objectIdAsJValue(x, formats)
      case x: Pattern => patternAsJValue(x)
      case x: UUID => uuidAsJValue(x)
      case x: DBRef => sys.error("DBRefs are not supported.")
      case _ => sys.error(s"not a mongotype  ${a.asInstanceOf[AnyRef].getClass.getName}")
    }
  }

  def dateAsJValue(d: Date, formats: Formats) = JObject(JField("$dt", JString(formats.dateFormat.format(d))) :: Nil)
  def objectIdAsJValue(oid: ObjectId): JValue = JObject(JField("$oid", JString(oid.toString)) :: Nil)
  def patternAsJValue(p: Pattern): JValue = JObject(JField("$regex", JString(p.pattern)) :: JField("$flags", JInt(p.flags)) :: Nil)
  def uuidAsJValue(u: UUID): JValue = JObject(JField("$uuid", JString(u.toString)) :: Nil)

  def objectIdAsJValue(oid: ObjectId, formats: Formats): JValue =
    if (isObjectIdSerializerUsed(formats))
      objectIdAsJValue(oid)
    else
      JString(oid.toString)

  /*
  * Check to see if the ObjectIdSerializer is being used.
  */
  private def isObjectIdSerializerUsed(formats: Formats): Boolean =
    formats.customSerializers.exists(_.getClass == objectIdSerializerClass)

  private val objectIdSerializerClass = classOf[ObjectIdSerializer]
}

