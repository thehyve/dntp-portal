#Entering new request form

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

	Scenario: 1. Create request
    Given there are no requests
    And I am logged in as the requester user
    When I go from the requests page to the create new request page
    And I fill the form with the following data
    """
    contactPersonName: Dr. P. Investigator
    contactPersonEmail: test+contactperson@dntp.thehyve.nl
    pathologistName: Dr. A. Pathologist
    pathologistEmail: test+pathologist@dntp.thehyve.nl
    requestTitle: Request 1
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
	And testing is paused to wait a bit
	And I click on the following objects
	"""
	informedConsentYes
	germlineMutationYes
	"""
	And testing is paused to wait a bit
	And I upload the file 'test-attachment.txt' to the element with id 'button_upload_informed_consent_form'
	#And I fill the form with the following data
    #"""
    #previousContactDescription: none
    #"""
    #And I upload the file 'test-attachment.txt' to the element with id 'test-upload-attachment2'
    And testing is paused to wait a bit
	And I click on the object with id 'submit-new-request'
    And I click on the 'OK' button
    Then I should be on the requests page
    And request 'Request 1' should be in the list of requests
    And request 'Request 1' should have status 'Received by PALGA advisor'
	