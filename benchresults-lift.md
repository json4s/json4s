# Benchresults
The results of the benchmarks before touching extraction and the scalasig parsing etc.

## Parsing

Scala std	 162570ms  
Jackson	        433ms  
json4s-native	780ms  
json4s-jackson	444ms  

## Serialization  
Java serialization (ser)          204ms  
Java noop                           0ms  
Java toString (ser)               112ms  

### Jackson with Scala module
Jackson serialization (full)      207ms  
Jackson serialization (ser)        68ms  
Jackson (deser)                    96ms  
Jackson AST (parse)               572ms  
Jackson AST (ser)                  26ms  

### Json4s direct AST
json4s-native AST (parse)         703ms  
json4s-jackson AST (parse)        609ms  
json4s-native AST (ser)           272ms  
json4s-jackson AST (ser)          107ms  

### Custom serializer
json4s-native (full)              833ms  
json4s-jackson (full)             627ms  
json4s-native (ser)               432ms  
json4s-jackson (ser)              254ms  
json4s-native (deser)             327ms  
json4s-jackson (deser)            311ms  
  
### No type hints
json4s-native (full)             4435ms  
json4s-jackson (full)            4182ms  
json4s-native (ser)              3994ms  
json4s-jackson (ser)             3647ms  
json4s-native (deser)             428ms  
json4s-jackson (deser)            412ms  

### Short type hints
json4s-native (full)             4989ms  
json4s-jackson (full)            4716ms  
json4s-native (ser)              4029ms  
json4s-jackson (ser)             3704ms  
json4s-native (deser)             791ms  
json4s-jackson (deser)            768ms  
  
### Full type hints
json4s-native (full)             6192ms  
json4s-jackson (full)            5749ms  
json4s-native (ser)              4592ms  
json4s-jackson (ser)             4220ms  
json4s-native (deser)            1509ms  
json4s-jackson (deser)           1433ms
