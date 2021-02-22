# Request portal for Dutch pathology labs

[![Build Status](https://travis-ci.org/thehyve/dntp-portal.svg?branch=dev)](https://travis-ci.org/thehyve/dntp-portal/branches)
[![codecov](https://codecov.io/gh/thehyve/dntp-portal/branch/dev/graph/badge.svg)](https://codecov.io/gh/thehyve/dntp-portal)

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

### Configure PostgreSQL database for development
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


## Run, test, publish

Make sure you have [npm](https://docs.npmjs.com/getting-started/installing-node) and Maven installed.

To run the application in production mode:
```bash
# Start the application in production mode
mvn -Dspring.profiles.active=prod spring-boot:run
```
There should now be an application running at [http://localhost:8092/](http://localhost:8092/).

Different profiles are available:

| Profile | Database      | Test data
|:------- |:------------- |:-----------------------
| `dev`   | H2, in memory | Default users and roles
| `test`  | PostgreSQL    | Default users and roles
| `prod`  | PostgreSQL    | None

To activate these profiles:

```bash
# Start the application with test accounts
mvn -Dspring.profiles.active=test spring-boot:run
# Start the application with an in-memory database
mvn -Dspring.profiles.active=dev spring-boot:run
```

For front-end development, you can start a hot-reloading version of the front-end
separately, after starting the application with `mvn spring-boot:run`:
```bash
# Start the front-end application
npm start
```
This should open the default browser at [http://localhost:9000/](http://localhost:9000/).

### Package
```bash
# Create a war package
mvn -Dspring.profiles.active=dev package
```
There should now be a `.war`-file in `target/dntp-portal-<version>.war`.
```bash
# Run the packaged application
java -jar target/dntp-portal-<version>.war
```

### Tests

Run all tests

```bash
# Run the testNG test suite
mvn -Dspring.profiles.active=dev test
```

Running front-end unit testing and e2e testing:
[Get `nodejs`](https://nodejs.org/en/download/).
```bash
# Start the application (in a separate console)
mvn -Dspring.profiles.active=dev spring-boot:run
# Install dependencies
npm install
# Run unit tests
npm test (currenty failing)
# Prepare webdriver for e2e tests
npx webdriver-manager update
# Run e2e tests
npx protractor
# Run only a selected feature
npx protractor --specs=e2e/scenario_complete_happy_request.feature
```

To select a particular version of the webdriver (instead of the latest), run:
```bash
# For chromium:
npx webdriver-manager update --versions.chrome=$(chromium-browser --version | cut -d ' ' -f 2)
# For Google Chrome:
npx webdriver-manager update --versions.chrome=$(google-chrome --version | cut -d ' ' -f 3)
```

### Publish
The project is configured to publish to the [Nexus repository of The Hyve](https://repo.thehyve.nl/).
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
Publish to the repository:
```
mvn -Dspring.profiles.active=dev deploy
```

### Fetch from repository
```
mvn dependency:get -Dartifact=nl.thehyve:dntp-portal:<version>:war -DremoteRepositories=https://repo.thehyve.nl/content/repositories/releases/ -Ddestination=dntpportal.jar
```


## Deployment

Instructions on how to set up a production instance of the application.

### Dependencies

- Java JRE 8
- A PostgreSQL server (version 9.5 or newer) listening on port 5432.
- An SMTP server, listening on port 25.
  - The email server must be allowed to send emails on behalf
    of the reply address in the application configuration.
    (check the DNS records of `aanvraag.palga.nl` and `palga.nl`)
- Package `haveged` installed.
- User `nobody` and group `nogroup`

### Setup database

```
sudo -u postgres psql
```
```sql
create user thehyve with password '<strong random db password>';
create database dntp_portal;
grant all privileges on database dntp_portal to thehyve;
```
The database schema will be created at application startup.

### Configure and run application

- Create directory `/home/dntp` with subdirectories (owned by `nobody`):
    - `logs`
    - `upload`
- Download the war file:
    ```bash
    curl -L -o dntp-portal.war https://repo.thehyve.nl/service/local/repositories/releases/content/nl/thehyve/dntp-portal/0.0.113/dntp-portal-0.0.113.war  
    ```
- Copy `dntp-portal.war` to `/home/dntp`.
- Create configuration file `/home/dntp/dntp.properties` (owned by `nobody`)
    ```properties
    # Database credentials
    spring.datasource.username=dntp_portal
    spring.datasource.password=<strong random db password>
    ```
- Create service `/etc/systemd/system/dntp.service`:

    ```editorconfig
    [Unit]
    Description=DNTP
    After=syslog.target network.target
    
    [Service]
    Type=simple
    ExecStart=/usr/bin/java -jar -server -Xms2g -Xmx2g -XX:MaxPermSize=512m -Djava.awt.headless=true -Dserver.port=8092 -Dspring.profiles.active=prod -Dspring.datasource.url=jdbc:postgresql://localhost/dntp_portal -Djava.security.egd=file:/dev/./urandom -Ddntp.server-name=aanvraag.palga.nl -Ddntp.server-port=443 -Ddntp.reply-address=aanvraag@palga.nl -Ddntp.from-address=aanvraag@palga.nl -Dspring.config.location=/home/dntp/dntp.properties /home/dntp/dntp-portal.war
    WorkingDirectory=/home/dntp
    User=nobody
    Group=nogroup
    UMask=0000
    Restart=always
    StandardOutput=journal

    [Install]
    WantedBy=multi-user.target
    ```
- Start the application:
    ```bash
    systemctl start dntp.service
    ```
- Check the application status and logs:
    ```bash
    # Check status
    systemctl status dntp.service
    # Inspect logs
    journalctl -u dntp.service -f
    ```
- Set up a reverse proxy with SSL listening on `aanvraag.palga.nl`
  with `http://localhost:8092` as target.
- Test that the application is available at https://aanvraag.palga.nl.
- Check the SSL configuration using the [SSL server test](https://ssllabs.com/ssltest).

#### Initial user
Create initial lab and Palga user (after the application has started):

- Create a dummy lab:
  ```sql
  INSERT INTO lab (id, active, name, number, hub_assistance_enabled) VALUES (1, true, 'Dummy', 0, false);
  ```
- Request a user account via the registration page.
- Find id of the user account:
  ```sql
  SELECT * FROM app_user;
  ```
- Find id of Palga role:
  ```sql
  SELECT id FROM role WHERE name = 'palga';
  ```
- Grant Palga role to user:
  ```sql
  UPDATE app_user_roles SET roles_id = <role_id> WHERE users_id = <user_id>;
  ```

#### Configure indexes
The following indexes are not automatically created,
but are important for the performance.
They can be created after the application has started.
```sql
create index var_procinst_name_index on act_hi_varinst (proc_inst_id_, name_ );
create index var_task_name_index on act_hi_varinst (task_id_, name_ );
```


## Release notes

### 0.0.107

User specialisms have been changed to be stored and exported in English.

```sql
update app_user set specialism = 'Gastroenterology' where specialism = 'Maag-darm-lever-ziekten';
update app_user set specialism = 'Gynaecology' where specialism = 'Gynaecologie';
update app_user set specialism = 'Dermatology' where specialism = 'Dermatologie';
update app_user set specialism = 'Medical Oncology' where specialism = 'Medische Oncologie';
update app_user set specialism = 'Internal Medicine' where specialism = 'Interne geneeskunde';
update app_user set specialism = 'Radiology' where specialism = 'Radiologie';
update app_user set specialism = 'Radiotherapy' where specialism = 'Radiotherapie';
update app_user set specialism = 'Haematology' where specialism = 'Hematologie';
update app_user set specialism = 'Throat-nose-ear' where specialism = 'Keel-neus-oor';
update app_user set specialism = 'Surgery' where specialism = 'Heelkunde';
update app_user set specialism = 'Epidemiology' where specialism = 'Epidemiologie';
update app_user set specialism = 'Primary care' where specialism = 'Eerstelijnsgeneeskunde';
update app_user set specialism = 'Cardiology' where specialism = 'Cardiologie';
update app_user set specialism = 'Pathology' where specialism = 'Pathologie';
update app_user set specialism = 'Lung Disease' where specialism = 'Longziekten';
update app_user set specialism = 'Urology' where specialism = 'Urologie';
update app_user set specialism = 'Neurology' where specialism = 'Neurologie';
update app_user set specialism = 'Endocrinology' where specialism = 'Endocrinologie';
```

### 0.0.106

New columns have been added to record the last assigned Palga adviser and
the request type, and to distinguish between different sorts of materials requests.
To update the database schema, run:

```sql
alter table request_properties add column last_assignee character varying(255);
alter table request_properties add column request_type character varying(255);
alter table request_properties add column block_materials_request boolean;
alter table request_properties add column he_slice_materials_request boolean;
alter table request_properties add column others_materials_request character varying(255);
```
Existing materials requests should be updated to select at least one of the sorts of materials request.
The following command selects both blocks and slides for existing materials requests: 
```sql
-- Update existing materials request to select blocks and slides
update request_properties
    set block_materials_request = true, he_slice_materials_request = true
    where process_instance_id in (
      select proc_inst_id_ from act_ru_variable where name_ = 'is_materials_request' and long_ = 1
    );
```

### 0.0.80
```sql
alter table lab_request add column return_date timestamp without time zone;
alter table lab_request add column sent_return_email boolean;

alter table request_properties add column biobank_request_number character varying(255);
alter table request_properties add column germline_mutation boolean;

CREATE TABLE request_properties_informed_consent_form_attachments (
    request_properties_id bigint NOT NULL,
    informed_consent_form_attachments_id bigint NOT NULL
);
ALTER TABLE request_properties_informed_consent_form_attachments OWNER TO thehyve;


ALTER TABLE ONLY request_properties_informed_consent_form_attachments
    ADD CONSTRAINT uk_46tqt7cfxfkb9p4coyjq4q6s4 UNIQUE (informed_consent_form_attachments_id);


ALTER TABLE ONLY request_properties_informed_consent_form_attachments
    ADD CONSTRAINT fk_46tqt7cfxfkb9p4coyjq4q6s4 FOREIGN KEY (informed_consent_form_attachments_id) REFERENCES file(id);

ALTER TABLE ONLY request_properties_informed_consent_form_attachments
    ADD CONSTRAINT fk_8i21ti5yf37152ttn4vljo275 FOREIGN KEY (request_properties_id) REFERENCES request_properties(id);
```


### 0.0.65
```sql
alter table lab_request alter reject_reason type varchar(10000);
```

### 0.0.60
```sql
alter table excerpt_list add column remark_column int4 not null default -1;
```

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
Copyright &copy; 2016&ndash;2021  Stichting PALGA

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
