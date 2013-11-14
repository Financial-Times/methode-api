class methode_api::monitoring::nagios {
  $thisenv = hiera('lifecycle_environment','')
  nagios::nrpe_checks::check_http{
  "${::certname}/1":
    url           => "http://localhost/healthcheck",
    port          => "8081",
    expect        => 'can search for content',
    size          => 1,
    action_url    => 'https://sites.google.com/a/ft.com/technology/',
    notes         => "Methode API health";
}
#  "${::certname}/2":
#    url           => "http://localhost/second-URL",
#    port          => "8080",
#    expect        => '6',
#    size          => 1,
#    action_url    => 'https://sites.google.com/a/ft.com/technology',
#    notes         => "Second URL to check";
#  }

#  nagios::nrpe_checks::check_tcp{
#    "${::certname}\1":
#      host          => "methode-service name ",
#      port          => 8080,
#      notes         => "check if owlim master can reach ${::certname} on 8080";
#
#    "${::certname}/2":
#      host          => "${::certname}",
#      port          => 8089,
#      notes         => "check JMX port";
#  }

}

