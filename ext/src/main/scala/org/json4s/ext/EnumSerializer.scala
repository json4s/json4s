/*
 * Copyright 2006-2010 WorldWide Conferencing, LLC
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
package ext

import scala.reflect.ClassTag

class EnumSerializer[E <: Enumeration: ClassTag](enumeration: E) extends Serializer[EnumValue[E]] {
  import JsonDSL._

  private[this] val EnumerationClass = classOf[Enumeration#Value]

  private[this] def isValid(json: JValue) = json match {
    case JInt(value) => enumeration.values.toSeq.map(_.id).contains(value.toInt)
    case _ => false
  }

  private[this] def enumerationValueToEnumValueOfE(value: enumeration.Value): EnumValue[E] =
    value.asInstanceOf[EnumValue[E]]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), EnumValue[E]] = {
    case (TypeInfo(EnumerationClass, _), json) if isValid(json) =>
      json match {
        case JInt(value) => enumerationValueToEnumValueOfE(enumeration(value.toInt))
        case value => throw new MappingException(s"Can't convert $value to $EnumerationClass")
      }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: Enumeration#Value if enumeration.values.exists(_ == i) => i.id
  }
}

class EnumNameSerializer[E <: Enumeration: ClassTag](enumeration: E) extends Serializer[EnumValue[E]] {
  import JsonDSL._

  private[this] val EnumerationClass = classOf[Enumeration#Value]

  private[this] def enumerationValueToEnumValueOfE(value: enumeration.Value): EnumValue[E] =
    value.asInstanceOf[EnumValue[E]]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), EnumValue[E]] = {
    case (_ @TypeInfo(EnumerationClass, _), json) if isValid(json) => {
      json match {
        case JString(value) => enumerationValueToEnumValueOfE(enumeration.withName(value))
        case value => throw new MappingException(s"Can't convert $value to $EnumerationClass")
      }
    }
  }

  private[this] def isValid(json: JValue) = json match {
    case JString(value) if enumeration.values.exists(_.toString == value) => true
    case _ => false
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: Enumeration#Value if enumeration.values.exists(_ == i) => i.toString
  }
}
