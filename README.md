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

Important for performance: setting the indexes appropriately, e.g.:
```
create index var_procinst_name_index on act_hi_varinst (proc_inst_id_, name_ );
```

Run `bower` to install the required Javascript and CSS libraries:
```
bower install
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

## Release notes
When updating from 0.0.4 to 0.0.5, an existing database can be updated with:
```
alter table excerpt_entry add selected boolean;
```
