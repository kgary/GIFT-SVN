/** 
The new table 'course' was added for GIFT 2017-1.  If the table doesn't exist in your current UMS db this command
will be used to create the table appropriately 
*/
	
create table APP.course (
	course_id varchar(255) not null,
	owner_name varchar(255) not null,
    course_path varchar(255) not null,
	
	primary key (course_id)
);

create INDEX course_path_idx on APP.course (course_path);