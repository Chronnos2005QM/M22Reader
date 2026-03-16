#!/bin/sh
JAVA_OPTS="-Xmx1536m"
exec java $JAVA_OPTS \
  -classpath "$( cd "$(dirname "$0")" && pwd)/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
