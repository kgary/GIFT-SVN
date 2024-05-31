/** 
The tables 'DataCollectionResultsLti' and 'GlobalUser' was added for GIFT 2017-1.  It modifies the experiment table to be a table to store any type of generic
data collection information.  Additionally, it creates a result table (similar to experiment subject data) that can be used for lti results.
*/

/** Modify the experiment table to have a generic type along with a reference to the source course. */
alter table App.experiment add column dataSetType varchar(255) default 'EXPERIMENT';
alter table App.experiment add column sourceCourseId varchar(255);

/** Create the datacollectionresults lti table (used to store data collection results for lti data set types). */
create table App.datacollectionresultslti (
    consumerKey_FK varchar(255) not null,
	consumerId_FK varchar(255) not null,
    startTime_PK timestamp,
    endTime timestamp,
    dataSetId_FK varchar(255) not null,
    messageLogFileName varchar(255),
	primary key (consumerKey_FK, consumerId_FK, startTime_PK),
	foreign key (consumerKey_FK, consumerId_FK) references App.ltiUserRecord(consumerKey_PK, consumerId_PK),
    foreign key (dataSetId_FK) references App.experiment(experimentId_PK)
);

/** The global user table is used to create a table of unique ids for users of all types (normal, lti) within this GIFT instance. */
create table App.globaluser (
    globalId_PK integer not null generated always as identity (start with 1, increment by 1),
    userType varchar(255) not null,
    primary key (globalId_PK)
);

/** Modify the lti userrecord table to add the global user id and remove the column referencing the old gift user table. Note that the 1=1 below is a dummy condition to delete all lti user records. */
delete from App.ltiuserrecord where 1=1;
alter table App.ltiuserrecord add column globalId_FK integer;
alter table App.ltiuserrecord add constraint globalid_foreign_key foreign key (globalId_FK) references App.globaluser(globalId_PK);
alter table App.ltiuserrecord drop column userId_FK;

/** Modify the course table to have a flag to show which records are deleted.  This is so that course UUIDs are always added to the table, but not removed so that the UUIDs are not somehow recycled. */
alter table App.course add column isDeleted boolean default false; 

/** Add a global id column in the domainsession table (similar to experiment users). */
alter table App.domainsession add column globalId_FK integer;
alter table App.domainsession add constraint ds_globalid_fk foreign key (globalId_FK) references App.globaluser(globalId_PK);

/** Add a global id column in the survey response table (similar to experiment users). */
alter table App.surveyresponse add column globalId_FK integer;
alter table App.surveyresponse add constraint sr_globalid_fk foreign key (globalId_FK) references App.globaluser(globalId_PK);

