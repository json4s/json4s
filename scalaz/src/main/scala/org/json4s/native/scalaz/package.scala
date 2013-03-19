package org.json4s
package native

import _root_.scalaz.Show
import _root_.scalaz.Scalaz.shows

package object scalaz {

  implicit def JValueShow[A <: JValue]: Show[A] = shows(renderJValue _ andThen compactJson _)

}
