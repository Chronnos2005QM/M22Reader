#!/bin/sh
JAVA_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
exec java $JAVA_OPTS \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
