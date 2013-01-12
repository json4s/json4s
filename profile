#!/bin/bash

function die() {
    echo "$*" 1>&2
    exit 1
}

MAINCLASS=org.json4s.benchmark.Runner


exec java $JAVA_OPTS -cp "./benchmark/target/scala-2.9.2/classes:./ast/target/scala-2.9.2/classes:./core/target/scala-2.9.2/classes:./native/target/scala-2.9.2/classes:./jackson/target/scala-2.9.2/classes:./ext/target/scala-2.9.2/classes:./mongo/target/scala-2.9.2/classes:$HOME/.sbt/0.12.1/boot/scala-2.9.2/lib/scala-library.jar:$HOME/.ivy2/cache/com.thoughtworks.paranamer/paranamer/jars/paranamer-2.5.2.jar:$HOME/.ivy2/cache/org.scala-lang/scalap/jars/scalap-2.9.2.jar:$HOME/.sbt/0.12.1/boot/scala-2.9.2/lib/scala-compiler.jar:$HOME/.ivy2/cache/com.fasterxml.jackson.core/jackson-databind/jars/jackson-databind-2.1.2.jar:$HOME/.ivy2/cache/joda-time/joda-time/jars/joda-time-2.1.jar:$HOME/.ivy2/cache/org.joda/joda-convert/jars/joda-convert-1.2.jar:$HOME/.ivy2/cache/org.mongodb/mongo-java-driver/jars/mongo-java-driver-2.10.1.jar:$HOME/.ivy2/cache/org.scalaj/scalaj-collection_2.9.1/jars/scalaj-collection_2.9.1-1.2.jar:$HOME/.ivy2/cache/com.google.code.java-allocation-instrumenter/java-allocation-instrumenter/jars/java-allocation-instrumenter-2.0.jar:$HOME/.ivy2/cache/asm/asm/jars/asm-3.3.1.jar:$HOME/.ivy2/cache/asm/asm-analysis/jars/asm-analysis-3.3.1.jar:$HOME/.ivy2/cache/asm/asm-tree/jars/asm-tree-3.3.1.jar:$HOME/.ivy2/cache/asm/asm-commons/jars/asm-commons-3.3.1.jar:$HOME/.ivy2/cache/asm/asm-util/jars/asm-util-3.3.1.jar:$HOME/.ivy2/cache/asm/asm-xml/jars/asm-xml-3.3.1.jar:$HOME/.ivy2/cache/com.google.caliper/caliper/jars/caliper-0.5-rc1.jar:$HOME/.ivy2/cache/com.google.code.findbugs/jsr305/jars/jsr305-1.3.9.jar:$HOME/.ivy2/cache/com.google.code.gson/gson/jars/gson-1.7.1.jar:$HOME/.ivy2/cache/com.fasterxml.jackson.module/jackson-module-scala_2.9.2/jars/jackson-module-scala_2.9.2-2.1.3.jar:$HOME/.ivy2/cache/com.fasterxml.jackson.core/jackson-core/jars/jackson-core-2.1.2.jar:$HOME/.ivy2/cache/com.fasterxml.jackson.core/jackson-annotations/jars/jackson-annotations-2.1.2.jar:$HOME/.ivy2/cache/com.google.guava/guava/jars/guava-13.0.1.jar" "$MAINCLASS" "$@"
