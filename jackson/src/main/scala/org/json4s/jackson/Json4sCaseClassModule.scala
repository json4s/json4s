package org.json4s.jackson

import com.fasterxml.jackson.databind.introspect.{AnnotatedField, AnnotatedConstructor, AnnotatedParameter, NopAnnotationIntrospector};

import org.scalastuff.scalabeans.{DeserializablePropertyDescriptor}
import collection.JavaConverters._

import java.{util => ju}

import org.scalastuff.scalabeans.ConstructorParameter

import com.fasterxml.jackson.databind.{BeanDescription, SerializationConfig}
import com.fasterxml.jackson.databind.ser.{BeanPropertyWriter, BeanSerializerModifier}
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition
import com.fasterxml.jackson.module.scala.JacksonModule

private object Json4sCaseClassBeanSerializerModifier extends BeanSerializerModifier {
  private val PRODUCT = classOf[Product]

  override def changeProperties(config: SerializationConfig,
                                beanDesc: BeanDescription,
                                beanProperties: ju.List[BeanPropertyWriter]): ju.List[BeanPropertyWriter] = {
    val jacksonIntrospector = config.getAnnotationIntrospector
    val list = for {
      cls <- Option(beanDesc.getBeanClass).toSeq if (PRODUCT.isAssignableFrom(cls))
      prop <- Json4sScalaBeansUtil.propertiesOf(cls)
      // Not completely happy with this test. I'd rather check the PropertyDescription
      // to see if it's a field or a method, but ScalaBeans doesn't expose that as yet.
      // I'm not sure if it truly matters as Scala generates method accessors for fields.
      // This is also realy inefficient, as we're doing a find on each iteration of the loop.
      method <- Option(beanDesc.findMethod(prop.name, Array()))
    } yield prop match {
      case cp: ConstructorParameter =>
        val param = beanDesc.getConstructors.get(0).getParameter(cp.index)
        asWriter(config, beanDesc, method, Option(jacksonIntrospector.findDeserializationName(param)))
      case _ => asWriter(config, beanDesc, method)
    }

    if (list.isEmpty) beanProperties else new ju.ArrayList[BeanPropertyWriter](list.toList.asJava)
  }

  private def asWriter(config: SerializationConfig, beanDesc: BeanDescription, member: AnnotatedMethod, primaryName: Option[String] = None) = {
    val javaType = config.getTypeFactory.constructType(member.getGenericType)
    val name = primaryName.getOrElse(member.getName)
    val propDef = new SimpleBeanPropertyDefinition(member, scala.reflect.NameTransformer.decode(name))
    new BeanPropertyWriter(propDef, member, null, javaType, null, null, null, false, null)
  }

}

trait Json4sCaseClassSerializerModule extends JacksonModule {
  this += { _.addBeanSerializerModifier(Json4sCaseClassBeanSerializerModifier) }
}
private object Json4sCaseClassAnnotationIntrospector extends NopAnnotationIntrospector {
  lazy val PRODUCT = classOf[Product]
  lazy val OPTION = classOf[Option[_]]
  lazy val LIST = classOf[List[_]]

  private def maybeIsCaseClass(cls: Class[_]): Boolean = {
    if (!PRODUCT.isAssignableFrom(cls)) false
    else if (OPTION.isAssignableFrom(cls)) false
    else if (LIST.isAssignableFrom(cls)) false
    else if (cls.getName.startsWith("scala.Tuple")) false
    else true
  }

  override def findDeserializationName(af: AnnotatedField): String = {
    val cls = af.getDeclaringClass
    if (!maybeIsCaseClass(cls)) null
    else {
      val properties = Json4sScalaBeansUtil.propertiesOf(cls)

      (properties.find {
        case dp: DeserializablePropertyDescriptor => af.getName.equals(dp.name)
        case _ => false
      }).map(s => scala.reflect.NameTransformer.decode(s.name)) getOrElse null
    }
  }

  override def findDeserializationName(param: AnnotatedParameter): String = {
    val cls = param.getDeclaringClass
    if (!maybeIsCaseClass(cls)) null
    else {
      param.getOwner match {
        case _: AnnotatedConstructor => findConstructorParamName(param)
        case _ => null
      }
    }
  }

  private def findConstructorParamName(param: AnnotatedParameter): String = {
    val cls = param.getDeclaringClass
    if (!maybeIsCaseClass(cls)) null
    else {
      val properties = Json4sScalaBeansUtil.propertiesOf(cls)

      properties.find {
        case cp: ConstructorParameter => cp.index == param.getIndex
        case _ => false
      }.map(s => scala.reflect.NameTransformer.decode(s.name)) getOrElse null
    }
  }

}

trait Json4sCaseClassDeserializerModule extends JacksonModule {
  this += { _.appendAnnotationIntrospector(Json4sCaseClassAnnotationIntrospector) }
}

trait Json4sCaseClassModule extends Json4sCaseClassSerializerModule with Json4sCaseClassDeserializerModule
