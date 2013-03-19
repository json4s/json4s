package org.json4s
package jackson

import _root_.scalaz.Show

package object scalaz {
  implicit def JValueShow[A <: JValue]: Show[A] = Show.shows(compactJson _)
}

