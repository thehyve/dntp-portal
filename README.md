# Request portal for Dutch pathology labs

[![Build Status](https://travis-ci.org/thehyve/dntp-portal.svg?branch=dev)](https://travis-ci.org/thehyve/dntp-portal/branches)

This repository hosts the code of a request portal, built for the
[Dutch National Tissuebank Portal](http://www.dntp.nl) project.
The portal is hosted at [aanvraag.palga.nl](https://aanvraag.palga.nl).
It allows researchers to submit requests to [PALGA](http://www.palga.nl),
the Dutch pathology database organisation.

## Issues
Project members can report issues in [JIRA](https://jira.thehyve.nl/projects/DNTPSD).

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
There should now be a `.war`-file in `target/dntp-portal-<version>.war`.
```bash
# run the packaged application
java -jar target/dntp-portal-<version>.war
```


### Tests

Run all tests

```bash
# run the testNG test suite
mvn -Dspring.profiles.active=dev test
```

Running front-end unit testing and e2e testing:
[Get `nodejs`](https://nodejs.org/en/download/).
```bash
# install dependencies
sudo npm install -g gulp protractor
# run unit testings
gulp test
# run e2e tests
protractor
```

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
### Fetch from repository
```
mvn dependency:get -Dartifact=nl.thehyve:dntp-portal:<version>:war -DremoteRepositories=https://repo.thehyve.nl/content/repositories/releases/ -Ddestination=dntpportal.jar
```

## Release notes

### 0.0.55
Set `date_submitted` for existing requests:
```sql
alter table request_properties add column date_submitted timestamp;

update request_properties set date_submitted =
(select t.start_time_
	from act_hi_taskinst t
	where request_properties.process_instance_id = t.proc_inst_id_
	and t.task_def_key_ = 'palga_request_review'
	limit 1
)
where date_submitted is null;
```

### 0.0.53
Modify these column definitions in an existing database:
```sql
# Add columns
alter table lab_request_comments add column comments_order int4 not null default -1;
alter table request_properties_comments add column comments_order int4 not null default -1;
alter table request_properties_approval_comments add column approval_comments_order int4 not null default -1;

# For lab request comments:

# Check if there are existing records that need to be updated
select * from lab_request_comments join comment on lab_request_comments.comments_id = comment.id where comments_order = -1 order by time_created;

# Same for request comments:
select * from request_properties_comments join comment on request_properties_comments.comments_id = comment.id where comments_order = -1 order by time_created;

# Same for request approval comments:
select * from request_properties_approval_comments join comment on request_properties_approval_comments.approval_comments_id = comment.id where approval_comments_order = -1 order by time_created;
```
To update the tables, an [update script](scripts/update_comment_tables) is available.

### 0.0.48
Modify these column definitions in an existing database:
```sql
alter table request_properties alter search_criteria type varchar(10000);
alter table request_properties alter laboratory_techniques type varchar(10000);
alter table request_properties alter privacy_committee_rationale type varchar(10000);
```

### 0.0.46
Add these new column definitions to an existing database:
```sql
alter table excerpt_list add column palga_patient_nr_column int4 not null default -1;
alter table excerpt_list add column palga_excerpt_nr_column int4 not null default -1;
alter table excerpt_list add column palga_excerpt_id_column int4 not null default -1;
```
Update existing `pathology_item` records to have the sequence number from the
excerpt list:
```sql
update pathology_item
set sequence_number = (select e.sequence_number
    from excerpt_entry e
    join excerpt_list l on l.id = e.excerpt_list_id
    join lab_request r on r.process_instance_id = l.process_instance_id
    join pathology_item i on i.pa_number = e.pa_number and i.lab_request_id = r.id
    where i.id = pathology_item.id
    limit 1);
```

### 0.0.42
When updating to version 0.0.42, an existing database can be updated with:
```sql
alter table lab add hub_assistance_enabled boolean default true;
```
### 0.0.5
When updating from 0.0.4 to 0.0.5, an existing database can be updated with:
```sql
alter table excerpt_entry add selected boolean;
```

## License
Copyright &copy; 2016  Stichting PALGA

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the [GNU Affero General Public License](agpl-3.0.txt)
along with this program. If not, see https://www.gnu.org/licenses/.
