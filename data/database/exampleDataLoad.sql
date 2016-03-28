DELETE FROM ESB_MESSAGE_HEADER WHERE id > 0;
COMMIT;
DELETE FROM ESB_MESSAGE_SENSITIVE_INFO WHERE id > 0;
COMMIT;
DELETE FROM ESB_MESSAGE WHERE id > 0;

TRUNCATE TABLE METADATA;
INSERT INTO METADATA (id, name, parent_id, type, value) VALUES (1,'SearchKeys',-1,'SearchKeys','');
INSERT INTO METADATA (id, name, parent_id, type, value) VALUES (2,'Entities',-1,'Entities','');

insert into METADATA (id,name,parent_id,type,value) values (3,'Organization','2','Entity','ORGANIZATION');
insert into METADATA (id,name,parent_id,type,value) values (4,'Repository','2','Entity','REPOSITORY');
insert into METADATA (id,name,parent_id,type,value) values (5,'User','2','Entity','USER');

insert into METADATA (id,name,parent_id,type,value) values (6,'OrgManager',3,'System','ORGMANAGER');
insert into METADATA (id,name,parent_id,type,value) values (7,'RepoManager',4,'System','REPOMANAGER');
insert into METADATA (id,name,parent_id,type,value) values (8,'UserManager',5,'System','USERMANAGER');

insert into METADATA (id,name,parent_id,type,value) values (9,'Name','6','SearchKey','name');
insert into METADATA (id,name,parent_id,type,value) values (10,'Name','7','SearchKey','name');
insert into METADATA (id,name,parent_id,type,value) values (11,'Name','8','SearchKey','name');
insert into METADATA (id,name,parent_id,type,value) values (12,'name',9,'XPATH','/GitHubOrganization/OrganizationName/text()');
insert into METADATA (id,name,parent_id,type,value) values (13,'name',10,'XPATH','/GitHubRepository/RepositoryName/text()');
insert into METADATA (id,name,parent_id,type,value) values (14,'name',11,'XPATH','/GitHubUser/Name/text()');

insert into METADATA (id,name,parent_id,type,value) values (15,'Visibility','6','SearchKey','visibility');
insert into METADATA (id,name,parent_id,type,value) values (16,'Visibility','7','SearchKey','visibility');
insert into METADATA (id,name,parent_id,type,value) values (17,'visibility',15,'XPATH','/GitHubOrganization/Visibility/text()');
insert into METADATA (id,name,parent_id,type,value) values (18,'visibility',16,'XPATH','/GitHubRepository/Visibility/text()');

insert into METADATA (id,name,parent_id,type,value) values (19,'Time Zone','8','SearchKey','timezone');
insert into METADATA (id,name,parent_id,type,value) values (20,'timezone',19,'XPATH','/GitHubUser/TimeZone/text()');

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 1, 'GitHubOrgCreate', 'There was a problem syncing your GitHub organization', 'That organization already exsits', 'GITHUB_ORG_ERROR', 'GitHubSystem', 'DATA_ERROR', '8675309', 'JMS8675309', 'XML', 1, '<GitHubOrganization><OrganizationName>esbtools</OrganizationName><BillingEmail>admin@esbtools.org</BillingEmail><OrganizationsPlan>Open Source</OrganizationsPlan><Visibility>Public</Visibility></GitHubOrganization>', 'GitHub', 'Internet', 'GITHUB_ORG_CREATE', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 2, 'GitHubOrgSync', 'There was a problem syncing your GitHub repository', 'Unable to deliver this message ', 'GITHUB_REPO_ERROR', 'GitHubSystem', 'SYSTEM_ERROR', '16049311', 'JMS16049311', 'XML', 1, '<GitHubOrganization> <OrganizationName>lightblue-platform</OrganizationName> <BillingEmail>admin@lightblue.io</BillingEmail> <OrganizationsPlan>Open Source</OrganizationsPlan><Visibility>Public</Visibility></GitHubOrganization>', 'GitHub', 'Internet', 'GITHUB_REPO_SYNC', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 3, 'GitHubOrgReaper', 'There was a problem deleting your GitHub repository', 'Your repository could not be deleted, child projects still exist', 'GITHUB_REPO_ERROR', 'GitHubSystem', 'DATA_ERROR', '7184981043', 'JMS7184981043', 'XML', 1, '<GitHubOrganization> <OrganizationName>darcy-framework</OrganizationName> <BillingEmail>admin@darcyframework.org</BillingEmail> <OrganizationsPlan>Open Source</OrganizationsPlan><Visibility>Public</Visibility></GitHubOrganization>', 'GitHub', 'Internet', 'GITHUB_REPO_REAP', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 4, 'GitHubRepoCreator', 'There was a problem creating your GitHub repository', 'Your repository could not be created', 'GITHUB_REPO_ERROR', 'GitHubSystem', 'DATA_ERROR', '3016968699', 'JMS3016968699', 'XML', 1, '<GitHubOrganization> <OrganizationName>lightblue-platform</OrganizationName> <BillingEmail>admin@lightblue.io</BillingEmail> <OrganizationsPlan>Open Source</OrganizationsPlan> </GitHubOrganization>', 'GitHub', 'Internet', 'GITHUB_REPO_CREATE', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 5, 'GitHubRepoCreator', 'There was a problem creating your GitHub repository', 'Your repository could not be created', 'GITHUB_REPO_ERROR', 'GitHubSystem', 'DATA_ERROR', '16049311', 'JMS16049311', 'XML', 1, '<GitHubRespository> <Owner>esbtools</Owner> <RepositoryName>esbtools.github.io</RepositoryName> <Description>GitHub Pages project for esbtools</Description> <Visibility>Public</Visibility> <InitializeWithReadMe>true</InitializeWithReadMe> <GitIgnores> <GitIgnore> /target </GitIgnore> </GitIgnores> <License>GNU General Public License v3.0</License> </GitHubRespository>', 'GitHub', 'Internet', 'GITHUB_REPO_CREATE', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 6, 'GitHubRepoCreator', 'There was a problem creating your GitHub repository', 'Your repository could not be created', 'GITHUB_REPO_ERROR', 'GitHubSystem', 'DATA_ERROR', '8036482713', 'JMS8036482713', 'XML', 1, '<GitHubRespository> <Owner>lightblue-platform</Owner> <RepositoryName>lightblue</RepositoryName> <Description>Submodules repo that brings together all source for lightblue-platform in one place.</Description> <Visibility>Public</Visibility> <InitializeWithReadMe>true</InitializeWithReadMe> <GitIgnores> <GitIgnore> /target </GitIgnore> </GitIgnores> <License>GNU General Public License v3.0</License> </GitHubRespository>', 'GitHub', 'Internet', 'GITHUB_REPO_CREATE', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 7, 'GitHubRepoCreator', 'There was a problem creating your GitHub repository', 'Your repository could not be created', 'GITHUB_REPO_ERROR', 'GitHubSystem', 'DATA_ERROR', '3016984196', 'JMS3016984196', 'XML', 1, '<GitHubRespository> <Owner>lightblue-applications</Owner> <RepositoryName>lightblue</RepositoryName> <Description>Web applications for managing data in lightblue</Description> <Visibility>Public</Visibility> <InitializeWithReadMe>true</InitializeWithReadMe> <GitIgnores> <GitIgnore> /target </GitIgnore> </GitIgnores> <License>GNU General Public License v3.0</License> </GitHubRespository>', 'GitHub', 'Internet', 'GITHUB_REPO_CREATE', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 8, 'GitHubRepoCreator', 'There was a problem creating your GitHub repository', 'Your repository could not be created', 'GITHUB_REPO_ERROR', 'GitHubSystem', 'DATA_ERROR', '3016981973', 'JMS3016981973', 'XML', 1, '<GitHubRespository> <Owner>lightblue-client</Owner> <RepositoryName>lightblue</RepositoryName> <Description>Client for interacting with lightblue services</Description> <Visibility>Public</Visibility> <InitializeWithReadMe>true</InitializeWithReadMe> <GitIgnores> <GitIgnore> /target </GitIgnore> </GitIgnores> <License>GNU General Public License v3.0</License> </GitHubRespository>', 'GitHub', 'Internet', 'GITHUB_REPO_CREATE', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 9, 'GitHubRepoCreator', 'There was a problem creating your GitHub repository', 'Your repository could not be created', 'GITHUB_REPO_ERROR', 'GitHubSystem', 'DATA_ERROR', '8638157306', 'JMS8638157306', 'XML', 1, '<GitHubRespository> <Owner>darcy-framework</Owner> <RepositoryName>darcy-framework</RepositoryName> <Description>Collection of all darcy projects aggregated as git submodules.</Description> <Visibility>Public</Visibility> <InitializeWithReadMe>true</InitializeWithReadMe> <GitIgnores> <GitIgnore> /target </GitIgnore> </GitIgnores> <License>GNU General Public License v3.0</License> </GitHubRespository>', 'GitHub', 'Internet', 'GITHUB_REPO_CREATE', 'GitHub', CURRENT_TIMESTAMP);

INSERT INTO ESB_MESSAGE (id, error_component, error_details, error_message, error_queue, error_system, error_type, message_guid, jms_message_id, message_type, occurrence_count, payload, service_name, source_location, source_queue, source_system, jms_message_timestamp) VALUES ( 10, 'GitHubUserCreator', 'There was a problem creating your GitHub user', 'That user already exists', 'GITHUB_USER_ERROR', 'GitHubSystem', 'DATA_ERROR', '8636447490', 'JMS8636447490', 'XML', 1, '<GitHubUser> <Login>derek63</Login> <Name>Derek</Name> <Location>Berlin, Germany</Location> <TimeZone>GMT+1</TimeZone> </GitHubUser>', 'GitHub', 'Internet', 'GITHUB_USER_CREATE', 'GitHub', CURRENT_TIMESTAMP);

TRUNCATE TABLE ESB_MESSAGE_HEADER;

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'esbtools', 1);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'lightblue-platform', 2);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'darcy-framework', 3);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'esb-message-admin', 4);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'esbtools.github.io', 5);

INSERT INTO ESB_MESSAGE_HEADER ( name, type, value, message_id) VALUES ( 'name', 'METADATA', 'lightblue-platform', 6);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'lightblue-applications', 7);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'lightblue-client', 8);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'darcy-framework', 9);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'name', 'METADATA', 'Derek Haynes', 10);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 1);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 2);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 3);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 4);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 5);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 6);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 7);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 8);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'visibility', 'METADATA', 'Public', 9);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'timezone', 'METADATA', 'GMT+1', 10);

INSERT INTO ESB_MESSAGE_HEADER (name, type, value, message_id) VALUES ( 'resubmitControlHeaderTest', 'METADATA', 'someQueueName', 10);
