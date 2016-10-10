package org.json4s

/**
  * Created by pankhuri on 8/18/16.
  */
import org.specs2.mutable._
import JsonAST.JSet


class JSetExamples extends Specification {

  "Intersection of JSet" in {
    val set1 = JSet(Set(JString("g"), JBool(true)))
    val set2 = JSet(Set(JBool(true)))
    set1 intersect(set2) must_== set2
  }

  "Union of JSet" in {
    val set1 = JSet(Set(JString("g"), JBool(true)))
    val set2 = JSet(Set(JBool(true)))
    set1 union(set2) must_== set1
  }

  "Difference of JSet" in {
    val set1 = JSet(Set(JString("g"), JBool(true)))
    val set2 = JSet(Set(JBool(true)))
    set1 difference(set2) must_== JSet(Set(JString("g")))
  }

  "Diff of JSet" in {
    val set1 = JSet(Set(JString("g")))
    val set2 = JSet(Set(JBool(true)))
    set1 diff set2 must_== Diff(JNothing, set2, set1)
  }

}

