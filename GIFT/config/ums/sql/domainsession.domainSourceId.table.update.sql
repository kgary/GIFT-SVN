/** 
The 'domainSourceId' column in 'DomainSession' was added for GIFT 2022-1.  If the column doesn't exist in your current UMS db this command
will be used to add the column to the table the table appropriately 
*/
	
alter table App.domainsession add column domainSourceId varchar(255) not null default 'legacy';
