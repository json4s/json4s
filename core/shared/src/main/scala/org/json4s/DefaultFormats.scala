package org.json4s

import org.json4s.prefs.{EmptyValueStrategy, ExtractionNullStrategy}
import java.lang.reflect.Type
import java.util.{Date, TimeZone}

/**
 * Default date format is UTC time.
 */
object DefaultFormats extends DefaultFormats {
  val UTC = TimeZone.getTimeZone("UTC")

  private val losslessDate = {
    def createSdf = {
      val f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      f.setTimeZone(UTC)
      f
    }
    new ThreadLocal(createSdf)
  }

}

trait DefaultFormats extends Formats {
  import java.text.{ParseException, SimpleDateFormat}

  private[this] val df = new ThreadLocal[SimpleDateFormat](dateFormatter)

  override val parameterNameReader: reflect.ParameterNameReader = reflect.ParanamerReader
  override val typeHints: TypeHints = NoTypeHints
  override val customSerializers: List[Serializer[?]] = Nil
  override val customKeySerializers: List[KeySerializer[?]] = Nil
  override val fieldSerializers: List[(Class[?], FieldSerializer[?])] = Nil
  override val wantsBigInt: Boolean = true
  override val wantsBigDecimal: Boolean = false
  override val primitives: Set[Type] = Set(classOf[JValue], classOf[JObject], classOf[JArray])
  override val companions: List[(Class[?], AnyRef)] = Nil
  override val strictOptionParsing: Boolean = false
  override val emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.default
  override val extractionNullStrategy: ExtractionNullStrategy = ExtractionNullStrategy.Keep
  override def strictFieldDeserialization: Boolean = false

  val dateFormat: DateFormat = new DateFormat {
    def parse(s: String) = try {
      Some(formatter.parse(s))
    } catch {
      case _: ParseException => None
    }

    def format(d: Date) = formatter.format(d)

    def timezone = formatter.getTimeZone

    private[this] def formatter = df()
  }

  protected def dateFormatter: SimpleDateFormat = {
    val f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    f.setTimeZone(DefaultFormats.UTC)
    f
  }

  /**
   * Lossless date format includes milliseconds too.
   */
  def lossless: Formats = new DefaultFormats {
    override def dateFormatter = DefaultFormats.losslessDate()
  }

  /**
   * Default formats with given <code>TypeHint</code>s.
   */
  def withHints(hints: TypeHints): Formats = new DefaultFormats {
    override val typeHints = hints
  }
}
