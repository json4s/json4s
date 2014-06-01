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
package native

import scala.reflect.Manifest

/** Functions to serialize and deserialize a case class.
 * Custom serializer can be inserted if a class is not a case class.
 * <p>
 * Example:<pre>
 * val hints = new ShortTypeHints( ... )
 * implicit val formats = Serialization.formats(hints)
 * </pre>
 *
 * @see org.json4s.TypeHints
 */
object Serialization extends Serialization  {
  import java.io.{Reader, StringWriter, Writer}
  /** Serialize to String.
   */
  def write[A <: AnyRef](a: A)(implicit formats: Formats): String = {
    (write(a, new StringWriter)(formats)).toString
  }

  /** Serialize to Writer.
   */
  def write[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    Extraction.decomposeWithBuilder(a, JsonWriter.streaming(out))(formats)
  }

  /** Serialize to String (pretty format).
   */
  def writePretty[A <: AnyRef](a: A)(implicit formats: Formats): String =
    (writePretty(a, new StringWriter)(formats)).toString

  /** Serialize to Writer (pretty format).
   */
  def writePretty[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    Extraction.decomposeWithBuilder(a, JsonWriter.streamingPretty(out))(formats)
  }

  /** Serialize to String (pretty format).
   */
  def writePrettyOld[A <: AnyRef](a: A)(implicit formats: Formats): String =
    (writePrettyOld(a, new StringWriter)(formats)).toString

  /** Serialize to Writer (pretty format).
   */
  def writePrettyOld[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
    Printer.pretty(JsonMethods.render(Extraction.decompose(a)(formats)), out)
  }

  /** Deserialize from a String.
   */
  def read[A](json: String)(implicit formats: Formats, mf: Manifest[A]): A = {
    JsonParser.parse(json, formats.wantsBigDecimal).extract(formats, mf)
  }

  @deprecated("You can use formats now to indicate you want to use decimals instead of doubles", "3.2.0")
  def read[A](json: String, useBigDecimalForDouble: Boolean)(implicit formats: Formats, mf: Manifest[A]): A =
    if (useBigDecimalForDouble) read(json)(formats.withBigDecimal, mf) else read(json)(formats.withDouble, mf)

  /** Deserialize from a Reader.
   */
  @deprecated("You can use formats now to indicate you want to use decimals instead of doubles", "3.2.0")
  def read[A](in: Reader, useBigDecimalForDouble: Boolean)(implicit formats: Formats, mf: Manifest[A]): A =
    if (useBigDecimalForDouble) read(in)(formats.withBigDecimal, mf) else read(in)(formats.withDouble, mf)

  /** Deserialize from a Reader.
   */
  def read[A](in: Reader)(implicit formats: Formats, mf: Manifest[A]): A = {
    JsonParser.parse(in, formats.wantsBigDecimal).extract(formats, mf)
  }

}
