# dntp-portal

## JIRA
- [DNTP project](https://jira.thehyve.nl/browse/DNTP)
- [Board](https://jira.thehyve.nl/secure/RapidBoard.jspa?rapidView=36)

## Links to external resources
| Tool/framework | Documentation | 
| ---------------| ------------- |
| IDE | [Spring Tool Suite](https://spring.io/tools/sts) |
| Web application framework | [Spring Boot](http://spring.io/guides/gs/spring-boot/) |
| Activiti business process modelling framework | [Activiti user guide](http://activiti.org/userguide/) |
| Javascript application framework | [AngularJS](https://docs.angularjs.org/guide) |
| Bower package manager | [Bower](http://bower.io/) |

## Instructions 
```
git clone git@github.com:thehyve/dntp-portal.git
cd dntp-portal
...
```

Configure PostgreSQL database:
```
sudo su - postgres
psql 
create user thehyve with password 'thehyve';
create database dntp_portal;
grant all privileges on database dntp_portal to thehyve;
```
or edit `src/main/resources/application.properties` to change
the database settings.

Make sure you have npm installed https://docs.npmjs.com/getting-started/installing-node

- Install tools
```
$ npm install 
```
- Install libs by running `bower` to install the required Javascript and CSS libraries:
```
$ bower install 
```

## Build with Maven

Make sure you have Maven installed, then run the application:
```
mvn spring-boot:run
```
There should now be an application running at [http://localhost:8092/](http://localhost:8092/).


Or create a `war`:
```
mvn package 
```
There should now be a `.war`-file in `target/dntp-portal-0.0.1-SNAPSHOT.war`.

## Run unit tests with Maven

```
mvn -Dspring.profiles.active=dev test
```

## Running front-end unit testing and e2e testing

* `gulp test` to launch your unit tests with Karma
* `gulp test:auto` to launch your unit tests with Karma in watch mode
* `gulp protractor` to launch your e2e tests with Protractor
* `gulp protractor:dist` to launch your e2e tests with Protractor on the dist files
