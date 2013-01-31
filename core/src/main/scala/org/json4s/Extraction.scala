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


import java.lang.reflect.{Constructor => JConstructor, Type}
import java.lang.{Integer => JavaInteger, Long => JavaLong, Short => JavaShort, Byte => JavaByte, Boolean => JavaBoolean, Double => JavaDouble, Float => JavaFloat}
import java.math.{BigDecimal => JavaBigDecimal}
import java.util.Date
import java.util.{ Map => JMap, Collection => JCollection }
import java.sql.Timestamp
import reflect.Reflector
import scala.reflect.Manifest
import java.nio.CharBuffer
import scalashim._
import java.util.concurrent.ConcurrentHashMap
import collection.JavaConverters._

/** Function to extract values from JSON AST using case classes.
 *
 *  See: ExtractionExamples.scala
 */
object Extraction {
  import Meta._
  import Meta.Reflection._

  /** Extract a case class from JSON.
   * @see org.json4s.JsonAST.JValue#extract
   * @throws MappingException is thrown if extraction fails
   */
  def extract[A](json: JValue)(implicit formats: Formats, mf: Manifest[A]): A = {
    def allTypes(mf: Manifest[_]): List[Class[_]] = mf.erasure :: (mf.typeArguments flatMap allTypes)
    try {
      val types = allTypes(mf)
//      println("the types: %s" format types)
      extract0(json, types.head, types.tail).asInstanceOf[A]
    } catch {
      case e: MappingException => throw e
      case e: Exception => throw new MappingException("unknown error", e)
    }
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
  def decomposeWithBuilder[T](a: Any, builder: JsonWriter[T])(implicit formats: Formats): T = {
//    println("decomposing: " + (if (a == null) "null" else a))
    val current = builder
    def prependTypeHint(clazz: Class[_], o: JObject) =
      JObject(JField(formats.typeHintFieldName, JString(formats.typeHints.hintFor(clazz))) :: o.obj)

    def addField(name: String, v: Any, obj: JsonWriter[T]) = {
      val f = obj.startField(name)
      decomposeWithBuilder(v, f)
    }

    val serializer = formats.typeHints.serialize
    val any = a.asInstanceOf[AnyRef]

    if (formats.customSerializer(formats).isDefinedAt(a)) {
      current addJValue formats.customSerializer(formats)(a)
    } else if (!serializer.isDefinedAt(a)) {

      val k = if (any != null) any.getClass else null

      // A series of if branches because of performance reasons
      if (any == null) {
        current.addJValue(JNull)
      } else if (classOf[JValue].isAssignableFrom(k)) {
        current.addJValue(any.asInstanceOf[JValue])
      } else if (Reflector.isPrimitive(any.getClass)) {
        writePrimitive(any, current)(formats)
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
      } else if (classOf[Iterable[_]].isAssignableFrom(k)) {
        val arr = current.startArray()
        val iter = any.asInstanceOf[Iterable[_]].iterator
        while(iter.hasNext) { decomposeWithBuilder(iter.next(), arr) }
        arr.endArray()
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
        val klass = Reflector.scalaTypeOf(k)
        val descriptor = Reflector.describe(klass).asInstanceOf[reflect.ClassDescriptor]
        val ctorParams = descriptor.mostComprehensive.map(_.name)
        val iter = descriptor.properties.iterator
        val obj = current.startObject()
        if (formats.typeHints.containsHint(k)) {
          val f = obj.startField(formats.typeHintFieldName)
          f.string(formats.typeHints.hintFor(k))
        }
        val fs = formats.fieldSerializer(k)
        while(iter.hasNext) {
          val prop = iter.next()

          val fieldVal = prop.get(any)
          val n = prop.name
          if (fs.isDefined) {
            val ff = (fs.get.serializer orElse Map((n, fieldVal) -> Some((n, fieldVal))))((n, fieldVal))
            if (ff.isDefined) {
              val Some((nn, vv)) = ff
              addField(nn, vv, obj)
            }
          } else if (ctorParams contains prop.name) addField(n, fieldVal, obj)
        }
        obj.endObject()
      }
    } else current addJValue prependTypeHint(any.getClass, serializer(any))
    current.result
  }

  /** Decompose a case class into JSON.
   * <p>
   * Example:<pre>
   * case class Person(name: String, age: Int)
   * implicit val formats = org.json4s.DefaultFormats
   * Extraction.decompose(Person("joe", 25)) == JObject(JField("age",JInt(25)) :: JField("name",JString("joe")) :: Nil)
   * </pre>
   */
  def decompose(a: Any)(implicit formats: Formats): JValue =
    decomposeWithBuilder(a, if (formats.wantsBigDecimal) JsonWriter.bigDecimalAst else JsonWriter.ast)

  private[this] def writePrimitive(a: Any, builder: JsonWriter[_])(implicit formats: Formats) = a match {
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

  /** Flattens the JSON to a key/value map.
   */
  def flatten(json: JValue): Map[String, String] = {
    def escapePath(str: String) = str

    def flatten0(path: String, json: JValue): Map[String, String] = {
      json match {
        case JNothing | JNull    => Map()
        case JString(s)          => Map(path -> ("\""+JsonAST.quote(s)+"\""))
        case JDouble(num)        => Map(path -> num.toString)
        case JDecimal(num)       => Map(path -> num.toString)
        case JInt(num)           => Map(path -> num.toString)
        case JBool(value)        => Map(path -> value.toString)
//        case JField(name, value) => flatten0(path + escapePath(name), value)
        case JObject(obj)        => obj.foldLeft(Map[String, String]()) { case (map, (name, value)) =>
          map ++ flatten0(s"$path.${escapePath(name)}", value)
        }
        case JArray(arr)         => arr.length match {
          case 0 => Map(path -> "[]")
          case _ => arr.foldLeft((Map[String, String](), 0)) {
                      (tuple, value) => (tuple._1 ++ flatten0(s"$path[${tuple._2}]", value), tuple._2 + 1)
                    }._1
        }
      }
    }

    flatten0("", json)
  }


  /** Unflattens a key/value map to a JSON object.
   */
  def unflatten(map: Map[String, String], useBigDecimalForDouble: Boolean = false): JValue = {
    import scala.util.matching.Regex

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

    val ArrayProp = new Regex("""^(\.([^\.\[]+))\[(\d+)\].*$""")
    val ArrayElem = new Regex("""^(\[(\d+)\]).*$""")
    val OtherProp = new Regex("""^(\.([^\.\[]+)).*$""")

    val uniquePaths = map.keys.foldLeft[Set[String]](Set()) {
      (set, key) =>
        key match {
          case ArrayProp(p, f, i) => set + p
          case OtherProp(p, f)    => set + p
          case ArrayElem(p, i)    => set + p
          case x @ _              => set + x
        }
    }.toList.sortWith(_ < _) // Sort is necessary to get array order right

    uniquePaths.foldLeft[JValue](JNothing) { (jvalue, key) =>
      jvalue.merge(key match {
        case ArrayProp(p, f, i) => JObject(List(JField(f, unflatten(submap(key)))))
        case ArrayElem(p, i)    => JArray(List(unflatten(submap(key))))
        case OtherProp(p, f)    => JObject(List(JField(f, unflatten(submap(key)))))
        case ""                 => extractValue(map(key))
      })
    }
  }

  private def extract0(json: JValue, clazz: Class[_], typeArgs: Seq[Class[_]])
                      (implicit formats: Formats): Any = {
    def mkMapping(clazz: Class[_], typeArgs: Seq[Class[_]])(implicit formats: Formats): Meta.Mapping = {
      if (clazz == classOf[List[_]] || clazz == classOf[Set[_]] || clazz.isArray)
        Col(TypeInfo(clazz, None), mkMapping(typeArgs.head, typeArgs.tail))
      else if (clazz == classOf[Map[_, _]])
        Dict(mkMapping(typeArgs.tail.head, typeArgs.tail.tail))
      else mappingOf(clazz, typeArgs)
    }
    if (clazz == classOf[Option[_]]) {
      val mapping = mkMapping(typeArgs.head, typeArgs.tail)
//      println("The initial mapping for the option, initial extract: %s" format mapping)
      json.toOption.map(extract0(_, mapping))
    } else {
      val mapping = mkMapping(clazz, typeArgs)
//      println("The initial mapping for initial extract: %s" format mapping)
      extract0(json, mapping)
    }
  }

  def extract(json: JValue, target: TypeInfo)(implicit formats: Formats): Any =
    extract0(json, mappingOf(target.clazz))

  private def extract0(json: JValue, mapping: Mapping)(implicit formats: Formats): Any = {
    def newInstance(constructor: Constructor, json: JValue) = {
      def findBestConstructor = {
        if (constructor.choices.size == 1) constructor.choices.head // optimized common case
        else {
          val argNames = json match {
            case JObject(fs) => fs.map(_._1)
            case x => Nil
          }
          constructor.bestMatching(argNames)
            .getOrElse(fail(s"No constructor for type ${constructor.targetType.clazz}, $json"))
        }
      }

      def setFields(a: AnyRef, json: JValue, constructor: JConstructor[_]) = json match {
        case o: JObject =>
          formats.fieldSerializer(a.getClass).map { serializer =>
            val constructorArgNames =
              Reflection.constructorArgs(a.getClass, constructor, formats.parameterNameReader, None).map(_._1).toSet
            val jsonFields = o.obj.map { f =>
              val JField(n, v) = (serializer.deserializer orElse Map(f -> f))(f)
              (n, (n, v))
            }.toMap

            val fieldsToSet =
              Reflection.fields(a.getClass).filterNot(f => constructorArgNames.contains(f._1))

            fieldsToSet.foreach { case (name, typeInfo) =>
              jsonFields.get(name).foreach { case (n, v) =>
                val typeArgs = typeInfo.parameterizedType
                  .map(_.getActualTypeArguments.map(_.asInstanceOf[Class[_]]).toList.zipWithIndex
                    .map { case (t, idx) =>
                      if (t == classOf[java.lang.Object]) ScalaSigReader.readField(name, a.getClass, idx)
                      else t
                    })
                val value = extract0(v, typeInfo.clazz, typeArgs.getOrElse(Nil))
                Reflection.setField(a, n, value)
              }
            }
          }
          a
        case _ => a
      }

      def instantiate = {
        val c = findBestConstructor
        val jconstructor = c.constructor

        val args = c.args.map(a => build(json \ a.path, a))
        try {
          if (jconstructor.getDeclaringClass == classOf[java.lang.Object])
            fail("No information known about type")

          val instance = jconstructor.newInstance(args.map(_.asInstanceOf[AnyRef]).toArray: _*)
          setFields(instance.asInstanceOf[AnyRef], json, jconstructor)
        } catch {
          case e @ (_:IllegalArgumentException | _:InstantiationException) =>
            def arg2str(a: Any) = if (a != null) a.asInstanceOf[AnyRef].getClass.getName else "null"
            fail(s"Parsed JSON values do not match with class constructor\nargs=${args.mkString(",")}" +
                 s"\narg types=${args.map(arg2str).mkString(",")}\nconstructor=$jconstructor")
        }
      }

      def mkWithTypeHint(typeHint: String, fields: List[JField], typeInfo: TypeInfo) = {
        val obj = JObject(fields filterNot (_._1 == formats.typeHintFieldName))
        val deserializer = formats.typeHints.deserialize
        if (!deserializer.isDefinedAt(typeHint, obj)) {
          val concreteClass = formats.typeHints.classFor(typeHint) getOrElse fail(s"Do not know how to deserialize '$typeHint'")
          val typeArgs = typeInfo.parameterizedType
            .map(_.getActualTypeArguments.toList.map(Meta.rawClassOf)).getOrElse(Nil)
          build(obj, mappingOf(concreteClass, typeArgs))
        } else deserializer(typeHint, obj)
      }

      val custom = formats.customDeserializer(formats)
      if (custom.isDefinedAt(constructor.targetType, json)) custom(constructor.targetType, json)
      else json match {
        case JNull => null
        case JObject(TypeHint(t, fs)) => mkWithTypeHint(t, fs, constructor.targetType)
        case _ => instantiate
      }
    }

    object TypeHint {
      def unapply(fs: List[JField]): Option[(String, List[JField])] =
        if (formats.typeHints == NoTypeHints) None
        else {
          val grouped = fs groupBy (_._1 == formats.typeHintFieldName)
          if (grouped.isDefinedAt(true))
            Some((grouped(true).head._2.values.toString, grouped.get(false).getOrElse(Nil)))
          else None
        }
    }

//    def newPrimitive(elementType: Class[_], elem: JValue) = convert(elem, elementType, formats)

    def newCollection(root: JValue, m: Mapping, constructor: Array[_] => Any) = {
      val array: Array[_] = root match {
        case JArray(arr)      => arr.map(build(_, m)).toArray
        case JNothing | JNull => Array[AnyRef]()
        case x                => fail(s"Expected collection but got $x for root $root and mapping $m")
      }

      constructor(array)
    }

    def build(root: JValue, mapping: Mapping): Any = {
//      println("building root: " + root)
//      println("building mapping: " + mapping)
      mapping match {
        case Value(targetType, default) => convert(root, targetType, formats, default)
        case c: Constructor => newInstance(c, root)
        case Cycle(targetType) => build(root, mappingOf(targetType))
        case Arg(path, m, optional, default) => mkValue(root, m, path, optional, default)
        case Col(targetType, m) =>
          val custom = formats.customDeserializer(formats)
          val c = targetType.clazz
          if (custom.isDefinedAt(targetType, root)) custom(targetType, root)
          else if (c == classOf[List[_]]) newCollection(root, m, a => List(a: _*))
          else if (c == classOf[Set[_]]) newCollection(root, m, a => Set(a: _*))
          else if (c.isArray) newCollection(root, m, mkTypedArray(c))
          else if (classOf[Seq[_]].isAssignableFrom(c)) newCollection(root, m, a => List(a: _*))
          else fail("Expected collection but got " + m + " for class " + c)
        case Dict(m) => root match {
          case JObject(xs) => Map(xs.map(x => (x._1, build(x._2, m))): _*)
          case x => fail("Expected object but got " + x)
        }
      }
    }

    def mkTypedArray(c: Class[_])(a: Array[_]) = {
      import java.lang.reflect.Array.{newInstance => newArray}

      a.foldLeft((newArray(c.getComponentType, a.length), 0)) { (tuple, e) => {
        java.lang.reflect.Array.set(tuple._1, tuple._2, e); (tuple._1, tuple._2 + 1)
      }}._1
    }

    def mkList(root: JValue, m: Mapping) = root match {
      case JArray(arr) => arr.map(build(_, m))
      case JNothing | JNull => Nil
      case x => fail(s"Expected array but got $x")
    }

    def mkValue(root: JValue, mapping: Mapping, path: String, optional: Boolean, default: Option[() => Any]) = {
      def defv(v: Any) = if (default.isDefined) default.get() else v
      if (optional && root == JNothing) defv(None)
      else {
        try {
          val x = if (root == JNothing && default.isDefined) default.get() else build(root, mapping)
          if (optional) { if (x == null) defv(None) else Some(x) }
          else if (x == null) defv(x)
          else x
        } catch {
          case e @ MappingException(msg, _) =>
            if (optional) defv(None) else fail(s"No usable value for $path\n$msg", e)
        }
      }
    }

    build(json, mapping)
  }

  private def convert(json: JValue, targetType: Class[_], formats: Formats, default: Option[() => Any]): Any = json match {
    case JInt(x) if (targetType == classOf[Int]) => x.intValue
    case JInt(x) if (targetType == classOf[JavaInteger]) => new JavaInteger(x.intValue)
    case JInt(x) if (targetType == classOf[BigInt]) => x
    case JInt(x) if (targetType == classOf[Long]) => x.longValue
    case JInt(x) if (targetType == classOf[JavaLong]) => new JavaLong(x.longValue)
    case JInt(x) if (targetType == classOf[Double]) => x.doubleValue
    case JInt(x) if (targetType == classOf[JavaDouble]) => new JavaDouble(x.doubleValue)
    case JInt(x) if (targetType == classOf[Float]) => x.floatValue
    case JInt(x) if (targetType == classOf[JavaFloat]) => new JavaFloat(x.floatValue)
    case JInt(x) if (targetType == classOf[Short]) => x.shortValue
    case JInt(x) if (targetType == classOf[JavaShort]) => new JavaShort(x.shortValue)
    case JInt(x) if (targetType == classOf[Byte]) => x.byteValue
    case JInt(x) if (targetType == classOf[JavaByte]) => new JavaByte(x.byteValue)
    case JInt(x) if (targetType == classOf[String]) => x.toString
    case JInt(x) if (targetType == classOf[Number]) => x.longValue
    case JDouble(x) if (targetType == classOf[Double]) => x
    case JDouble(x) if (targetType == classOf[JavaDouble]) => new JavaDouble(x)
    case JDouble(x) if (targetType == classOf[Float]) => x.floatValue
    case JDouble(x) if (targetType == classOf[JavaFloat]) => new JavaFloat(x.floatValue)
    case JDouble(x) if (targetType == classOf[String]) => x.toString
    case JDouble(x) if (targetType == classOf[Int]) => x.intValue
    case JDouble(x) if (targetType == classOf[Long]) => x.longValue
    case JDouble(x) if (targetType == classOf[Number]) => x
    case JDecimal(x) if (targetType == classOf[Double]) => x.doubleValue()
    case JDecimal(x) if (targetType == classOf[JavaDouble]) => new JavaDouble(x.doubleValue())
    case JDecimal(x) if (targetType == classOf[BigDecimal]) => x
    case JDecimal(x) if (targetType == classOf[JavaBigDecimal]) => x.bigDecimal
    case JDecimal(x) if (targetType == classOf[Float]) => x.floatValue
    case JDecimal(x) if (targetType == classOf[JavaFloat]) => new JavaFloat(x.floatValue)
    case JDecimal(x) if (targetType == classOf[String]) => x.toString
    case JDecimal(x) if (targetType == classOf[Int]) => x.intValue
    case JDecimal(x) if (targetType == classOf[Long]) => x.longValue
    case JDecimal(x) if (targetType == classOf[Number]) => x
    case JString(s) if (targetType == classOf[String]) => s
    case JString(s) if (targetType == classOf[Symbol]) => Symbol(s)
    case JString(s) if (targetType == classOf[Date]) => formats.dateFormat.parse(s).getOrElse(fail(s"Invalid date '$s'"))
    case JString(s) if (targetType == classOf[Timestamp]) => new Timestamp(formats.dateFormat.parse(s).getOrElse(fail(s"Invalid date '$s'")).getTime)
    case JBool(x) if (targetType == classOf[Boolean]) => x
    case JBool(x) if (targetType == classOf[JavaBoolean]) => new JavaBoolean(x)
    case j: JValue if (targetType == classOf[JValue]) => j
    case j: JObject if (targetType == classOf[JObject]) => j
    case j: JArray if (targetType == classOf[JArray]) => j
    case JNull => null
    case JNothing =>
      default map (_.apply()) getOrElse fail(s"Did not find value which can be converted into ${targetType.getName}")
    case _ =>
      val custom = formats.customDeserializer(formats)
      val typeInfo = TypeInfo(targetType, None)
      if (custom.isDefinedAt(typeInfo, json)) custom(typeInfo, json)
      else fail(s"Do not know how to convert $json into $targetType")
  }



}