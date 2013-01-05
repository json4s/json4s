package org.json4s

import scala.collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap
import collection.mutable.{ArrayBuffer, ListBuffer}
import java.util
import util.Date
import collection.mutable

object Extraction2 {
  private class Memo[A, R] {
    private val cache = new ConcurrentHashMap[A, R]().asScala
    def memoize(x: A, f: A => R): R = cache.getOrElseUpdate(x, f(x))
  }


  /** Extract a case class from JSON.
   * @see org.json4s.JsonAST.JValue#extract
   * @throws MappingException is thrown if extraction fails
   */
  def extract[A](json: JValue)(implicit formats: Formats, mf: Manifest[A]): A = {
    try {
//      extract0(json, Preamble.descriptorOf(mf.erasure)).asInstanceOf[A]
      null
    } catch {
      case e: MappingException => throw e
      case e: Exception => throw new MappingException("unknown error", e)
    }
    null.asInstanceOf[A]
  }

  /** Extract a case class from JSON.
   * @see org.json4s.JsonAST.JValue#extract
   */
  def extractOpt[A](json: JValue)(implicit formats: Formats, mf: Manifest[A]): Option[A] =
    try { Some(extract(json)(formats, mf)) } catch { case _: MappingException => None }

  /** Decompose a case class into JSON.
   * <p>
   * Example:<pre>
   * case class Person(name: String, age: Int)
   * implicit val formats = org.json4s.DefaultFormats
   * Extraction.decompose(Person("joe", 25)) == JObject(JField("age",JInt(25)) :: JField("name",JString("joe")) :: Nil)
   * </pre>
   */
  def decomposeWithBuilder[T](a: Any, builder: Builder[T])(implicit formats: Formats): T = {
    val current = builder
    def prependTypeHint(clazz: Class[_], o: JObject) =
      JObject(JField(formats.typeHintFieldName, JString(formats.typeHints.hintFor(clazz))) :: o.obj)

    def addField(name: String, v: Any, obj: Builder[T]) = {
      val f = obj.startField(name)
      decomposeWithBuilder(v, f)
    }

    val serializer = formats.typeHints.serialize
    val any = a.asInstanceOf[AnyRef]

    if (formats.customSerializer(formats).isDefinedAt(a)) {
      formats.customSerializer(formats)(a)
    } else if (!serializer.isDefinedAt(a)) {
      val k = if (any != null) any.getClass else null

      // A series of if branches because of performance reasons
      if (any == null) {
        JNull
      } else if (Reflect.isPrimitive(any.getClass)) {
        primitive2jvalue(any, current)(formats)
      } else if (classOf[Map[_, _]].isAssignableFrom(k)) {
        val obj = current.startObject()
        val iter = any.asInstanceOf[Map[_, _]].iterator
        while(iter.hasNext) {
          iter.next() match {
            case (k: String, v) => addField(k, v, obj)
            case (k: Symbol, v) => addField(k.name, v, obj)
          }
        }
        obj.endObject()
      } else if (classOf[Collection[_]].isAssignableFrom(k)) {
        val arr = current.startArray()
        val iter = any.asInstanceOf[Collection[_]].iterator
        while(iter.hasNext) { decomposeWithBuilder(iter.next(), arr) }
      } else if (k.isArray) {
        val arr = current.startArray()
        val iter = any.asInstanceOf[Array[_]].iterator
        while(iter.hasNext) { decomposeWithBuilder(iter.next(), arr) }
        arr.endArray()
      } else if (classOf[Option[_]].isAssignableFrom(k)) {
        val v = any.asInstanceOf[Option[_]]
        if (v.isDefined) {
          decomposeWithBuilder(v.get, current)
        }
      } else {
        val klass = Reflect.scalaTypeOf(k)
        val descriptor = Reflect.describe(klass).asInstanceOf[Reflect.ClassDescriptor]
        val iter = descriptor.properties.iterator
        val obj = current.startObject()
        if (formats.typeHints.containsHint(k)) {
          val f = obj.startField(formats.typeHintFieldName)
          f.string(formats.typeHints.hintFor(k))
        }
        while(iter.hasNext) {
          val prop = iter.next()
          val fieldVal = prop.get(any)
          val n = prop.name
          val fs = formats.fieldSerializer(prop.returnType.erasure)
          if (fs.isDefined) {
            val ff = (fs.get.serializer orElse Map((n, fieldVal) -> Some((n, fieldVal))))((n, fieldVal))
            if (ff.isDefined) {
              val Some((nn, vv)) = ff
              addField(nn, vv, obj)
            }
          } else addField(n, fieldVal, obj)
        }
        obj.endObject()
      }
    } else prependTypeHint(any.getClass, serializer(any))
    current.result
  }

  def decompose(a: Any)(implicit formats: Formats): JValue = decomposeWithBuilder(a, new JDoubleAstRootBuilder)

  /** Flattens the JSON to a key/value map.
   */
  def flatten(json: JValue): Map[String, String] = {
    def escapePath(str: String) = str

    def flatten0(path: String, json: JValue): Map[String, String] = {
      json match {
        case JNothing | JNull    => Map()
        case JString(s)          => Map(path -> ("\"" + JsonAST.quote(s) + "\""))
        case JDouble(num)        => Map(path -> num.toString)
        case JDecimal(num)       => Map(path -> num.toString)
        case JInt(num)           => Map(path -> num.toString)
        case JBool(value)        => Map(path -> value.toString)
        case JField(name, value) => flatten0(path + escapePath(name), value)
        case JObject(obj)        => obj.foldLeft(Map[String, String]()) { case (map, (name, value)) =>
          map ++ flatten0(path + "." + escapePath(name), value)
        }
        case JArray(arr)         => arr.length match {
          case 0 => Map(path -> "[]")
          case _ => arr.foldLeft((Map[String, String](), 0)) {
                      (tuple, value) => (tuple._1 ++ flatten0(path + "[" + tuple._2 + "]", value), tuple._2 + 1)
                    }._1
        }
      }
    }

    flatten0("", json)
  }


  /** Unflattens a key/value map to a JSON object.
   */
  def unflatten(map: Map[String, String], useBigDecimalForDouble: Boolean = false): JValue = {

    def extractValue(value: String): JValue = value.toLowerCase match {
      case ""      => JNothing
      case "null"  => JNull
      case "true"  => JBool(true)
      case "false" => JBool(false)
      case "[]"    => JArray(Nil)
      case x @ _   =>
        if (value.charAt(0).isDigit) {
          if (value.indexOf('.') == -1) JInt(BigInt(value))
          else {
            if (!useBigDecimalForDouble) JDouble(ParserUtil.parseDouble(value))
            else JDecimal(BigDecimal(value))
          }
        }
        else JString(ParserUtil.unquote(value.substring(1)))
    }

    def submap(prefix: String): Map[String, String] =
      Map(
        map.filter(t => t._1.startsWith(prefix)).map(
          t => (t._1.substring(prefix.length), t._2)
        ).toList.toArray: _*
      )

    val ArrayProp = """^(\.([^\.\[]+))\[(\d+)\].*$""".r
    val ArrayElem = """^(\[(\d+)\]).*$""".r
    val OtherProp = """^(\.([^\.\[]+)).*$""".r

    val uniquePaths = {
      map.keys.foldLeft[Set[String]](Set()) {
        (set, key) =>
          key match {
            case ArrayProp(p, f, i) => set + p
            case OtherProp(p, f)    => set + p
            case ArrayElem(p, i)    => set + p
            case x @ _              => set + x
          }
      }.toList.sortWith(_ < _)
    } // Sort is necessary to get array order right

    uniquePaths.foldLeft[JValue](JNothing) { (jvalue, key) =>
      jvalue.merge(key match {
        case ArrayProp(p, f, i) => JObject(List(JField(f, unflatten(submap(key)))))
        case ArrayElem(p, i)    => JArray(List(unflatten(submap(key))))
        case OtherProp(p, f)    => JObject(List(JField(f, unflatten(submap(key)))))
        case ""                 => extractValue(map(key))
      })
    }
  }

  def extract(json: JValue, target: TypeInfo)(implicit formats: Formats): Any = {
    try {
      val descriptor = Reflect.describe(target.clazz)
      null
    } catch {
      case e: MappingException => throw e
      case e: Exception => throw new MappingException("unknown error", e)
    }

  }
    //extract0(json, mappingOf(target.clazz))


  def primitive2jvalue(a: Any, builder: Builder[_])(implicit formats: Formats) = a match {
    case x: String => builder.string(x)
    case x: Int => builder.int(x)
    case x: Long => builder.long(x)
    case x: Double => builder.double(x)
    case x: Float => builder.float(x)
    case x: Byte => builder.byte(x)
    case x: BigInt => builder.bigInt(x)
    case x: BigDecimal => builder.bigDecimal(x)
    case x: Boolean => builder.boolean(x)
    case x: Short => builder.short(x)
    case x: java.lang.Integer => builder.int(x)
    case x: java.lang.Long => builder.long(x)
    case x: java.lang.Double => builder.double(x)
    case x: java.lang.Float => builder.float(x)
    case x: java.lang.Byte => builder.byte(x)
    case x: java.lang.Boolean => builder.boolean(x)
    case x: java.lang.Short => builder.short(x)
    case x: Date => builder.string(formats.dateFormat.format(x))
    case x: Symbol => builder.string(x.name)
    case _ => sys.error("not a primitive " + a.asInstanceOf[AnyRef].getClass)
  }


}