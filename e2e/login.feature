Feature: Log in
  As a user
  I want to log in
  So that I can access restricted content
  
  Background:
    Given I am logged out

  Scenario: Correct log in
    Given I am on the login page
    When I log in as the palga user
    Then I should be on the requests page
    
  Scenario: Correct log in 2
    Given I am on the login page
    When I log in as the requester user
    Then I should be on the requests page

  Scenario: Incorrect log in
    Given I am on the login page
    When I log in as an invalid user
    Then I should see an error message
