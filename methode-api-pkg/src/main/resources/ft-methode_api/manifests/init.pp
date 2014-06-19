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

    $jar_name = 'methode-api-service.jar'
    $dir_heap_dumps = "/var/log/apps/methode-api-heap-dumps"

    class { 'nagios::client': }
    class { 'hosts::export': hostname => "$certname" }
    class { "${module_name}::monitoring": }

    runnablejar { "${module_name}_runnablejar":
        service_name        => "${module_name}",
        service_description => 'Methode API',
        jar_name            => "${jar_name}",
        artifact_location   => "${module_name}/methode-api-service.jar",
        config_file_content => template("${module_name}/config.yml.erb"),
        status_check_url    => "http://localhost:8081/ping";
    }

    file { "sysconfig":
        path    => "/etc/sysconfig/${module_name}",
        ensure  => 'present',
        content => template("${module_name}/sysconfig.erb"),
        mode    => 644;
    }

    File['sysconfig']
    -> Runnablejar["${module_name}_runnablejar"]
    -> Class["${module_name}::monitoring"]

    file { "heap-dumps-dir":
        path    => "${dir_heap_dumps}",
        owner   => "${module_name}",
        group   => "${methode_api}",
        ensure  => 'directory',
        mode    => 744;
    }
}
