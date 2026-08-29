package org.json4s.scalap.scalasig

import org.scalatest.wordspec.AnyWordSpec

class ClassFileParserConstantDynamicSpec extends AnyWordSpec {
  private val DynamicConstant = ("Dynamic: bootstrapMethodAttrIndex = (\\d+), NameAndType: " +
    "StringBytesPair\\((\\w+),\\[B@\\p{XDigit}+\\), " +
    "StringBytesPair\\(([\\w/$]+);,\\[B@\\p{XDigit}+\\)").r

  "ClassFileParser" should {
    "parse CONSTANT_Dynamic" in {
      val clazz = classOf[ConstantDynamicExample]
      val classAsPath = clazz.getName.replace('.', '/') + ".class"
      val bytes = clazz.getClassLoader.getResourceAsStream(classAsPath).readAllBytes()
      val parsed = ClassFileParser.parse(ByteCode(bytes))
      val size = parsed.header.constants.size

      val actual = (1 to size)
        .map { index =>
          parsed.header.constants(index)
        }
        .collect { case s: String => s }
        .collect { case DynamicConstant(n, methodName, className) =>
          (n, methodName, className)
        }
        .toSet
      val expect = Set(
        ("1", "invoke", "Ljava/lang/Enum$EnumDesc"),
        ("2", "invoke", "Ljava/lang/constant/ClassDesc")
      )
      assert(actual == expect)
    }
  }
}
