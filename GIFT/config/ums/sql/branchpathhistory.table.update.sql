/** 
The 'experimentId_PK' column in 'BranchPathHistory' was added for GIFT 2020-1.  If the column doesn't exist in your current UMS db this command
will be used to add the column to the table the table appropriately 
*/
	
alter table App.branchpathhistory add column experimentId_PK varchar(255) not null default 'N/A';
alter table App.branchpathhistory drop primary key;
alter table App.branchpathhistory add primary key (pathId_PK, courseId_PK, experimentId_PK, branchId_PK);
