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
	And the page should contain the text '<a href="http://www.palga.nl/" target="_blank">Stichting PALGA</a>'
	