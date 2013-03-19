package org.json4s
package native

import _root_.scalaz.Show

package object scalaz {

  implicit def JValueShow[A <: JValue]: Show[A] = Show.shows(renderJValue _ andThen compactJson _)

}
