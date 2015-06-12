#Complete process for request for excerpts, PA reports and materials

Feature: scenario request Request for excerpts + PA reports + materials 
  Background:
    Given I am logged out
    # And request with title "Request 1" is deleted
    # And all lab-requests with title "Request 1" are deleted
    
  Scenario: 1. Create request 
    Given I am logged in as the requester user
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
    radio-excerpts-PA-reports-materials
    radio-data-link-own-data
    informed-consent-no
    """
    reason-pers-data: reason
    """
    And I click on the object with id 'submit-new-request'
    Then I should be on the requests page
    And request 'Request 1' should be in the list of requests
    And request 'Request 1' should have status 'Review'

  Scenario: 2. Claim and send requests to Scientific council
    Given I am logged in as the palga user
    And I am on the requests page
    When I claim the request with title 'Request 1'
    And I click on the request with title 'Request 1' 
    And I click on the following objects
      """
      requester-checked
      associated-lab
      allowed-request
      person-autorised
      agreement_reached
      """
    And I click on the object with id 'submit'
    Then request 'Request 1' should have status 'Approval'
    # And email is send to scientific council, check manually!

  Scenario: 3b vote for request
    Given I am logged in as the scientific council user
    And I am on the requests page
    When I click on the request with title 'Request 1' 
    And I click on the object with id 'vote-request-accepted'
    And I fill the form with the following data
      """
      comment: ok
      """
    And I click on the object with id 'vote-add-comment'
    And I am on the requests page
    Then request should have vote 'ACCEPTED'
  
 
  Scenario: 4a attach excerpt list
    Given I am logged in as the palga user
    And I have claimed the request with title 'Request 1'
    When I click on the request with title 'Request 1' 
    And I click on the following objects
      """
      scientific-com-approved 
      no-privacy-issue
      """
    # And I attach file <???> to request. Pause possible to insert this manually?
    And I click on the object with id 'finalise-request'
    Then status of request with title 'Request 1' is 'Data delivery'

#Scenario: 5a check receipt of excerpt lists
#Given I am logged in as the requester user
#And I am on the requests page
#When I click on the request with title 'Request 1'
#Then an attachment should be present

Scenario: 6 select PA numbers
    Given I am logged in as the requester user
    And I am on the requests page
    When I click on the request with title 'Request 1' 
    And I click on the object with id 'select-PA-numbers'
    And I select all PA-numbers
    And I click on the object with id 'submit-selection'
    And I fill the form with the following data
      """
      remarks: please send all
      """  
    And I click on the object with id 'submit-ok'
    And I am on the requests page
    And I click on the object with id 'lab-requests'
    And I am on the lab-requests page
    Then 2 requests with title 'Request 1' should be present
    
Scenario:7a1  Palga user can view lab requests 
    Given I am logged in as the palga user
    When I click on the object with id 'lab-requests'
    And I am on the lab-requests page
    Then 2 requests with title 'Request 1' should be present

Scenario:7a2  Palga user can download the PA list
    Given I am logged in as the palga user
    And I am on the lab-requests page
    When I click on the first request with title 'Request 1'
    And I click on the object with id 'download-PA-numbers'
    #Then ??

Scenario: 8 Claim and view request as lab user
    Given I am logged in as the lab user
    And I am on the requests page
    When I claim the request with title 'Request 1'
    And I click on the request with title 'Request 1'    
    