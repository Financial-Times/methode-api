class methode_api::monitoring::nagios {

  notify {"Running the nagios.pp ":}

  @@nagios_service { 'check_http__app01-methode-api-pr-uk.content-platform.int.cloud.ft.com_ftlnx01829-lvpr-uk-int_8081_/healthcheck_OK_200_1_3.0_3.0':
  ensure              => 'present',
  action_url          => 'https://sites.google.com/a/ft.com/dynamic-publishing-team/home/methode-api',
  check_command       => 'check_http!ftlnx01829-lvpr-uk-int!8081!/healthcheck!OK!200!1!3.0!3.0',
  host_name           => 'app01-methode-api-pr-uk.content-platform.int.cloud.ft.com',
  max_check_attempts  => '5',
  notes               => 'Severity 1 \n Service unavailable \n Methode Api healthchecks are failing consistently. Please check http://ftlnx01829-lvpr-uk-int:8081/healthcheck \n\n',
  notes_url           => 'https://sites.google.com/a/ft.com/ftplatform/5-monitoring/d-action-guidence',
  service_description => 'HTTP: ftlnx01829-lvpr-uk-int:8081/healthcheck:OK:200:1:3.0:3.0 ',
  target              => '/etc/nagios/nagios_service.cfg',
  use                 => 'generic-service',
}

}

