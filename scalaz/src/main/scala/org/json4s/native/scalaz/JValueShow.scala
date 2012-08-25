package org.json4s
package native
package scalaz

import _root_.scalaz.Show

object JValueShow {

  import org.json4s.native.JsonMethods._

  implicit def JValueShow[A <: JValue]: Show[A] = new Show[A] {
    def show(json: A) = compact(render(json)).toList
  }

}
