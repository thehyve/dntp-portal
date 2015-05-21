'use strict';

describe('The main view', function () {
  var loginPage;
  var navPage
  var page;

  beforeEach(function () {
    //browser.get('http://localhost:3000/index.html');
    browser.get('http://localhost:8092/');
    loginPage = require('./login.po');
    loginPage.verify();
    loginPage.submit();
    navPage = require('./nav.po');
  });

  afterEach(function () {
    navPage.verify();
    navPage.doLogout();
  });

  it('logs in properly', function() {
      //TODO: verify post-login page components
      browser.sleep(1000);
  })

  it('creates a new request', function() {
      //TODO: implement me
      browser.sleep(1000);
  })

});
