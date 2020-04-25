package org.json4s

import org.specs2.mutable.Specification

object EitherTest {

  case class OptionInt(i: Option[Int])

  case class EitherIntString(i: Either[Int, String])

  case class EitherStringInt(i: Either[String, Int])

  case class EitherListIntListString(i: Either[List[String], List[Int]])

  case class EitherListStringListInt(i: Either[List[Int], List[String]])

  case class EitherListListStringMapStringInt(i: Either[List[List[String]], List[Map[String, List[Int]]]])

}

class JacksonEitherTest extends EitherTest[JValue]("Native") with jackson.JsonMethods

abstract class EitherTest[T](mod: String) extends Specification with JsonMethods[T] {

  import EitherTest._

  implicit val formats: Formats = DefaultFormats + ShortTypeHints(List(classOf[Either[_, _]], classOf[List[_]]))


  (mod + " EitherTest Specification") should {
    "See that it works for Option[Int]" in {
      val opt = OptionInt(Some(39))
      Extraction.decompose(opt).extract[OptionInt].i.get must_== 39
    }

    "Work for Either[Int, String]" in {
      val opt = EitherIntString(Left(39))
      Extraction.decompose(opt).extract[EitherIntString].i.left.get must_== 39

      val opt2 = EitherIntString(Right("hello"))
      Extraction.decompose(opt2).extract[EitherIntString].i.right.get must_== "hello"
    }

    "Work for Either[List[Int], List[String]]" in {
      val opt = EitherListStringListInt(Left(List(1, 2, 3)))
      Extraction.decompose(opt).extract[EitherListStringListInt].i.left.get must_== List(1, 2, 3)

      val opt2 = EitherListStringListInt(Right(List("hello", "world")))
      Extraction.decompose(opt2).extract[EitherListStringListInt].i.right.get must_== List("hello", "world")
    }

    "Work for Either[List[List[String]], List[Map[String, List[Int]]]]" in {
      val opt = EitherListListStringMapStringInt(Left(List(List("a", "b", "c"), List("d", "e", "f"))))
      Extraction.decompose(opt).extract[EitherListListStringMapStringInt].i.left.get must_== List(List("a", "b", "c"), List("d", "e", "f"))

      val opt2 = EitherListListStringMapStringInt(Right(
        List(
          Map("hello" -> List(5, 4, 3, 2, 1), "world" -> List(10, 20, 30)),
          Map("bye" -> List(10), "world" -> List(10, 20, 30))
        )))
      Extraction.decompose(opt2).extract[EitherListListStringMapStringInt].i.right.get must_== List(
        Map("hello" -> List(5, 4, 3, 2, 1), "world" -> List(10, 20, 30)),
        Map("bye" -> List(10), "world" -> List(10, 20, 30))
      )
    }
  }
}
