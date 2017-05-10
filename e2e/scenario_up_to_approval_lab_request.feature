#Request for excerpts + PA reports + materials + clinical data, selection of excerpts by palga user

Feature: Scenario up to approval lab request by lab user
  Background:
    Given I am logged out

  Scenario: 0. Select language
    Given I am on the login page
    And I click on the object with id 'language_selection'
    And I click on the object with id 'select_language_en'
    Then the object with class 'selected-language-en' should be present

  Scenario: 1. Create request
    Given there are no requests
	#all requests deleted here
    And I am logged in as the requester user
    When I go from the requests page to the create new request page
    And I fill the form with the following data
    """
    contactPersonName: Dr. P. Investigator
    contactPersonEmail: test+contactperson@dntp.thehyve.nl
    pathologistName: Dr. A. Pathologist
    pathologistEmail: test+pathologist@dntp.thehyve.nl
    requestTitle: Request 2
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
    excerptsRequest
    paReportRequest
    materialsRequest
    clinicalDataRequest
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
    And request 'Request 2' should be in the list of requests
    And request 'Request 2' should have status 'Received by PALGA advisor'

  Scenario: 2. Claim and send requests to Scientific council
    Given I am logged in as the palga user
    # And I am on the requests page
    When I claim the request with title 'Request 2'
    And I click on the request with title 'Request 2'
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
    Then request 'Request 2' should have status 'Waiting for approval'
    # And email is send to scientific council, check manually!

  Scenario: 4a attach excerpt list
    Given I am logged in as the palga user
    When I click on the request with title 'Request 2'
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
    And I go to the 'requests' page
    Then request 'Request 2' should have status 'Data delivered, select excerpts'

  Scenario: 5a check receipt of excerpt lists
    Given I am on the requests page
    And I am logged in as the requester user
    When I click on the request with title 'Request 2'
    Then an excerpt should be attached to the request

  Scenario: 6 select all PA numbers by Palga user
   Given I am logged in as the palga user
   # And I am on the requests page
   When I click on the request with title 'Request 2'
   And I scroll to the bottom of the page
   And testing is paused to wait a bit
   And I click on the object with id 'select_all_excerpts'
   And I click on the 'Approve selection' button
   And I click on the 'OK' button
   And I go to the 'lab requests' page
   Then I should see 4 lab requests in the list

  