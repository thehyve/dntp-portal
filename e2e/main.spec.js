'use strict';

describe('The main view', function () {
  var loginPage;
  var page;

  beforeEach(function () {
    //browser.get('http://localhost:3000/index.html');
    browser.get('http://localhost:8092/');
    loginPage = require('./login.po');
    loginPage.verify();
    loginPage.submit();
  });

  afterEach(function () {
    //TODO: logout here
  });

  /*
  it('should include jumbotron with correct data', function() {
    expect(page.h1El.getText()).toBe('\'Allo, \'Allo!');
    expect(page.imgEl.getAttribute('src')).toMatch(/assets\/images\/yeoman.png$/);
    expect(page.imgEl.getAttribute('alt')).toBe('I\'m Yeoman');
  });

  it('list more than 5 awesome things', function () {
    expect(page.thumbnailEls.count()).toBeGreaterThan(5);
  });
  */

  it('login works', function() {
      //TODO: verify post-login page components
      browser.sleep(5000);
  })

});
