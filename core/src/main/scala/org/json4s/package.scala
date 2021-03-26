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

package org

package object json4s {

  type JField = JsonAST.JField

  val TypeInfo = reflect.TypeInfo
  type TypeInfo = reflect.TypeInfo

  type ParameterNameReader = reflect.ParameterNameReader

  implicit def convertToJsonInput[A: AsJsonInput](input: A): JsonInput = AsJsonInput.asJsonInput[A](input)
  implicit def jvalue2extractable(jv: JValue): ExtractableJsonAstNode = new ExtractableJsonAstNode(jv)
  implicit def jvalue2monadic(jv: JValue): MonadicJValue = new MonadicJValue(jv)
  implicit def jsonwritable[T: Writer](a: T): ToJsonWritable[T] = new ToJsonWritable[T](a)

}
