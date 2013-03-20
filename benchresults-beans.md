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
json4s-native AST (parse)         759ms  
json4s-jackson AST (parse)        509ms  
json4s-native AST (ser)           401ms  
json4s-jackson AST (ser)           89ms  

### Custom serializer
json4s-native (full)              760ms  
json4s-jackson (full)             535ms  
json4s-native (ser)               391ms  
json4s-jackson (ser)              196ms  
json4s-native (deser)             298ms  
json4s-jackson (deser)            279ms  
json4s-native old pretty          600ms  

### No type hints
json4s-native (full)             1053ms  
json4s-jackson (full)             833ms  
json4s-native (ser)               507ms  
json4s-jackson (ser)              327ms  
json4s-native (deser)             460ms  
json4s-jackson (deser)            438ms  
json4s-native old pretty          765ms  

### Short type hints
json4s-native (full)             1660ms  
json4s-jackson (full)            1369ms  
json4s-native (ser)               703ms  
json4s-jackson (ser)              414ms  
json4s-native (deser)             816ms  
json4s-jackson (deser)            796ms  
json4s-native old pretty         1030ms  

### Full type hints
json4s-native (full)             2183ms  
json4s-jackson (full)            1874ms  
json4s-native (ser)              1052ms  
json4s-jackson (ser)              617ms  
json4s-native (deser)             970ms  
json4s-jackson (deser)            945ms  
json4s-native old pretty         1359ms  