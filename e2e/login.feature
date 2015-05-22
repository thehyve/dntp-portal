Feature: Log in
  As a user
  I want to log in
  So that I can access restricted content

  Scenario: Correct log in
    Given I am on the login page
    When I log in as the palga user
    Then I should be on the requests page

  Scenario: Incorrect log in
    Given I am on the login page
    When I log in with invalid credentials
    Then I should see a log in error
