/** 
The new table 'LtiUserRecord' was added for GIFT 2017-1.  If the table doesn't exist in your current UMS db this command
will be used to create the table appropriately 
*/
	
create table APP.ltiuserrecord (
	consumerKey_PK varchar(255) not null,
	consumerId_PK varchar(255) not null,
	userId_FK integer not null,
	launchRequestTimestamp timestamp not null,
	primary key (consumerKey_PK, consumerId_PK),
	foreign key (userId_FK) references app.GIFTUser(userid_pk)
);