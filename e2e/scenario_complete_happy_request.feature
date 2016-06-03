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
	Then the page should contain the text 'Language: en'

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
    address1: dreef
    postalcode: 1234
    city: Amsterdam
    billingEmail: fin@f.f
    telephone: 1234567890
    chargeNumber: 1234
    researchNumber: 10    
    """
    And I click on the following objects
    """
    previousContactYes
    radio-excerpts-PA-materials
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
    And I click on the 'Finish' button
    And I click on the 'OK' button
    When I upload the file 'test-excerptlist.csv' to the element with id 'test-upload-excerpt-list'
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'Data delivery and selection'

  Scenario: 5a check receipt of excerpt lists
    Given I am on the requests page
    And I am logged in as the requester user
    When I click on the request with title 'Request 1'
    Then an excerpt should be attached to the request

  Scenario: 6 select PA numbers
   Given I am logged in as the requester user
   # And I am on the requests page
   When I click on the request with title 'Request 1'
   And I scroll to the bottom of the page
   And testing is paused to wait a bit
   And I click on the object with id 'select-pa-numbers'
   # We should be able to do it automatically... But it doesn't work, so we do it manually
   And testing is paused to select all PA numbers of the current request
   And I click on the 'Submit selection' button
   And I enter the text 'no remarks'
   And I click on the 'OK' button
   Then I should see 0 lab requests in the list

  Scenario: 6a Palga approves selection
   Given I am logged in as the palga user
   # And I am on the requests page
   When I click on the request with title 'Request 1'
   And I scroll to the bottom of the page
   And I click on the 'Approve selection' button
   And I click on the 'OK' button
   And I go to the 'lab requests' page
   Then I should see 4 lab requests in the list

   Scenario: 7 accept request as lab user
    Given I am logged in as the lab 106 user
    # And I am on the lab requests page
    And I click on the lab request with id 'YYYY-1-106'
    And I claim the current request
    And I click on the 'Actions' button
    And I click on the 'Approve' button
    And I click on the 'OK' button
    Then the current request should have 'Approved' status
    Then the page should contain the text 'PA reports have NOT been sent to the requester.'

  Scenario: 8 reject request as a lab user
    Given I am logged in as the lab 104 user
    # And I am on the lab requests page
    When I click on the lab request with id 'YYYY-1-104'
    And I claim the current request
    And I click on the 'Actions' button
    And I click on the 'Reject' button
    And I enter the text 'none'
    And I click on the 'OK' button
    Then the current request should have 'Rejected' status

  Scenario: 9 palga user sees approval and rejection of requests
    Given I am logged in as the palga user
    When I go from the requests page to the lab requests page
    Then the page should contain the text 'Approved'
    And the page should contain the text 'Rejected'

  Scenario: 10 requester can comment on lab request and see its status
    Given I am logged in as the requester user
    When I go from the requests page to the lab requests page
    And I click on the lab request with id 'YYYY-1-106'
    And I click on the 'Notes' button
    And I fill the form with the following data
    """
    noteText: test note
    """
    And I click on the 'Add note' button
    And I go to the 'lab requests' page
    Then I should see 4 lab requests in the list
    And the page should contain the text 'Approved'
    And the page should contain the text 'Rejected'

  Scenario: 11 lab user can download PA list (manual check)
    Given I am logged in as the lab 106 user
    # And I am on the lab requests page
    When I click on the lab request with id 'YYYY-1-106'
    And testing is paused to download the PA list and check its contents
    Then the scenario should always succeed

  Scenario: 12 lab user can mark PA numbers as sent
    Given I am logged in as the lab 106 user
    # And I am on the lab requests page
    When I click on the lab request with id 'YYYY-1-106'
    And I click on the 'Actions' button
    And I click on the object with id 'paReportsSent'
    And I click on the 'Update PA reports status' button
    Then the page should contain the text 'Approved'
    And the page should contain the text 'PA reports have been sent to the requester.'

  Scenario: 13 samples are visible for the lab user and can be filtered
    Given I am logged in as the lab 106 user
    When I go from the lab requests page to the samples page
    Then the page should contain the text 'T23-45678'
    And the page should contain the text 'T34-56789'
    When I fill the form with the following data
    """
    request-search: T23
    """
    Then the page should contain the text 'T23-45678'
    And the page should not contain the text 'T34-56789'

  Scenario: 14 lab user can register which samples will be sent
    Given I am logged in as the lab 106 user
    # And I am on the lab requests page
    When I click on the lab request with id 'YYYY-1-106'
    And testing is paused to enter the sample codes manually
    Then the scenario should always succeed

  Scenario: 15 lab user can see which samples will be sent from the samples view
    Given I am logged in as the lab 106 user
    When I go from the lab requests page to the samples page
    And testing is paused to check that the sample codes are present
    Then the scenario should always succeed

  Scenario: 16 lab user can register that samples have been sent
    Given I am logged in as the lab 106 user
    # And I am on the lab requests page
    When I click on the lab request with id 'YYYY-1-106'
    And I click on the 'Actions' button
    And I click on the 'Send materials' button
    And I click on the 'OK' button
    Then the current request should have 'Materials sent' status

  Scenario: 17 palga is able to see that one of the lab requests has been sent
    Given I am logged in as the palga user
    When I go from the requests page to the lab requests page
    Then the page should contain the text 'Materials sent'

  Scenario: 18 requester can register that samples have been received
    Given I am logged in as the requester user
    When I go from the requests page to the lab requests page
    And I click on the lab request with id 'YYYY-1-106'
    And I click on the 'Actions' button
    And I click on the object with id 'samplesMissing'
    And I fill the form with the following data
    """
    missingSamples: 1, 2, 3
    """
    And I click on the 'Materials received' button
    And I click on the 'OK' button
    Then the current request should have 'Received' status

  Scenario: 19 palga is able to see that one of the lab requests has been received
    Given I am logged in as the palga user
    When I go from the requests page to the lab requests page
    Then the page should contain the text 'Received'

  Scenario: 20 recall a sample as a lab user (manually)
    Given I am logged in as the lab 106 user
    When I go from the lab requests page to the samples page
    And testing is paused to check that the samples can be recalled (email)
    Then the scenario should always succeed

  Scenario: 21 requester can register samples as returning
    Given I am logged in as the requester user
    When I go from the requests page to the lab requests page
    And I click on the lab request with id 'YYYY-1-106'
    And I click on the 'Actions' button
    And I click on the 'Return materials' button
    And I click on the 'OK' button
    Then the current request should have 'Sent in return' status

  Scenario: 22 palga is able to see one of the lab requests as returning
    Given I am logged in as the palga user
    When I go from the requests page to the lab requests page
    Then the page should contain the text 'Sent in return'

  Scenario: 23 lab user can register samples as returned
   Given I am logged in as the lab 106 user
   # And I am on the lab requests page
   When I click on the lab request with id 'YYYY-1-106'
   And I click on the 'Actions' button
   And I click on the object with id 'samplesMissing'
   And I fill the form with the following data
   """
   missingSamples: 1, 2, 3
   """
   And I click on the 'Materials returned' button
   And I am on the lab requests page
   Then the page should contain the text 'Completed'

  Scenario: 24 palga is able to see Request 1 as completed
   Given I am logged in as the palga user
   # And I am on the requests page
   Then the page should contain the text 'Closed'
