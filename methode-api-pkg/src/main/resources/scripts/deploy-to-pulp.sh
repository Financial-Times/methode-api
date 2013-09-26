#!/bin/bash

start_time=`date +%s`
MODULEFULLPATH="${project.build.directory}/ft-methode_api"

/usr/bin/pulp-admin login -u admin -p admin

/usr/local/bin/puppet module build "$MODULEFULLPATH"
/usr/bin/pulp-admin puppet repo uploads upload --repo-id localforge --f "$MODULEFULLPATH/pkg/ft-methode_api-${project.version}.tar.gz"
/usr/bin/pulp-admin puppet repo publish run --repo-id=localforge

count=0
poll_int=5
total_poll_time=60
max_retry_times=`expr $total_poll_time / $poll_int`
http_status=$(curl -v --connect-timeout 1 -m 5 -w "\n%{http_code}\n" -X HEAD "http://pulp.svc.ft.com/pulp/puppet/localforge/system/releases/f/ft/ft-methode_api-${project.version}.tar.gz" 2>/tmp/ft-methode_api.log| tail -1)
while [[ $http_status  != "200" ]]
do
    echo "Puppet module has not been published yet. Waiting before retrying ...."
    let count++
    sleep $poll_int
    if [ $count == $max_retry_times ]; then
        echo "Timed out after waiting 60 seconds for the puppet module to appear in pulp "
        exit 2
    fi

    http_status=$(curl -v --connect-timeout 1 -m 5 -w "\n%{http_code}\n" -X HEAD "http://pulp.svc.ft.com/pulp/puppet/localforge/system/releases/f/ft/ft-methode_api-${project.version}.tar.gz" 2>/tmp/ft-methode_api.log| tail -1)
    
done


end_time=`date +%s`
total_time=`expr $end_time - $start_time`
echo "Publish to pulp succeeded. Took $total_time seconds"
echo ""
exit 0
