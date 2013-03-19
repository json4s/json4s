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
json4s-native AST (parse)         715ms
json4s-jackson AST (parse)        452ms
json4s-native AST (ser)           265ms
json4s-jackson AST (ser)           99ms

### Custom serializer
json4s-native (full)              668ms
json4s-jackson (full)             523ms
json4s-native (ser)               307ms
json4s-jackson (ser)              200ms
json4s-native (deser)             279ms
json4s-jackson (deser)            263ms

### No type hints
json4s-native (full)             1078ms
json4s-jackson (full)             886ms
json4s-native (ser)               413ms
json4s-jackson (ser)              326ms
json4s-native (deser)             517ms
json4s-jackson (deser)            475ms

### Short type hints
json4s-native (full)             1818ms
json4s-jackson (full)            1559ms
json4s-native (ser)               578ms
json4s-jackson (ser)              439ms
json4s-native (deser)            1063ms
json4s-jackson (deser)            958ms

### Full type hints
json4s-native (full)             2149ms
json4s-jackson (full)            1804ms
json4s-native (ser)               850ms
json4s-jackson (ser)              594ms
json4s-native (deser)            1160ms
json4s-jackson (deser)           1078ms