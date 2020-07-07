package org.json4s.scalap.scalasig

import org.specs2.mutable.Specification
import java.io._
import java.util.jar.JarFile
import scala.collection.JavaConverters._

class ClassFileParserSpec extends Specification {
  "ClassFileParser" should {
    "parse ConstantPackage and ConstantModule" in {
      if (scala.util.Properties.isJavaAtLeast("9")) {
        val clazz = classOf[javax.xml.bind.JAXB]
        val bytes = getModuleInfo(clazz)
        val parsed = ClassFileParser.parse(ByteCode(bytes))
        val size = parsed.header.constants.size
        size must_== 32
        val constants = (1 to size).map{ index =>
          parsed.header.constants(index)
        }
        constants must contain("ConstantModule: java.xml.bind")
        constants must contain("ConstantPackage: javax/xml/bind")
      } else {
        ok
      }
    }
  }

  private def getModuleInfo(c: Class[_]): Array[Byte] = {
    val jarFile = new JarFile(new File(c.getProtectionDomain.getCodeSource.getLocation.getFile))
    val classes = jarFile.entries.asScala
    classes.collectFirst {
      case s if s.toString == "module-info.class" =>
        getBytes(jarFile.getInputStream(s))
    }.getOrElse(sys.error("not found"))
  }

  private def getBytes(in: InputStream): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val buffer = new Array[Byte](1024)
    @annotation.tailrec
    def read(): Unit = {
      val byteCount = in.read(buffer)
      if (byteCount >= 0) {
        out.write(buffer, 0, byteCount)
        read()
      }
    }
    read()
    out.toByteArray()
  }
}
