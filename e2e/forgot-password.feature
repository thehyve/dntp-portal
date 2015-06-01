Feature: Forgot Password
  As a user
  I want to be able to recover my password
  
  Scenario: Forgot Password is reachable from the login page
    Given I am on the login page
    When I go from the login page to the forgot password page
    Then I should be on the forgot password page

  Scenario: Forgot password shows success message for any email
    Given I am on the forgot password page
    When I fill the form with the following data
    """
    email: email@example.com
    """
    And I click on the object with id 'submit-forgot-password'
    Then I should see a success message
