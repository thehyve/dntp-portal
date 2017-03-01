Feature: Log in
  As a user
  I want to log in
  So that I can access restricted content
  
  Background:
    Given I am logged out

  Scenario: 0. Select language
    Given I am on the login page
    And I click on the object with id 'language_selection'
    And I click on the object with id 'select_language_en'
    Then the object with class 'selected-language-en' should be present
	And the page should contain the text '<p class="text-center text-info ng-binding">
        DNTP — Dutch National Tissuebank Portal
        &nbsp; | &nbsp;
        © 2017
        <a href="http://www.palga.nl/" target="_blank">Stichting PALGA</a>
        &nbsp; | &nbsp;
        <a href="https://github.com/thehyve/dntp-portal" target="_blank" class="ng-binding">Bron</a>
    </p>'
	
  Scenario: 1.Request page
    Given I am on the login page
    When I log in as the palga user
    Then I should be on the requests page
	And the page should contain the text '
<p class="text-center text-info ng-binding">
        DNTP — Dutch National Tissuebank Portal
        &nbsp; | &nbsp;
        © 2017
        <a href="http://www.palga.nl/" target="_blank">Stichting PALGA</a>
        &nbsp; | &nbsp;
        <a href="https://github.com/thehyve/dntp-portal" target="_blank" class="ng-binding">Bron</a>
    </p>'
    
  Scenario: 2.Lab request page
    Given I am on the login page
    When I log in as the lab user
    Then I should be on the lab requests page
	And the page should contain the text '<p class="text-center text-info ng-binding">
        DNTP — Dutch National Tissuebank Portal
        &nbsp; | &nbsp;
        © 2017
        <a href="http://www.palga.nl/" target="_blank">Stichting PALGA</a>
        &nbsp; | &nbsp;
        <a href="https://github.com/thehyve/dntp-portal" target="_blank" class="ng-binding">Bron</a>
    </p>'
	