package com.tt.json4s

import scala.scalajs.js

object TestUtils {
  def readFile(file: String): String = {
    js.Dynamic.global
      .require("fs")
      .readFileSync("native-core/shared/src/test/resources" + file, "utf8")
      .asInstanceOf[String]
  }
}
