Feature: The requester should be able to create a new request and see an overview of them
  Background:
    Given I am logged in as the requester user

  Scenario: Request for numbers
    When I go from the requests page to the create new request page
    And I fill the form with the following data
      """
      contactPersonName: Nobody
      requestTitle: Request 1
      background: None
      researchQuestion: what is the secret of life?
      hypothesis: There is no secret
      methods: Modern methods
      """
    And I click on the object with id 'submit-new-request'
    Then I should be on the requests page
