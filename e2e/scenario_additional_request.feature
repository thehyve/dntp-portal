
Feature: scenario additional request
  Background:
    Given I am logged out

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
    biobankRequestNumber: request_1
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
    paReportRequest
    materialsRequest
    linkageWithPersonalDataYes
    informedConsentNo
    """
    And I fill the form with the following data
    """
    previousContactDescription: none
    linkageWithPersonalDataNotes: notes
    reasonUsingPersonalData: reason
    """
    #And I upload the file 'test-attachment.txt' to the element with id 'test-upload-attachment1'
    #And I upload the file 'test-attachment.txt' to the element with id 'test-upload-attachment2'
    And I click on the object with id 'submit-new-request'
    And I click on the 'OK' button
    Then I should be on the requests page
    And request 'Request 1' should be in the list of requests
    And request 'Request 1' should have status 'Received by PALGA advisor'

  Scenario: 2. Claim and send requests to Scientific council
    Given I am logged in as the palga user
    # And I am on the requests page
    When I claim the request with title 'Request 1'
    And I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I click on the following objects
      """
      requesterValid
      requesterLabValid
      requesterAllowed
      contactPersonAllowed
      agreementReached
      """
    And I click on the 'Submit to scientific council' button
    And I click on the 'OK' button
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'Waiting for approval'
    # And email is send to scientific council, check manually!

  Scenario: 4a attach excerpt list
    Given I am logged in as the palga user
    When I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I click on the following objects
      """
      radio-ppc_handled_according_mandate
      scientificCouncilApproved
      privacyCommitteeApproved
      """
    And I click on the 'Finish submission process' button
    And I click on the 'OK' button
    When I upload the file 'test-excerptlist.csv' to the element with id 'test-upload-excerpt-list'
    And testing is paused to wait a bit
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'Data delivered, select excerpts'

  Scenario: 6 Select PA numbers
   Given I am logged in as the palga user
   When I click on the request with title 'Request 1'
   And I scroll to the bottom of the page
   And testing is paused to wait a bit
   And I click on the object with id 'select_all_excerpts'
   Then the current request should have 'Selection received' status

  Scenario: 6a Palga approves selection
   Given I am logged in as the palga user
   # And I am on the requests page
   When I click on the request with title 'Request 1'
   And I scroll to the bottom of the page
   And I click on the 'Approve selection' button
   And I click on the 'OK' button
   And I go to the 'lab requests' page
   And testing is paused to wait a bit
   Then I should see 4 lab requests in the list

  Scenario: 7 create additional request
    Given I am logged in as the palga user
    When I click on the request with title 'Request 1'
    And I click on the object with id 'create_additional_request'
    And I click on the 'OK' button
    # I am now on the page of the new request
    Then I should see a link to the request with id 'YYYY-1'

  Scenario: 8a additional request contains link to parent
    Given I am logged in as the palga user
    Then I should see a link to the request with id 'YYYY-1'
    And I should see a link to the request with id 'YYYY-1-A1'

  Scenario: 8b parent contains link to additional request
    Given I am logged in as the palga user
    When I click on the request with id 'YYYY-1'
    Then I should see a link to the request with id 'YYYY-1-A1'

  Scenario: 8c additional request contains link to parent
    Given I am logged in as the palga user
    When I click on the request with id 'YYYY-1-A1'
    Then I should see a link to the request with id 'YYYY-1'
	
  Scenario: 8d additional request contains previously entered data
    Given I am logged in as the palga user
    #When I claim the request with id 'YYYY-1-A1'
	And I click on the request with id 'YYYY-1-A1'
	#And I click on the 'Edit' button
	And testing is paused to wait a bit
	And the static form contains
    """
    Principal investigator: Dr. P. Investigator
    Principal investigator email: test+contactperson@dntp.thehyve.nl
	Pathologist: Dr. A. Pathologist
    Pathologist email: test+pathologist@dntp.thehyve.nl
    Title: Request 1
    Background: None
    Research question: test
    Hypothesis: theory
    Methods: Modern methods
    Search criteria: methods + test + modern
    Study period: 2015--2016
    Laboratory techniques: Cucumber, protractor
    Charge number: 1234
    Grant provider: Some sponsor
    Grant number: 10
    Biobank request number: request_1
    Data linkage: Linkage with own patients or cohort or linkage between registries.
    Data linkage information: notes
    Explanation why linkage is allowed without informed consent: reason
    """
	
  Scenario: 8e change all fields in additional request
    Given I am logged in as the palga user
	When I claim the request with id 'YYYY-1-A1'
    And I claim the request with id 'YYYY-1-A1'
    And I click on the request with id 'YYYY-1-A1'
	And I click on the 'Edit' button
	And testing is paused to wait a bit
    And I fill the form with the following data
    """
    contactPersonName: Dr. P.I
    contactPersonEmail: test+contact@dntp.thehyve.nl
    pathologistName: Dr. A. Pat
    pathologistEmail: test+pat@dntp.thehyve.nl
    requestTitle: Test_HP1
    background: Test
    researchQuestion: test1a
    hypothesis: theory 1a
    methods: Modern methods  1a
    searchCriteria: methods + test + modern
    studyPeriod: 2015--2017
    biobankRequestNumber: request_1a
    laboratoryTechniques: Cucumber, protractor  1a
    address1: straat
    postalcode: 12
    city: Amsterdam
    billingEmail: financial@f.f
    telephone: 111111111
    chargeNumber: 12
    grantProvider: Some sponsor 1a
    researchNumber: 101
	"""
	And I click on the object with id 'button-skip-approval'
    And I click on the 'OK' button
    Then I should be on the requests page
    And request 'Test_HP1' should be in the list of requests
    #And request 'Test_HP1' should have status
	And testing is paused to wait a bit

	#Scenario: 9a. Claim and send requests to Scientific council for additional request
    #Given I am logged in as the palga user
    #When I claim the request with title 'Request 1a'
    #And I click on the request with title 'Request 1a'
    #And I click on the 'Edit' button
    #And I click on the 'Submit to scientific council' button
    #And I click on the 'OK' button
    #And I go to the 'requests' page
    #Then request 'Request 1a' should have status 'Waiting for approval'
    #And testing is paused to check email scientific council
	
  Scenario: 9b attach excerpt list for additional request
    Given I am logged in as the palga user
    When I click on the request with title 'Request 1a'
    And I click on the 'Edit' button
    And I click on the following objects
      """
      #radio-ppc_handled_according_mandate
      scientificCouncilApproved
      #privacyCommitteeApproved
      """
    And I click on the 'Finish submission process' button
    And I click on the 'OK' button
    When I upload the file 'test-excerptlist.csv' to the element with id 'test-upload-excerpt-list'
    And testing is paused to wait a bit
    And I go to the 'requests' page
    Then request 'Request 1a' should have status 'Data delivered, select excerpts'

  Scenario: 9c Select PA numbers for additional request
   Given I am logged in as the palga user
   When I click on the request with title 'Request 1a'
   And I scroll to the bottom of the page
   And testing is paused to wait a bit
   And I click on the object with id 'select_all_excerpts'
   Then the current request should have 'Selection received' status

  Scenario: 9d Palga approves selection for additional request
   Given I am logged in as the palga user
   # And I am on the requests page
   When I click on the request with title 'Request 1a'
   And I scroll to the bottom of the page
   And I click on the 'Approve selection' button
   And I click on the 'OK' button
   And I go to the 'lab requests' page
   And testing is paused to wait a bit
   Then I should see 8 lab requests in the list