End to end testing
==================

# Given

* I am on the <page-name> page
* I am logged in as the <user-type> user

# When

* I log in as the <user-type> user
* I go from the <origin> page to the <destination> page
* I fill the form with the following data (see new-request.feature for an example)
* I click on the object with id '<id>' (the <id> is the css id of the object)

# Then

* I should see a <message-type> message
* I should be on the <page-name> page

# General information

## Currently implemented placeholders

* <page-name>: login, requests, lab requests, forgot password
* <user-type>: palga, requester, invalid
* <origin> <destination> pairs: login to forgot password, requests to create new request

## Comments

If you start a line with a `#`, the line will be ignored. You can use this to write comments.