# Benchresults
The results of the benchmarks before touching extraction and the scalasig parsing etc.

## Parsing
Scala std	162570ms
Jackson	433ms
json4s-native	780ms
json4s-jackson	444ms

## Serialization

### No type hints
Jackson serialization (full)	191ms
json4s-native (full)	4393ms
json4s-jackson (full)	3994ms

Java serialization (ser)	131ms
Jackson serialization (ser)	33ms
json4s-native (ser)	3780ms
json4s-jackson (ser)	3525ms

Jackson (deser)	114ms
json4s-native (deser)	456ms
json4s-jackson (deser)	383ms

### Short type hints
Jackson serialization (full)	158ms
json4s-native (full)	5093ms
json4s-jackson (full)	4585ms

Java serialization (ser)	129ms
Jackson serialization (ser)	34ms
json4s-native (ser)	3982ms
json4s-jackson (ser)	3539ms

Jackson (deser)	107ms
json4s-native (deser)	882ms
json4s-jackson (deser)	811ms

### Full type hints

Jackson serialization (full)	159ms
json4s-native (full)	5573ms
json4s-jackson (full)	4951ms
Java serialization (ser)	136ms

Jackson serialization (ser)	33ms
json4s-native (ser)	4116ms
json4s-jackson (ser)	3560ms

Jackson (deser)	107ms
json4s-native (deser)	1294ms
json4s-jackson (deser)	1216ms