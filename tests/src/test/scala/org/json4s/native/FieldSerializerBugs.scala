package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import FieldSerializerBugs._

class FieldSerializerBugs extends AnyWordSpec {
  import native.Serialization
  import Serialization.{read, write => swrite}

  implicit val formats: Formats = DefaultFormats + FieldSerializer[AnyRef]()

  /* FIXME: it doesn't cause a stack overflow but the ser/deser doesn't work
  "AtomicInteger should not cause stack overflow" in {
    import java.util.concurrent.atomic.AtomicInteger

    val ser = swrite(new AtomicInteger(1))
    val atomic = read[AtomicInteger](ser)
    assert(atomic.get == 1)
  }
   */

  "Serializing a singleton object should not cause stack overflow" in {
    swrite(SingletonObject)
  }

  "Name with symbols is correctly serialized" in {
    implicit val formats: Formats = DefaultFormats + FieldSerializer[AnyRef]()

    val s = WithSymbol(5)
    val str = Serialization.write(s)
    assert(str == """{"a-b*c":5}""")
    assert(read[WithSymbol](str) == s)
  }

  "FieldSerialization should work with Options" in {
    implicit val formats: Formats = DefaultFormats + FieldSerializer[ClassWithOption]()

    val t = new ClassWithOption
    t.field = Some(5)
    assert(read[ClassWithOption](Serialization.write(t)).field == Some(5))
  }

  "FieldSerializer's manifest should not be overridden when it's added to Formats" in {
    val fieldSerializer = FieldSerializer[Type1](FieldSerializer.renameTo("num", "yum"))
    implicit val formats: Formats = DefaultFormats + (fieldSerializer: FieldSerializer[_])
    val expected1 = JObject(JField("yum", JInt(123)))
    val expected2 = JObject(JField("num", JInt(456)))
    assert(Extraction.decompose(Type1(123)) == expected1)
    assert(Extraction.decompose(Type2(456)) == expected2)
  }
}

object FieldSerializerBugs {
  case class WithSymbol(`a-b*c`: Int)

  class ClassWithOption {
    var field: Option[Int] = None
  }

  case class Type1(num: Int)
  case class Type2(num: Int)

  object SingletonObject
}
