
Feature: change addresses labs
  Background:
    Given I am logged out
    # And request with title "Request 1" is deleted
    # And all lab-requests with title "Request 1" are deleted

  Scenario: 0. Select language
    Given I am on the login page
    And I click on the object with id 'language_selection'
    And I click on the object with id 'select_language_en'
    Then the object with class 'selected-language-en' should be present

  Scenario: 1. Change address of lab 100
    Given I am logged in as the palga user
	And testing is paused to wait a bit
	When I am on the labs page
	And I open the 'Edit' form for 'AMC, afd. Pathologie'
	And I fill the form with the following data
	"""
	address1: lab100straat
	address2: 100
	postalcode: 100
	city: Amsterdam
	telephone: 0101001001
	"""
	
	Scenario: 2. Change address of lab 106
    Given I am logged in as the palga user
	And testing is paused to wait a bit
	When I am on the labs page
	And I open the 'Edit' form for 'Laboratorium voor Pathologie (PAL), Dordrecht'
	And I fill the form with the following data
	"""
	address1: lab106straat
	address2: 106
	postalcode: 106
	city: Dordrecht
	telephone: 0101001006
	"""