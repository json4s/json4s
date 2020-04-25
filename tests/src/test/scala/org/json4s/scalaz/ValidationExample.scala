package org.json4s.scalaz

import scalaz._
import scalaz.syntax.bind._
import scalaz.syntax.traverse._
import scalaz.std.list._
import scalaz.syntax.validation._
import JsonScalaz._
import org.json4s._

import org.specs2.mutable.Specification

class ValidationExample extends Specification {

  case class Person(name: String, age: Int)

  "Validation" should {
    def min(x: Int): Int => Result[Int] = (y: Int) =>
      if (y < x) Fail("min", s"$y < $x") else y.success

    def max(x: Int): Int => Result[Int] = (y: Int) =>
      if (y > x) Fail("max", s"$y > $x") else y.success

    val json = native.JsonParser.parse(""" {"name":"joe","age":17} """)

    // Note 'apply _' is not needed on Scala 2.8.1 >=
    "fail when age is less than min age" in {
      // Age must be between 18 an 60
      val person = Person.applyJSON(field[String]("name"), validate[Int]("age") >==> min(18) >==> max(60))
      person(json) must_== Failure(NonEmptyList(UncategorizedError("min", "17 < 18", Nil)))
    }

    "pass when age within limits" in {
      val person = Person.applyJSON(field[String]("name"), validate[Int]("age") >==> min(16) >==> max(60))
      person(json) must_== Success(Person("joe", 17))
    }
  }

  case class Range(start: Int, end: Int)

  // This example shows:
  // * a validation where result depends on more than one value
  // * parse a List with invalid values
  "Range filtering" should {
    val json = native.JsonParser.parse(""" [{"s":10,"e":17},{"s":12,"e":13},{"s":11,"e":8}] """)

    val ascending = (x1: Int, x2: Int) => {
      if (x1 > x2) Fail("asc", s"${x1} > ${x2}") else (x1, x2).success
    }.disjunction

    // Valid range is a range having start <= end
    implicit def rangeJSON: JSONR[Range] = new JSONR[Range] {
      def read(json: JValue) =
        (((field[Int]("s")(json) |@| field[Int]("e")(json))(ascending)).disjunction.join map Range.tupled).validation
    }

    "fail if lists contains invalid ranges" in {
      val r = fromJSON[List[Range]](json)
      r.swap.toOption.get.list must_== IList(UncategorizedError("asc", "11 > 8", Nil))
    }

    "optionally return only valid ranges" in {
      val ranges = json.children.map(fromJSON[Range]).filter(_.isSuccess).sequenceU
      ranges must_== Success(List(Range(10, 17), Range(12, 13)))
    }
  }
}
