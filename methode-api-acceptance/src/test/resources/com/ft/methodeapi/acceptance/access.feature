Feature: Access

  Background: 
    Given the MethodeAPI service is running and connected to Methode

  Scenario: An Article can be successfully retrieved
    Given an article exists in Methode
    When I attempt to access the article
    Then the article should be available from the MethodeAPI
    And the article should have the expected metadata
    And the article should have the expected content
