package org.json4s

object TestUtils {
  def readFile(file: String): String = {
    val path = "native-core/shared/src/test/resources" + file
    scala.io.Source.fromFile(path, "utf8").getLines().mkString("\n")
  }
}
