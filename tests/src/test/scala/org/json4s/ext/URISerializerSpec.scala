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

import java.net.URI
import org.specs2.mutable.Specification

class NativeURISerializerSpec extends URISerializerSpec("Native") {
  val s: Serialization = native.Serialization
}

class JacksonURISerializerSpec extends URISerializerSpec("Jackson") {
  val s: Serialization = jackson.Serialization
}

abstract class URISerializerSpec(mod: String) extends Specification {

  def s: Serialization
  implicit lazy val formats = s.formats(NoTypeHints) ++ JavaTypesSerializers.all

  (mod + " URISerializer Specification") should {
    "Serialize URI's" in {
      val x = SubjectWithURI(uri = URI.create("/test"))
      val ser = s.write(x)
      s.read[SubjectWithURI](ser) must_== x
    }
  }
}

case class SubjectWithURI(uri: URI)
