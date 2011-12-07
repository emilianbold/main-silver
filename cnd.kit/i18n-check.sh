#!/bin/sh -x
WORKSPACE=$1
LOG=$2
MAILTO=$3

WORKSPACE=${WORKSPACE:-..};
LOG=${LOG:-${WORKSPACE}/i18n-check.log};


# check workspace  existence and access
if [ ! -d ${WORKSPACE} ]; then
	echo "Error: ${WORKSPACE} is not a directory"
	exit 4
fi
if [ ! -r ${WORKSPACE} ]; then
	echo "Error: can not read ${WORKSPACE}"
	exit 8
fi

#WORKSPACE=`(cd ${WORKSPACE}; pwd)`

#echo WORKSPACE=$WORKSPACE
#echo LOG=$LOG

cd ${WORKSPACE}

perl nbbuild/misc/i18ncheck.pl cnd* asm* dlight* remotefs* lib.terminalemulator/src terminal | grep -v "/versioning/core/" | grep -v "/test/" | grep -v "cnd.antlr/" | grep -v "generated/" | grep -v "parser/FortranLexicalPrepass.java" | grep -v "parser/FortranTokenStream.java" | tee ${LOG}
cnt=`cat ${LOG} | wc -l`
if [ ${cnt} -gt 0 ]; then
	echo "I18n check FAILED"
	if [ -n "${MAILTO}" ]; then
		mailx -s "I18n check FAILED" -r "${MAILTO}" "${MAILTO}" < ${LOG}
	fi
else
	echo "I18n check SUCCEEDED - no warnings"
fi
