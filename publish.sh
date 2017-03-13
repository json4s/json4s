#!/bin/bash

sbt ++2.10.6 javaVersionCheck publishSigned \
    ++2.11.8 javaVersionCheck publishSigned
