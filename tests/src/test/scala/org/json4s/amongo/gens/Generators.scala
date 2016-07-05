package org.json4s.amongo.gens

import java.util.Date

import org.bson.types.ObjectId
import org.json4s.amongo.model.{EmbeddedObject, MultipliedObject, SerializerRequiredObjects, SimpleObject}
import org.scalacheck.{Arbitrary, Gen}

/**
  * Created by talg on 09/06/2016.
  */
object Generators {
  def simpleObjectGen = for {
    s <- Gen.listOfN(10, Gen.alphaNumChar).map(_.mkString)
    i <- Arbitrary.arbInt.arbitrary
    l <- Arbitrary.arbLong.arbitrary
    b <- Arbitrary.arbBool.arbitrary
    o <- Arbitrary.arbOption[Int](Arbitrary.arbInt).arbitrary
  } yield SimpleObject(
    _id = ObjectId.get,
    string = s,
    int = i,
    long = l,
    boolean = b,
    nullValue = null,
    option = o
  )

  def embeddedObjectGen: Gen[EmbeddedObject] = for {
    s <- Gen.listOfN(10, Gen.alphaNumChar).map(_.mkString)
    si <- Gen.option(embeddedObjectGen)
    l <- Gen.choose(0, 5).flatMap(n => Gen.listOfN(n,Arbitrary.arbInt.arbitrary))
    aO <- Gen.choose(0, 5).flatMap(n => Gen.listOfN(n,simpleObjectGen))
  } yield EmbeddedObject(
    string = s,
    embd = si,
    list = l,
    arrObj = aO
  )

  def multipliedObject: Gen[MultipliedObject] = for {
    simpleObjects <- Gen.choose(1,10).flatMap(n => Gen.listOfN(n, simpleObjectGen))
  } yield MultipliedObject(
    ids = simpleObjects.map(_._id),
    strings = simpleObjects.map(_.string),
    ints = simpleObjects.map(_.int),
    longs = simpleObjects.map(_.long),
    booleans = simpleObjects.map(_.boolean),
    nulls = simpleObjects.map(_.nullValue),
    options = simpleObjects.map(_.option)
  )

  def serializerRequiredObjGen = for {
    d <- Gen.choose(1000,100000).flatMap(n => new Date(System.currentTimeMillis() - n))
    u <- Gen.uuid
  } yield SerializerRequiredObjects(
    _id = ObjectId.get,
    uuid = u,
    date = d
  )
}
