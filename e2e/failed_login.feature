Feature: Failed login
  As a user
  I want to be able to recover my password

  Background:
    Given I am logged out

  Scenario: Try to login 10 times
    Given I am on the login page
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test1
    """
    And I click on the 'Login' button
    Then the page should contain the text 'Bad credentials'
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test2
    """
    And I click on the 'Login' button
    Then the page should contain the text 'Bad credentials'
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test3
    """
    And I click on the 'Login' button
    Then the page should contain the text 'Bad credentials'
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test4
    """
    And I click on the 'Login' button
    Then the page should contain the text 'Bad credentials'
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test5
    """
    And I click on the 'Login' button
    Then the page should contain the text 'Bad credentials'
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test6
    """
    And I click on the 'Login' button
    Then the page should contain the text 'Bad credentials'
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test7
    """
    And I click on the 'Login' button
    Then the page should contain the text 'Bad credentials'
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test8
    """
    And I click on the 'Login' button
    Then the page should contain the text 'Bad credentials'
    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test9
    """
    And I click on the 'Login' button

    And I fill the form with the following data
    """
    username: test+palga@dntp.thehyve.nl
    password: test10
    """
    And I click on the 'Login' button
    Then the page should contain the text 'User account blocked because of too many failed login attempts. Please retry in an hour.'