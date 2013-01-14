package org.json4s

import org.specs2.mutable.Specification

class JsonDynamicAstSpec extends Specification {
	
	import org.json4s.native.JsonMethods._
	
	"Dynamic access should allow transveral of objects" in {
		val jsonTree = parse("""{"foo":{"bar": 3}}""")
		jsonTree.foo.bar must_== JInt(3)
	}

}