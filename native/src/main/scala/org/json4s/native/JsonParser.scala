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

import org.json4s.ParserUtil.{Buffer, parseDouble, ParseException}

/** JSON parser.
 */
object JsonParser {
  import java.io._



  /** Parsed tokens from low level pull parser.
   */
  sealed abstract class Token
  case object OpenObj extends Token
  case object CloseObj extends Token
  case class FieldStart(name: String) extends Token
  case object End extends Token
  case class StringVal(value: String) extends Token
  case class IntVal(value: BigInt) extends Token
  case class LongVal(value: Long) extends Token
  case class DoubleVal(value: Double) extends Token
  case class BigDecimalVal(value: BigDecimal) extends Token
  case class BoolVal(value: Boolean) extends Token
  case object NullVal extends Token
  case object OpenArr extends Token
  case object CloseArr extends Token

  def parse(in: JsonInput, useBigDecimalForDouble: Boolean): JValue = {
    parse(in, useBigDecimalForDouble, useBigIntForLong = true)
  }

  def parse(in: JsonInput, useBigDecimalForDouble: Boolean, useBigIntForLong: Boolean): JValue = {
    in match {
      case StringInput(s) => parse(s, useBigDecimalForDouble, useBigIntForLong)
      case ReaderInput(rdr) => parse(rdr, useBigDecimalForDouble, useBigIntForLong)
      case StreamInput(stream) => parse(new InputStreamReader(stream, "UTF-8"), useBigDecimalForDouble, useBigIntForLong)
      case FileInput(file) => parse(new FileReader(file), useBigDecimalForDouble, useBigIntForLong)
    }
  }

  /** Return parsed JSON.
   * @throws ParseException is thrown if parsing fails
   */
  def parse(s: String): JValue = parse(s, useBigDecimalForDouble = false, useBigIntForLong = true)
  /** Return parsed JSON.
   * @throws ParseException is thrown if parsing fails
   */
  def parse(s: String, useBigDecimalForDouble: Boolean): JValue = parse(s, useBigDecimalForDouble = useBigDecimalForDouble, useBigIntForLong = true)
  /** Return parsed JSON.
   * @throws ParseException is thrown if parsing fails
   */
  def parse(s: String, useBigDecimalForDouble: Boolean, useBigIntForLong: Boolean): JValue =
    parse(new Buffer(new StringReader(s), false), useBigDecimal = useBigDecimalForDouble, useBigInt = useBigIntForLong)

  /** Return parsed JSON.
   * @param closeAutomatically true (default) if the Reader is automatically closed on EOF
   * @param useBigDecimalForDouble true if double values need to be parsed as BigDecimal
   * @param useBigIntForLong true if long values need to be parsed as BigInt
   * @throws ParseException is thrown if parsing fails
   */
  def parse(s: Reader, closeAutomatically: Boolean = true, useBigDecimalForDouble: Boolean = false, useBigIntForLong: Boolean = true): JValue =
    parse(new Buffer(s, closeAutomatically), useBigDecimal = useBigDecimalForDouble, useBigInt = useBigIntForLong)

  /** Return parsed JSON.
   */
  def parseOpt(s: String): Option[JValue] = parseOpt(s, useBigDecimalForDouble = false)
  /** Return parsed JSON.
   */
  def parseOpt(s: String, useBigDecimalForDouble: Boolean): Option[JValue] =
    try { parse(s, useBigDecimalForDouble).toOption } catch { case e: Exception => None }

  /** Return parsed JSON.
   * @param closeAutomatically true (default) if the Reader is automatically closed on EOF
   */
  def parseOpt(s: Reader, closeAutomatically: Boolean = true, useBigDecimalForDouble: Boolean = false): Option[JValue] =
    try { parse(s, closeAutomatically, useBigDecimalForDouble).toOption } catch { case e: Exception => None }

  /** Parse in pull parsing style.
   * Use <code>p.nextToken</code> to parse tokens one by one from a string.
   * @see org.json4s.JsonParser.Token
   */
  def parse[A](s: String, p: Parser => A): A = parse(s, p, useBigDecimalForDouble = false)
  /** Parse in pull parsing style.
   * Use <code>p.nextToken</code> to parse tokens one by one from a string.
   * @see org.json4s.JsonParser.Token
   */
  def parse[A](s: String, p: Parser => A, useBigDecimalForDouble: Boolean): A =
    parse(new StringReader(s), p, useBigDecimalForDouble)

  /** Parse in pull parsing style.
   * Use <code>p.nextToken</code> to parse tokens one by one from a stream.
   * The Reader must be closed when parsing is stopped.
   * @see org.json4s.JsonParser.Token
   */
  def parse[A](s: Reader, p: Parser => A): A = parse(s, p, useBigDecimalForDouble = false)
  /** Parse in pull parsing style.
   * Use <code>p.nextToken</code> to parse tokens one by one from a stream.
   * The Reader must be closed when parsing is stopped.
   * @see org.json4s.JsonParser.Token
   */
  def parse[A](s: Reader, p: Parser => A, useBigDecimalForDouble: Boolean): A =
    parse(s, p, useBigDecimalForDouble, useBigIntForLong = true)
  /** Parse in pull parsing style.
   * Use <code>p.nextToken</code> to parse tokens one by one from a stream.
   * The Reader must be closed when parsing is stopped.
   * @see org.json4s.JsonParser.Token
   */
  def parse[A](s: Reader, p: Parser => A, useBigDecimalForDouble: Boolean, useBigIntForLong: Boolean): A =
    p(new Parser(new ParserUtil.Buffer(s, false), useBigDecimalForDouble, useBigIntForLong = useBigIntForLong))

  private def parse(buf: ParserUtil.Buffer, useBigDecimal: Boolean, useBigInt: Boolean): JValue = {
    try {
      astParser(new Parser(buf, useBigDecimal, useBigInt), useBigDecimal, useBigInt)
    } catch {
      case e: ParseException => throw e
      case e: Exception => throw new ParseException("parsing failed", e)
    } finally { buf.release }
  }

  
  



  private val astParser = (p: Parser, useBigDecimal: Boolean, useBigIntForLong: Boolean) => {
    val vals = new ValStack(p)
    var token: Token = null
    var root: Option[JValue] = None

    // This is a slightly faster way to correct order of fields and arrays than using 'map'.
    def reverse(v: JValue): JValue = v match {
      case JObject(l) => JObject((l.map { case (n, v) => (n, reverse(v)) }).reverse)
      case JArray(l) => JArray(l.map(reverse).reverse)
      case x => x
    }

    def closeBlock(v: Any): Unit = {
      @inline def toJValue(x: Any) = x match {
        case json: JValue => json
        case scala.util.control.NonFatal(_) => p.fail(s"unexpected field $x")
      }

      vals.peekOption match {
        case Some((name: String, value)) =>
          vals.pop(classOf[JField])
          val obj = vals.peek(classOf[JObject])
          vals.replace(JObject((name, toJValue(v)) :: obj.obj))
        case Some(o: JObject) =>
          vals.replace(JObject(vals.peek(classOf[JField]) :: o.obj))
        case Some(a: JArray) => vals.replace(JArray(toJValue(v) :: a.arr))
        case Some(x) => p.fail(s"expected field, array or object but got $x")
        case None => root = Some(reverse(toJValue(v)))
      }
    }

    def newValue(v: JValue): Unit = {
      vals.peekAny match {
        case (name: String, value) =>
          vals.pop(classOf[JField])
          val obj = vals.peek(classOf[JObject])
          vals.replace(JObject((name, v) :: obj.obj))
        case a: JArray => vals.replace(JArray(v :: a.arr))
        case _ => p.fail("expected field or array")
      }
    }

    do {
      token = p.nextToken
      token match {
        case OpenObj          => vals.push(JObject(Nil))
        case FieldStart(name) => vals.push(JField(name, null))
        case StringVal(x)     => newValue(JString(x))
        case IntVal(x)        => newValue(JInt(x))
        case LongVal(x)       => newValue(JLong(x))
        case DoubleVal(x)     => newValue(JDouble(x))
        case BigDecimalVal(x) => newValue(JDecimal(x))
        case BoolVal(x)       => newValue(JBool(x))
        case NullVal          => newValue(JNull)
        case CloseObj         => closeBlock(vals.popAny)
        case OpenArr          => vals.push(JArray(Nil))
        case CloseArr         => closeBlock(vals.pop(classOf[JArray]))
        case End              =>
      }
    } while (token != End)

    root getOrElse JNothing
  }

  private val EOF = (-1).asInstanceOf[Char]

  private class ValStack(parser: Parser) {
    import java.util.LinkedList
    private[this] val stack = new LinkedList[Any]()

    def popAny = stack.poll
    def pop[A](expectedType: Class[A]) = convert(stack.poll, expectedType)
    def push(v: Any) = stack.addFirst(v)
    def peekAny = stack.peek
    def peek[A](expectedType: Class[A]) = convert(stack.peek, expectedType)
    def replace[A](newTop: Any) = stack.set(0, newTop)

    private def convert[A](x: Any, expectedType: Class[A]): A = {
      if (x == null) parser.fail("expected object or array")
      try { x.asInstanceOf[A] } catch { case _: ClassCastException => parser.fail(s"unexpected $x") }
    }

    def peekOption = if (stack isEmpty) None else Some(stack.peek)
  }

  class Parser(buf: Buffer, useBigDecimalForDouble: Boolean, useBigIntForLong: Boolean) {
    import java.util.LinkedList

    private[this] val blocks = new LinkedList[BlockMode]()
    private[this] var fieldNameMode = true

    def fail(msg: String) = throw new ParseException(s"$msg\nNear: ${buf.near}", null)

    /** Parse next Token from stream.
     */
    def nextToken: Token = {
      def parseString: String = 
        try {
          ParserUtil.unquote(buf)
        } catch {
          case p: ParseException => throw p
          case scala.util.control.NonFatal(_) => fail("unexpected string end")
        }

      def parseValue(first: Char) = {
        var wasInt = true
        var doubleVal = false
        val s = new StringBuilder
        s.append(first)
        while (wasInt) {
          val c = buf.next
          if (c == '.' || c == 'e' || c == 'E') {
            doubleVal = true
            s.append(c)
          } else if (!(Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '-' || c == '+')) {
            wasInt = false
            buf.back
          } else s.append(c)
        }
        val value = s.toString
        if (doubleVal) {
          if (useBigDecimalForDouble) { BigDecimalVal(BigDecimal(value)) } else { DoubleVal(parseDouble(value)) }
        } else {
          if (useBigIntForLong) IntVal(BigInt(value)) else LongVal(value.toLong)
        }
      }

      while (true) {
        (buf.next: @annotation.switch) match {
          case '{' =>
            blocks.addFirst(OBJECT)
            fieldNameMode = true
            return OpenObj
          case '}' =>
            blocks.poll
            return CloseObj
          case '"' =>
            if (fieldNameMode && blocks.peek == OBJECT) return FieldStart(parseString)
            else {
              fieldNameMode = true
              return StringVal(parseString)
            }
          case 't' =>
            fieldNameMode = true
            if (buf.next == 'r' && buf.next == 'u' && buf.next == 'e') {
              return BoolVal(true)
            }
            fail("expected boolean")
          case 'f' =>
            fieldNameMode = true
            if (buf.next == 'a' && buf.next == 'l' && buf.next == 's' && buf.next == 'e') {
              return BoolVal(false)
            }
            fail("expected boolean")
          case 'n' =>
            fieldNameMode = true
            if (buf.next == 'u' && buf.next == 'l' && buf.next == 'l') {
              return NullVal
            }
            fail("expected null")
          case ':' =>
            if (blocks.peek == ARRAY) fail("Colon in an invalid position")
            fieldNameMode = false
          case '[' =>
            blocks.addFirst(ARRAY)
            return OpenArr
          case ']' =>
            fieldNameMode = true
            blocks.poll
            return CloseArr
          case ' ' | '\n' | ',' | '\r' | '\t' =>
          case c =>
            if(EOF == c){
              buf.automaticClose
              return End
            }else if(Character.isDigit(c) || c == '-' || c == '+'){
              fieldNameMode = true
              return parseValue(c)
            }else{
              fail(s"unknown token $c")
            }
        }
      }
      buf.automaticClose
      End
    }

    sealed abstract class BlockMode
    case object ARRAY extends BlockMode
    case object OBJECT extends BlockMode
  }

}
