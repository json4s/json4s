package org.json4s
package native
package scalaz

import _root_.scalaz.Show

object JValueShow {

  implicit def JValueShow[A <: JValue]: Show[A] = new Show[A] {
    def show(json: A) = NativeJsonMethods.compact(NativeJsonMethods.render(json)).toList
  }

}
