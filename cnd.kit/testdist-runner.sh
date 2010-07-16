#!/bin/sh

# This script downloads the latest CND incremental build and runs its tests.
# Any argument(s) given to this script will be passed to the JVM
# executing unit tests.
#
# Important environment variables:
#
# WORKSPACE (required, set by Hudson)
#   Full path to directory where all files will be stored.
#   Note that all existing files from WORKSPACE are deleted first.
# GET (optional)
#   If set to something non-empty, then download and unpack build artifacts
#   from Hudson first. If empty, then just run the tests without downloading
#   anything.
# HUDSON_URL (optional, set by Hudson)
#   URL of hudson instance that produces netbeans.zip and testdist.zip
#   Default value is http://elif:8080/hudson/
# UPSTREAM_NO (optional, set by Hudson)
#   Build number to download and test.
#   If not specified, then last successful build is used.
# EXECUTOR_NUMBER (optional, set by Hudson)
#   Hudson executor number. Useful if several executors are running the
#   tests in parallel.
# MODULES (optional)
#   Colon-separated list of NB modules to run unit tests for.
#   By default includes all modules of DLight, CND, internal terminal,
#   and native execution.
# ANT (optional)
#   Path to ant executable. By default "ant".

if [ -z "${WORKSPACE}" ]; then
    echo "WORKSPACE is not set!"
    echo "Beware: if GET is set, this script will remove everything in WORKSPACE first!"
    exit
fi

if [ -n "${GET}" ]; then
    cd "${WORKSPACE}" && rm -rf *

    HUDSON_URL=${HUDSON_URL:-http://elif:8080/hudson/}
    BUILD_NUM=${UPSTREAM_NO:-`wget -qO - ${HUDSON_URL}job/cnd-build/lastSuccessfulBuild/buildNumber`}
    wget -q "${HUDSON_URL}job/cnd-build/${BUILD_NUM}/artifact/netbeans.zip"
    wget -q "${HUDSON_URL}job/cnd-build/${BUILD_NUM}/artifact/testdist.zip"
    unzip -qo netbeans.zip
    unzip -qo testdist.zip
fi

cd "${WORKSPACE}/unit"
MODULES=${MODULES:-`ls -d dlight/* cnd/* ide/*terminal* ide/*nativeex* | paste -s -d : -`}
cd "${WORKSPACE}"
${ANT:-ant} -f "${WORKSPACE}/all-tests.xml" \
-Dbasedir="${WORKSPACE}/unit" \
-Dnetbeans.dest.dir="${WORKSPACE}/netbeans" \
-Dmodules.list="${MODULES}" \
-Dtest.disable.fails=true \
-Dtest.dist.timeout=1000000 \
-Dtest.run.args="-ea -XX:PermSize=32m -XX:MaxPermSize=200m -Xmx512m -Djava.io.tmpdir=/var/tmp/hudson${EXECUTOR_NUMBER} -Dcnd.remote.sync.root.postfix=hudson${EXECUTOR_NUMBER} $*"
