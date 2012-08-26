package org.json4s
package jackson

import com.fasterxml.jackson.core.{JsonToken, JsonParser}
import com.fasterxml.jackson.module.scala._
import com.fasterxml.jackson.module.scala.deser.UntypedObjectDeserializerModule

trait JValueModule extends JacksonModule {
  this += (_ addSerializers JValueSerializerResolver)
  this += JValueDeserializerResolver
}

class Json4sScalaModule
            extends JacksonModule
            with JValueModule
            with EnumerationModule
            with OptionModule
            with SeqModule
            with IterableModule
            with TupleModule
            with MapModule
            with CaseClassModule
            with SetModule
            with UntypedObjectDeserializerModule
object Json4sScalaModule extends Json4sScalaModule
