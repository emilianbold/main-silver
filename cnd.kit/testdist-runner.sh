#!/bin/sh

# This script downloads latest CND build and executes its tests.
# Needs HUDSON_URL, WORKSPACE and EXECUTOR_NUMBER env vars (normally set by Hudson).

rm -rf extralibs/ qa-functional/ unit/ README.txt tasks.jar *.xml *.zip

BUILD_NUM=${UPSTREAM_NO:-`wget -qO - ${HUDSON_URL}job/cnd-build/lastSuccessfulBuild/buildNumber`}
wget -q "${HUDSON_URL}job/cnd-build/${BUILD_NUM}/artifact/netbeans.zip"
wget -q "${HUDSON_URL}job/cnd-build/${BUILD_NUM}/artifact/testdist.zip"
unzip -qo netbeans.zip
unzip -qo testdist.zip

cd unit
MODULES=${MODULES:-`ls -d dlight/* cnd/* ide/*terminal* ide/*nativeex* | paste -s -d : -`}
cd ..
${ANT:-ant} -f all-tests.xml \
-Dbasedir="${WORKSPACE}/unit" \
-Dnetbeans.dest.dir="${WORKSPACE}/netbeans" \
-Dmodules.list="${MODULES}" \
-Dtest.disable.fails=true \
-Dtest.dist.timeout=1000000 \
-Dtest.run.args="-ea -XX:PermSize=32m -XX:MaxPermSize=200m -Xmx512m -Djava.io.tmpdir=/var/tmp/hudson${EXECUTOR_NUMBER} -Dcnd.remote.sync.root.postfix=hudson${EXECUTOR_NUMBER} $*"
