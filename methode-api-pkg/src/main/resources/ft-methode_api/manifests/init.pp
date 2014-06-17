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
    class { 'methode_api::monitoring': }

    runnablejar { 'methode_api_runnablejar':
        service_name => 'methode_api',
        service_description => 'Methode API',
        jar_name => 'methode-api-service.jar',
        config_file_content => template('methode_api/config.yml.erb'),
        artifact_location => 'methode_api/methode-api-service.jar',
        status_check_url => "http://localhost:8081/ping";
    }

    Class [ 'nagios::client' ] ->
    Class [ 'methode_api::monitoring' ] ->
    Class [ 'hosts::export' ]->
    Runnablejar['methode_api_runnablejar']

    file { "heap-dumps-dir":
        path    => "/var/log/apps/methode-api-heap-dumps",
        owner   => 'methode_api',
        group   => 'methode_api',
        ensure  => 'directory',
        mode    => 744;
    }

    file { "sysconfig":
        path    => "/etc/sysconfig/methode_api",
        content => template('methode_api/sysconfig.erb'),
        mode    => 644;
    }
}

