#!/bin/bash

sbt javaVersionCheck +publishSigned sonatypeRelease
