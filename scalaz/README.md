Scalaz support for json4s
=========================

This project adds a type class to parse JSON:

    trait JSON[A] {
      def read(json: JValue): Result[A]
      def write(value: A): JValue
    }

    type Result[A] = ValidationNEL[Error, A]

Function 'read' returns an Applicative Functor, enabling parsing in an applicative style.

Simple example
--------------

    scala> import scalaz._
    scala> import Scalaz._
    scala> import org.json4s.scalaz.JsonScalaz._
    scala> import org.json4s._

    scala> case class Address(street: String, zipCode: String)
    scala> case class Person(name: String, age: Int, address: Address)
  
    scala> val json = parse(""" {"street": "Manhattan 2", "zip": "00223" } """)
    scala> (field[String]("street")(json) |@| field[String]("zip")(json)) { Address }
    res0: Success(Address(Manhattan 2,00223))

    scala> (field[String]("streets")(json) |@| field[String]("zip")(json)) { Address }
    res1: Failure("no such field 'streets'")

Notice the required explicit types when reading fields from JSON. The library comes with helpers which
can lift functions with pure values into "parsing context". This works well with Scala's type inferencer:

    scala> Address.applyJSON(field("street"), field("zip"))(json)
    res2: Success(Address(Manhattan 2,00223))

Function 'applyJSON' above lifts function 

    (String, String) => Address 

to

    (JValue => Result[String], JValue => Result[String]) => (JValue => Result[Address])

Example which adds a new type class instance
--------------------------------------------

    scala> implicit def addrJSONR: JSONR[Address] = Address.applyJSON(field("street"), field("zip"))

    scala> val p = JsonParser.parse(""" {"name":"joe","age":34,"address":{"street": "Manhattan 2", "zip": "00223" }} """)
    scala> Person.applyJSON(field("name"), field("age"), field("address"))(p)
    res0: Success(Person(joe,34,Address(Manhattan 2,00223)))

Validation
----------

Applicative style parsing works nicely with validation and data conversion. It is easy to compose 
transformations with various combinators Scalaz provides. An often used combinator is called a Kleisli 
composition >=>.

    def min(x: Int): Int => Result[Int] = (y: Int) => 
      if (y < x) Fail("min", y + " < " + x) else y.success

    def max(x: Int): Int => Result[Int] = (y: Int) => 
      if (y > x) Fail("max", y + " > " + x) else y.success

    // Creates a function JValue => Result[Person]
    Person.applyJSON(field[String]("name"), validate[Int]("age") >==> min(18) >==> max(60))

Installation
------------

Add dependency to your SBT project description:

    val json4s_scalaz = "org.json4s" %% "json4s-scalaz" % "XXX"

Links
-----

* [More examples](https://github.com/lift/framework/tree/master/core/json-scalaz/src/test/scala/net/lifweb/json/scalaz)
* [Scalaz](http://code.google.com/p/scalaz/)
* [Kleisli composition](http://www.haskell.org/hoogle/?hoogle=%28a+-%3E+m+b%29+-%3E+%28b+-%3E+m+c%29+-%3E+%28a+-%3E+m+c%29)
