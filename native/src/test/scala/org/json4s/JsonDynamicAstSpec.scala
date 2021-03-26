package org.json4s

import org.scalatest.wordspec.AnyWordSpec

class JsonDynamicAstSpec extends AnyWordSpec {

  import org.json4s.native.JsonMethods._
  import org.json4s.DynamicJValue._

  val tree = parse("""{"foo":{"bar": 3}}""")

  "Dynamic access should allow transveral of objects" in {
    val jsonTree = dyn(tree)
    assert(jsonTree.foo.bar == dyn(JInt(3)))
  }

  "Facilitate comparisons between DynamicJValues" in {
    val jsonTree = dyn(tree)
    assert(jsonTree == dyn(parse("""{"foo":{"bar": 3}}""")))
  }

  "Hashcode equality should work for JValues and Dynamics" in {
    val jsonTree = dyn(tree)
    assert(JInt(3).hashCode == jsonTree.foo.bar.hashCode)
  }

  "DynamicJValue should render correctly" in {
    val jsonTree = dyn(tree)
    assert(compact(render(jsonTree)) == compact(render(tree)))
  }

  "Perform MonadicJValue operations" in {
    val jsonTree = dyn(tree)
    assert({ jsonTree \\ "foo" } == { tree \\ "foo" })
  }

}
