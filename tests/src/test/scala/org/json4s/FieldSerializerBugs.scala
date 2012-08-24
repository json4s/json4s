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

import org.specs.Specification
import native._

object FieldSerializerBugs extends Specification {
  import Serialization.{read, write => swrite}

  implicit val formats = DefaultFormats + FieldSerializer[AnyRef]()

/* FIXME: For some reason this fails on CI
  "AtomicInteger should not cause stack overflow" in {
    import java.util.concurrent.atomic.AtomicInteger

    val ser = swrite(new AtomicInteger(1))
    val atomic = read[AtomicInteger](ser)
    atomic.get mustEqual 1
  }
  */

  "Name with symbols is correctly serialized" in {
    implicit val formats = DefaultFormats + FieldSerializer[AnyRef]()

    val s = WithSymbol(5)
    val str = Serialization.write(s)
    str mustEqual """{"a-b*c":5}"""
    read[WithSymbol](str) mustEqual s
  }

  "FieldSerialization should work with Options" in {
    implicit val formats = DefaultFormats + FieldSerializer[ClassWithOption]()

    val t = new ClassWithOption
    t.field = Some(5)
    read[ClassWithOption](Serialization.write(t)).field mustEqual Some(5)
  }

  case class WithSymbol(`a-b*c`: Int)

  class ClassWithOption {
    var field: Option[Int] = None
  }
}


