/**
The new table DataCollectionPermission was added for GIFT 2019-1. If the table doesn't exist in your current UMS db this command
will be used to create the table appropriately 
*/

create table App.datacollectionpermission (
	dataCollectionId_FK varchar(255) not null,
	username varchar(255) not null,
	dataCollectionUserRole varchar(255) not null,
	primary key (dataCollectionId_FK, username),
	foreign key (dataCollectionId_FK) references App.experiment(experimentId_PK)
);