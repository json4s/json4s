package org.json4s.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the primary constructor that used to get the fields to serialize from the class.
 *
 * Marking two constructors or more as primary in the same class will cause the serializer
 * to throw IllegalArgumentException.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface PrimaryConstructor {

}
