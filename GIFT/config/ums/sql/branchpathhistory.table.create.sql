/** 
The new table 'BranchPathHistory' was added for GIFT 2017-1.  If the table doesn't exist in your current UMS db this command
will be used to create the table appropriately 
*/
	
create table APP.branchpathhistory (
	pathId_PK integer not null,
	courseId_PK varchar(255) not null,
	experimentId_PK varchar(255) not null,
	branchId_PK integer not null,
	actualCnt integer not null,
	cnt integer not null,
	primary key (pathId_PK, courseId_PK, experimentId_PK, branchId_PK)
);