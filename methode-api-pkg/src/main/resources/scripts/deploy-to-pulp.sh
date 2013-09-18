#!/bin/bash

MODULEFULLPATH="${project.build.directory}/ft-methode_api"

/usr/bin/pulp-admin login -u admin -p admin

/usr/local/bin/puppet module build "$MODULEFULLPATH"
/usr/bin/pulp-admin puppet repo uploads upload --repo-id localforge --f "$MODULEFULLPATH/pkg/ft-methode_api-${project.version}.tar.gz"
/usr/bin/pulp-admin puppet repo publish run --repo-id=localforge
