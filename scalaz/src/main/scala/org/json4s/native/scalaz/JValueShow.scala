package org.json4s
package native
package scalaz

import _root_.scalaz.Show

object JValueShow {

  implicit def JValueShow[A <: JValue]: Show[A] = new Show[A] {
    def show(json: A) = JsonMethods.compact(JsonMethods.render(json)).toList
  }

}
