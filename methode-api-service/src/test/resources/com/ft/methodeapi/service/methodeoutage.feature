@MethodeOutage
Feature: Methode outage

  Scenario: Should return 503 when Methode is down
    Given that Methode is down
    When I attempt to access an article
    Then I should get a service unavailable error
    
  Scenario: Should return 503 when Methode checked exception occurs
    Given that Methode throws an Exception
    When I attempt to access an article
    Then I should get a service unavailable error
  