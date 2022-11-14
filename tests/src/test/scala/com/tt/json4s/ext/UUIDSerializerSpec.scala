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

package com.tt.json4s
package ext

import java.util.UUID
import org.scalatest.wordspec.AnyWordSpec

class NativeUUIDSerializerSpec extends UUIDSerializerSpec("Native") {
  val s: Serialization = native.Serialization
}

/**
 * System under specification for UUIDSerializer.
 */
abstract class UUIDSerializerSpec(mod: String) extends AnyWordSpec {

  def s: Serialization
  implicit lazy val formats: Formats = s.formats(NoTypeHints) ++ JavaTypesSerializers.all

  (mod + " UUIDSerializer Specification") should {
    "Serialize UUID's" in {
      val x = SubjectWithUUID(id = UUID.randomUUID())
      val ser = s.write(x)
      assert(s.read[SubjectWithUUID](ser) == x)
    }

    "Throw mapping exceptions when fails to create uuid from string" in {
      val nonUUIDString = "abcdef"
      assertThrows[MappingException] {
        JString(nonUUIDString).extract[UUID]
      }
    }
  }

}

case class SubjectWithUUID(id: UUID)
