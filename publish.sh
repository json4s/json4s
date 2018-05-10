#!/bin/bash

sbt ++2.10.7 javaVersionCheck publishSigned \
    ++2.11.12 javaVersionCheck publishSigned
