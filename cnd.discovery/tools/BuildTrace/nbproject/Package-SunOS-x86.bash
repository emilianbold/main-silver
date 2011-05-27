#!/bin/bash -x

#
# Generated - do not edit!
#

# Macros
TOP=`pwd`
CND_PLATFORM=OracleSolarisStudio-Solaris-x86
CND_CONF=SunOS-x86
CND_DISTDIR=dist
CND_BUILDDIR=build
NBTMPDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}/tmp-packaging
TMPDIRNAME=tmp-packaging
OUTPUT_PATH=${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/libBuildTrace.so
OUTPUT_BASENAME=libBuildTrace.so
PACKAGE_TOP_DIR=

# Functions
function checkReturnCode
{
    rc=$?
    if [ $rc != 0 ]
    then
        exit $rc
    fi
}
function makeDirectory
# $1 directory path
# $2 permission (optional)
{
    mkdir -p "$1"
    checkReturnCode
    if [ "$2" != "" ]
    then
      chmod $2 "$1"
      checkReturnCode
    fi
}
function copyFileToTmpDir
# $1 from-file path
# $2 to-file path
# $3 permission
{
    cp "$1" "$2"
    checkReturnCode
    if [ "$3" != "" ]
    then
        chmod $3 "$2"
        checkReturnCode
    fi
}

# Setup
cd "${TOP}"
mkdir -p ${CND_DISTDIR}/package
rm -rf ${NBTMPDIR}
mkdir -p ${NBTMPDIR}

# Copy files and create directories and links
cd "${TOP}"
makeDirectory "${NBTMPDIR}/Linux-x86"
copyFileToTmpDir "dist/Linux-x86/libBuildTrace.so" "${NBTMPDIR}/${PACKAGE_TOP_DIR}Linux-x86/libBuildTrace.so" 0755

cd "${TOP}"
makeDirectory "${NBTMPDIR}/Linux-x86_64"
copyFileToTmpDir "dist/Linux-x86_64/libBuildTrace.so" "${NBTMPDIR}/${PACKAGE_TOP_DIR}Linux-x86_64/libBuildTrace.so" 0755

cd "${TOP}"
makeDirectory "${NBTMPDIR}/SunOS-sparc"
copyFileToTmpDir "dist/SunOS-sparc/libBuildTrace.so" "${NBTMPDIR}/${PACKAGE_TOP_DIR}SunOS-sparc/libBuildTrace.so" 0755

cd "${TOP}"
makeDirectory "${NBTMPDIR}/SunOS-sparc_64"
copyFileToTmpDir "dist/SunOS-sparc_64/libBuildTrace.so" "${NBTMPDIR}/${PACKAGE_TOP_DIR}SunOS-sparc_64/libBuildTrace.so" 0755

cd "${TOP}"
makeDirectory "${NBTMPDIR}/SunOS-x86"
copyFileToTmpDir "dist/SunOS-x86/libBuildTrace.so" "${NBTMPDIR}/${PACKAGE_TOP_DIR}SunOS-x86/libBuildTrace.so" 0755

cd "${TOP}"
makeDirectory "${NBTMPDIR}/SunOS-x86_64"
copyFileToTmpDir "dist/SunOS-x86_64/libBuildTrace.so" "${NBTMPDIR}/${PACKAGE_TOP_DIR}SunOS-x86_64/libBuildTrace.so" 0755


# Generate zip file
cd "${TOP}"
rm -f ${CND_DISTDIR}/package/cnd-build-trace-1.0.zip
cd ${NBTMPDIR}
zip -r  ../../../../${CND_DISTDIR}/package/cnd-build-trace-1.0.zip *
checkReturnCode

# Cleanup
cd "${TOP}"
rm -rf ${NBTMPDIR}
