# Request portal for Dutch pathology labs

This repository hosts the code of a request portal, built for the [Dutch National Tissuebank Portal](http://www.dntp.nl) project.
The portal is hosted at [aanvraag.palga.nl](https://aanvraag.palga.nl). Its allows researchers to submit requests to [PALGA](http://www.palga.nl), the Dutch pathology database organisation.

## Project
Issues are reported to our [JIRA project](https://jira.thehyve.nl/browse/DNTP).

## Development 

### Technology
| Tool/framework | Documentation | 
| ---------------| ------------- |
| IDE | [Spring Tool Suite](https://spring.io/tools/sts) |
| Maven | [Maven](https://maven.apache.org/) |
| Web application framework | [Spring Boot](http://spring.io/guides/gs/spring-boot/) |
| Activiti business process modelling framework | [Activiti user guide](http://activiti.org/userguide/) |
| Javascript application framework | [AngularJS](https://docs.angularjs.org/guide) |
| NodeJS package manager | [npm](https://docs.npmjs.com/getting-started/installing-node) |

### Git repository
```
git clone git@github.com:thehyve/dntp-portal.git
cd dntp-portal
...
```

### Configure PostgreSQL database
```
sudo -u postgres psql
```
```sql
create user thehyve with password 'thehyve';
create database dntp_portal;
grant all privileges on database dntp_portal to thehyve;
```
Alternatively, edit `src/main/resources/application.properties` to change
the database settings.

Important for performance: setting the indexes appropriately, e.g.:
```sql
create index var_procinst_name_index on act_hi_varinst (proc_inst_id_, name_ );
create index var_task_name_index on act_hi_varinst (task_id_, name_ );
```

## Run, test, deploy

Make sure you have [npm](https://docs.npmjs.com/getting-started/installing-node) and Maven installed.

```bash
# run the application
mvn spring-boot:run
```
There should now be an application running at [http://localhost:8092/](http://localhost:8092/).


### Package
```bash
# create a war package
mvn package
```
There should now be a `.war`-file in `target/dntp-portal-&lt;version&gt;.war`.
```bash
# run the packaged application
java -jar target/dntp-portal-0.0.31-SNAPSHOT.war
```


### Tests

```bash
# run the testNG test suite
mvn -Dspring.profiles.active=dev test
```

Running front-end unit testing and e2e testing:
* `sudo npm install -g protractor` to install Protractor.
* `protractor` to run end to end tests with Protractor.

### Deployment
The project is configured to deploy to the [Nexus repository of The Hyve](https://repo.thehyve.nl/).
Credentials are stored in `~/.m2/settings.xml`:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                        http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>nl.thehyve.nexus</id>
            <username>USERNAME</username>
            <password>PASSWORD</password>
        </server>
    </servers>
</settings>
```
Deploy to the repository:
```
mvn -Dspring.profiles.active=dev deploy
```

## Release notes
When updating from 0.0.4 to 0.0.5, an existing database can be updated with:
```sql
alter table excerpt_entry add selected boolean;
```
