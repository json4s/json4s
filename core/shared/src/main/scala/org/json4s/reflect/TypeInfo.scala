package org.json4s.reflect

import java.lang.reflect.ParameterizedType

case class TypeInfo(clazz: Class[?], parameterizedType: Option[ParameterizedType])
