#!/bin/bash

start_time=`date +%s`
MODULEFULLPATH="${project.build.directory}/ft-methode_api"

/usr/local/bin/forge-admin.py --publish --source $MODULEFULLPATH
if [[ $? -ne 0 ]]; then
    echo -e "Attempt to publish $MODULEFULLPATH failed with code $?.\n"
    exit 255
fi
echo ""
exit 0
