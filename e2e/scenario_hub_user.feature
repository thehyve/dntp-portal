#Complete process for request for excerpts, PA reports and materials

Feature: scenario request Request for excerpts + PA reports + materials + clinical data
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
    And testing is paused to wait a bit
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
    And I select dropdown option 'National request'
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
   Then I should see 4 lab requests in the list

  Scenario: 6b Hub user can see requests from associated labs
   Given I am logged in as the hub user
   Then I should see 2 lab requests in the list

  Scenario: 6c Hub user can access lab request
   Given I am logged in as the hub user
   When I click on the lab request with id 'YYYY-1-104'
   Then the current request should have 'Under review by lab' status

  Scenario: 7 accept request as lab user
    Given I am logged in as the lab 104 user
    # And I am on the lab requests page
    And I click on the lab request with id 'YYYY-1-104'
    And I claim the current request
    And I click on the 'Actions' button
    And I click on the 'Approve' button
    And I click on the 'OK' button
    And I click on the object with id 'hub-modal-save-button'
    Then the current request should have 'Approved' status
    Then the page should contain the text 'PA reports have NOT been sent to the requester.'

  Scenario: 8 hub user can comment on lab request and see its status
    Given I am logged in as the hub user
    When I go from the requests page to the lab requests page
    And I click on the lab request with id 'YYYY-1-104'
    And I click on the 'Notes' button
    And I fill the form with the following data
    """
    noteText: test note from hub user
    """
    And I click on the 'Add note' button
    And I go to the 'lab requests' page
    Then I should see 2 lab requests in the list
    And the page should contain the text 'Approved'
    And the page should contain the text 'Under review by lab'

  Scenario: 9 hub user can mark PA reports as sent
    Given I am logged in as the hub user
    # And I am on the lab requests page
    When I click on the lab request with id 'YYYY-1-104'
    And I claim the current request
    And I click on the 'Actions' button
    And I click on the object with id 'paReportsSent'
    And I click on the 'Update PA reports status' button
    Then the page should contain the text 'Approved'
    And the page should contain the text 'PA reports have been sent to the requester.'

  Scenario: 9 hub user can mark clinical data as sent
    Given I am logged in as the hub user
    # And I am on the lab requests page
    When I click on the lab request with id 'YYYY-1-104'
    And I click on the 'Actions' button
    And I click on the object with id 'clinicalDataSent'
    And I click on the 'Update clinical data status' button
    Then the page should contain the text 'Approved'
    And the page should contain the text 'Desired information for retrieving clinical data from treating physician was sent to the requester.'

  Scenario: 10 samples are visible for the hub user
    Given I am logged in as the hub user
    When I go from the lab requests page to the samples page
    Then the page should contain the text 'T12-34567'

  Scenario: 11 hub user can register that samples have been sent
    Given I am logged in as the hub user
    # And I am on the lab requests page
    When I click on the lab request with id 'YYYY-1-104'
    And I click on the 'Actions' button
    And I click on the 'Send materials' button
    And I click on the 'OK' button
    Then the current request should have 'Materials sent' status

  Scenario: 12 requester can register that samples have been received
    Given I am logged in as the requester user
    When I go from the requests page to the lab requests page
    And I click on the lab request with id 'YYYY-1-104'
    And I click on the 'Actions' button
    And I click on the object with id 'samplesMissing'
    And I fill the form with the following data
    """
    missingSamples: 1, 2, 3
    """
    And I click on the 'PA material received' button
    And I click on the 'OK' button
    Then the current request should have 'Received' status

  Scenario: 13 requester can register samples as returning
    Given I am logged in as the requester user
    When I go from the requests page to the lab requests page
    And I click on the lab request with id 'YYYY-1-104'
    And I click on the 'Actions' button
    And I click on the 'Return materials' button
    And I click on the 'OK' button
    Then the current request should have 'Sent in return' status

  Scenario: 14 hub user can register samples as returned
   Given I am logged in as the hub user
   # And I am on the lab requests page
   When I click on the lab request with id 'YYYY-1-104'
   And I click on the 'Actions' button
   And I click on the object with id 'samplesMissing'
   And I fill the form with the following data
   """
   missingSamples: 1, 2, 3
   """
   And I click on the 'PA material returned' button
   And I am on the lab requests page
   Then the page should contain the text 'Completed'
