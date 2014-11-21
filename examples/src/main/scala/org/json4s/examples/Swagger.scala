package org.json4s
package examples

import java.util.{Date => JDate, Locale}
import ext.{ JodaTimeSerializers, EnumNameSerializer }
import org.joda.time._
import format.ISODateTimeFormat

sealed trait HttpMethod {
  /**
   * Flag as to whether the method is "safe", as defined by RFC 2616.
   */
  val isSafe: Boolean
}
case object Options extends HttpMethod {
  val isSafe = true
  override def toString = "OPTIONS"
}
case object Get extends HttpMethod {
  val isSafe = true
  override def toString = "GET"
}
case object Head extends HttpMethod {
  val isSafe = true
  override def toString = "HEAD"
}
case object Post extends HttpMethod {
  val isSafe = false
  override def toString = "POST"
}
case object Put extends HttpMethod {
  val isSafe = false
  override def toString = "PUT"
}
case object Delete extends HttpMethod {
  val isSafe = false
  override def toString = "DELETE"
}
case object Trace extends HttpMethod {
  val isSafe = true
  override def toString = "TRACE"
}
case object Connect extends HttpMethod {
  val isSafe = false
  override def toString = "CONNECT"
}
case object Patch extends HttpMethod {
  val isSafe = false
  override def toString = "PATCH"
}
case class ExtensionMethod(name: String) extends HttpMethod {
  val isSafe = false
}

object HttpMethod {
  private[this] val methodMap =
    Map(List(Options, Get, Head, Post, Put, Delete, Trace, Connect, Patch) map {
      method => (method.toString, method)
    } : _*)

  /**
   * Maps a String as an HttpMethod.
   *
   * @param name a string representing an HttpMethod
   * @return the matching common HttpMethod, or an instance of `ExtensionMethod`
   * if no method matches
   */
  def apply(name: String): HttpMethod = {
    val canonicalName = name.toUpperCase(Locale.ENGLISH)
    methodMap.getOrElse(canonicalName, ExtensionMethod(canonicalName))
  }

  /**
   * The set of common HTTP methods: GET, HEAD, POST, PUT, DELETE, TRACE,
   * CONNECT, and PATCH.
   */
  val methods: Set[HttpMethod] = methodMap.values.toSet
}

case class ListApi(path: String, description: String)
case class ApiListing(swaggerVersion: String, apiVersion: String, apis: List[ListApi])

case class Api(resourcePath: String,
               listingPath: Option[String],
               description: Option[String],
               apis: List[Endpoint],
               models: Map[String, Model]) {
  def toJValue = Api.toJValue(this)
}

object Api {
  import SwaggerSerializers._

  lazy val Iso8601Date = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)

  implicit val formats = new DefaultFormats {
    override val dateFormat = new DateFormat {
      def format(d: JDate) = new DateTime(d).toString(Iso8601Date)
      def parse(s: String) = try {
        Option(Iso8601Date.parseDateTime(s).toDate)
      } catch {
        case _ ⇒ None
      }
    }
  } ++ Seq(
    new EnumNameSerializer(ParamType),
    new HttpMethodSerializer,
    new ParameterSerializer,
    new AllowableValuesSerializer,
    new ModelFieldSerializer) ++ JodaTimeSerializers.all

  def toJValue(doc: Any) = (Extraction.decompose(doc)(formats).noNulls)


}

object SwaggerSerializers {
  import JsonDSL._


  class HttpMethodSerializer extends CustomSerializer[HttpMethod](formats => ({
    case JString(meth) => HttpMethod(meth)
  }, {
    case x: HttpMethod ⇒ JString(x.toString)
  }))

  private[this] val simpleTypeList: List[String] = List("string", "number", "int", "boolean", "object", "Array", "null", "any")
  private[this] def listType(key: String, name: String, isUnique: Boolean): JValue = {
    val default = (key -> "Array") ~ ("uniqueItems" -> isUnique)
    val arrayType = name.substring(name.indexOf("[") + 1, name.indexOf("]"))
    if (simpleTypeList.contains(arrayType))
      default ~ ("items" -> (("type" -> arrayType): JValue))
    else
      default ~ ("items" -> (("$ref" -> arrayType): JValue))
  }

  private[this] def serializeDataType(key: String, dataType: DataType.DataType) = {
    dataType.name match {
      case n if n.toUpperCase(Locale.ENGLISH).startsWith("LIST[") => listType(key, n, isUnique = false)
      case n if n.toUpperCase(Locale.ENGLISH).startsWith("SET[") => listType(key, n, isUnique = true)
      case n => (key -> n): JValue
    }
  }

  private[this] def deserializeDataType(key: String, jv: JValue)(implicit formats: Formats): DataType.DataType = {
    jv \ key match {
      case JString("Array") =>
        val arrayType = (
          (jv \ "items" \ "type").extractOpt[String] orElse
          (jv \ "items" \ "$ref").extractOpt[String]).getOrElse(throw new MappingException(s"Can't get the array type for $jv"))
        jv \ "required" match {
          case JBool(true) =>
            DataType(s"SET[$arrayType]")
          case JBool(false) =>
            DataType(s"LIST[$arrayType]")
          case x => throw new MappingException(s"Can't deserialize the data type required property for $x")
        }
      case JString(x) => DataType(x)
      // does not handle a reference yet
      case x => throw new MappingException(s"Can't deserialize data type for $x")
    }
  }

  class ParameterSerializer extends CustomSerializer[Parameter](formats => ({
    case json =>
      implicit val fmts: Formats = formats
      Parameter(
        name = (json \ "name").extractOrElse(""),
        description = (json \ "description").extractOrElse(""),
        dataType = deserializeDataType("dataType", json),
        notes = (json \ "notes").extractOpt[String],
        paramType = (json \ "paramType").extractOrElse(ParamType.Query),
        defaultValue = (json \ "defaultValue").extractOpt[String],
        allowableValues = (json \ "allowableValues").extractOrElse(AllowableValues.AnyValue),
        required = (json \ "required").extractOrElse(true),
        allowMultiple = (json \ "allowMultiple").extractOrElse(false)
      )
  }, {
    case x: Parameter =>
      implicit val fmts = formats
      ("name" -> x.name) ~
      ("description" -> x.description) ~
      ("notes" -> x.notes) ~
      ("defaultValue" -> x.defaultValue) ~
      ("allowableValues" -> Extraction.decompose(x.allowableValues)) ~
      ("required" -> x.required) ~
      ("paramType" -> x.paramType.toString) ~
      ("allowMultiple" -> x.allowMultiple) merge serializeDataType("dataType", x.dataType)
  }))

  class ModelFieldSerializer extends CustomSerializer[ModelField](formats => ({
    case json =>
      implicit val fmts = formats
      ModelField(
        name = (json \ "name").extractOrElse(""),
        description = (json \ "description").extractOpt[String],
        `type` = deserializeDataType("type", json),
        defaultValue = (json \ "defaultValue").extractOpt[String],
        enum = (json \ "enum" \\ classOf[JString]),
        required = (json \ "required").extractOrElse(true)
      )
  }, {
    case x: ModelField =>
      implicit val fmts = formats
      val c = ("description" -> x.description) ~
      ("defaultValue" -> x.defaultValue) ~
      ("enum" -> x.enum) ~
      ("required" -> x.required)
      c merge serializeDataType("type", x.`type`)
  }))

  class AllowableValuesSerializer extends CustomSerializer[AllowableValues](formats => ({
    case json =>
      implicit val fmts = formats
      json \ "valueType" match {
        case JString(x) if x.equalsIgnoreCase("LIST") =>
          AllowableValues.AllowableValuesList((json \ "values").extract[List[String]])
        case JString(x) if x.equalsIgnoreCase("RANGE") =>
          AllowableValues.AllowableRangeValues(Range((json \ "min").extract[Int], (json \ "max").extract[Int]))
        case _ => AllowableValues.AnyValue
      }
  },{
    case AllowableValues.AnyValue ⇒ JNothing
    case AllowableValues.AllowableValuesList(values)  ⇒
      implicit val fmts = formats
      ("valueType" -> "LIST") ~ ("values" -> Extraction.decompose(values))
    case AllowableValues.AllowableRangeValues(range)  ⇒ ("valueType" -> "RANGE") ~ ("min" -> range.start) ~ ("max" -> range.end)
  }))
}

object ParamType extends Enumeration {
  type ParamType = Value

  val Body = Value("body")
  val Query = Value("query")
  val Path = Value("path")
  val Header = Value("header")
}

object DataType {
  case class DataType(name: String)

  val Void = DataType("void")
  val String = DataType("string")
  val Int = DataType("int")
  val Boolean = DataType("boolean")
  val Date = DataType("date")
  val Enum = DataType("enum")
  val List = DataType("List")
  val Map = DataType("Map")
  val Tuple = DataType("tuple")

  object GenList {
    def apply(): DataType = List
    def apply(v: DataType): DataType = new DataType(s"List[${v.name}]")
  }

  object GenMap {
    def apply(): DataType = Map
    def apply(k: DataType, v: DataType): DataType = new DataType(s"Map[${k.name}, ${v.name}]")
  }

  def apply(name: String) = new DataType(name)
  def apply[T](implicit mf: Manifest[T]) = new DataType(mf.erasure.getSimpleName)
}

trait AllowableValues

object AllowableValues {
  case object AnyValue extends AllowableValues
  case class AllowableValuesList[T <% JValue](values: List[T]) extends AllowableValues
  case class AllowableRangeValues(values: Range) extends AllowableValues

  def apply(): AllowableValues = empty
  def apply[T <% JValue](values: T*): AllowableValues = apply(values.toList)
  def apply[T <% JValue](values: List[T]): AllowableValues = {
    AllowableValuesList(values)
  }
  def apply(values: Range): AllowableValues = AllowableRangeValues(values)
  def empty = AnyValue
}

case class Parameter(name: String,
                     description: String,
                     dataType: DataType.DataType,
                     notes: Option[String] = None,
                     paramType: ParamType.ParamType = ParamType.Query,
                     defaultValue: Option[String] = None,
                     allowableValues: AllowableValues = AllowableValues.AnyValue,
                     required: Boolean = true,
                     allowMultiple: Boolean = false)

case class ModelField(name: String,
                      description: Option[String],
                      `type`: DataType.DataType,
                      defaultValue: Option[String] = None,
                      enum: List[String] = Nil,
                      required: Boolean = true)

object ModelField {
  implicit def modelField2tuple(m: ModelField) = (m.name, m)
}

case class Model(id: String,
                 description: Option[String],
                 properties: Map[String, ModelField]) {

  def setRequired(property: String, required: Boolean) =
    copy(properties = (properties + (property -> properties(property).copy(required = required))))
}

object Model {
  implicit def model2tuple(m: Model) = (m.id, m)
}

case class Operation(httpMethod: HttpMethod,
                     responseClass: String,
                     summary: String,
                     notes: Option[String] = None,
                     deprecated: Boolean = false,
                     nickname: Option[String] = None,
                     parameters: List[Parameter] = Nil,
                     errorResponses: List[Error] = Nil)

case class Endpoint(path: String,
                    description: Option[String],
                    secured: Boolean = false,
                    operations: List[Operation] = Nil)

case class Error(code: Int,
                 reason: String)
