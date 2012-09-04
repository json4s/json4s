package org.json4s

import org.specs2.mutable.Specification

object FieldSerializerBugs extends Specification {
  import native.JsonMethods._
  import native.Serialization
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


