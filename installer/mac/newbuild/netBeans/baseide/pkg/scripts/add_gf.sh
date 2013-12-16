#!/bin/sh
nb_dir=$1
gf_dir=$2

echo Changing netbeans.conf in $nb_dir
echo GlassFish is in $gf_dir

if [ "$nb_dir" = "" ] || [ "$gf_dir" = "" ]
then
  exit
fi
if [ -d "$nb_dir" ] && [ -d "$gf_dir" ]
then
  cd "$nb_dir" 
  cd Contents/Resources/NetBeans*/
  curdir=`pwd`
  dirname=`dirname "$0"`
  jdk_home=`"$dirname"/get_current_jdk.sh`
  "$jdk_home"/bin/java -cp \
                           platform/core/core.jar:platform/lib/boot.jar:platform/lib/org-openide-modules.jar:platform/core/org-openide-filesystems.jar:platform/lib/org-openide-util.jar:platform/lib/org-openide-util-lookup.jar:enterprise/modules/org-netbeans-modules-glassfish-common.jar:enterprise/modules/ext/glassfish-tooling-sdk.jar \
                           \
                           org.netbeans.modules.glassfish.common.registration.AutomaticRegistration \
                           \
                           "$curdir/nb" \
                           "$gf_dir/glassfish"
  val=$?

  if [ $val -eq 0 ] ; then
     echo "GlassFish installed at $gf_dir integrated with NetBeans installed at $nb_dir"
  else
     echo "GlassFish installed at $gf_dir was not integrated with NetBeans installed at $nb_dir, error code is $val"
  fi
fi

