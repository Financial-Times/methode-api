# Class: methode_api
#
# This module manages methode_api
#
# Parameters:
#
# Actions:
#
# Requires:
#
# Sample Usage:
#
# [Remember: No empty lines between comments and class definition]
class methode_api {

    class { 'nagios::client': }
    class { 'hosts::export': hostname => "$certname" }

    class { 'runnablejar':
        service_name => 'methode_api',
        service_description => 'Methode API',
        jar_name => 'methode-api-service-${project.version}.jar',
        config_file_content => template('methode_api/config.yml.erb'),
        status_check_url => "http://localhost:8080/build-info";
    }
}
