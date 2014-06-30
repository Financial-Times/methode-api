#!/bin/bash

start_time=`date +%s`
MODULEFULLPATH="${project.build.directory}/ft-methode_api"

/usr/local/bin/forge-admin.py --publish --source $MODULEFULLPATH
echo ""
exit 0
