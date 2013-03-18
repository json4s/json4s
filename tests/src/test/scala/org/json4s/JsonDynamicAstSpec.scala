package org.json4s

import org.specs2.mutable.Specification

class JsonDynamicAstSpec extends Specification {
  
  import org.json4s.native.JsonMethods._
  import org.json4s.DynamicJValue._
  
  val tree = parse("""{"foo":{"bar": 3}}""")
  
  "Dynamic access should allow transveral of objects" in {
    val jsonTree = dyn(tree)
    jsonTree.foo.bar must_== dyn(JInt(3))
  }
  
  "Facilitate comparisons between DynamicJValues" in {
    val jsonTree = dyn(tree)
    jsonTree must_== dyn(parse("""{"foo":{"bar": 3}}"""))
  }

  "Hashcode equality should work for JValues and Dynamics" in {
    val jsonTree = dyn(tree)
    JInt(3).hashCode must_== jsonTree.foo.bar.hashCode
  }

  "DynamicJValue should render correctly" in {
    val jsonTree = dyn(tree)
    compact(render(jsonTree)) must_== compact(render(tree))
  }

  "Perform MonadicJValue operations" in {
    val jsonTree = dyn(tree)
    jsonTree \\ "foo" must_== tree \\ "foo"
  }

}