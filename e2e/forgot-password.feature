Feature: Forgot Password
  As a user
  I want to recover my password
  So I can recover access to my account

  Scenario: Forgot Password is reachable from the login page
    Given I am on the login page
    When I click on "Forgot Password" from the login page
    Then I should be on the forgot password page

  Scenario: Forgot password shows success message for any email
    Given I am on the forgot password page
    When I submit any email
    Then I should see a success message
