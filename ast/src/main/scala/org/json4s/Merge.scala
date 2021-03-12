/*
 * Copyright 2009-2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json4s

import JsonAST._

import scala.annotation.tailrec

/**
 * Use fundep encoding to improve return type of merge function
 *  (see: http://www.chuusai.com/2011/07/16/fundeps-in-scala/)
 *
 *  JObject merge JObject = JObject
 *  JArray  merge JArray  = JArray
 *  _       merge _       = JValue
 */
private[json4s] trait MergeDep[A <: JValue, B <: JValue, R <: JValue] {
  def apply(val1: A, val2: B): R
}

private[json4s] trait LowPriorityMergeDep {
  implicit def jjj[A <: JValue, B <: JValue]: MergeDep[A, B, JValue] = new MergeDep[A, B, JValue] {
    def apply(val1: A, val2: B): JValue = merge(val1, val2)

    private def merge(val1: JValue, val2: JValue): JValue = (val1, val2) match {
      case (JObject(xs), JObject(ys)) => JObject(Merge.mergeFields(xs, ys))
      case (JArray(xs), JArray(ys)) => JArray(Merge.mergeVals(xs, ys))
      case (JNothing, x) => x
      case (x, JNothing) => x
      case (_, y) => y
    }
  }
}

private[json4s] trait MergeDeps extends LowPriorityMergeDep {
  implicit object ooo extends MergeDep[JObject, JObject, JObject] {
    def apply(val1: JObject, val2: JObject): JObject = JObject(Merge.mergeFields(val1.obj, val2.obj))
  }

  implicit object aaa extends MergeDep[JArray, JArray, JArray] {
    def apply(val1: JArray, val2: JArray): JArray = JArray(Merge.mergeVals(val1.arr, val2.arr))
  }
}

/**
 * Function to merge two JSONs.
 */
object Merge {

  /**
   * Return merged JSON.
   * <p>
   * Example:<pre>
   * val m = ("name", "joe") ~ ("age", 10) merge ("name", "joe") ~ ("iq", 105)
   * m: JObject(List((name,JString(joe)), (age,JInt(10)), (iq,JInt(105))))
   * </pre>
   */
  def merge[A <: JValue, B <: JValue, R <: JValue](val1: A, val2: B)(implicit instance: MergeDep[A, B, R]): R =
    instance(val1, val2)

  private[json4s] def mergeFields(vs1: List[JField], vs2: List[JField]): List[JField] = {

    @tailrec
    def mergeRec(acc: List[JField], xleft: List[JField], yleft: List[JField]): List[JField] = xleft match {
      case Nil => acc ++ yleft
      case (xn, xv) :: xs =>
        yleft find (_._1 == xn) match {
          case Some(y @ (yn @ _, yv)) =>
            mergeRec(acc ++ List(JField(xn, merge(xv, yv))), xs, yleft filterNot (_ == y))
          case None => mergeRec(acc ++ List(JField(xn, xv)), xs, yleft)
        }
    }

    mergeRec(List(), vs1, vs2)
  }

  private[json4s] def mergeVals(vs1: List[JValue], vs2: List[JValue]): List[JValue] = {
    @tailrec
    def mergeRec(acc: List[JValue], xleft: List[JValue], yleft: List[JValue]): List[JValue] = xleft match {
      case Nil => acc ++ yleft
      case x :: xs =>
        yleft find (_ == x) match {
          case Some(y) => mergeRec(acc ++ List(merge(x, y)), xs, yleft filterNot (_ == y))
          case None => mergeRec(acc ++ List(x), xs, yleft)
        }
    }

    mergeRec(List(), vs1, vs2)
  }

  private[json4s] trait Mergeable extends MergeDeps {
    implicit def j2m[A <: JValue](json: A): MergeSyntax[A] = new MergeSyntax(json)

    class MergeSyntax[A <: JValue](json: A) {

      /**
       * Return merged JSON.
       * @see org.json4s.Merge#merge
       */
      def merge[B <: JValue, R <: JValue](other: B)(implicit instance: MergeDep[A, B, R]): R =
        Merge.merge(json, other)(instance)
    }
  }
}
