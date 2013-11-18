class methode_api::monitoring::nagios {
  $thisenv = hiera('lifecycle_environment','')
  nagios::nrpe_checks::check_http{
  "${::certname}/1":
    url           => "http://localhost/healthcheck",
    port          => "8081",
    expect        => 'can search for content',
    size          => 1,
    action_url    => 'https://sites.google.com/a/ft.com/dynamic-publishing-team/home/methode-api',
    notes         => 'Severity 1 \\n Service unavailable \\n Methode API healthchecks are failing consistently. Please check http://${::hostname}:8081/healthcheck \\n\\n',
    ctime	  => '3.0';
  }
}

