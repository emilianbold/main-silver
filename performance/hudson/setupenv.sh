#!/bin/bash -x

if test ! -e /space/hudsonserver/master 
then

cd $performance/j2se
# ant -Dnetbeans.dest.dir=$netbeans_dest
# rm -rf "$WORKSPACE"/j2se

ant test-unit -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEStartupTest* -Dnetbeans.dest.dir=$netbeans_dest -Dperformance.testutilities.dist.jar=$perfjar -Dnetbeans.keyring.no.master=true -Drepeat=1 -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.performance.exec.dir=$execdir -Dnbplatform.default.harness.dir=$platdefharness

cp -R build/test/unit/work/ "$WORKSPACE"/startup/work
cp -R build/test/unit/results/ "$WORKSPACE"/startup/results
rm -rf "$WORKSPACE"/j2se/userdir0
rm -rf "$WORKSPACE"/j2se/tmpdir

cd "$performance"
buildnum=`cat "$reposdir"/build.number`
str1="<property name=\"perftestrun.buildnumber\" value=\"$buildnum\"/>"
str2="<property name=\"env.BUILD_NUMBER\" value=\"`echo $BUILD_NUMBER`\" />"
str3="<property name=\"env.JOB_NAME\" value=\"`echo $JOB_NAME`\" />"
export str="$str1 $str2 $str3"

# ergonomics root
cd "$project_root"
rm -rf nbbuild/nbproject/private
ant bootstrap

# performance project
cd "$performance"

ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true

buildnum=`cat "$reposdir"/build.number`
str1="<property name=\"perftestrun.buildnumber\" value=\"$buildnum\"/>"
str2="<property name=\"env.BUILD_NUMBER\" value=\"`echo $BUILD_NUMBER`\" />"
str3="<property name=\"env.JOB_NAME\" value=\"`echo $JOB_NAME`\" />"
export str="$str1 $str2 $str3"

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml 

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml 

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml 

cp -R build/test/unit/work/ "$WORKSPACE"/fod
cp -R build/test/unit/results/ "$WORKSPACE"/fod
rm -rf "$WORKSPACE"/fod/userdir0
rm -rf "$WORKSPACE"/fod/tmpdir


cd "$performance"

ant test-unit -Dsuite.dir=test -Dtest.includes=**/MeasureScanningTest* -Dnetbeans.dest.dir=$netbeans_dest -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -DSuspendSupport.disabled=true-Drepeat=3 -Dorg.netbeans.editor.linewrap=true

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.scanning.MeasureScanningTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.scanning.MeasureScanningTest.xml
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.scanning.MeasureScanningTest.xml

cp -R build/test/unit/work/ "$WORKSPACE"/scanning
cp -R build/test/unit/results/ "$WORKSPACE"/scanning
rm -rf "$WORKSPACE"/scanning/userdir0
rm -rf "$WORKSPACE"/scanning/tmpdir

fi
