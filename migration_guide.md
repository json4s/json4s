Migration from older versions
=============================

3.3.0 ->
--------

json4s 3.3 basically should be source code compatible with 3.2.x. Since json4s 3.3.0, We've started using [MiMa](https://github.com/typesafehub/migration-manager) for binary compatibility verification not to repeat the bin compatibility issue described [here](https://github.com/json4s/json4s/issues/225).

The behavior of `.toOption` on JValue has changed. Now both `JNothing` and `JNull` return None.
For the old behavior you can use `toSome` which will only turn a `JNothing` into a None.

All the merged pull requests:
https://github.com/json4s/json4s/pulls?q=is%3Apr+is%3Aclosed+milestone%3A3.3

3.0.0 ->
--------

JField is no longer a JValue. This means more type safety since it is no longer possible
to create invalid JSON where JFields are added directly into JArrays for instance. The most
noticeable consequence of this change are that map, transform, find and filter come in
two versions:

```scala
def map(f: JValue => JValue): JValue
def mapField(f: JField => JField): JValue
def transform(f: PartialFunction[JValue, JValue]): JValue
def transformField(f: PartialFunction[JField, JField]): JValue
def find(p: JValue => Boolean): Option[JValue]
def findField(p: JField => Boolean): Option[JField]
//...
```

Use *Field functions to traverse fields in the JSON, and use the functions without 'Field'
in the name to traverse values in the JSON.

2.2 ->
------

Path expressions were changed after version 2.2. Previous versions returned JField, which
unnecessarily complicated the use of the expressions. If you have used path expressions
with pattern matching like:

```scala
val JField("bar", JInt(x)) = json \ "foo" \ "bar"
```

it is now required to change that to:

```scala
val JInt(x) = json \ "foo" \ "bar"
```

