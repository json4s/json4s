# Benchresults
The results of the benchmarks before touching extraction and the scalasig parsing etc.

## Parsing

Scala std	 162570ms  
Jackson	        433ms  
json4s-native	780ms  
json4s-jackson	444ms  

## Serialization  

[info] Running org.json4s.examples.SerBench  
Jackson serialization (full) 186ms  
Java serialization (ser)     178ms  
Jackson serialization (ser)   48ms  
Jackson (deser)              109ms  
  
### No type hints  
json4s-native (full)	4334ms  
json4s-jackson (full)	4016ms  
json4s-native (ser)	    3767ms  
json4s-jackson (ser)	3450ms  
json4s-native (deser)	 465ms  
json4s-jackson (deser)	 423ms  

<<<<<<< HEAD
### Short type hints  
json4s-native (full)	4974ms  
json4s-jackson (full)	4562ms  
json4s-native (ser)	    3992ms  
json4s-jackson (ser)	3501ms  
json4s-native (deser)	 851ms  
json4s-jackson (deser)	 857ms    
  
### Full type hints  
json4s-native (full)	6414ms  
json4s-jackson (full)	5817ms  
json4s-native (ser)	    5582ms  
json4s-jackson (ser)	4036ms  
json4s-native (deser)	1834ms  
json4s-jackson (deser)	1692ms  
