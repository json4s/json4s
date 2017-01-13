package org.json4s

import java.util.Locale.ENGLISH

object StringTransformations {
  //snake
  private[json4s] def underscore(word: String): String = {
    val firstPattern = "([A-Z]+)([A-Z][a-z])".r
    val secondPattern = "([a-z\\d])([A-Z])".r
    val replacementPattern = "$1_$2"
      secondPattern.replaceAllIn(
        firstPattern.replaceAllIn(
          word, replacementPattern), replacementPattern).toLowerCase
  }

  private[json4s] def camelize(word: String): String = {
    if (word.nonEmpty) {
      val w = pascalize(word)
      w.substring(0, 1).toLowerCase(ENGLISH) + w.substring(1)
    } else {
      word
    }
  }

  private[json4s] def pascalize(word: String): String = {
    val lst = if(word.startsWith("_")) {
      word.split("_").toList.tail
    } else {
      word.split("_").toList
    }
    (lst.headOption.map(s ⇒ s.substring(0, 1).toUpperCase(ENGLISH) + s.substring(1)).get ::
      lst.tail.map(s ⇒ s.substring(0, 1).toUpperCase + s.substring(1))).mkString("")
  }
}
