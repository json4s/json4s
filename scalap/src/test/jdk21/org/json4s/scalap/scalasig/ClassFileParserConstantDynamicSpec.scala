package org.json4s.scalap.scalasig

import org.scalatest.wordspec.AnyWordSpec

class ClassFileParserConstantDynamicSpec extends AnyWordSpec {
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
        .collect {
          case s"Dynamic: bootstrapMethodAttrIndex = ${n}, NameAndType: StringBytesPair(invoke,${bytes1}), StringBytesPair(${className};,${bytes2})" =>
            (n, className)
        }
        .toSet
      val expect = Set(
        ("1", "Ljava/lang/Enum$EnumDesc"),
        ("2", "Ljava/lang/constant/ClassDesc")
      )
      assert(actual == expect)
    }
  }
}
