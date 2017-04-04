Feature: Log in
  As a user
  I want to log in
  So that I can access restricted content
  
  Background:
    Given I am logged out

  Scenario: 0. Select language and check for reference on login page
    Given I am on the login page
    And I click on the object with id 'language_selection'
    And I click on the object with id 'select_language_en'
    Then the object with class 'selected-language-en' should be present
	And the page should contain the text '<a href="http://www.palga.nl/" target="_blank">Stichting PALGA</a>'
	
  Scenario: 1. check on requests page
    Given I am logged in as the palga user
    And I am on the requests page
	Then the page should contain the text '<a href="http://www.palga.nl/" target="_blank">Stichting PALGA</a>'
	
  Scenario: 2. check on lab requests page
    Given I am logged in as the palga user
    And I am on the lab requests page
	Then the page should contain the text '<a href="http://www.palga.nl/" target="_blank">Stichting PALGA</a>'
	
  Scenario: 3. check on samples page
    Given I am logged in as the palga user
    And I am on the samples page
	Then the page should contain the text '<a href="http://www.palga.nl/" target="_blank">Stichting PALGA</a>'
	
	