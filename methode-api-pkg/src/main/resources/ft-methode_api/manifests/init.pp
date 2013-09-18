# Class: content_store_api_read
#
# This module manages content_store_api_read
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
class content_store_api_read {

    class { 'nagios::client': }
    class { 'hosts::export': hostname => "$certname" }

    class { 'runnablejar':
        service_name => 'content_store_api_read',
        service_description => 'Content Store API Read',
        jar_name => 'content-store-api-read-${project.version}.jar',
        config_file_content => template('content_store_api_read/config.yml.erb');
    }
}
