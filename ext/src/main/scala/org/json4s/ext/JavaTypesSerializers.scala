/*
 * Copyright 2007-2011 WorldWide Conferencing, LLC
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

import java.net.{URI, URL}
import java.util.UUID

import scala.util.control.NonFatal

object JavaTypesSerializers {
  val all = List(UUIDSerializer, URLSerializer, URISerializer)
}

case object UUIDSerializer
  extends CustomSerializer[UUID](format =>
    (
      {
        case JString(s) =>
          try {
            UUID.fromString(s)
          } catch {
            case NonFatal(e) =>
              throw MappingException(e.getMessage, new java.lang.IllegalArgumentException(e))
          }
        case JNull => null
      },
      { case x: UUID =>
        JString(x.toString)
      }
    )
  )

case object URLSerializer
  extends CustomSerializer[URL](format =>
    (
      {
        case JString(s) => new URL(s)
        case JNull => null
      },
      { case x: URL =>
        JString(x.toString)
      }
    )
  )

case object URISerializer
  extends CustomSerializer[URI](format =>
    (
      {
        case JString(s) => URI.create(s)
        case JNull => null
      },
      { case x: URI =>
        JString(x.toString)
      }
    )
  )
