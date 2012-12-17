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
package scalaz


import _root_.scalaz._
import Scalaz._

trait Lifting { this: Types =>
  implicit def Func2ToJSON[A: JSONR, B: JSONR, R](z: (A, B) => R) = new {
    def applyJSON(a: JValue => Result[A], b: JValue => Result[B]): JValue => Result[R] = 
      (json: JValue) => Apply[Result].apply2(a(json),b(json))(z)
  }

  implicit def Func3ToJSON[A: JSONR, B: JSONR, C: JSONR, R](z: (A, B, C) => R) = new {
    def applyJSON(a: JValue => Result[A], b: JValue => Result[B], c: JValue => Result[C]): JValue => Result[R] = 
      (json: JValue) => Apply[Result].apply3(a(json),b(json),c(json))(z)
  }

  implicit def Func4ToJSON[A: JSONR, B: JSONR, C: JSONR, D: JSONR, R](z: (A, B, C, D) => R) = new {
    def applyJSON(a: JValue => Result[A], b: JValue => Result[B], c: JValue => Result[C], d: JValue => Result[D]): JValue => Result[R] = 
      (json: JValue) => Apply[Result].apply4(a(json),b(json),c(json),d(json))(z)
  }

  implicit def Func5ToJSON[A: JSONR, B: JSONR, C: JSONR, D: JSONR, E: JSONR, R](z: (A, B, C, D, E) => R) = new {
    def applyJSON(a: JValue => Result[A], b: JValue => Result[B], c: JValue => Result[C], d: JValue => Result[D], e: JValue => Result[E]): JValue => Result[R] = 
      (json: JValue) => Apply[Result].apply5(a(json),b(json),c(json),d(json),e(json))(z)
  }

  implicit def Func6ToJSON[A: JSONR, B: JSONR, C: JSONR, D: JSONR, E: JSONR, F: JSONR, R](z: (A, B, C, D, E, F) => R) = new {
    def applyJSON(a: JValue => Result[A], b: JValue => Result[B], c: JValue => Result[C], d: JValue => Result[D], e: JValue => Result[E], f: JValue => Result[F]): JValue => Result[R] = 
      (json: JValue) => Apply[Result].apply6(a(json),b(json),c(json),d(json),e(json),f(json))(z)
  }

  implicit def Func7ToJSON[A: JSONR, B: JSONR, C: JSONR, D: JSONR, E: JSONR, F: JSONR, G: JSONR, R](z: (A, B, C, D, E, F, G) => R) = new {
    def applyJSON(a: JValue => Result[A], b: JValue => Result[B], c: JValue => Result[C], d: JValue => Result[D], e: JValue => Result[E], f: JValue => Result[F], g: JValue => Result[G]): JValue => Result[R] = 
      (json: JValue) => Apply[Result].apply7(a(json),b(json),c(json),d(json),e(json),f(json),g(json))(z)
  }

  implicit def Func8ToJSON[A: JSONR, B: JSONR, C: JSONR, D: JSONR, E: JSONR, F: JSONR, G: JSONR, H: JSONR, R](z: (A, B, C, D, E, F, G, H) => R) = new {
    def applyJSON(a: JValue => Result[A], b: JValue => Result[B], c: JValue => Result[C], d: JValue => Result[D], e: JValue => Result[E], f: JValue => Result[F], g: JValue => Result[G], h: JValue => Result[H]): JValue => Result[R] = 
      (json: JValue) => Apply[Result].apply8(a(json),b(json),c(json),d(json),e(json),f(json),g(json),h(json))(z)
  }
}
