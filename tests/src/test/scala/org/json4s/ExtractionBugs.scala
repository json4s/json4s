package org.json4s

import org.specs2.mutable.Specification
import reflect.{ClassDescriptor, PrimaryConstructor, Reflector}
import org.json4s.native.Document
import java.util
import java.math.{BigDecimal => JavaBigDecimal, BigInteger => JavaBigInteger}

import org.specs2.specification.core.Fragments

object NativeExtractionBugs extends ExtractionBugs[Document]("Native") with native.JsonMethods
object JacksonExtractionBugs extends ExtractionBugs[JValue]("Jackson") with jackson.JsonMethods

trait SharedModule {
  case class SharedObj(name: String, visible: Boolean = false)
}

object PingPongGame extends SharedModule

object WithPrimitiveAlias {
  type MyDouble = Double
}
case class WithPrimitiveAlias(foo: Seq[WithPrimitiveAlias.MyDouble])

object ExtractionBugs {

  case class Response(data: List[Map[String, Int]])

  case class OptionOfInt(opt: Option[Int])

  case class PMap(m: Map[String, List[String]])

  case class ManyConstructors(id: Long, name: String, lastName: String, email: String) {
    def this() = this(0, "John", "Doe", "")
    def this(name: String) = this(0, name, "Doe", "")
    def this(name: String, email: String) = this(0, name, "Doe", email)
  }

  case class ManyConstructorsWithPrimary @PrimaryConstructor() (id: Long, name: String, lastName: String, email: String) {
    def this() = this(0, "John", "Doe", "")
    def this(name: String) = this(0, name, "Doe", "")
    def this(id: Long, name: String, lastName: String, email: String, domain: String) = {
      this(id, name, lastName, email + "@" + domain)
    }
  }

  case class ManyPrimaryConstructors @PrimaryConstructor() () {
    @PrimaryConstructor def this(name: String) = this()
  }

  case class ExtractWithAnyRef()

  case class UnicodeFieldNames(`foo.bar,baz`: String)

  object HasCompanion {
    def hello = "hello"
  }
  case class HasCompanion(nums: List[Int])

  case class ABigDecimal(num: BigDecimal)

  case class AJavaBigDecimal(num: JavaBigDecimal)

  case class AJavaBigInteger(num: JavaBigInteger)

  trait Content {
    def id: String
    def name: String
  }

  object Content {

    class ContentClass(val id: String, val name: String) extends Content

    def apply(id: String, name: String): Content = {
      new ContentClass(id, name)
    }
  }

  trait SomeOtherContent {
    def path: String
    def isFoo: Boolean
    def content: Content
  }

  object SomeOtherContent {

    class SomeOtherContentClass(val path: String, val isFoo: Boolean, val content: Content) extends SomeOtherContent

    def apply(path: String, isFoo: Boolean, content: Content): SomeOtherContent = {
      new SomeOtherContentClass(path, isFoo, content)
    }
  }

  trait ContentWithOption {
    def path: Option[String]
    def age: Option[Long]
    def content: Content
  }

  object ContentWithOption {

    class ContentWithOptionClass(val path: Option[String], val age: Option[Long], val content: Content) extends ContentWithOption

    def apply(path: Option[String], age: Option[Long], content: Content): ContentWithOption = {
      new ContentWithOptionClass(path, age, content)
    }
  }

  trait TraitWithTypeMember {

    type A

    def a: A

    val seq = Seq(a)
  }

  case class ClassWithSuperTypeMember(a: String) extends TraitWithTypeMember {
    override type A = String
  }

  object CaseClassWithCompanion {

    def apply(v: String): CaseClassWithCompanion = CaseClassWithCompanion(v, "Bar")

  }

  case class CaseClassWithCompanion(value: String, other: String)

  case class CompanionSample(s: String, i: Int)

  object CompanionSample {
    def apply(str: String, i1: Int, i2: Int): CompanionSample = CompanionSample(str, i1 + i2)
  }

  /** Custom serializer for MapImplementation
   *  This is used to show that custom (strange) serialisations were once broken.
   */
  class MapImplementationSerializer extends CustomSerializer[MapImplementation](formats => (
    { case MapImplementationSerializer.strangeSerialization => new MapImplementation() },
    { case _: MapImplementation => MapImplementationSerializer.strangeSerialization }
  ))

  object MapImplementationSerializer {
    val strangeSerialization = Extraction.decompose(MapImplementation.content)(DefaultFormats)
  }
}
abstract class ExtractionBugs[T](mod: String) extends Specification with JsonMethods[T] {

  import ExtractionBugs._
  implicit val formats: Formats = DefaultFormats.withCompanions(
    classOf[PingPongGame.SharedObj] -> PingPongGame) + new MapImplementationSerializer()

  "Primitive type should not hang" in {
    val a = WithPrimitiveAlias(Vector(1.0, 2.0, 3.0, 4.0))
    try {
      Extraction.decompose(a)
    } catch {
      case _: MappingException =>  {}
    }
    1 must_== 1
  }

  (mod+" Extraction bugs Specification") should {
    "ClassCastException (BigInt) regression 2 must pass" in {
      val opt = OptionOfInt(Some(39))
      Extraction.decompose(opt).extract[OptionOfInt].opt.get must_== 39
    }

    "Extraction should not fail when Maps values are Lists" in {
      val m = PMap(Map("a" -> List("b"), "c" -> List("d")))
      Extraction.decompose(m).extract[PMap] must_== m
    }

    "Extraction should not fail when class is defined in a trait" in {
      val inst = PingPongGame.SharedObj("jeff", visible = true)
      val extr = Extraction.decompose(inst)
      extr must_== JObject("name" -> JString("jeff"), "visible" -> JBool(true))
      extr.extract[PingPongGame.SharedObj] must_== inst
    }

    "Extraction should always choose constructor with the most arguments if more than one constructor exists" in {
      val args = Reflector.describe[ManyConstructors].asInstanceOf[ClassDescriptor].mostComprehensive
      args.size must_== 4
    }

    "Extraction should always choose primary constructor if exists" in {
      val args = Reflector.describe[ManyConstructorsWithPrimary].asInstanceOf[ClassDescriptor].mostComprehensive
      args.size must_== 4
      args.map(_.name) must_== Seq("id", "name", "lastName", "email")
    }

    "Extraction should throw exception if two or more constructors marked as primary" in {
      Reflector.describe[ManyPrimaryConstructors].asInstanceOf[ClassDescriptor].mostComprehensive must
        throwA[IllegalArgumentException]
    }

    "Extraction should handle AnyRef" in {
      implicit val formats = DefaultFormats.withHints(FullTypeHints(classOf[ExtractWithAnyRef] :: Nil))
      val json = JObject(JField("jsonClass", JString(classOf[ExtractWithAnyRef].getName)) :: Nil)
      val extracted = Extraction.extract[AnyRef](json)
      extracted must_== ExtractWithAnyRef()
    }

    "Extraction should work with unicode encoded field names (issue 1075)" in {
      parse("""{"foo.bar,baz":"x"}""").extract[UnicodeFieldNames] must_== UnicodeFieldNames("x")
    }

    "Extraction should not fail if case class has a companion object" in {
      parse("""{"nums":[10]}""").extract[HasCompanion] must_== HasCompanion(List(10))
    }

    "Extraction should not fail if using companion objects apply method" in {
      val content: Content = parse("""{"id":"some-id", "name":"some-name"}""").extract[Content]
      content must haveClass[Content.ContentClass]
      content.id must_== "some-id"
      content.name must_== "some-name"
    }

    "Extraction should not fail if using companion objects apply method to extract nested json field" in {
      val json = """{"path":"some-path", "isFoo":false, "content": {"id":"some-id", "name":"some-name"}}"""
      val someOtherContent: SomeOtherContent = parse(json).extract[SomeOtherContent]
      someOtherContent must haveClass[SomeOtherContent.SomeOtherContentClass]
      val content = someOtherContent.content
      someOtherContent.isFoo must_== false
      someOtherContent.path must_== "some-path"
      content.id must_== "some-id"
      content.name must_== "some-name"
    }

    "Extraction should not fail if using companion objects apply method with fields with options of primitive types" in {
      val json = """{"path":"some-path", "age": 5, "content": {"id":"some-id", "name":"some-name"}}"""
      val contentWithOption: ContentWithOption = parse(json).extract[ContentWithOption]
      contentWithOption must haveClass[ContentWithOption.ContentWithOptionClass]
      val content = contentWithOption.content
      contentWithOption.path must_== Some("some-path")
      contentWithOption.age must_== Some(5)
      content.id must_== "some-id"
      content.name must_== "some-name"
    }

    "Extraction should not fail if using companion objects apply method with fields with options of primitive types (option field missing)" in {
      val json = """{"path":"some-path", "content": {"id":"some-id", "name":"some-name"}}"""
      val contentWithOption: ContentWithOption = parse(json).extract[ContentWithOption]
      contentWithOption must haveClass[ContentWithOption.ContentWithOptionClass]
      val content = contentWithOption.content
      contentWithOption.path must_== Some("some-path")
      contentWithOption.age must_== None
      content.id must_== "some-id"
      content.name must_== "some-name"
    }

    "Issue 1169" in {
      val json = parse("""{"data":[{"one":1, "two":2}]}""")
      json.extract[Response] must_== Response(List(Map("one" -> 1, "two" -> 2)))
    }

    "Extraction should extract a java.util.ArrayList as array" in {
      Extraction.decompose(new util.ArrayList[String]()) must_== JArray(Nil)
    }

    "Extraction should extract a java.util.ArrayList as array" in {
      val json = parse("""["one", "two"]""")
      val lst = new util.ArrayList[String]()
      lst.add("one")
      lst.add("two")
      json.extract[util.ArrayList[String]] must_== lst
    }

    "Extraction should be able to call companion object apply method even when c'tors exists" in {
      val json = parse("""{"v": "Foo"}""")
      val expected = CaseClassWithCompanion("Foo")
      json.extract[CaseClassWithCompanion] must_== expected
    }

    "Parse 0 as JavaBigDecimal" in {
      val bjd = AJavaBigDecimal(BigDecimal("0").bigDecimal)
      parse("""{"num": 0}""",useBigDecimalForDouble = true).extract[AJavaBigDecimal] must_== bjd
      parse("""{"num": 0}""").extract[AJavaBigDecimal] must_== bjd
    }

    "Extract a JavaBigDecimal from a decimal value" in {
      val jbd = AJavaBigDecimal(BigDecimal("12.305").bigDecimal)
      parse("""{"num": 12.305}""", useBigDecimalForDouble = true).extract[AJavaBigDecimal] must_== jbd
      parse("""{"num": 12.305}""").extract[AJavaBigDecimal] must_== jbd
    }

    "Parse 0 as java BigInteger" in {
      val bji = AJavaBigInteger(BigInt("0").bigInteger)
      parse("""{"num": 0}""").extract[AJavaBigInteger] must_== bji
    }


    "Extract a java BigInteger from a long value" in {
      val bji = AJavaBigInteger(BigInt(Long.MaxValue).bigInteger)
      parse(s"""{"num": ${Long.MaxValue}}""").extract[AJavaBigInteger] must_== bji
    }


    "Parse 0 as BigDecimal" in {
      val bd = ABigDecimal(BigDecimal("0"))
      parse("""{"num": 0}""", useBigDecimalForDouble = true).extract[ABigDecimal] must_== bd
    }

    "Extract a bigdecimal from a decimal value" in {
      val bd = ABigDecimal(BigDecimal("12.305"))
      parse("""{"num": 12.305}""", useBigDecimalForDouble = true).extract[ABigDecimal] must_== bd
    }

    "Decompose a class with a super-type type member" in {
      val obj = ClassWithSuperTypeMember("foo")

      val result = Extraction.decompose(obj)

      result mustEqual JObject("a" -> JString("foo"))
    }

    "An implementation of Map should serialize with a CustomSerializer" in {
      Extraction.decompose(new MapImplementation()) must_== MapImplementationSerializer.strangeSerialization
    }

    "An implementation of Map should deserialize with a CustomSerializer" in {
      Extraction.extract[MapImplementation](MapImplementationSerializer.strangeSerialization) must_== new MapImplementation()
    }

    "Apply can't be mostComprehensive" in {
      val obj = CompanionSample("hello", 1, 2)
      val json = Extraction.decompose(obj)
      json mustEqual JObject("s" -> JString("hello"), "i" -> JInt(3))
    }

    "Extract error should preserve error message when strict option parsing is enabled" in {
      implicit val formats = new DefaultFormats {
        override val strictOptionParsing: Boolean = true
      }

      val obj = parse("""{"opt": "not an int"}""".stripMargin)

      Extraction.extract[OptionOfInt](obj) must throwA(
        new MappingException(
          """
            |No usable value for opt
            |Do not know how to convert JString(not an int) into int
            |""".stripMargin.trim))
    }

    "Extract should succeed for optional field with null value" in {
      val obj = parse("""{"opt":null}""".stripMargin)
      Extraction.extract[OptionOfInt](obj) must_== OptionOfInt(None)
    }

    "Extract should succeed for missing optional field" in {
      val obj = parse("""{}""".stripMargin)
      Extraction.extract[OptionOfInt](obj) must_== OptionOfInt(None)
    }

    "Extract should fail when strictOptionParsing is on and extracting from JNull" in {
      implicit val formats = new DefaultFormats {
        override val strictOptionParsing: Boolean = true
      }

      Extraction.extract[OptionOfInt](JNull) must throwA(
        new MappingException("No value set for Option property: opt")
      )
    }

    Fragments.foreach(Seq[JValue](
      JNothing,
      JInt(5),
      JString("---"),
      JObject(Nil),
      JArray(Nil)
    )) { obj =>
      s"Extract should fail when strictOptionParsing is on and extracting from ${obj.toString}" in {
        implicit val formats = new DefaultFormats {
          override val strictOptionParsing: Boolean = true
        }

        Extraction.extract[OptionOfInt](obj) must throwA(
          new MappingException("No usable value for opt\nNo value set for Option property: opt")
        )
      }
    }
  }
}
