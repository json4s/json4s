
## Benchresults

[info] Running org.json4s.examples.SerBench
Jackson serialization (full)	178ms
Java serialization (ser)	180ms
Jackson serialization (ser)	50ms
Jackson (deser)	103ms

### No type hints
json4s-native (full)	1439ms
json4s-jackson (full)	 951ms
json4s-native (ser)	     699ms
json4s-jackson (ser)	 430ms
json4s-native (deser)	 478ms
json4s-jackson (deser)	 430ms

### Short type hints
json4s-native (full)	1890ms
json4s-jackson (full)	1377ms
json4s-native (ser)	     892ms
json4s-jackson (ser)	 463ms
json4s-native (deser)	 850ms
json4s-jackson (deser)	 786ms

### Full type hints
json4s-native (full)	3484ms
json4s-jackson (full)	2901ms
json4s-native (ser)	    1521ms
json4s-jackson (ser)	 995ms
json4s-native (deser)	1810ms
json4s-jackson (deser)	1750ms