#Entering new request form including upload IC form and changing form by palga user

Feature: scenario request Request for excerpts + PA reports + materials
  Background:
    Given I am logged out
    # And request with title "Request 1" is deleted
    # And all lab-requests with title "Request 1" are deleted

  Scenario: 0. Select language
    Given I am on the login page
    And I click on the object with id 'language_selection'
    And I click on the object with id 'select_language_en'
    Then the object with class 'selected-language-en' should be present

	Scenario: 1. Create request with IC form attached
    Given there are no requests
    And I am logged in as the requester user
    When I go from the requests page to the create new request page
    And I fill the form with the following data
    """
    contactPersonName: Dr. P. Investigator
    contactPersonEmail: test+contactperson@dntp.thehyve.nl
    pathologistName: Dr. A. Pathologist
    pathologistEmail: test+pathologist@dntp.thehyve.nl
    requestTitle: Request 1 IC
    background: None
    researchQuestion: test
    hypothesis: theory
    methods: Modern methods
    searchCriteria: methods + test + modern
    studyPeriod: 2015--2016
    biobankRequestNumber: bio_request_123
    laboratoryTechniques: Cucumber, protractor
    address1: dreef
    postalcode: 1234
    city: Amsterdam
    billingEmail: fin@f.f
    telephone: 1234567890
    chargeNumber: 1234
    grantProvider: Some sponsor
    researchNumber: 10
    """
    And I click on the following objects
    """
    previousContactYes
    statisticsRequestFalse
    excerptsRequest
    paReportRequest
    materialsRequest
    linkageWithPersonalDataYes
    """
    And I fill the form with the following data
    """
    previousContactDescription: none
    linkageWithPersonalDataNotes: notes
    """
	And I click on the following objects
	"""
	informedConsentYes
	germlineMutationYes
	"""
	And I upload the file 'test-attachment.txt' to the element with id 'test-upload-informed-consent-form'
	And I click on the object with id 'submit-new-request'
    And I click on the 'OK' button
    Then I should be on the requests page
    And request 'Request 1 IC' should be in the list of requests
    And request 'Request 1 IC' should have status 'Received by PALGA advisor'
	
  Scenario: 2.change IC form by palga user	
	Given I am logged in as the palga user
	When I claim the request with title 'Request 1 IC'
    And I click on the request with title 'Request 1 IC'
    And I click on the 'Edit' button
	And the page should contain the text 'test-attachment.txt'
	And testing is paused to remove previously uploaded IC form
	And I upload the file 'IC_form.txt' to the element with id 'test-upload-informed-consent-form'
	Then the page should contain the text 'IC_form.txt'
	And the page should not contain the text 'test-attachment.txt'
  