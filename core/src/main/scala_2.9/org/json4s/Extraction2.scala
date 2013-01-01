package org.json4s

import org.scalastuff.scalabeans.{BeanDescriptor, Preamble}
import org.json4s.Meta.Reflection
import scala.collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap
import org.scalastuff.scalabeans.types.ScalaType
import collection.mutable.{ArrayBuffer, ListBuffer}
import java.util

object Extraction2 {

  private class Memo[A, R] {
    private val cache = new ConcurrentHashMap[A, R]().asScala
    def memoize(x: A, f: A => R): R = cache.getOrElseUpdate(x, f(x))
  }


  private val instrospectors = new Memo[ScalaType, BeanDescriptor]
  private val scalaTypes = new Memo[Class[_], ScalaType]

  /** Extract a case class from JSON.
   * @see org.json4s.JsonAST.JValue#extract
   * @throws MappingException is thrown if extraction fails
   */
  def extract[A](json: JValue)(implicit formats: Formats, mf: Manifest[A]): A = {
    try {
      extract0(json, Preamble.descriptorOf(mf.erasure)).asInstanceOf[A]
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
  def decompose(a: Any)(implicit formats: Formats): JValue = {
    def prependTypeHint(clazz: Class[_], o: JObject) =
      JObject(JField(formats.typeHintFieldName, JString(formats.typeHints.hintFor(clazz))) :: o.obj)

    def mkObject(clazz: Class[_], fields: List[JField]) =
      if (formats.typeHints.containsHint(clazz)) prependTypeHint(clazz, JObject(fields)) else JObject(fields)

    val serializer = formats.typeHints.serialize
    val any = a.asInstanceOf[AnyRef]

    if (formats.customSerializer(formats).isDefinedAt(a)) {
      formats.customSerializer(formats)(a)
    } else if (!serializer.isDefinedAt(a)) {
      val k = any.getClass
      if (any == null) {
        JNull
      } else if (Reflection.isPrimitive(any.getClass)) {
        Reflection.primitive2jvalue(any)(formats)
      } else if (classOf[Map[_, _]].isAssignableFrom(k)) {
        JObject((any.asInstanceOf[Map[_, _]] map {
          case (k: String, v) => JField(k, decompose(v))
          case (k: Symbol, v) => JField(k.name, decompose(v))
        }).toList)
      } else if (classOf[Collection[_]].isAssignableFrom(k)) {
        JArray(any.asInstanceOf[Collection[_]].toList map decompose)
      } else if (k.isArray) {
        JArray(any.asInstanceOf[Array[_]].toList map decompose)
      } else if (classOf[Option[_]].isAssignableFrom(k)) {
        any.asInstanceOf[Option[_]].flatMap[JValue] { y => Some(decompose(y)) }.getOrElse(JNothing)
      } else {
        val klass = scalaTypes.memoize(k, Preamble.scalaTypeOf(_))
        val descriptor = instrospectors.memoize(klass, Preamble.descriptorOf(_))
        val iter = descriptor.properties.iterator
        val fields = new util.LinkedList[JField]().asScala
        if (formats.typeHints.containsHint(k))
          fields += JField(formats.typeHintFieldName, JString(formats.typeHints.hintFor(k)))
        while(iter.hasNext) {
          val prop = iter.next()
          val fieldVal = prop.get[AnyRef](any)
          val n = Meta.unmangleName(prop.name)
          val fs = formats.fieldSerializer(klass.erasure)
          (if (fs.isDefined) {
            val ff = (fs.get.serializer orElse Map((n, fieldVal) -> Some((n, fieldVal))))((n, fieldVal))
            ff map {
              case (nn, vv) => JField(nn, decompose(vv))
            } getOrElse JField(n, JNothing)
          } else fields += JField(n, decompose(fieldVal)))
        }
        JObject(fields.toList)
      }

//      any match {
//        case x: Collection[_] => JArray(x.toList map decompose)
//        case x if (x.getClass.isArray) => JArray(x.asInstanceOf[Array[_]].toList map decompose)
//        case x: Option[_] => x.flatMap[JValue] { y => Some(decompose(y)) }.getOrElse(JNothing)
//        case x =>
//          val klass = scalaTypes.memoize(x.getClass, Preamble.scalaTypeOf(_))
//          val descriptor = instrospectors.memoize(klass, Preamble.descriptorOf(_))
//          val iter = descriptor.properties.iterator
//          val fields = new util.LinkedList[JField]().asScala
//          if (formats.typeHints.containsHint(x.getClass))
//            fields += JField(formats.typeHintFieldName, JString(formats.typeHints.hintFor(x.getClass)))
//          while(iter.hasNext) {
//            val prop = iter.next()
//            val fieldVal = prop.get[AnyRef](x)
//            val n = Meta.unmangleName(prop.name)
//            val fs = formats.fieldSerializer(klass.erasure)
//            (if (fs.isDefined) {
//              val ff = (fs.get.serializer orElse Map((n, fieldVal) -> Some((n, fieldVal))))((n, fieldVal))
//              ff map {
//                case (nn, vv) => JField(nn, decompose(vv))
//              } getOrElse JField(n, JNothing)
//            } else fields += JField(n, decompose(fieldVal)))
//          }
//          JObject(fields.toList)
//      }
    } else prependTypeHint(any.getClass, serializer(any))
  }

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
      val descriptor = Preamble.descriptorOf(target.clazz)
      null
    } catch {
      case e: MappingException => throw e
      case e: Exception => throw new MappingException("unknown error", e)
    }

  }
    //extract0(json, mappingOf(target.clazz))

  private[this] def extract0(json: JValue, descriptor: BeanDescriptor)(implicit formats: Formats): Any = {

  }


}