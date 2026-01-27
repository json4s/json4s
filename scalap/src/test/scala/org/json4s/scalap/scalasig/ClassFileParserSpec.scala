package org.json4s.scalap.scalasig

import java.io.File
import java.util.jar.JarFile
import org.scalatest.wordspec.AnyWordSpec
import scala.collection.JavaConverters.*

class ClassFileParserSpec extends AnyWordSpec {
  "ClassFileParser" should {
    "parse ConstantPackage and ConstantModule" in {
      val clazz = classOf[javax.xml.bind.JAXB]
      val bytes = getModuleInfo(clazz)
      val parsed = ClassFileParser.parse(ByteCode(bytes))
      val size = parsed.header.constants.size
      assert(size == 32)
      val constants = (1 to size).map { index =>
        parsed.header.constants(index)
      }
      assert(constants.contains("ConstantModule: java.xml.bind"))
      assert(constants.contains("ConstantPackage: javax/xml/bind"))
    }
  }

  private def getModuleInfo(c: Class[?]): Array[Byte] = {
    val jarFile = new JarFile(new File(c.getProtectionDomain.getCodeSource.getLocation.getFile))
    val classes = jarFile.entries.asScala
    classes
      .collectFirst {
        case s if s.toString == "module-info.class" =>
          jarFile.getInputStream(s).readAllBytes()
      }
      .getOrElse(sys.error("not found"))
  }
}
