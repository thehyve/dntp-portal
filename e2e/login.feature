Feature: Log in
  As a user
  I want to log in
  So that I can access restricted content
  
  Background:
    Given I am logged out

  Scenario: 0. Select language
    Given I am on the login page
    And I click on the object with id 'language_selection'
    And I click on the object with id 'select_language_en'
    Then the object with class 'selected-language-en' should be present
	
  Scenario: 1.Correct log in Palga
    Given I am on the login page
    When I log in as the palga user
    Then I should be on the requests page
	
  Scenario: 2.Correct log in requester
    Given I am on the login page
    When I log in as the requester user
    Then I should be on the requests page
    
  Scenario: 3.Correct log in scientific council
    Given I am on the login page
    When I log in as the scientific council user
    #Then the scenarioshould always succeed'
	Then I should be on the requests page

  Scenario: 4.Correct log in lab user
    Given I am on the login page
    When I log in as the lab user
    Then I should be on the lab requests page
	
  Scenario: 5.Correct log in hub user
    Given I am on the login page
    When I log in as the hub user
    Then I should be on the lab requests page

  Scenario: 6.Incorrect log in
    Given I am on the login page
    When I log in as an invalid user
    Then I should see an error message
	