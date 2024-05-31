/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.table.DbSurveyContext;
import mil.arl.gift.ums.db.table.DbSurveyContextSurvey;

/**
 * Utility class (containing only static methods) to help perform 'eager fetching' of the DbSurveyContext object.
 * The DbSurveyContext object contains getSurveyContextSurveys() which is LAZY fetch by default.  This was changed
 * to LAZY because it was found that the getSurveyContextSurveys() call was very expensive and could cause a significant
 * increase in UMS database memory in certain situations such as course validation.   By LAZY, this means when users
 * query the DbSurveyContext object from the database, the getSurveyContextSurveys() object is null, since this is an
 * expensive query in the database.  
 * 
 * If the caller needs the DbSurveyContext.getSurveyContextSurveys() object filled in, the following methods (getSurveyContextEager)
 * can be used to fill in the data from the database.  Note that this is a more expensive query and should be used only in places where
 * it is necessary to get the data.  The "Eager" term is used to match the hibernate term of fetching eager to signify that it is a
 * more expensive query.
 * 
 * There are some caveats in using this utility method.  If an existing session is passed in, that session MUST be the same session
 * that the DbSurveyContext object was retrieved from the database (and BOTH the fetch of the DbSurveyContext object and the 
 * DbSurveyContext.getSurveyContextSurveys() should be done in the same session/transaction).  If this is not done, Hibernate will
 * throw an exception.
 * 
 * @author nblomberg
 *
 */
public class SurveyContextUtil {
    
    /**
     * Constructor - private to prevent instantiation.
     */
    private SurveyContextUtil() {
        
    }
    
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(SurveyContextUtil.class);
    
    /**
     * Fills out the dbSurveyContext lazy loaded parameters.  In this case, the surveycontextsurveys object is filled in
     * for a dbSurveyContext.  The "Eager" term applies to a hibernate term for "Eager" fetching which is a more expensive 
     * database call.  For queries that only need the dbSurveyContext (without the surveycontextsurveys, this method is not needed)
     * However if the full object needs to be filled out, this method will fill in the surveycontextsurveys object.  
     * 
     * Note that this MUST be done in the same transaction/session of when the dbSurveyContext object was retrieved from the database, otherwise
     * Hibernate will throw an exception.  
     * 
     * @param surveyContextId The dbSurveyContext id to perform an 'eager' fetch for.  This fills in the entire object, specifically 
     * the dbSurveyContext.getSurveyContextSurveys() object.
     * @param dbMgr The database manager that controls access to the database.  This cannot be null.
     */
    public static DbSurveyContext getSurveyContextEager(int surveyContextId, UMSDatabaseManager dbMgr) {
        
        if (dbMgr == null) {
            throw new IllegalArgumentException("The database manager cannot be null.");
        }
        
        Session session = dbMgr.getCurrentSession();

        session.beginTransaction();
        DbSurveyContext surveyContext = getSurveyContextEager(surveyContextId, dbMgr, session);
        session.close();
        
        return surveyContext;
    }
    
    /**
     * Fills out the dbSurveyContext lazy loaded parameters.  In this case, the surveycontextsurveys object is filled in
     * for a dbSurveyContext.  The "Eager" term applies to a hibernate term for "Eager" fetching which is a more expensive 
     * database call.  For queries that only need the dbSurveyContext (without the surveycontextsurveys, this method is not needed)
     * However if the full object needs to be filled out, this method will fill in the surveycontextsurveys object.  
     * 
     * Note that this MUST be done in the same transaction/session of when the dbSurveyContext object was retrieved from the database, otherwise
     * Hibernate will throw an exception.  
     * 
     * @param surveyContextId The dbSurveyContext id to perform an 'eager' fetch for.  This fills in the entire object, specifically 
     * the dbSurveyContext.getSurveyContextSurveys() object.
     * @param dbMgr The database manager that controls access to the database. This cannot be null.
     * @param session The current session to use.  This MUST be done in the same transaction/session of when the dbSurveyContext object was retrieved
     * or else Hibernate will throw an exception. This cannot be null.
     */
    public static DbSurveyContext getSurveyContextEager(int surveyContextId, UMSDatabaseManager dbMgr, Session session) {

        if (dbMgr == null) {
            throw new IllegalArgumentException("The database manager cannot be null.");
        }
        if (session == null) {
            throw new IllegalArgumentException("The session cannot be null.");
        }
        
        DbSurveyContext dbSurveyContext = dbMgr.selectRowById(surveyContextId, DbSurveyContext.class, session);

        // This is okay to be null.
        if (dbSurveyContext != null) {
            Hibernate.initialize(dbSurveyContext.getSurveyContextSurveys());      
        }

        return dbSurveyContext;
    }
    
    /**
     * Fills out the dbSurveyContext lazy loaded parameters while excluding database entries for dynamically generated knowledge assessment surveys. 
     * In this case, the surveycontextsurveys object is filled in for a dbSurveyContext. The "Eager" term applies to a hibernate term for "Eager" 
     * fetching which is a more expensive database call.  For queries that only need the dbSurveyContext (without the surveycontextsurveys, this 
     * method is not needed). However if the full object needs to be filled out, this method will fill in the surveycontextsurveys object, minus
     * any knowledge assessment surveys.  
     * 
     * In short, this method is essentially an alternate version of {@link #getSurveyContextEager(int, UMSDatabaseManager)} that also avoids generating
     * database objects for any knowledge assessment surveys generated for question banks. This greatly reduces the time needed to fetch survey contexts
     * for courses with question banks that have been run a number of times, since each run of the course increases the number of generated surveys and
     * increases the time to fetch the survey context.
     * 
     * @param surveyContextId The dbSurveyContext id to perform an 'eager' fetch for.  This fills in the entire object, specifically 
     * the dbSurveyContext.getSurveyContextSurveys() object.
     * @param dbMgr The database manager that controls access to the database.  This cannot be null.
     */
	public static DbSurveyContext getSurveyContextWithoutGeneratedSurveys(int surveyContextId, UMSDatabaseManager dbMgr) {
        
        if (dbMgr == null) {
            throw new IllegalArgumentException("The database manager cannot be null.");
        }
        
        Session session = dbMgr.getCurrentSession();
        session.beginTransaction();
        try{
            DbSurveyContext dbSurveyContext = getSurveyContextWithoutGeneratedSurveys(surveyContextId, dbMgr, session);
            return dbSurveyContext;
        }finally{
            
            if(session.isOpen()){
                session.close();
            }
        }          
    }
 
	 /**
     * Fills out the dbSurveyContext lazy loaded parameters while excluding database entries for dynamically generated knowledge assessment surveys. 
     * In this case, the surveycontextsurveys object is filled in for a dbSurveyContext.  The "Eager" term applies to a hibernate term for "Eager" 
     * fetching which is a more expensive  database call.  For queries that only need the dbSurveyContext (without the surveycontextsurveys, this 
     * method is not needed) However if the full object needs to be filled out, this method will fill in the surveycontextsurveys object, minus
     * any knowledge assessment surveys.    
     * 
     * In short, this method is essentially an alternate version of {@link #getSurveyContextEager(int, UMSDatabaseManager)} that also avoids generating
     * database objects for any knowledge assessment surveys generated for question banks. This greatly reduces the time needed to fetch survey contexts
     * for courses with question banks that have been run a number of times, since each run of the course increases the number of generated surveys and
     * increases the time to fetch the survey context.
     * 
     * @param surveyContextId The dbSurveyContext id to perform an 'eager' fetch for.  This fills in the entire object, specifically 
     * the dbSurveyContext.getSurveyContextSurveys() object.
     * @param dbMgr The database manager that controls access to the database. This cannot be null.
     * @param session The current session to use.  This MUST be done in the same transaction/session of when the dbSurveyContext object was retrieved
     * or else Hibernate will throw an exception. This cannot be null.
     */
	public static DbSurveyContext getSurveyContextWithoutGeneratedSurveys(int surveyContextId, UMSDatabaseManager dbMgr, Session session) {
     
	     if (dbMgr == null) {
	         throw new IllegalArgumentException("The database manager cannot be null.");
	     }
	     
	     DbSurveyContext dbSurveyContext = dbMgr.selectRowById(surveyContextId, DbSurveyContext.class, session);
	
	     // This is okay to be null.
	     if (dbSurveyContext != null) {
	     	
	     	//perform a query that requests all of the surveys in this survey context that are NOT dynamically generated
	     	String queryString = "from DbSurveyContextSurvey as scs where scs.surveyContext.surveyContextId = " + surveyContextId 
	     			+ " and scs.giftKey not like '" + Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY + " : %'";
	         
	     	final int UNUSED_INDEX = -1;
	     	
	     	Set<DbSurveyContextSurvey> contextSurveys = new HashSet<DbSurveyContextSurvey>(
	     			dbMgr.selectRowsByQuery(DbSurveyContextSurvey.class, queryString, UNUSED_INDEX, UNUSED_INDEX, session));
	     	
	     	dbSurveyContext.setSurveyContextSurveys(contextSurveys);
	     }
	     
	     return dbSurveyContext;
	 }
	
	/**
     * Fills out the dbSurveyContext lazy loaded parameters while excluding database entries for dynamically generated knowledge assessment surveys. 
     * In this case, the surveycontextsurveys object is filled in for a dbSurveyContext.  The "Eager" term applies to a hibernate term for "Eager" 
     * fetching which is a more expensive  database call.  For queries that only need the dbSurveyContext (without the surveycontextsurveys, this 
     * method is not needed) However if the full object needs to be filled out, this method will fill in the surveycontextsurveys object, minus
     * any knowledge assessment surveys.    
     * 
     * In short, this method is essentially an alternate version of {@link #getSurveyContextEager(int, UMSDatabaseManager)} that also avoids generating
     * database objects for any knowledge assessment surveys generated for question banks. This greatly reduces the time needed to fetch survey contexts
     * for courses with question banks that have been run a number of times, since each run of the course increases the number of generated surveys and
     * increases the time to fetch the survey context.
     * 
     * @param dbSurveyContext The dbSurveyContext to perform an 'eager' fetch for.  This fills in the entire object, specifically 
     * the dbSurveyContext.getSurveyContextSurveys() object.
     * @param dbMgr The database manager that controls access to the database. This cannot be null.
     * @param session The current session to use.  This MUST be done in the same transaction/session of when the dbSurveyContext object was retrieved
     * or else Hibernate will throw an exception. This cannot be null.
     */
    public static void getSurveyContextWithoutGeneratedSurveys(DbSurveyContext dbSurveyContext, UMSDatabaseManager dbMgr, Session session) {
        
        if (dbMgr == null) {
            throw new IllegalArgumentException("The database manager cannot be null.");
        }
        if (session == null) {
            throw new IllegalArgumentException("The session cannot be null.");
        }
        
        // This is okay to be null.
        if (dbSurveyContext != null) {
        	
        	int surveyContextId = dbSurveyContext.getSurveyContextId();
	     	
	     	//perform a query that requests all of the surveys in this survey context that are NOT dynamically generated
	     	String queryString = "from DbSurveyContextSurvey as scs where scs.surveyContext.surveyContextId = " + surveyContextId 
	     			+ " and scs.giftKey not like '" + Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY + " : %'";
	         
	     	final int UNUSED_INDEX = -1;
	     	
	     	Set<DbSurveyContextSurvey> contextSurveys = new HashSet<DbSurveyContextSurvey>(
	     			dbMgr.selectRowsByQuery(DbSurveyContextSurvey.class, queryString, UNUSED_INDEX, UNUSED_INDEX, session));
	     	
	     	dbSurveyContext.setSurveyContextSurveys(contextSurveys);
	     }
        
    }
    
    /**
     * Deletes a survey context survey from the database. This is functionally the same as removing the survey context survey from its survey context,
     * but deleting the survey context survey directly avoids unnecessarily fetching the entire survey context from the database first, saving both time 
     * and memory when the survey context itself is otherwise unneeded.
     *
     * @param giftKey The GIFT key identifying the survey context survey within its survey context
     * @param surveyContextId The id of the survey to delete from the database
     * @param username used for write permission checks
     * @param dbMgr The database manager that controls access to the database. This cannot be null.
     * @throws Exception if there was a problem deleting
     */
    public static boolean deleteSurveyContextSurvey(String giftKey, int surveyContexId, String username, UMSDatabaseManager dbMgr) throws DetailedException {
    	
    	if (dbMgr == null) {
            throw new IllegalArgumentException("The database manager cannot be null.");
        }
        
    	Session session = null;
        try {

            session = dbMgr.createNewSession();
            session.beginTransaction();

	        if(Surveys.isSurveyContextEditable(surveyContexId, username)){
	            try{
	                
	            	// Need to use a query to delete here, since performing a regular delete from the hibernate session will
	            	// cause the survey table to recreate the survey context survey via cascade, effectively undoing the delete
	                Query deleteQuery = session.createQuery(
	                		"delete DbSurveyContextSurvey where " + Surveys.SURVEY_CONTEXT_SURVEY_GIFT_KEY_PROPERTY + " = '" + giftKey 
	                		+ "' and " + Surveys.SURVEY_CONTEXT_SURVEY_SURVEY_CONTEXT_ID_PROPERTY + " = " + surveyContexId);
	                
	                deleteQuery.executeUpdate();
	                
	                session.getTransaction().commit();
	                
	                return true;
	                
	            }catch(Exception e){
	                throw new DetailedException("Failed to delete survey named '"+giftKey+"' in survey context" + surveyContexId +".",
	                        "The user name of '"+username+"' has permissions to edit the survey context with id "+ surveyContexId+" but an exception was thrown while deleting from the database.", e);
	            }
	            
	        }else{
	            //ERROR
	            throw new DetailedException("Failed to delete survey named '"+giftKey+"' in survey context" + surveyContexId +".",
	                    "The user name of '"+username+"' doesn't have permission to edit the survey contextwith id "+surveyContexId+".", null);
	        }

	    } catch (Exception e) {
	        
	        if(session != null){
	            session.getTransaction().rollback();
	        }
	        
	        throw e;
	        
	    } finally {
	        
	        if(session != null && session.isOpen()){
	            session.close();
	        }
	    }
    }
}
