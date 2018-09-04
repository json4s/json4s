#!/bin/bash
sbt ++2.10.7 \
    publishSigned \
    ++2.11.12 \
    publishSigned \
    ++2.12.6 \
    publishSigned \
    ++2.13.0-M5 \
    json4s-ast/publishSigned \
    json4s-core/publishSigned \
    json4s-ext/publishSigned \
    json4s-jackson/publishSigned \
    json4s-mongo/publishSigned \
    json4s-native/publishSigned \
    json4s-scalap/publishSigned \
    json4s-scalaz/publishSigned \
    sonatypeRelease
