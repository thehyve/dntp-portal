# Security requirements and testing plan

This page serves to document security
requirements and provides a security review checklist for web applications built with
Spring Boot (including Spring Security) and Angular.



## Application

<dl>
<dt>Application name</dt>
<dd>DNTP portal</dd>
<dt>Production URL</dt>
<dd>https://aanvraag.palga.nl</dd>
</dl>

### Review history

| Application version | Date           | Reviewer       |
|:------------------- |:-------------- |:-------------- |
| 0.0.94              | 2 October 2017 | Gijs Kant      |


## Security requirements

### Introduction

Spring Boot and Angular offer out-of-the-box protection against SQL injection attacks, cross-site scripting attacks, and more.
Spring Boot supports role-based permissions, such that, e.g., only administrators can add and edit user accounts.
On top of that, we have to implement data-driven access control: users should only be able to access and modify the requests that they own (have created)
or are assigned to.
We use an SSL certificate for securing the communication between user and server.

Possible vulnerabilities/attack scenario&rsquo;s, if the application is not properly secured, include:
- Someone gains access to the server and finds/accesses the database;
- Someone finds a vulnerability in Spring Boot/other libraries/our application and exploits it to get access to the data;
- Someone sets up a man-in-the-middle attack and can act to the system as the user whose connection is compromised;
- Someone acquires authentication credentials of a user (possibly the administrator) and can login and act as the user.

These and other vulnerabilities/attacks are within the scope of the OWASP Application Security Verification Standard (ASVS) standard.
Compliance with the standard should address all of these issues.

Still, a user may have exposed user credentials to an attacker (e.g., using the same password on a compromised system).
Mitigation of such attacks (e.g., using cryptocards for authentication) is out of scope for this system.
Included is, however, logging of user activity (enabling analysis of the damage when an intruder has been detected).

We aim at compliance with level 2 of the [OWASP Application Security Verification Standard (ASVS), 2016 version][owasp2016:asvs].
Level 2 of the standard contains security verification requirements on several categories:
authentication, session management, access control, malicious input handling, cryptography, error handling and logging,
data protection, communications, HTTP, malicious controls, business logic, files and resources,
web services, and configuration.

In the next section we summarise these requirements. In the section on [Measures](#chapter-measures),
we describe the measures taken to meet those requirements and list the known weaknesses of the system.



### Security guidelines and checklist

We aim at compliance with level 2 from the [OWASP Application Security Verification Standard (ASVS)][owasp2016:asvs]. We give a summary of the requirements below.
OWASP also provides a [security testing guide][owasp2013:testing] and
a [Top 10][owasp2013:top10] of security risks.

#### Application specific requirements

The client prioritises security of data over availability
of the application.

#### Summary of security requirements from ASVS, level 2

This list of requirements is presented as a checklist. The check box should be ticked if the requirement
is known to be covered by Spring, Angular or the application.
Labels or an explanation should added to each requirement to indicate how the requirement is satisfied.
The labels `Application`, `Spring`, `Angular`, `Apache`, `Annotations`, `Validator`, `JPA`, `TLS` `Puppet`, `BPM` refer to
components described in
the [implementation](#section-implementation) section.
If not ticked, an explanation should be added in the [open issues](#section-issues) section.

<dl>

<dt>V2: Authentication</dt>
<dd>Requirements:

- [x] Non-public pages require authentication (enforced server-side). `Annotations`
- [x] Strong passwords. `Validator`
- [x] No weaknesses in account update/creation functionality.
      _Specific routes for account editing._
- [x] Enable to securely update credentials. `TLS` `Application`
- [x] Logging of authentication decisions (for security investigations).
- [x] Secure password hashing and storage. `Spring`
- [x] No leaking of credentials. `Spring`
- [x] No harvesting of user accounts possible through login functionality.
      _Same response on failed login attempts for existing and non-existing accounts._
- [x] No default passwords active.
      _Part of deployment checklist._
- [ ] Blocking attacks that try multiple passwords and multiple accounts. `Application`
      _Implemented for trying multiple passwords, not for multiple accounts.
      Not covered: [V2.20, V15.1](#V2.20,V15.1)_.
- [x] &lsquo;Forgot password&rsquo; with expiring unique link, not sending an actual password. `Application`
- [x] No &lsquo;secret questions&rsquo;.
- [ ] Possibility to disallow a number of previously chosen passwords.
      _Not covered: [V2.25](#V2.25)_.
- [x] No pre-filling of credentials fields, allow the use of password managers, allow
      long passwords and passphrases. `Application`
- [ ] TOTP or other soft token, mobile push, or other
      offline recovery mechanism for account recovery.
      :warning: _Not implemented: [V2.22](#V2.22)._
- [ ] Re-authentication, two factor or transaction signing for high value transactions.
      _Not implemented: [V2.26](#V2.26)._
</dd>


<dt>V3: Session management</dt>
<dd>Requirements:

- [x] Proper session management (invalidation, timeout, new session id after login,
only accept framework generated sessions; session tokens long enough and securely generated,
limited cookie domain). `Spring`
- [x] Logout links. `Spring` `Application`
- [x] No leaking of the session id in URLs. `Spring`
- [x] `HttpOnly`. `Spring`
- [x] Secure properties for session cookies. `Spring`
- [ ] Disallow duplicate sessions for a user originating from different machines.
      Listing active sessions, ability to terminate sessions.
      _Not implemented: [V3.16&ndash;V3.18](#V3.16-V3.18)._
</dd>


<dt>V4: Access control</dt>
<dd>Requirements:

- [x] Allow users only to secured functions, pages, files that they are authorised to use. `Annotations`
- [x] Allow users only to directly access and manipulate objects (with id reference)
for which they have permission. `Annotations`
- [x] Access controls fail securely. `Annotations`
- [x] Directory browsing disabled.
- [x] Verify consistency of client-side and server-side access control.
  all access control enforced server-side. `Application` `Annotations`
- [x] Access control rules and data not changeable by users (e.g., changing ownership of data). `Application` `Annotations`
- [x] Logging of access control decisions. `Annotations`
- [x] Strong random tokens against Cross-site Request Forgery (CSRF/XSRF). `Spring`
- [x] Aggregate access control protection (V4.17), e.g., scraping by generating
a large sequence of requests (a possibly authenticated user looking for security holes). `mod_security`
</dd>


<dt>V5: Malicious input handling</dt>
<dd>Requirements:

- [x] No buffer overflow vulnerabilities. `Spring` `Application`
      _Risk mitigated by checking health of libraries: [V5.1](#V5.1)._
- [x] UTF8 specified for input. `Spring`
- [x] Input validation on server-side. Failure rejects input. `Spring` `Application`
- [x] Protection agains injection attacks (SQL, OS). `Spring` `JPA`
- [x] Escape untrusted client data. `Angular`
- [x] Protect sensitive fields (e.g., role, passwords) from automatic binding. `Spring`
      _Discussion: [V5.16&ndash;V5.17](#V5.16-V5.17)._
- [x] Protection against parameter pollution attacks. `Spring`.
      _Discussion: [V5.16&ndash;V5.17](#V5.16-V5.17)._
</dd>


<dt>V7: Cryptography at rest</dt>
<dd>

Requirements:

- [ ] Cryptography performed server-side. _(N/A)_
- [x] Cryptography functions fail securely.
- [x] Access to master secret protected. _SSL private key is only accessible by Apache_.
- [x] Random numbers generated for security (session id, password hashing,
XSRF tokens) generated by secure cryptographic functions. `Spring`
- [x] Policy for managing cryptographic keys (creating, distributing, revoking, expiry).
- [ ] Personally Identifiable Information should be encrypted at rest.
      _Not implemented: [V7.12](#7.12)_.
- [ ] Sensitive fields zeroed.
      _Not implemented: [V7.13, V9.11](#V7.13,V9.11)_.
- [x] Credentials (re)set at installation. `Puppet`
      _Configurable, set by Puppet._
</dd>


<dt>V8: Error handling and logging</dt>
<dd>Requirements:

- [x] No logging of sensitive data, session id, personal details, credentials, stack traces.
- [x] Logging implemented server-side, only on trusted devices.
- [x] Error handling logic should deny access by default (e.g., throw exception which is not caught). `Spring`
- [x] Time synchronisation on devices that write logs. `Puppet` _NTP active by default._
</dd>


<dt>V9: Data protection</dt>

<dd>Requirements:

- [x] No client side caching, local storage, autocomplete features on sensitive fields. `Spring` `Application` `Angular`
- [x] Sensitive data in body, not in URL parameters. `Application`
- [x] `no-cache` and `no-store` Cache-Control headers for sensitive data.
  _Discussion: [V9.4](#V9.4)._ `Spring`
- [x] Protection of cached data server side. `Application`
- [x] Accessing sensitive data is logged. `Application`. _Logging aspect._
- [ ] Sensitive data zeroed.
  _Not implemented: [V7.13, V9.11](#V7.13,V9.11)._
</dd>


<dt>V10: Communications security</dt>
<dd>Requirements:

- [x] Valid SSL certificate chain.
  _[V10.1, V10.3](#V10.1,V10.3)._ `Puppet`
- [x] Use SSL for all connections.
  _[V10.1, V10.3](#V10.1,V10.3)._ `Puppet`
- [x] SSL connection failures are logged. `Apache`
  _Logged by SSL proxy._
- [ ] Connections to external systems are authenticated.
  _(N/A)_
- [ ] Connections to external systems use an account with minimum privileges.
  _(N/A)_
- [x] Proper TLS configuration (HSTS, strong algorithms and cyphers, etc.).
  _Check by SSL Labs in the deployment checklist._
</dd>


<dt>V11: HTTP security configuration</dt>
<dd>Requirements:

- [x] Only accept defined set of request methods. `Spring` `mod_security`.
- [x] Response headers contain content type header with safe character set. `Spring`
- [x] Use `X-Frame-Options` and `X-XSS-Protection: 1; mode=block` headers to prevent clickjacking
      and cross-site scripting.
- [x] Do not expose system information in HTTP headers.
- [x] Proper `X-Content-Type-Options: nosniff` or `Content-Disposition` headers. `Spring`
- [x] Content security policy (CSPv2) set. `Spring` `Application`
</dd>


<dt>V13: Malicious controls</dt>
<dd>Requirements:

- [x] Verify that the application source code, and as
      many third party libraries as possible, does not
      contain back doors, Easter eggs, and logic flaws in
      authentication, access control, input validation,
      and the business logic of high value transactions.
      `Annotations` `BPM` `Spring` `Application`
      _Discussion: [V13.2](#13.2)._
</dd>


<dt>V15: Business logic</dt>
<dd>Requirements:

- [x] Do not allow spoofing of actions as another user, e.g., hijacking another
user&rsquo;s session, or spoofing a user id. `Spring`
- [x] Detection of and protection against brute force and denial of service attacks.
  `mod_security`
- [x] Process steps in correct order. `BPM`
- [ ] Process steps taken in realistic human time.
      _Won&rsquo;t verify, would require different setup for automated testing.)._
</dd>


<dt>V16: Files and resources</dt>
<dd>Requirements:

- [ ] URL redirects do not include unvalidated data. _(N/A)._
- [x] Canonise file names and paths. _Check if there is file upload or download functionality._
- [ ] Files from untrusted sources scanned by anti-virus scanner.
      _Won&rsquo;t implement: [V16.3](#V16.3)._
- [x] No untrusted parameters used in lookup of file locations;
- [x] Parameters from untrusted sources canonised, input validated, output encoded
      to prevent file inclusion attacks. `Spring` `Angular`
- [x] CORS headers properly set. `Spring`
- [x] Files from untrusted sources (uploads) stored outside webroot;
- [x] By default access to remote resources or systems is denied by the application.
- [x] The application does not execute data from untrusted sources.
- [x] Use web standards.
</dd>


<dt>V18: Web services</dt>
<dd>Requirements:

- [x] Same encoding style in server and client. `Spring` `Angular`
- [x] Management functions of the web service secured.
- [x] XML and JSON validation in place. `Spring`, `Jackson`
- [x] Session based authentication and authorisation. No static `API keys`.
  _Discussion: [V18.6](#V18.6)._
- [x] REST service protected from CSRF. `Spring` :warning:
  _Discussion: [V18.7](#V18.7)._
- [x] Service checks expected content type. `Spring`
- [x] Use TLS encryption on the connection. `TLS`
</dd>


<dt>V19: Configuration</dt>
<dd>Requirements:

- [x] Up-to-date security configurations and versions.
- [ ] Communication between components encrypted when on different systems. _(N/A)_
- [ ] Least privileged account used when connecting to other components.
  _Database is updated by the application at startup, which requires a quite privileged account._
  _However, Hibernate is configured to only update, not drop tables._
- [x] Isolate / sandbox / containerise components where possible to prevent attacks to target other
  applications on the same system.
- [x] Secure build and deployment processes. _Full test suite and library check included in deployment script._
</dd>
</dl>



## <a id="chapter-measures"></a> Security measures

Most requirements are met by the built-in security of the Spring and Angular
frameworks and by our server configuration.
In the [implementation](#section-implementation) section we list the security measures that have been
taken and cover most of the requirements.
In the [open issues](#section-issues) section we list the open issues that need to be evaluated.
In the section on [testing](#section-testing) we list the tests that are being performed on
code and deployment.
The section on [known weaknesses](#section-knownproblems) lists the known vulnerabilities.


### <a id="section-implementation"></a> Implementation and frameworks

The measures that have been taken to secure the application:

- Spring security for authentication and authorization;
- Spring and Angular prevent Cross-site request forgery (CSRF);
- Access to functionality is protected using Spring routing rules;
- Access to data in controller logic and using security annotations.
- Using JPA derived queries in the repository classes
(SQL queries are derived from interface method names), preventing SQL injection;
- Input sanitisation by Spring (and Angular);
- Custom password strength validator.



For securing the infrastructure:

- Application is run in user space;
- Apache proxy in between, application not directly approachable;
- Only ports 22, 80, and 443 open from the outside (firewall rules);
- On port 80, Apache should only forward to https (rewrite rule,
status 301 (Moved permanently));
- `mod_security` has been added to Apache;
- Access log by Apache, logging all incoming requests (URIs, not data),
`mod_security` and by the application.



### <a id="section-libraries"></a> Guidelines for choosing libraries

Guidelines for choice of 3rd party libraries:
- Open source
- Active development (no abandoned projects);
- Open community: issue tracker, forum or chat channels available
  where there is active response to questions;
- Preferably widely adopted and recommended as best practice;


List of back end libraries in use:
- The [Spring framework](https://projects.spring.io/spring-framework/),
  [Spring Boot](https://projects.spring.io/spring-boot/),
  [Spring Cloud](https://projects.spring.io/spring-cloud/),
  [Spring Security](https://projects.spring.io/spring-security/) and
  [Spring Data](http://projects.spring.io/spring-data/).
- The [Jackson](https://github.com/FasterXML/jackson) serialisation libraries.
- PostgreSQL
- [H2](https://github.com/h2database/h2database), the Java SQL database (used for testing).
- Hibernate
- The [Activiti](https://github.com/Activiti/Activiti) business process model engine.
- Groovy and Scala for scripting in Activiti.
- Apache Tomcat JDBC
- OpenCSV (the recent `com.opencsv` version)



### <a id="section-issues"></a> Open issues

There is a list of requirements for which it is not clear if they are met and
they are not included in the checks in the next section.

<dl>
<dt><a id="V2.20,V15.1"></a>V2.20, V15.1</dt>
<dd>

> Blocking attacks that try multiple passwords and multiple accounts.
  Detection of and protection against brute force and denial of service attacks.

Trying multiple passwords will result in blocking; trying multiple accounts not, but there won&rsquo;t be many accounts, so this is OK.
Rate limiting can be used as mitigation and is supported by Spring Cloud, but is not configured yet.
</dd>


<dl>
<dt><a id="V2.22"></a>V2.22</dt>
<dd>

> TOTP or other soft token, mobile push, or other offline recovery mechanism for account recovery.

Recovery by a random expiring link instead.
</dd>


<dt><a id="V2.25"></a>V2.25</dt>
<dd>

> Possibility to disallow a number of previously chosen passwords.

Actually, this requirement does not add security.
</dd>


<dl>
<dt><a id="V2.26"></a>V2.26</dt>
<dd>

> Re-authentication, two factor or transaction signing for high value transactions.

:warning: Not implemented.
An alternative approach could be a limited timeout for sessions, requiring
regular re-authentication.
Not implemented yet!
</dd>


<dt><a id="V3.16-V3.18"></a>V3.16 &ndash; V3.18</dt>
<dd>

> Disallow duplicate sessions for a user originating from different machines.
  Listing active sessions, ability to terminate sessions.

:warning: Won&rsquo;t implement disallowing of duplicate sessions, as it would limit usability without clear security benefit.
Listing of active sessions is not implemented.
An alternative approach could be a limited timeout for sessions, requiring
regular re-authentication.
Not implemented yet!
</dd>



<dt><a id="V4.17"></a>V4.17</dt>
<dd>

> Aggregate access control protection, e.g., scraping by generating
a large sequence of requests (a possibly authenticated user looking for security holes).

Too many failed requests will trigger a rule in `mod_security` to respond with `Service unavailable`.
</dd>


<dt><a id="V5.1"></a>V5.1</dt>
<dd>

> No buffer overflow vulnerabilities.

This requirement is difficult to test. We mitigate this risk primarily by checking
the security bulletins of 3rd party libraries and checking libraries for maturity and
active community. See the section on [choosing libraries](#section-libraries).
</dd>


<dt><a id="V5.16-V5.17"></a>V5.16 &ndash; V5.17</dt>
<dd>

> Protect sensitive fields (e.g., role, passwords) from automatic binding.
  Protection against parameter pollution attacks.

We follow the Spring Boot best practices for specifying controllers and binding to
input parameters. All controller logic has a specific HTTP action associated with it,
the parameters are all listed and typed.
We use representation object (simple Java classes) instead of domain classes (entities
that represent the objects in the database) in controller actions.
Sensitive fields are excluded from these representation classes, except for the actions
that are specific for updating sensitive data. E.g., a password field is only included
in the registration and password update representation class and not in the account
update representation class.
</dd>


<dt><a id="V7.12"></a>V7.12</dt>
<dd>

> Personally Identifiable Information should be encrypted at rest.

Encryption at rest is not applied. This would prevent attacks on data through physical
access to the hardware that stores the data. That same hardware has the sensitive data
in memory.

We rely on a trusted hosting provider instead, which is out of scope of the application security requirements.
</dd>


<dt><a id="V7.13,V9.11"></a>V7.13, V9.11</dt>
<dd>

> Sensitive fields zeroed.

New requirement (since 3.0.1). Not yet implemented, would need considerate effort to review and change
code dealing with sensitive data.
</dd>


<dt><a id="V9.4"></a>V9.4</dt>
<dd>

> `no-cache` and `no-store` `Cache-Control` headers for sensitive data.

These cache control properties are being set by Spring by default.
</dd>


<dt><a id="V9.4"></a>V9.4</dt>
<dd>

> SSL connection failures are logged.

These are logged by Apache and `mod_security`.
</dd>


<dt><a id="V10.1,V10.3"></a>V10.1, V10.3</dt>
<dd>

> Valid SSL certificate chain.
  Use SSL for all connections.

Our Puppet configuration applies SSL certifications this way by means of an Apache
instance that is properly configured.
</dd>



<dt><a id="V13.2"></a>V13.2</dt>
<dd>

> Verify that the application source code, and as
      many third party libraries as possible, does not
      contain back doors, Easter eggs, and logic flaws in
      authentication, access control, input validation,
      and the business logic of high value transactions.

There is a guideline for [choosing libraries](#section-libraries) that are created by
an active community.
High value transactions have been written as business process models (BPM) and are
executed by a BPM engine, which strictly enforces the business logic.
Using Spring annotations, we implemented many input validation rules.
Access control and authentication are implemented using Spring Security and
custom access control annotations and aspects that have been extensively tested.
The application is testes extensively by unit test, integration tests and user interface tests.
</dd>


<dt><a id="V16.3"></a>V16.3</dt>
<dd>

> Files from untrusted sources scanned by anti-virus scanner.

Won&rsquo;t implement, this is a client side concern.
</dd>


<dt><a id="V16.5"></a>V16.5</dt>
<dd>

> Parameters from untrusted sources canonised, input validated, output encoded
to prevent file inclusion attacks.

Input validation is provided by Spring, Jackson and the application. Safe encoding
of input and output (e.g., escaping of tags) is done by Angular.
</dd>
</dl>



<dt><a id="V18.6"></a>V18.6</dt>
<dd>

> Session based authentication and authorisation. No static &lsquo;API keys&rsquo;.

We use the built-in authentication and session management of Spring Security
and built our own library of authorisation rules based on Spring Security.
</dd>



<dt><a id="V18.7"></a>V18.7</dt>
<dd>

> REST service protected from CSRF.

E.g., origin checks, csrf nonces, referrer checks.



### <a id="section-testing"></a> Security testing

#### Code checks

The analysis that has been done on code/application level:
- Security code review:
  - Proper routes, method annotation on all REST controllers?
  - How are user roles assigned? Who can change them? Should only be administrators.
  - No custom queries or all nicely prepared using prepared statements
(JPA provides this);
  - Check that user input is not used for:
determining filesystem paths, checking for access, determining user id or roles,
database key in a query. (A common error: checking access based on the _id_ in the url
(`@PathVariable`), but fetching the database object based on the _id_ in the request body.)
- Test injection:
  - Test for XSS attacks: try to insert HTML and Javascript in input fields;
this should be handled automagically by Spring.
- Regularly check <https://pivotal.io/security> for vulnerabilities in Spring.



#### Deployment checks

Disable dev/test behaviour on the production environment.
Checking this list is part of deployment to production.

- Default users disabled (e.g., `palga` &mdash; check startup log of application and user administration);
- No action `/test/clear/` available;
- Check account blocking period after _n_ failed login attempts.
- Check for SSL certificate, make sure key chain is secure and valid. Check with: <https://www.ssllabs.com/ssltest/>.
- Check that requests on port 80 are forwarded to `https`.
- Check that the database is not accessible (except from localhost) and passwords are properly set.
- Check that database password in the application configuration is not exposed (e.g., in `puppet`).
- Use scripts that checks if the current deployment uses the
newest versions of included frameworks:
    ```bash
    mvn versions:display-dependency-updates
    mvn versions:display-plugin-updates
    mvn dependency-check:check
    npm outdated
    bower list
    ```


### <a id="section-knownproblems"></a> Known weaknesses

- Information sent plain text by email: password recovery link.




## References

- [OWASP (2016), _Application Security Verification Standard 3.0.1_][owasp2016:asvs]
- [OWASP (2013), _Top 10 (2013)_][owasp2013:top10]
- [OWASP (2013), _Testing Guide 4.0_][owasp2013:testing]

[owasp2016:asvs]: https://www.owasp.org/images/3/33/OWASP_Application_Security_Verification_Standard_3.0.1.pdf (OWASP Application Security Verification Standard 3.0.1)
[owasp2013:top10]: https://www.owasp.org/images/f/f8/OWASP_Top_10_-_2013.pdf "OWASP Top 10 (2013)"
[owasp2013:testing]: https://www.owasp.org/images/1/19/OTGv4.pdf (OWASP Testing Guide 4.0)
[podium]: https://github.com/thehyve/podium (Podium request portal)
