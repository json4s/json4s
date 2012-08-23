Benchmarking standard Scala Json parser, Jackson parser and lift-json parser
----------------------------------------------------------------------------

Benchmark measures how long it takes to parse 50 000 times the first JSON document
from http://www.json.org/example.html. 

Facts:

* Ubuntu 8.10
* Lenovo T60p
* Scala 2.7.4
* java version "1.6.0_10"
  Java(TM) SE Runtime Environment (build 1.6.0_10-b33)
  Java HotSpot(TM) Server VM (build 11.0-b15, mixed mode)
* Exec: scala Jsonbench

Parsing 50 000 json documents:

    Scala std	  167127 ms
    Jackson       370 ms
    lift-json	  465 ms

Summary:

* Jackson was fastest.
* lift-json was about 350 times faster than standard Scala parser.

Serialization benchmark, Java serialization and lift-json
---------------------------------------------------------

See Serbench.scala

Facts:

* Ubuntu 8.10
* Lenovo T60p
* Scala 2.7.4
* java version "1.6.0_10"
  Java(TM) SE Runtime Environment (build 1.6.0_10-b33)
  Java HotSpot(TM) Server VM (build 11.0-b15, mixed mode)
* Exec: scala Serbench

Serializing 20 000 instances (No type hints):

    Java serialization (full)     1889 ms
    lift-json (full)              1542 ms
    Java serialization (ser)       373 ms
    lift-json (ser)                833 ms
    Java serialization (deser)    1396 ms
    lift-json (deser)	           615 ms

Serializing 20 000 instances (Using type hints, both short and full gives similar results):

    Java serialization (full)     1912 ms
    lift-json (full)              2268 ms

Summary:

* Total time about same (serialization + deserialization).
* Java serializes faster.
* lift-json deserializes faster.
* Using type hints comes with a performance penalty.
