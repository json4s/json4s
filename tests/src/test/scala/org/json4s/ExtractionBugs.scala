package org.json4s

import org.scalatest.wordspec.AnyWordSpec
import reflect.{ClassDescriptor, PrimaryConstructor, Reflector}
import org.json4s.native.Document
import java.util
import java.math.{BigDecimal => JavaBigDecimal, BigInteger => JavaBigInteger}

class NativeExtractionBugs extends ExtractionBugs[Document]("Native") with native.JsonMethods
class JacksonExtractionBugs extends ExtractionBugs[JValue]("Jackson") with jackson.JsonMethods

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

  case class ManyConstructorsWithPrimary @PrimaryConstructor() (
    id: Long,
    name: String,
    lastName: String,
    email: String
  ) {
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

  case class AJavaDouble(num: Double)

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

    class ContentWithOptionClass(val path: Option[String], val age: Option[Long], val content: Content)
      extends ContentWithOption

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

  /**
   * Custom serializer for MapImplementation
   *  This is used to show that custom (strange) serialisations were once broken.
   */
  class MapImplementationSerializer
    extends CustomSerializer[MapImplementation](formats =>
      (
        { case MapImplementationSerializer.strangeSerialization => new MapImplementation() },
        { case _: MapImplementation => MapImplementationSerializer.strangeSerialization }
      )
    )

  object MapImplementationSerializer {
    val strangeSerialization = Extraction.decompose(MapImplementation.content)(DefaultFormats)
  }
}
abstract class ExtractionBugs[T](mod: String) extends AnyWordSpec with JsonMethods[T] {

  import ExtractionBugs._
  implicit val formats: Formats =
    DefaultFormats.withCompanions(classOf[PingPongGame.SharedObj] -> PingPongGame) + new MapImplementationSerializer()

  "Primitive type should not hang" in {
    assertThrows[MappingException] {
      val a = WithPrimitiveAlias(Vector(1.0, 2.0, 3.0, 4.0))
      Extraction.decompose(a)
    }
  }

  (mod + " Extraction bugs Specification") should {
    "ClassCastException (BigInt) regression 2 must pass" in {
      val opt = OptionOfInt(Some(39))
      assert(Extraction.decompose(opt).extract[OptionOfInt].opt.get == 39)
    }

    "Extraction should not fail when Maps values are Lists" in {
      val m = PMap(Map("a" -> List("b"), "c" -> List("d")))
      assert(Extraction.decompose(m).extract[PMap] == m)
    }

    "Extraction should not fail when class is defined in a trait" in {
      val inst = PingPongGame.SharedObj("jeff", visible = true)
      val extr = Extraction.decompose(inst)
      assert(extr == JObject("name" -> JString("jeff"), "visible" -> JBool(true)))
      assert(extr.extract[PingPongGame.SharedObj] == inst)
    }

    "Extraction should always choose constructor with the most arguments if more than one constructor exists" in {
      val args = Reflector.describe[ManyConstructors].asInstanceOf[ClassDescriptor].mostComprehensive
      assert(args.size == 4)
    }

    "Extraction should always choose primary constructor if exists" in {
      val args = Reflector.describe[ManyConstructorsWithPrimary].asInstanceOf[ClassDescriptor].mostComprehensive
      assert(args.size == 4)
      assert(args.map(_.name) == Seq("id", "name", "lastName", "email"))
    }

    "Extraction should throw exception if two or more constructors marked as primary" in {
      assertThrows[IllegalArgumentException] {
        Reflector.describe[ManyPrimaryConstructors].asInstanceOf[ClassDescriptor].mostComprehensive
      }
    }

    "Extraction should handle AnyRef" in {
      implicit val formats: Formats = DefaultFormats.withHints(FullTypeHints(classOf[ExtractWithAnyRef] :: Nil))
      val json = JObject(JField("jsonClass", JString(classOf[ExtractWithAnyRef].getName)) :: Nil)
      val extracted = Extraction.extract[AnyRef](json)
      assert(extracted == ExtractWithAnyRef())
    }

    "Extraction should work with unicode encoded field names (issue 1075)" in {
      assert(parse("""{"foo.bar,baz":"x"}""").extract[UnicodeFieldNames] == UnicodeFieldNames("x"))
    }

    "Extraction should not fail if case class has a companion object" in {
      assert(parse("""{"nums":[10]}""").extract[HasCompanion] == HasCompanion(List(10)))
    }

    "Extraction should not fail if using companion objects apply method" in {
      val content: Content = parse("""{"id":"some-id", "name":"some-name"}""").extract[Content]
      assert(content.getClass == classOf[Content.ContentClass])
      assert(content.id == "some-id")
      assert(content.name == "some-name")
    }

    "Extraction should not fail if using companion objects apply method to extract nested json field" in {
      val json = """{"path":"some-path", "isFoo":false, "content": {"id":"some-id", "name":"some-name"}}"""
      val someOtherContent: SomeOtherContent = parse(json).extract[SomeOtherContent]
      assert(someOtherContent.getClass == classOf[SomeOtherContent.SomeOtherContentClass])
      val content = someOtherContent.content
      assert(someOtherContent.isFoo == false)
      assert(someOtherContent.path == "some-path")
      assert(content.id == "some-id")
      assert(content.name == "some-name")
    }

    "Extraction should not fail if using companion objects apply method with fields with options of primitive types" in {
      val json = """{"path":"some-path", "age": 5, "content": {"id":"some-id", "name":"some-name"}}"""
      val contentWithOption: ContentWithOption = parse(json).extract[ContentWithOption]
      assert(contentWithOption.getClass == classOf[ContentWithOption.ContentWithOptionClass])
      val content = contentWithOption.content
      assert(contentWithOption.path == Some("some-path"))
      assert(contentWithOption.age == Some(5))
      assert(content.id == "some-id")
      assert(content.name == "some-name")
    }

    "Extraction should not fail if using companion objects apply method with fields with options of primitive types (option field missing)" in {
      val json = """{"path":"some-path", "content": {"id":"some-id", "name":"some-name"}}"""
      val contentWithOption: ContentWithOption = parse(json).extract[ContentWithOption]
      assert(contentWithOption.getClass == classOf[ContentWithOption.ContentWithOptionClass])
      val content = contentWithOption.content
      assert(contentWithOption.path == Some("some-path"))
      assert(contentWithOption.age == None)
      assert(content.id == "some-id")
      assert(content.name == "some-name")
    }

    "Issue 1169" in {
      val json = parse("""{"data":[{"one":1, "two":2}]}""")
      assert(json.extract[Response] == Response(List(Map("one" -> 1, "two" -> 2))))
    }

    "Extraction should extract a java.util.ArrayList as array. empty" in {
      assert(Extraction.decompose(new util.ArrayList[String]()) == JArray(Nil))
    }

    "Extraction should extract a java.util.ArrayList as array. non empty" in {
      val json = parse("""["one", "two"]""")
      val lst = new util.ArrayList[String]()
      lst.add("one")
      lst.add("two")
      assert(json.extract[util.ArrayList[String]] == lst)
    }

    "Extraction should be able to call companion object apply method even when c'tors exists" in {
      val json = parse("""{"v": "Foo"}""")
      val expected = CaseClassWithCompanion("Foo")
      assert(json.extract[CaseClassWithCompanion] == expected)
    }

    "Parse 0 as JavaBigDecimal" in {
      val bjd = AJavaBigDecimal(BigDecimal("0").bigDecimal)
      assert(parse("""{"num": 0}""", useBigDecimalForDouble = true).extract[AJavaBigDecimal] == bjd)
      assert(parse("""{"num": 0}""").extract[AJavaBigDecimal] == bjd)
    }

    "Extract a JavaBigDecimal from a decimal value" in {
      val jbd = AJavaBigDecimal(BigDecimal("12.305").bigDecimal)
      assert(parse("""{"num": 12.305}""", useBigDecimalForDouble = true).extract[AJavaBigDecimal] == jbd)
      assert(parse("""{"num": 12.305}""").extract[AJavaBigDecimal] == jbd)
    }

    "Parse 0 as java BigInteger" in {
      val bji = AJavaBigInteger(BigInt("0").bigInteger)
      assert(parse("""{"num": 0}""").extract[AJavaBigInteger] == bji)
    }

    "does not hang when parsing big integers" in {
      assertThrows[Exception] {
        parse(s"""{"num": ${"9" * 10000000}}""", useBigIntForLong = false)
      }
    }

    "Extract a java BigInteger from a long value" in {
      val bji = AJavaBigInteger(BigInt(Long.MaxValue).bigInteger)
      assert(parse(s"""{"num": ${Long.MaxValue}}""").extract[AJavaBigInteger] == bji)
    }

    "Parse 0 as BigDecimal" in {
      val bd = ABigDecimal(BigDecimal("0"))
      assert(parse("""{"num": 0}""", useBigDecimalForDouble = true).extract[ABigDecimal] == bd)
    }

    "Extract a bigdecimal from a decimal value" in {
      val bd = ABigDecimal(BigDecimal("12.305"))
      assert(parse("""{"num": 12.305}""", useBigDecimalForDouble = true).extract[ABigDecimal] == bd)
    }

    "Decompose a class with a super-type type member" in {
      val obj = ClassWithSuperTypeMember("foo")

      val result = Extraction.decompose(obj)

      assert(result == JObject("a" -> JString("foo")))
    }

    "An implementation of Map should serialize with a CustomSerializer" in {
      assert(Extraction.decompose(new MapImplementation()) == MapImplementationSerializer.strangeSerialization)
    }

    "An implementation of Map should deserialize with a CustomSerializer" in {
      assert(
        Extraction.extract[MapImplementation](
          MapImplementationSerializer.strangeSerialization
        ) == new MapImplementation()
      )
    }

    "Apply can't be mostComprehensive" in {
      val obj = CompanionSample("hello", 1, 2)
      val json = Extraction.decompose(obj)
      assert(json == JObject("s" -> JString("hello"), "i" -> JInt(3)))
    }

    "Extract error should preserve error message when strict option parsing is enabled" in {
      implicit val formats: Formats = new DefaultFormats {
        override val strictOptionParsing: Boolean = true
      }

      val obj = parse("""{"opt": "not an int"}""".stripMargin)

      try {
        Extraction.extract[OptionOfInt](obj)
        fail()
      } catch {
        case e: MappingException =>
          assert(e.getMessage == """
            |No usable value for opt
            |Do not know how to convert JString(not an int) into int
            |""".stripMargin.trim)
      }
    }

    "Extract should succeed for optional field with null value" in {
      val obj = parse("""{"opt":null}""".stripMargin)
      assert(Extraction.extract[OptionOfInt](obj) == OptionOfInt(None))
    }

    "Extract should succeed for missing optional field" in {
      val obj = parse("""{}""".stripMargin)
      assert(Extraction.extract[OptionOfInt](obj) == OptionOfInt(None))
    }

    "Extract should succeed for missing optional field when strictOptionParsing is on" in {
      implicit val formats: Formats = new DefaultFormats {
        override val strictOptionParsing: Boolean = true
      }
      val obj = parse("""{}""".stripMargin)
      assert(Extraction.extract[OptionOfInt](obj) == OptionOfInt(None))
    }

    "Extract should fail when strictOptionParsing is on and extracting from JNull" in {
      implicit val formats: Formats = new DefaultFormats {
        override val strictOptionParsing: Boolean = true
      }

      try {
        Extraction.extract[OptionOfInt](JNull)
        fail()
      } catch {
        case e: MappingException =>
          assert(e.getMessage == "No value set for Option property: opt")
      }
    }

    Seq[JValue](
      JNothing,
      JInt(5),
      JString("---"),
      JArray(Nil)
    ).foreach { obj =>
      s"Extract should fail when strictOptionParsing is on and extracting from ${obj.toString}" in {
        implicit val formats: Formats = new DefaultFormats {
          override val strictOptionParsing: Boolean = true
        }

        try {
          Extraction.extract[OptionOfInt](obj)
          fail()
        } catch {
          case e: MappingException =>
            assert(e.getMessage == "No usable value for opt\nNo value set for Option property: opt")
        }
      }
    }
  }
}
