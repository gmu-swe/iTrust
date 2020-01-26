#!usr/bin/env bash
path="$(cd "$(dirname "$0")" && pwd)"
SOURCES=../../../maven-extension/src/main/resources/config-files/demo/sources
SINKS=../../../maven-extension/src/main/resources/config-files/demo/sinks
TAINT_THROUGHS=../../../maven-extension/src/main/resources/config-files/demo/taintThrough
AUTO_TAINT="-Dphosphor.sources=${SOURCES} -Dphosphor.sinks=${SINKS} -Dphosphor.taintThrough=${TAINT_THROUGHS}"

INST_JAVA="${HOME}/.phosphor-jvm/bin/java"
M2_REPO="${HOME}/.m2/repository"
PHOSPHOR_JAR="${M2_REPO}/edu/gmu/swe/phosphor/Phosphor/0.0.4-SNAPSHOT/Phosphor-0.0.4-SNAPSHOT.jar"
RIVULET_CORE_JAR="${M2_REPO}/io/rivulet/rivulet-core/1.0.0-SNAPSHOT/rivulet-core-1.0.0-SNAPSHOT.jar"
SERVER_JAR="${M2_REPO}/io/rivulet/embedded-server/1.0.0-SNAPSHOT/embedded-server-1.0.0-SNAPSHOT.jar"
PHOSPHOR_OPTS="taintSourceWrapper=io.rivulet.internal.RivuletAutoTaintWrapper,ignore=io/rivulet/internal/,arrayindex,priorClassVisitor=io.rivulet.internal.RivuletCV,ignoredMethod=org/mindrot/jbcrypt/BCrypt.encipher([II)V,taintSources=${SOURCES},taintSinks=${SINKS},taintThrough=${TAINT_THROUGHS}"
JAVA_FLAGS="-Xmx6g -DphosphorCacheDirectory=cached-phosphor -Xbootclasspath/p:${PHOSPHOR_JAR}:${RIVULET_CORE_JAR} -javaagent:${PHOSPHOR_JAR}=${PHOSPHOR_OPTS}"

USER_FLAGS="-Difc.criticalReproductionFiles" #-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
CMD="$INST_JAVA $JAVA_FLAGS -cp $SERVER_JAR $USER_FLAGS io.rivulet.internal.server.EmbeddedServer 8080 8182 target/iTrust-23.0.0.war iTrust"
echo $CMD
$CMD
