# Benchresults

## Serialization
Java serialization (ser)          232ms
Java noop                           0ms
Java toString (ser)               115ms

### Jackson with Scala module
Jackson serialization (full)      207ms
Jackson serialization (ser)        64ms
Jackson (deser)                   107ms
Jackson AST (parse)               603ms
Jackson AST (ser)                  26ms
  
### Json4s direct AST
json4s-native AST (parse)         707ms
json4s-jackson AST (parse)        445ms
json4s-native AST (ser)           282ms
json4s-jackson AST (ser)           98ms

### Custom serializer
json4s-native (full)              630ms
json4s-jackson (full)             560ms
json4s-native (ser)               288ms
json4s-jackson (ser)              214ms
json4s-native (deser)             287ms
json4s-jackson (deser)            250ms

### No type hints
json4s-native (full)             1020ms
json4s-jackson (full)             903ms
json4s-native (ser)               398ms
json4s-jackson (ser)              368ms
json4s-native (deser)             482ms
json4s-jackson (deser)            422ms

### Short type hints
json4s-native (full)             1718ms
json4s-jackson (full)            1510ms
json4s-native (ser)               594ms
json4s-jackson (ser)              448ms
json4s-native (deser)            1007ms
json4s-jackson (deser)            913ms

### Full type hints
json4s-native (full)             2113ms
json4s-jackson (full)            1767ms
json4s-native (ser)               818ms
json4s-jackson (ser)              610ms
json4s-native (deser)            1158ms
json4s-jackson (deser)           1313ms