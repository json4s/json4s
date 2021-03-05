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



  type JValue   = JsonAST.JValue
  val  JNothing = JsonAST.JNothing
  val  JNull    = JsonAST.JNull
  type JString  = JsonAST.JString
  val  JString  = JsonAST.JString
  type JDouble  = JsonAST.JDouble
  val  JDouble  = JsonAST.JDouble
  type JDecimal = JsonAST.JDecimal
  val  JDecimal = JsonAST.JDecimal
  type JLong    = JsonAST.JLong
  val  JLong    = JsonAST.JLong
  type JInt     = JsonAST.JInt
  val  JInt     = JsonAST.JInt
  type JBool    = JsonAST.JBool
  val  JBool    = JsonAST.JBool
  type JField   = JsonAST.JField
  val  JField   = JsonAST.JField
  type JObject  = JsonAST.JObject
  val  JObject  = JsonAST.JObject
  type JArray   = JsonAST.JArray
  val  JArray   = JsonAST.JArray
  type JSet     = JsonAST.JSet
  val  JSet     = JsonAST.JSet

  val  TypeInfo = reflect.TypeInfo
  type TypeInfo = reflect.TypeInfo

  trait ParameterNameReader extends reflect.ParameterNameReader

  implicit def string2JsonInput(s: String): JsonInput = StringInput(s)
  implicit def reader2JsonInput(rdr: java.io.Reader): JsonInput = ReaderInput(rdr)
  implicit def stream2JsonInput(stream: java.io.InputStream): JsonInput = StreamInput(stream)
  implicit def file2JsonInput(file: java.io.File): JsonInput = FileInput(file)
  implicit def jvalue2extractable(jv: JValue) = new ExtractableJsonAstNode(jv)
  implicit def jvalue2monadic(jv: JValue) = new MonadicJValue(jv)
  implicit def jsonwritable[T: Writer](a: T) = new ToJsonWritable[T](a)

}
