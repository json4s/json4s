package org.json4s
package native

import _root_.scalaz.Show

package object scalaz {

  implicit def JValueShow[A <: JValue]: Show[A] = new Show[A] {
    def show(json: A) = JsonMethods.compact(JsonMethods.render(json)).toList
  }

}
