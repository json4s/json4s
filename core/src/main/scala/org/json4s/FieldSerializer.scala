/*
 * Copyright 2009-2011 WorldWide Conferencing, LLC
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

/**
 * Serializer which serializes all fields of a class too.
 *
 * Serialization can be intercepted by giving two optional PartialFunctions as
 * constructor parameters:
 * <p>
 * <pre>
 * FieldSerializer[WildDog](
 *   renameTo("name", "animalname") orElse ignore("owner"),
 *   renameFrom("animalname", "name")
 * )
 * </pre>
 */
case class FieldSerializer[A: Manifest](
  serializer:   PartialFunction[(String, Any), Option[(String, Any)]] = Map(),
  deserializer: PartialFunction[JField, JField] = Map()
)
 
object FieldSerializer {
  def renameFrom(name: String, newName: String): PartialFunction[JField, JField] = {
    case JField(`name`, x) => JField(newName, x)
  }

  def ignore(name: String): PartialFunction[(String, Any), Option[(String, Any)]] = {
    case (`name`, _) => None
  }

  def renameTo(name: String, newName: String): PartialFunction[(String, Any), Option[(String, Any)]] = {
    case (`name`, x) => Some(newName, x)
  }
}
