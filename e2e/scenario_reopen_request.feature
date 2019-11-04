#Complete process for request for excerpts, PA reports and materials

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
    blockMaterialsRequest
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

  Scenario: 1a reopen request by Palga
    Given I am logged in as the palga user
    # And I am on the requests page
    When I claim the request with title 'Request 1'
    And I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I click on the 'Reopen' button
    And I click on the 'OK' button
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'Open, not yet submitted'

  Scenario: 1b resubmit request by requesterAllowed
    Given I am logged in as the requester user
    And I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I click on the object with id 'submit-new-request'
    And I click on the 'OK' button
    And I go to the 'requests' page
    Then request 'Request 1' should be in the list of requests
    And request 'Request 1' should have status 'Received by PALGA advisor'

  Scenario: 2. Claim and send requests to Scientific council
    Given I am logged in as the palga user
    # And I am on the requests page
    When I claim the request with title 'Request 1'
    And I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I select dropdown option '(Other)'
    And I fill the field with name 'requestTypeOther' with contents 'Test request type'
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

  Scenario: 3b vote for request
    Given I am logged in as the scientific council user
    # And I am on the requests page
    When I click on the request with title 'Request 1'
    And I fill the form with the following data
      """
      commentText: ok
      """
    And I click on the 'Add comment' button
    And I click on the object with id 'vote-accepted'
    And I go to the 'requests' page
    Then request 'Request 1' should have vote 'Accepted'

  Scenario: 3c reopen request by Palga
    Given I am logged in as the palga user
    # And I am on the requests page
    When I claim the request with title 'Request 1'
    And I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I click on the 'Reopen' button
    And I click on the 'OK' button
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'Open, not yet submitted'

  Scenario: 3d resubmit request by requester
    Given I am logged in as the requester user
    And I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I click on the object with id 'submit-new-request'
    And I click on the 'OK' button
    And I go to the 'requests' page
    Then request 'Request 1' should be in the list of requests
    And request 'Request 1' should have status 'Received by PALGA advisor'

  Scenario: 3e resubmit for approval by scientific council
    Given I am logged in as the palga user
    # And I am on the requests page
    When I claim the request with title 'Request 1'
    And I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I click on the 'Submit to scientific council' button
    And I click on the 'OK' button
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'Waiting for approval'

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
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'Data delivered, select excerpts'

