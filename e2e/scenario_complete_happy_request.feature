#Complete process for request for excerpts, PA reports and materials

Feature: scenario request Request for excerpts + PA reports + materials
  Background:
    Given I am logged out
    # And request with title "Request 1" is deleted
    # And all lab-requests with title "Request 1" are deleted

  Scenario: 1. Create request
    Given I am logged in as the requester user
    And there are no requests
    And I am on the requests page
    When I go from the requests page to the create new request page
    And I fill the form with the following data
        """
        contactPersonName: Nobody
        requestTitle: Request 1
        background: None
        researchQuestion: test
        hypothesis: theory
        methods: Modern methods
        """
    And I click on the following objects
    """
    radio-excerpts-PA-materials
    linkageWithPersonalDataYes
    informedConsentNo
    """
    And I fill the form with the following data
    """
    linkageWithPersonalDataNotes: notes
    reasonUsingPersonalData: reason
    """
    And I click on the object with id 'submit-new-request'
    And I click on the 'OK' button
    Then I should be on the requests page
    And request 'Request 1' should be in the list of requests
    And request 'Request 1' should have status 'Review'

  Scenario: 2. Claim and send requests to Scientific council
    Given I am logged in as the palga user
    And I am on the requests page
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
    And I click on the 'Submit for approval' button
    And I click on the 'OK' button
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'Approval'
    # And email is send to scientific council, check manually!

  Scenario: 3b vote for request
    Given I am logged in as the scientific council user
    And I am on the requests page
    When I click on the request with title 'Request 1'
    And I fill the form with the following data
      """
      commentText: ok
      """
    And I click on the 'Add comment' button
    And I click on the object with id 'vote-accepted'
    And I go to the 'requests' page
    Then request 'Request 1' should have vote 'ACCEPTED'

  Scenario: 4a attach excerpt list
    Given I am logged in as the palga user
    When I claim the request with title 'Request 1'
    And I click on the request with title 'Request 1'
    And I click on the 'Edit' button
    And I click on the following objects
      """
      scientificCouncilApproved
      privacyCommitteeApproved
      """
    And I click on the 'Finalise' button
    And I click on the 'OK' button
    And I claim the current request
    And I upload the file 'test-excerptlist.csv' to the element with id 'test-upload-excerpt-list'
    And I go to the 'requests' page
    Then request 'Request 1' should have status 'DataDelivery'

  Scenario: 5a check receipt of excerpt lists
    Given I am logged in as the requester user
    And I am on the requests page
    When I click on the request with title 'Request 1'
    Then an excerpt should be attached to the request

  #Scenario: 6 select PA numbers
  #  Given I am logged in as the requester user
  #  And I am on the requests page
  #  When I click on the request with title 'Request 1'
  #  #The following command doesn't work:
  #  #And I click on the 'Select PA numbers' button
  #  #We use the following as a temporary work-around
  #  And I go to select PA numbers of the current request
  #  And I click on all 'Select' buttons
  #  And I click on the 'Submit selection' button
  #  And I fill the text box with the words 'please send all'
  #  And I click on the 'OK' button
  #  And I go to the 'lab requests' page
  #  Then I should see 2 links with title 'Request 1'
