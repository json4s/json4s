package org.json4s

/**
 * Created by pankhuri on 8/18/16.
 */
import org.scalatest.wordspec.AnyWordSpec

class JSetExamples extends AnyWordSpec {

  "Intersection of JSet" in {
    val set1 = JSet(Set(JString("g"), JBool(true)))
    val set2 = JSet(Set(JBool(true)))
    assert((set1 intersect set2) == set2)
  }

  "Union of JSet" in {
    val set1 = JSet(Set(JString("g"), JBool(true)))
    val set2 = JSet(Set(JBool(true)))
    assert((set1 union set2) == set1)
  }

  "Difference of JSet" in {
    val set1 = JSet(Set(JString("g"), JBool(true)))
    val set2 = JSet(Set(JBool(true)))
    assert((set1 difference set2) == JSet(Set(JString("g"))))
  }

  "Diff of JSet" in {
    val set1 = JSet(Set(JString("g")))
    val set2 = JSet(Set(JBool(true)))
    assert((set1 diff set2) == Diff(JNothing, set2, set1))
  }

}
