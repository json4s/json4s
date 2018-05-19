package org.json4s

private[json4s] abstract class Compat {
  type CanBuildFrom[-From, -A, +C] = scala.collection.generic.CanBuildFrom[From, A, C]
}
