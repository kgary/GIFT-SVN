/** 
The new column 'lastModified' was added to SurveyContext table for GIFT 2018-1.  If the column doesn't exist in your current UMS db this command
will be used to create the column appropriately 
*/	
ALTER TABLE APP.surveycontext
ADD lastModified timestamp;
