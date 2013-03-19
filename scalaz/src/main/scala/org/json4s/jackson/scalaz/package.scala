package org.json4s
package jackson

import _root_.scalaz.Show
import _root_.scalaz.Scalaz.shows

package object scalaz {
  implicit def JValueShow[A <: JValue]: Show[A] = shows(compactJson(_))
}

