package org.json4s.amongo.model

import java.util.{Date, UUID}

import org.bson.types.ObjectId

/**
  * Created by talg on 09/06/2016.
  */
case class SimpleObject(_id: ObjectId, string: String, int: Int, long: Long, boolean: Boolean, nullValue: AnyRef, option: Option[Int])

case class EmbeddedObject(string: String, embd: Option[EmbeddedObject], list: List[Int], arrObj: List[SimpleObject])

case class MultipliedObject(ids: List[ObjectId], strings: List[String], ints: List[Int], longs: List[Long], booleans: List[Boolean], nulls: List[AnyRef], options: List[Option[Int]])

case class SerializerRequiredObjects(_id: ObjectId, uuid: UUID, date: Date)