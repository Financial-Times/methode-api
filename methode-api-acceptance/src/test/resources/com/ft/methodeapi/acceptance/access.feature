@Access
Feature: Requests for articles

  Background: 
    Given the MethodeAPI service is running and connected to Methode

@Smoke
  Scenario: An Article can be successfully retrieved
    Given an article exists in Methode
    When I attempt to access the article
    Then the article should be available from the MethodeAPI
    And the article should have the expected metadata
    And the article should have the expected content
	And the article should have the expected workflow status

  Scenario: An article that doesn't exist is not found
    Given an article does not exist in Methode
    When I attempt to access the non-existent article
    Then the article should not be available from the MethodeAPI

@Performance
  Scenario: Should return within 2s 99% of the time
    Given an article exists in Methode
    When 10 users access the article a total of 500 times
    Then it is returned within 2000ms at least 99% of the time
