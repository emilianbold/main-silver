#!/bin/bash

# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.

# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.

# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"

# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.

set -x -e

dmgname=$1
volname=$2
outputdir=$3

bunzip2 -d -c `dirname $0`/template.sparseimage.bz2 > ./dist/template.sparseimage

mkdir ./dist/mountpoint
hdiutil mount -verbose -mountpoint ./dist/mountpoint ./dist/template.sparseimage

rm -rf ./dist/mountpoint/*
echo "Running rsync..."
rsync -a ./dist_pkg/inst_package/ ./dist/mountpoint/
echo "Running diskutil rename..."
diskutil rename `pwd`/dist/mountpoint "$volname"
echo "Running hdiutil detach..."
hdiutil detach -verbose ./dist/mountpoint

if [ ! -z $outputdir ]; then
    mkdir -p ./dist/$outputdir
fi

echo "Running hdiutil create..."
hdiutil create -verbose -srcdevice `pwd`/dist/template.sparseimage ./dist/"$outputdir$dmgname"
rm -f ./dist/template.sparseimage
rmdir ./dist/mountpoint
