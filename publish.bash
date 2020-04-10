#! /bin/bash

export ARTIFACTORY_USER=admin
export ARTIFACTORY_PASS=9gIAl09112z5xm64ZY

sbt clean compile test package publish
