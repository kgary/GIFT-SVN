/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.db;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.Table;
import javax.persistence.TableGenerator;

import mil.arl.gift.common.ConfigurationException;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the common database manager methods for database connections via hibernate.  
 * It contains methods to select, insert, update and delete rows in the database tables.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractHibernateDatabaseManager {

	/** instance of the logger */
	private static Logger logger = LoggerFactory.getLogger(AbstractHibernateDatabaseManager.class);

    private AbstractHibernateUtil hibernateUtil;
    
    /** A regular expression used to locate words and phrases in a search text expression */
	private static final String wordExpression = 
		"-?\"[^\"]*\"" +	//double quotes around phrases(s)
        "|-?[A-Za-z0-9']+"  //single word
	;
	
	/** A regular expression used to locate binary operators in a search text expression */
	private static final String binaryOperatorExpression = "(" + wordExpression + ")(\\s+(AND|OR)\\s+(" + wordExpression + "))+";
	
	/** A pattern used to identify terms in a search text expression*/
	private static final Pattern searchTermPattern = Pattern.compile(binaryOperatorExpression + "|" + wordExpression);   

    /**
     * Constructor
     *
     * @param hibernateUtil The Hibernate utility to use to do hibernate operations
     */
    public AbstractHibernateDatabaseManager(AbstractHibernateUtil hibernateUtil) {
        this.hibernateUtil = hibernateUtil;
        initialize();
    }

    /**
	 * Initialize class logic
	 */
	private void initialize(){	

		//setup hibernate
		hibernateUtil.getConfig();

		if(hibernateUtil.getSessionFactory() == null){
			throw new HibernateException("There was a problem configuring the connection to the database");
		}
	}

	/**
	 * Erase all data in the tables
	 * @throws ConfigurationException if there was a problem connecting to the database
	 */
	public void recreateDB() throws ConfigurationException{
	    hibernateUtil.buildSessionFactory(true);
	}
	
	/**
	 * Restore the database from a file containing the necessary information about the database data.
	 * In MySQL this is a ".sql" script file with INSERT statements.
	 * In Derby, for GIFT, this is a ".zip" file of a Derby database (UMS or LMS).
	 * 
	 * @param databaseFile - the file containing information for a GIFT database.  
	 * For Derby, the ".zip" file structure must match that of the GIFT derbyDb:
	 *                 - derbyDb
	 *                     - GiftLms  
	 *                                 (choose at least one of these sub-folders to include)
	 *                     - GiftUms 
     * @param constraintOrderedTableNames an ordering of table names based on key constraints in those tables.
     *                  The ordering should be based on SQL INSERT operations where, for example, a foreign key
     *                  must exist in a table before creating an entry that references that foreign key in a different table.                       
     * @param constraintOrderedTablesToClear an ordering of table names based on key constraints in those tables to remove
     *                  all rows from.  This operation supports the idea of restoring a database by making the tables match
     *                  what is in the backup.  Can be null or empty.
	 * @param permissionsTableNames a list of all visible and editable to usernames tables.
	 * @throws Exception - if there was a problem restoring the database via the hibernate connection.
	 * @throws ConfigurationException - if there was a problem connecting to the database.
	 */
	public void restoreDatabase(File databaseFile, List<String> constraintOrderedTableNames, List<String> constraintOrderedTablesToClear, List<String> permissionsTableNames)
			throws Exception, ConfigurationException {
	    
        if(databaseFile == null || !databaseFile.exists()){
            throw new IllegalArgumentException("The restore file of "+databaseFile+" doesn't exist.");
        }
	    
        if(getCurrentSession() != null){
            
            if(getCurrentSession().getTransaction().isActive()){
                getCurrentSession().flush();
            }
            
            getCurrentSession().disconnect();
            getCurrentSession().close();
        }
    
        if(hibernateUtil.getSessionFactory() != null){
            hibernateUtil.getSessionFactory().close();
        }
        
        hibernateUtil.restoreFrom(databaseFile, constraintOrderedTableNames, constraintOrderedTablesToClear, permissionsTableNames);
	}
	
    /**
     * Backup the database currently connected too. 
     * 
     * @param backupFileNameNoExtension the full file name of where to backup the database too.  
     *              Note: A file extension will be added based on the type of backup performed.
     * @param tableClasses optional list of table classes to backup.  If this is null the entire database will be backed up.
     * @param permissionsTableNames a list of visible and editable usernames tables to backup.
     * @throws Exception if there was a severe problem backing up the database 
     */
	public void backupDatabase(String backupFileNameNoExtension, List<Class<? extends Object>> tableClasses, List<String> permissionsTableNames) throws Exception{
	    hibernateUtil.backupTo(backupFileNameNoExtension, tableClasses, permissionsTableNames);
	}	

	/**
	 * Clean up database connection
	 * 
	 * @throws Exception if there was a severe problem cleaning up
	 */
	public void cleanup() throws Exception{	    
	    hibernateUtil.shutdown();
	}

	/**
	 * Gets the current session. Used by clients that want to manage their own session.
	 * 
	 * @return the current session.  Can return null.
	 */
	public Session getCurrentSession() {

	    if(hibernateUtil.getSessionFactory() != null){
	        return hibernateUtil.getSessionFactory().getCurrentSession();
	    }else{
	        return null;
	    }
	}
	
    /**
     * Return the schema value from the hibernate configuration file.
     * This is useful when needed to reference the db tables using SQL (not HQL) statements.
     * For example:  "Select * from App.SurveyContext" is an SQL statement where "App" is the schema.
     * 
     * @return the schema property value
     */
    public String getSchemaFromConfig(){
        return hibernateUtil.getSchemaFromConfig();
    }
    
    /**
	 * Creates a new Hibernate session
     * 
     * @return Session The new Hibernate session
     */
    public Session createNewSession() {
        
        //Debugging purposes to check the actual URLs which maybe different for export logic purposes
        //String cUrl = hibernateUtil.getConfig().getProperty("connection.url");
        //String hcUrl = hibernateUtil.getConfig().getProperty("hibernate.connection.url");
        return hibernateUtil.getSessionFactory().openSession();
    }
    
    /**
     * Gets the list of hibernate classes for the user data classes.
     *
     * The list follows an ordering of table names based on key constraints in those tables.
     * The ordering is based on SQL INSERT operations where, for example, a foreign key
     * must exist in a table before creating an entry that references that foreign key in a different table. 
     *          
     * The order in the list matters when executing delete operations due to foreign key constraints in various tables.
     * Basically, if you try to delete rows from a table where that table has a column used as a foreign key in another table 
     *
     * @return The list of hibernate classes for the user data classes
     *              Note: the returned list is unmodifiable because it follows the key constraint ordering for INSERT operations.
     */
    public abstract List<Class<?>> getUserDataTableClasses();
    
    /**
     * Clear all the entries in the database that were created by user and domain sessions in GIFT.
     * 
     * @throws HibernateException if there was a problem executing a query
     */
    public abstract void clearUserData();
    
    /**
     * Gets a list of user data table names
     *
     * @return The list of user data table names.  Can be empty but not null.
     */
    public List<String> getUserDataTableNames() {
        return getTableNames(getUserDataTableClasses());
    }
    
    /**
     * Gets a list of table names for the list of table classes.  The table names will include generated tables
     * referenced by the tables in the specified list.
     * 
     * @param tableClasses list of table classes to get the names for, including any generated table names
     * @return table names for the specified table classes
     */
    public static List<String> getTableNames(List<Class<?>> tableClasses){
        
        List<String> tableNames = new ArrayList<>();

        for (Class<? extends Object> clazz : tableClasses) {

            if (clazz.isAnnotationPresent(Table.class)) {
                
                try {
                    Table clazzTable = clazz.getAnnotation(Table.class);

                    if (clazzTable != null) {

                        tableNames.add(clazzTable.name());

                        tableNames.addAll(getTableGeneratorTableName(clazz));
                    }

                } catch (@SuppressWarnings("unused") Throwable ex) {
                }
            }
        }

        return tableNames;
    }
    
    /**
     * Gets a list of tables that is generated by the specified table class.  Generated tables
     * are usually used to keep track of unique id's in order to increment them.
     * 
     * @param tableClass - a database table class to check for generated tables in.
     * @return the list of generated table names generated by the specified table class.  Can be empty but not null.
     */
    public static List<String> getTableGeneratorTableName(Class<?> tableClass){
        
        List<String> tableNames = new ArrayList<>();
        
        if (tableClass.isAnnotationPresent(Table.class)) {
            
            try {
                Table clazzTable = tableClass.getAnnotation(Table.class);

                if (clazzTable != null) {

                    for (Method clazzMethod : tableClass.getMethods()) {

                        if (clazzMethod.isAnnotationPresent(TableGenerator.class)) {

                            TableGenerator tableGenerator = clazzMethod.getAnnotation(TableGenerator.class);

                            if (tableGenerator != null) {

                                tableNames.add(tableGenerator.table());
                            }
                        }
                    }
                }

            } catch (@SuppressWarnings("unused") Throwable ex) {
            }
        }
        
        return tableNames;
    }
        
	/**
	 * Insert the provided data as a row in a table in the db schema
	 * 
	 * @param data - row of data to insert into a table
	 * @param session - caller provided Session.
	 * @throws Exception if there was a problem inserting the row into the database
	 */	
	public void insertRow(Object data, Session session) throws Exception {

		try{

			session.save(data);

			if(logger.isInfoEnabled()){
			    logger.info("Database insert succeeded for "+data);
			}

		}catch(Exception e){

			throw e;  //allow the session creator to manage cleaning up any transactions
		}

	}


	/**
	 * Insert the provided data as a row in a table in the db schema
	 * 
	 * @param data - row of data to insert into a table
	 * @throws Exception if there was a problem inserting the row
	 */
	public void insertRow(Object data) throws Exception{

		Session session = null;
		try{
    		session = hibernateUtil.getSessionFactory().getCurrentSession();
    
    		session.beginTransaction();
    
    		insertRow(data, session);
    
    		if(logger.isInfoEnabled()){
    		    logger.info("Successfully inserted "+data+", attempting to commit changes");
    		}
    		    
    		session.getTransaction().commit();
    		
        }catch(Exception e){
                
            if(session != null){
                //prevent transactions from locking tables when a commit has failed
                session.getTransaction().rollback();
            }
            
            throw new Exception("Failed to insert the row in the database because of an exception while inserting", e);
            
        } finally {
            
            if(session != null && session.isOpen()) {
                session.close();
            }
        }

	}
    
    /**
     * Delete the provided data from a row in a table in the db schema
     *
     * @param data - the data to remove from the db
     * @param session the hibernate session to do the work in.  If null a new session will be created and committed but rolled back
     * upon failure.
     * @throws Exception if there was a problem deleting.
     */
    public void deleteRow(Object data, Session session) throws Exception {
        
        if(session == null){
            deleteRow(data);
        }else{
            session.delete(data);            
            
            if(logger.isInfoEnabled()){
                logger.info("Database delete succeeded for " + data);
            }
        }

    }

	/**
	 * Delete the provided data from a row in a table in the db schema
	 * 
	 * @param data - the data to remove from the db
	 * @throws Exception if there was a problem deleting
     */
    public void deleteRow(Object data) throws Exception {

        Session session = null;
        try {
            //can create as many sessions as needed
            session = hibernateUtil.getSessionFactory().getCurrentSession();

            session.beginTransaction();

            deleteRow(data, session);

            if(logger.isInfoEnabled()){
                logger.info("Successfully deleted " + data + ", attempting to commit changes");
            }

            session.getTransaction().commit();

            if(logger.isInfoEnabled()){
                logger.info("Database delete succeeded for " + data);
            }

        } catch (Exception e) {
            
            if (session != null) {
                //prevent transactions from locking tables when a commit has failed
                session.getTransaction().rollback();
            }            
            
            throw e;
            
        } finally {
            
            if(session != null && session.isOpen()) {
                session.close();
            }
        }

    }

    /**
     * Update the row in the table where the object resides
     *
     * @param data - the data to update in the db
     * @param session - the session to do the work in
     * @return boolean - true iff the update completed without error
     */
    public boolean updateRow(Object data, Session session) {

        boolean success = false;

        try {
            session.update(data);

            success = true;

            if(logger.isInfoEnabled()){
                logger.info("Database update succeeded for " + data);
            }

        } catch (Exception e) {

            logger.error("DatabaseManager caught exception while trying to update: " + data, e);
        }

        return success;
    }

    /**
     * Update the row in the table where the object resides
     *
     * @param data the data to update in the db
     * @return boolean - true iff the update completed without error
     */
    public boolean updateRow(Object data) {

        boolean success = false;

        Session session = null;
        try {
            //can create as many sessions as needed
            session = hibernateUtil.getSessionFactory().getCurrentSession();

            //MH 11/9: attempting to alleviate the issue of quick back-to-back transactions on the same data contents
            // causing transaction time out issues (ticket #260).
            Transaction transaction = session.beginTransaction();
            transaction.setTimeout(10);

            success = updateRow(data, session);

            if (success) {

                if(logger.isInfoEnabled()){
                    logger.info("Successfully updated " + data + ", attempting to commit changes");
                }

                session.getTransaction().commit();

                if(logger.isInfoEnabled()){    
                    logger.info("Database update succeeded for " + data);
                }

            } else {

                logger.error("Failed to update " + data + ", rolling back changes");

                session.getTransaction().rollback();
            }

        } catch (Exception e) {

            logger.error("DatabaseManager caught exception while trying to update: " + data, e);
            success = false;

            if (session != null) {
                //prevent transactions from locking tables when a commit has failed
                session.getTransaction().rollback();
            }
        } finally{
            
            if(session != null && session.isOpen()){
                session.close();
            }
        }

        return success;
    }

    /**
     * Insert the provided data as a row in a table in the database if it
     * doesn't exist, otherwise update the existing object
     *
     * @param data The object to save or update
     * @param session the session to do the work in
     * @return boolean True if the update completed without error
     */
    public boolean saveOrUpdateRow(Object data, Session session) {

        boolean success = false;

        try {

            session.saveOrUpdate(data);

            success = true;

            if(logger.isInfoEnabled()){
                logger.info("Database save/update succeeded for " + data);
            }

        } catch (Exception e) {

            logger.error("DatabaseManager caught exception while trying to save/update: " + data, e);
            throw e;
        }

        return success;
    }
    
    /**
     * Insert the provided data as a row in a table in the database if it
     * doesn't exist, otherwise update the existing object
     * 
     * @param data The object to save or update
     * @return boolean True if the update completed without error
     */
    public boolean saveOrUpdateRow(Object data) {

        boolean success;

        Session session = null;

        try {
            session = hibernateUtil.getSessionFactory().getCurrentSession();

            session.beginTransaction();

            success = saveOrUpdateRow(data, session);

            if (success) {

                if(logger.isInfoEnabled()){
                    logger.info("Successfully save/updated " + data + ", attempting to commit changes");
                }

                session.getTransaction().commit();
                
                if(logger.isInfoEnabled()){
                    logger.info("Database save/update succeeded for " + data);
                }

            } else {
                logger.error("Failed to save/update " + data + ", rolling back changes");

                session.getTransaction().rollback();
            }

        } catch (Exception e) {

            logger.error("DatabaseManager caught exception while trying to save/update: " + data, e);
            success = false;

            if (session != null) {
                //prevent transactions from locking tables when a commit has failed
                session.getTransaction().rollback();
            }
            
        } finally {
            
            if(session != null && session.isOpen()) {
                session.close();
            }
        }

        return success;
    }

	/**
	 * Support method for selectRowByExample.  This method "expects" to be called with a row list of length 0 or 1.  
	 * An error is written to the log if rows has length greater than 1.
	 * 
	 * @param rows list of rows from which a single row is selected.
	 * @param example example object used by the caller to generate the list.  Only used for error logging.
	 * @return null if list is empty, otherwise returns first object in the list. 
	 */
	private <E> E getRowFromRowList(List<E> rows, Object example) {

		E row = null;
		if(rows.size() == 1){
			row = rows.get(0);
		}else if(rows.size() > 1){
			logger.error("query criteria returned " + rows.size()+ " rows with example of "+example + " - using first one in list by default");
			row = rows.get(0);
		}

		return row;
	}


	/**
	 * Return the row which has entries that match the example object.  
	 * Using an Example allows you to construct a query criterion from a given instance.
	 * 
	 * NOTE: Version properties, identifiers and associations are ignored. By default, null valued properties are excluded.<br/>
     * <br/>
     * NOTE: THIS CAN BE AN EXPENSIVE CALL.  Be cautious when using this on a table that can have many rows as we found 
     * out in #3407.
	 * 
     * @param <E> the row class 
     * @param example - an instance of a row with attributes provided that match the rows to be returned.
	 * @param rowClass - the table annotation class (the table to get the row from)
	 * @return E - the row found to have the primary key value
	 */
	public <E> E selectRowByExample(Object example, Class<E> rowClass){

		List<E> rows = selectRowsByExample(example, rowClass);

		return getRowFromRowList(rows, example);
	}


	/**
	 * Return the row which has entries that match the example object.  
	 * Using an Example allows you to construct a query criterion from a given instance.
	 * 
	 * NOTE: Version properties, identifiers and associations are ignored. By default, null valued properties are excluded.<br/>
     * <br/>
     * NOTE: THIS CAN BE AN EXPENSIVE CALL.  Be cautious when using this on a table that can have many rows as we found 
     * out in #3407.
	 * 
     * @param <E> the row class 
     * @param example - an instance of a row with attributes provided that match the rows to be returned.
	 * @param rowClass - the table annotation class (the table to get the row from)
	 * @param session - caller provided Session.
	 * @return E - the row found to have the primary key value
	 */
	public <E> E selectRowByExample(Object example, Class<E> rowClass, Session session){

		List<E> rows = selectRowsByExample(example, rowClass, session);

		return getRowFromRowList(rows, example);
	}


	/**
	 * Return the rows which has entries that match the example object.<br/>  
	 * Using an Example allows you to construct a query criterion from a given instance.<br/>
	 * <br/>
	 * NOTE: Version properties, identifiers and associations are ignored. By default, null valued properties are excluded.<br/>
	 * <br/>
	 * NOTE: THIS CAN BE AN EXPENSIVE CALL.  Be cautious when using this on a table that can have many rows as we found 
	 * out in #3407.
	 * 
     * @param <E> the row class 
     * @param example - an instance of a row with attributes provided that match the rows to be returned.
	 *         Note: this can be null if all rows are desired
	 * @param rowClass - the table annotation class (the table to get the row from)
	 * @return the row found to have the primary key value.  Can be empty but not null.
	 */
	public <E> List<E> selectRowsByExample(Object example, Class<E> rowClass){

		//ClassMetadata md = HibernateUtil.getSessionFactory().getClassMetadata(rowClass);
		//String pkColumnName = md.getIdentifierPropertyName();
		//String[] pNames = md.getPropertyNames();
		//Type[] pTypes = md.getPropertyTypes();	

		//start transaction
		Session session = hibernateUtil.getSessionFactory().getCurrentSession();
		try {
    		session.beginTransaction();
    
    		List<E> rows = selectRowsByExample(example, rowClass, session);
    
    		return rows;
    		
    	} finally {
            
            if(session.isOpen()) {
                session.close();
            }
        }
	}

	/**
	 * Return the rows which has entries that match the example object.  <br/>
	 * Using an Example allows you to construct a query criterion from a given instance.<br/>
	 * <br/>
	 * NOTE: Version properties, identifiers and associations are ignored. By default, null valued properties are excluded.<br/>
	 * <br/>
     * NOTE: THIS CAN BE AN EXPENSIVE CALL.  Be cautious when using this on a table that can have many rows as we found 
     * out in #3407.
     * 
     * @param <E> the row class 
     * @param example - an instance of a row with attributes provided that match the rows to be returned.
	 *               Note: this can be null if all rows are desired
	 * @param rowClass - the table annotation class (the table to get the row from)
	 * @param session - caller provided Session object
	 * @return the row found to have similar attribute values as the example.  Can be empty but not null.
	 */
	public <E> List<E> selectRowsByExample(Object example, Class<E> rowClass, Session session){

		Criteria criteria = session.createCriteria(rowClass);	
		
		if(example != null){
    		// Customizations:
    		// excludeZeroes - in some classes we have integers (for ids) which java defaults to zero
    		criteria.add(Example.create(example).excludeZeroes().excludeProperty("doNotUse"));	
		}

		List<?> rowsRaw = criteria.list();
		List<E> rows = new ArrayList<E>(rowsRaw.size());
		for(Object obj : rowsRaw){
			rows.add(rowClass.cast(obj));
		}

		return rows;
	}
	
	/**
	 * Return all rows of a given class type.<br/>
     * <br/>
     * NOTE: THIS CAN BE AN EXPENSIVE CALL.  Be cautious when using this on a table that can have many rows as we found 
     * out in #3407.
	 * 
     * @param <E> the row class
     * @param rowClass - the table annotation class (the table to get the row from)
	 * @return all rows of the class type.   Can be empty but not null.
	 */
	public <E> List<E> selectAllRows(Class<E> rowClass){
	    return selectRowsByExample(null, rowClass);
	}
	
	/**
     * Return all rows of a given class type.<br/>
     * <br/>
     * NOTE: THIS CAN BE AN EXPENSIVE CALL.  Be cautious when using this on a table that can have many rows as we found 
     * out in #3407.
     * 
     * @param <E> the row class
     * @param rowClass - the table annotation class (the table to get the row from)
     * @param session - caller provided Session.
     * @return all rows of the class type.   Can be empty but not null.
     */
	public <E> List<E> selectAllRows(Class<E> rowClass, Session session){
        return selectRowsByExample(null, rowClass, session);
    }
	
    /**
     * Select data from a table using an HQL query. Results are capped
     * by the starting index and max results.  This select rows by query is useful when your column values
     * can contain single quotes or other special characters which need to be escaped in the query.
     * 
     * @param <E> Class type that the results will be cast to
     * @param rowClass Table class
     * @param query HQL Query to execute.
     * @param startIndex Optional starting row index to return from results. Set to -1 if none specified
     * @param maxResults Optional maximum results to return. Set to -1 if not specified
     * @param session the session to perform the select in.  Doesn't close the session.
     * @return List of results.  Won't be null but can be empty.  If the query is null, empty list is returned.
     * @throws HibernateException if there was a problem selecting the rows
     */
	//protected because on the Hibernate database managers should use the Hibernate Query class, this helps abstract hibernate
	//from the rest of GIFT
    protected <E> List<E> selectRowsByQuery(Class<E> rowClass, Query query, int startIndex, int maxResults, Session session) throws HibernateException {
        
        if (startIndex != -1) {
            query.setFirstResult(startIndex);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        List<?> rowsRaw = query.list();
        
        List<E> rows = new ArrayList<E>(rowsRaw.size());
        for (Object obj : rowsRaw) {
            rows.add(rowClass.cast(obj));
        }

        return rows;
    }
	
    /**
     * Select data from a table using an HQL query. Results are capped
     * by the starting index and max results.  This select rows by query is useful when your column values
     * can contain single quotes or other special characters which need to be escaped in the query.
     * 
     * @param <E> Class type that the results will be cast to
     * @param rowClass Table class
     * @param query HQL Query to execute.
     * @param startIndex Optional starting row index to return from results. Set to -1 if none specified
     * @param maxResults Optional maximum results to return. Set to -1 if not specified
     * @return List of results.  Won't be null but can be empty.  If the query is null, empty list is returned.
     * @throws HibernateException if there was a problem selecting the rows
     */
    //protected because on the Hibernate database managers should use the Hibernate Query class, this helps abstract hibernate
    //from the rest of GIFT
	protected <E> List<E> selectRowsByQuery(Class<E> rowClass, Query query, int startIndex, int maxResults) throws HibernateException {
	    
	    if(query == null){
	        return new ArrayList<E>(0);
	    }
	    
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try{        
            return selectRowsByQuery(rowClass, query, startIndex, maxResults, session);
        }finally {
                
            if(session.isOpen()){
                //end the session by releasing the JDBC connection and cleaning up
                session.close();
            }
        }
	}
    
    /**
     * Select data from a table using an HQL query string. Results are capped
     * by the starting index and max results.
     * @param <E> Class type that the results will be cast to
     * @param rowClass Table class
     * @param queryString HQL Query string starting.</br>
     * Note: this should not contain single quotes or other special characters.  If the column value you are
     * looking for could contain a single quote or other special characters that need to be escaped (e.g. course name), 
     * then use {@link #selectRowsByQuery(Class, Query, int, int)}.
     * @param startIndex Optional starting row index to return from results. Set to -1 if none specified
     * @param maxResults Optional maximum results to return. Set to -1 if not specified
     * @return List of results.  Won't be null but can be empty.
     * @throws HibernateException if there was a problem selecting the rows
     */
    public <E> List<E> selectRowsByQuery(Class<E> rowClass, String queryString, int startIndex, int maxResults) throws HibernateException {
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try{  
            return selectRowsByQuery(rowClass, queryString, startIndex, maxResults, session);
        }finally {
            
            if(session.isOpen()){
                //end the session by releasing the JDBC connection and cleaning up
                session.close();
            }
        }
    }
    
    /**
     * Select data from a table using an HQL query string. Results are capped
     * by the starting index and max results.
     * @param <E> Class type that the results will be cast to
     * @param rowClass Table class
     * @param queryString HQL Query string starting.</br>
     * Note: this should not contain single quotes or other special characters.  If the column value you are
     * looking for could contain a single quote or other special characters that need to be escaped (e.g. course name), 
     * then use {@link #selectRowsByQuery(Class, Query, int, int)}.
     * @param startIndex Optional starting row index to return from results. Set to -1 if none specified
     * @param maxResults Optional maximum results to return. Set to -1 if not specified
     * @param session - caller provided Session. Doesn't close the session.
     * @return List of results. Won't be null but can be empty. If the query is null, empty list is returned.
     * @throws HibernateException if there was a problem selecting the rows
     */
    public <E> List<E> selectRowsByQuery(Class<E> rowClass, String queryString, int startIndex, int maxResults, Session session) throws HibernateException {

        Query query = session.createQuery(queryString);
        return selectRowsByQuery(rowClass, query, startIndex, maxResults, session);
    }
    
    /**
     * Select data from a table using an HQL query string. Results are capped
     * by the starting index and max results.
     * @param <E> Class type that the results will be cast to
     * @param rowClass Table class
     * @param queryString HQL Query string starting.</br>
     * Note: this should not contain single quotes or other special characters.  If the column value you are
     * looking for could contain a single quote or other special characters that need to be escaped (e.g. course name), 
     * then use {@link #selectRowsByQuery(Class, Query, int, int)}.
     * @param startIndex Optional starting row index to return from results. Set to -1 if none specified
     * @param maxResults Optional maximum results to return. Set to -1 if not specified
     * @param listParams Optional a mapping of list parameters in the query to the collections that they represent.
     * Can be used to provide lists of values as parameters for the query. Set to null if not specified.
     * @return List of results.  Won't be null but can be empty.
     * @throws HibernateException if there was a problem selecting the rows
     */
    public <E> List<E> selectRowsByQuery(Class<E> rowClass, String queryString, int startIndex, int maxResults, Map<String, Collection<? extends Object>> listParams) throws HibernateException {
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try{  
            return selectRowsByQuery(rowClass, queryString, startIndex, maxResults, listParams, session);
        }finally {
            
            if(session.isOpen()){
                //end the session by releasing the JDBC connection and cleaning up
                session.close();
            }
        }
    }
    
    /**
     * Select data from a table using an HQL query string. Results are capped
     * by the starting index and max results.
     * @param <E> Class type that the results will be cast to
     * @param rowClass Table class
     * @param queryString HQL Query string starting.</br>
     * Note: this should not contain single quotes or other special characters.  If the column value you are
     * looking for could contain a single quote or other special characters that need to be escaped (e.g. course name), 
     * then use {@link #selectRowsByQuery(Class, Query, int, int)}.
     * @param startIndex Optional starting row index to return from results. Set to -1 if none specified
     * @param maxResults Optional maximum results to return. Set to -1 if not specified
     * @param listParams Optional a mapping of list parameters in the query to the collections that they represent.
     * Can be used to provide lists of values as parameters for the query. Set to null if not specified.
     * @param session - caller provided Session.
     * @return List of results
     * @throws HibernateException if there was a problem selecting the rows
     */
    public <E> List<E> selectRowsByQuery(Class<E> rowClass, String queryString, int startIndex, int maxResults, Map<String, Collection<? extends Object>> listParams, Session session) throws HibernateException {

        Query query = session.createQuery(queryString);
        
        if(listParams != null) {
            for(Map.Entry<String, Collection<? extends Object>> param : listParams.entrySet()) {
                query.setParameterList(param.getKey(), param.getValue());
            }
        }
        
        return selectRowsByQuery(rowClass, query, startIndex, maxResults, session);
    }
    
    /**
     * Runs the SQL (not HQL) query to delete one or more rows from the database.
     * 
     * @param sqlQueryString the SQL (not SQL) delete statement
     * @return the number of rows deleted.  Will be 0 if no rows were deleted.
     * @throws HibernateException if there was a problem creating the session or completing the delete statement
     * in the transaction.
     */
    public int deleteRowsBySQLQuery(String sqlQueryString) throws HibernateException{
        
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        try{
            session.beginTransaction();
            Query query = session.createSQLQuery(sqlQueryString);
            
            int rowsDeleted = query.executeUpdate();
            
            session.getTransaction().commit();
            
            return rowsDeleted;
        }catch(Exception e){
            throw new HibernateException("Failed to delete rows because an exception was thrown.", e);
        }finally{
            
            if(session.isOpen()){
                session.close();
            }
        }
    }
    
    /**
     * Runs the SQL (not HQL) query to execute the select statement.
     * 
     * @param sqlQueryString the SQL (not HQL) select statement
     * @return the list of objects returned by the query. Will be empty if no results are found.
     * @throws HibernateException if there was a problem creating the session or completing the select statement
     * in the transaction.
     */
    @SuppressWarnings({"unchecked" })
    public List<Object> executeSelectSQLQuery(String sqlQueryString) throws HibernateException{
        
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        try{
            session.beginTransaction();
            return session.createSQLQuery(sqlQueryString).list();
        }catch(Exception e){
            throw new HibernateException("Failed to select row(s) because an exception was thrown.", e);
        }finally{
            
            if(session.isOpen()){
                session.close();
            }
        }
    }
    
    /**
     * Runs the SQL (not HQL) query to execute the select statement and returns a resulting list
     * of the given type.
     * 
     * @param sqlQueryString the SQL (not HQL) select statement
     * @param clazz the generic type of the resulting list. Cannot be null.
     * @return the list of objects returned by the query. Will be empty if no results are found.
     * @throws HibernateException if there was a problem creating the session or completing the select statement
     * in the transaction.
     */
    @SuppressWarnings("unchecked")
    public <E> List<E> executeSelectSQLQuery(String sqlQueryString, Class<E> clazz) throws HibernateException{
        
        if(clazz == null) {
            throw new IllegalArgumentException("The class of objects to be returned by the SQL query cannot be null.");
        }
        
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        try{
            session.beginTransaction();
            return session.createSQLQuery(sqlQueryString).list();
        }catch(Exception e){
            throw new HibernateException("Failed to select row(s) because an exception was thrown.", e);
        }finally{
            
            if(session.isOpen()){
                session.close();
            }
        }
    }
    
    /**
     * Runs the SQL (not HQL) query to execute the update statement.
     * 
     * @param sqlQueryString the SQL (not SQL) update statement
     * @param session the session to perform the query in.  If null the current session will be used to create a transaction and close the session.
     * @return the number of objects updated by the query. Will be zero if no rows are updated.
     * @throws HibernateException if there was a problem creating the session or completing the update statement
     * in the transaction.
     */
    public int executeUpdateSQLQuery(String sqlQueryString, Session session) throws HibernateException{
        
        boolean manageSession = false;
        if(session == null){
            session = hibernateUtil.getSessionFactory().getCurrentSession();
            manageSession = true;
        }
        
        try{
            if(manageSession){
                session.beginTransaction();
            }
            return session.createSQLQuery(sqlQueryString).executeUpdate();
        }catch(Exception e){
            throw new HibernateException("Failed to update row(s) because an exception was thrown.", e);
        }finally{
            
            if(manageSession && session.isOpen()){
                session.close();
            }
        }
    }
    
    /**
     * Runs the SQL (not HQL) query to execute the delete statement.
     * 
     * @param sqlQueryString the SQL (not SQL) delete statement
     * @param session the session to perform the query in.  If null the current session will be used to create a transaction and close the session.
     * @return the number of objects deleted by the query. Will be zero if no rows are deleted.
     * @throws HibernateException if there was a problem creating the session or completing the delete statement
     * in the transaction.
     */
    public int executeDeleteSQLQuery(String sqlQueryString, Session session) throws HibernateException{
        
        boolean manageSession = false;
        if(session == null){
            session = hibernateUtil.getSessionFactory().getCurrentSession();
            manageSession = true;
        }
        
        try{
            if(manageSession){
                session.beginTransaction();
            }
            return session.createSQLQuery(sqlQueryString).executeUpdate();
        }catch(Exception e){
            throw new HibernateException("Failed to delete row(s) because an exception was thrown.", e);
        }finally{
            
            if(manageSession && session.isOpen()){
                session.close();
            }
        }
    }
    
    /**
     * Runs the SQL (not HQL) query to execute the insert statement.
     * 
     * @param sqlQueryString the SQL (not SQL) insert statement
     * @param session the session to perform the query in.  If null the current session will be used to create a transaction and close the session.
     * @return the number of objects inserted by the query. Will be zero if no rows are inserted.
     * @throws HibernateException if there was a problem creating the session or completing the insert statement
     * in the transaction.
     */
    public int executeInsertSQLQuery(String sqlQueryString, Session session) throws HibernateException{
        
        boolean manageSession = false;
        if(session == null){
            session = hibernateUtil.getSessionFactory().getCurrentSession();
            manageSession = true;
        }
        
        try{
            if(manageSession){
                session.beginTransaction();
            }
            return session.createSQLQuery(sqlQueryString).executeUpdate();
        }catch(Exception e){
            throw new HibernateException("Failed to delete row(s) because an exception was thrown.", e);
        }finally{
            
            if(manageSession && session.isOpen()){
                session.close();
            }
        }
    }
    
    /**
     * Get the total number of rows in the table represented by the class
     * @param <E> Table class type
     * @param rowClass Table class
     * @return Number of items in the object table
     */
    public <E> long getRowCount(Class<E> rowClass) {
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            Criteria criteria = session.createCriteria(rowClass);
            criteria.setProjection(Projections.rowCount());
            
            List<?> rowsRaw = criteria.list();
            long numRows = (Long) rowsRaw.get(0);
            
            return numRows;
            
        } finally {
            
            if(session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Get the total number of rows in a table using an HQL query string
     * @param queryString HQL Count query string, e.g. select count(*) from...
     * @param session optional session to perfom the query in, if null a session will be created and closed.
     * @return Number of items returned by the query
     */
    public long getRowCountByQuery(String queryString, Session session) {
        
        boolean createdSession = false;
        try {
            if(session == null){
                session = getCurrentSession();
                session.beginTransaction();
                createdSession = true;
            }

            Query query = session.createQuery(queryString);
            
            List<?> rowsRaw = query.list();
            long numRows = (Long) rowsRaw.get(0);
            
            return numRows;
            
        } finally {
            
            if(createdSession && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Return the row which has the primary key value specified.
     *
     * @param <E> The row class
     * @param pkValue - the primary key value of the row to return.
     * @param rowClass - the table annotation class (the table to get the row
     * from)
     * @param session The session to select a row in.  If null a new session is created and closed.
     * @return E - the row found to have the primary key value.  Can be null.
     */
    public <E> E selectRowById(Object pkValue, Class<E> rowClass, Session session) {

        boolean isLocalSession = false;
        if(session == null){
            isLocalSession = true;
            session = getCurrentSession();
            session.beginTransaction();
        }

        try{
            ClassMetadata md = hibernateUtil.getSessionFactory().getClassMetadata(rowClass);
            String pkColumnName = md.getIdentifierPropertyName();
    
            /* Hibernate will perform a join on tables that have a one-to-many
             * relationship. This means that if you are trying to query a table item
             * with id, you could potentially return duplicate objects since the
             * table could look like below. To prevent this, set the Criteria's
             * result transformer to DISTINCT_ROOT_ENTITY which will only return 1
             * instance of the item from the table. This differs from SQL's 'select
             * distinct' because the query will still search the joined table and
             * get the duplicates, but then the result is filtered before being
             * returned here. */
    
            /*-
             * Example: Attempting to select My Collection by Id '123'.
             * Collection (Table A) has one-to-many Items (Table B) 
             * 
             * Table A
             * | Collection Id | Collection Name |
             * | ------------- | --------------- |
             * |      123      |  My Collection  | 
             * 
             * Table B
             * | Item Id | Item Name | Collection Id_FK |
             * | ------- | --------- | ---------------- |
             * |    A    |   Apple   |       123        |
             * |    B    |   Banana  |       456        |
             * |    C    |   Carrot  |       123        |
             * 
             * Join of A and B that is used for the query
             * | Collection Id | Collection Name | Item Id | Item Name | Collection Id _FK |
             * | ------------- | --------------- | ------- | --------- | ----------------- |
             * |      123      |  My Collection  |    A    |   Apple   |        123        |
             * |      123      |  My Collection  |    C    |   Carrot  |        123        |
             * 
             * You can easily see now that querying by Collection Id will return 2 rows instead of the expected 1.
             * Criteria.DISTINCT_ROOT_ENTITY recognizes that the same Collection is being returned twice and removes the duplicate.
             */
    
            Criteria criteria = session.createCriteria(rowClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.add(Restrictions.eq(pkColumnName, pkValue));
    
            List<?> rowsRaw = criteria.list();
            List<E> rows = new ArrayList<E>(rowsRaw.size());
            for (Object obj : rowsRaw) {
                rows.add(rowClass.cast(obj));
            }
    
            E row = null;
            if (rows.size() == 1) {
                row = rows.get(0);
                if(logger.isInfoEnabled()){
                    logger.info("query criteria found an entry with primary key column named " + pkColumnName + " value of " + pkValue + " => " + row);
                }
            } else if (rows.size() > 1) {
                logger.error("query criteria returned " + rows.size() + " rows with primary key column named " + pkColumnName + " value of " + pkValue + " - using first one in list by default");
                row = rows.get(0);
            }
    
            return row;
        }finally{
            if(isLocalSession && session.isOpen()){
                session.close();
            }
        }
    }

    /**
     * Return the row which has the primary key value specified.
     *
     * @param <E> The row class
     * @param pkValue - the primary key value of the row to return.
     * @param rowClass - the table annotation class (the table to get the row
     * from)
     * @return E - the row found to have the primary key value. Can be null.
     */
    public <E> E selectRowById(Object pkValue, Class<E> rowClass) {

        //start transaction
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
    
            E row = selectRowById(pkValue, rowClass, session);
    
            return row;
            
        } finally {
            
            //end the session by releasing the JDBC connection and cleaning up
            if(session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Return the rows which has the primary key value specified.
     *
     * @param <E> The table annotation class (the table to get the row from)
     * @param propertyName The property name of the key
     * @param pkValue - the primary key value of the row to return.
     * @param rowClass - the table annotation class (the table to get the row
     * from)
     * @param session the session to do the work in
     * @return E - the row found to have the primary key value
     */
    public <E> List<E> selectRowsById(String propertyName, Object pkValue, Class<E> rowClass, Session session) {

        Criteria criteria = session.createCriteria(rowClass);
        criteria.add(Restrictions.eq(propertyName, pkValue));

        List<?> rowsRaw = criteria.list();
        List<E> rows = new ArrayList<E>(rowsRaw.size());
        for (Object obj : rowsRaw) {
            rows.add(rowClass.cast(obj));
        }

        return rows;
    }
    
    /**
     * Return the rows which has the primary key value specified.
     *
     * @param <E> The table annotation class (the table to get the row from)
     * @param propertyName The property name of the key
     * @param pkValue - the primary key value of the row to return.
     * @param rowClass - the table annotation class (the table to get the row
     * from)
     * @return E - the row found to have the primary key value
     */
    public <E> List<E> selectRowsById(String propertyName, Object pkValue, Class<E> rowClass) {

        //start transaction
        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
    
            List<E> rows = selectRowsById(propertyName, pkValue, rowClass, session);
    
            return rows;
        
        } finally {
            
            if(session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Return the row which has the two-tuple composite key values specified.
     *
     * @param <E> The table annotation class (the table to get the row from)
     * @param firstIdPropertyName The property name of the first key
     * @param firstId The composite key value of the first key
     * @param secondIdPropertyName The property name of the second key
     * @param secondId The composite key value of the second key
     * @param clazz The table annotation class (the table to get the row from)
     * @param session  the session to do the work in
     * @return E The row found to have the composite key
     */
    public <E> E selectRowByTwoTupleCompositeId(String firstIdPropertyName, Object firstId, String secondIdPropertyName, Object secondId, Class<E> clazz, Session session) {

        Criteria criteria = session.createCriteria(clazz);
        criteria.add(Restrictions.eq(firstIdPropertyName, firstId));
        criteria.add(Restrictions.eq(secondIdPropertyName, secondId));

        List<?> rowsRaw = criteria.list();
        List<E> rows = new ArrayList<>(rowsRaw.size());

        for (Object obj : rowsRaw) {

            rows.add(clazz.cast(obj));
        }

        E row = null;

        if (rows.size() == 1) {

            row = rows.get(0);
            if(logger.isInfoEnabled()){
                logger.info("query criteria found an entry with primary key column named "
                        + firstIdPropertyName + " value of " + firstId
                        + " and " + secondIdPropertyName + " value of " + secondId
                        + " => " + row);
            }

        } else if (rows.size() > 1) {

            logger.error("query criteria returned " + rows.size() + " rows with primary key column named "
                    + firstIdPropertyName + " value of " + firstId
                    + " and " + secondIdPropertyName + " value of " + secondId
                    + " - using first one in list by default");
            row = rows.get(0);
        }

        return row;
    }
    
    /**
     * Return the row which has the two-tuple composite key values specified.
     *
     * @param <E> The table annotation class (the table to get the row from)
     * @param firstIdPropertyName The property name of the first key
     * @param firstId The composite key value of the first key
     * @param secondIdPropertyName The property name of the second key
     * @param secondId The composite key value of the second key
     * @param clazz The table annotation class (the table to get the row from)
     * @return E The row found to have the composite key
     */
    public <E> E selectRowByTwoTupleCompositeId(String firstIdPropertyName, Object firstId, String secondIdPropertyName, Object secondId, Class<E> clazz) {

        //start transaction

        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        
        try {
            session.beginTransaction();
            
            E row = selectRowByTwoTupleCompositeId(firstIdPropertyName, firstId, secondIdPropertyName, secondId, clazz, session);
            
            return row;
            
        } finally {
        
            if(session.isOpen()) {
                session.close();
            }
        }
    }
    
    
    
    /**
     * Return the row which has the two-tuple composite key values specified.  The
     * hibernate Cache is bypassed here (see notes in the function).  This can required
     * in situations such as when a write to the database is very quickly followed by a read 
     * on the same object.  In that particular case, a quick read could pull and older value
     * from the hibernate cache.  To solve that, hibernate allows mechanisms to bypass the 
     * cache.  The result is the latest result from the database is retrieved (at the expense
     * of hitting the database instead of the cache).
     *
     * @param <E> The table annotation class (the table to get the row from)
     * @param firstIdPropertyName The property name of the first key
     * @param firstId The composite key value of the first key
     * @param secondIdPropertyName The property name of the second key
     * @param secondId The composite key value of the second key
     * @param clazz The table annotation class (the table to get the row from)
     * @return E The row found to have the composite key
     */
    public <E> E selectRowByTwoTupleCompositeIdBypassCache(String firstIdPropertyName, Object firstId, String secondIdPropertyName, Object secondId, Class<E> clazz) {

        //start transaction

        Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        try{
            session.beginTransaction();
            
            E row = selectRowByTwoTupleCompositeId(firstIdPropertyName, firstId, secondIdPropertyName, secondId, clazz, session);
            
            // The session.refresh(<object>) forces a reload from the database (which effectively bypasses the hibernate cache).
            // This can be useful in certain circumstances. For example a write to the database followed by a quick read, could
            // result in getting a stale cache value rather than the latest value from the database.
            //
            // Hibernate uses the refresh method to force the object to be reloaded.   Antother mechanism was explored that would
            // work as well, which involves using StatelessSessions.  The code for using stateless sessions looks like the following:
            //  ... 
            //       StatelessSession session = hibernateUtil.getSessionFactory().openStatelessSession();
            //       ... <do work>
            //       session.close();
            // ...
            // For now the refresh on the specific object works for our needs, but in the future, it may be beneficial to have
            // a stateless session where needed, or a better mechanism for 'refreshing' objects where needed.
            if (row != null) {
                session.refresh(row);
            }
            
            return row;
        
        } finally {
            
            if(session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Return the rows containing the specified text in the specified column.
     * 
     * @param rowClass The table annotation class (the table to get the row from)
     * @param session The session to do the work in
     * @param columnName The name of the column to search for the text in
     * @param searchText The string of text to be located
     * @param <E> the type of object to return
     * @return The list of rows found to contain the specified text
     */
    public <E> List<E> selectRowsByText(Class<E> rowClass, Session session, String columnName, String searchText){      		
    	
    	Set<E> result = new HashSet<E>();
    	
    	Scanner textParser = new Scanner(searchText);

    	String currentTerm;
    	
    	//scan the search text for each term matching the recognized search term pattern
    	while((currentTerm = textParser.findInLine(searchTermPattern)) != null){		
    		
    		//if a term starts with a '-', then all rows captured by searching for the remainder of the term will be
    		//removed from the result
    		if(currentTerm.startsWith("-")){
    			   			
    			//if there are already rows in the result, search for the remainder of the search term and remove all
    			//rows found in the search
    			if(!result.isEmpty()){    				
    				result.removeAll(selectRowsByText(rowClass, session, columnName, currentTerm.substring(1)));
    			
    			//otherwise, add all the rows in the table to the result, search for the remainder of the search term, 
    			//and remove all rows found in the search
    			}else{    				
    				Query query = session.createQuery("from "+rowClass.getName()+" where " + columnName + " like '%%'");
        			
        			List<?> rowsRaw = query.list();
        	       
        	        for (Object obj : rowsRaw) {
        	            result.add(rowClass.cast(obj));
        	        }
        	        
        	        result.removeAll(selectRowsByText(rowClass, session, columnName, currentTerm.substring(1)));
    			}    			
    		
    		//if a term matches the regular expression for a binary operator chain, perform the binary operations
    		//specified and add the resulting rows to the result
    		}else if(currentTerm.matches(binaryOperatorExpression)){
    			
    			//parse the binary operator chain for its operands
    			List<String> operands = Arrays.asList(currentTerm.split("\\s+AND\\s+|\\s+OR\\s+")); 
    			
    			//parse the binary operator chain for its operators
    			for(String operand: operands){
    				currentTerm = currentTerm.replaceAll(operand, "");
    			}
    			
    			currentTerm = currentTerm.trim();
    			
    			List<String> operators = Arrays.asList(currentTerm.split("\\s+"));
    			
    			//for each operand, perform the next binary operation specified using result of the previous 
    			//binary operation and the operand itself
    			Set<E> binaryOpResult = new HashSet<E>();    			
    			for(String operand : operands){
    				
    				int i = operands.indexOf(operand);
    						
    				if(operands.indexOf(operand) == 0){
    					binaryOpResult.addAll(selectRowsByText(rowClass, session, columnName, operand));   	
    					
    				}else{
    					if(operators.get(i-1).matches("AND")){
    						binaryOpResult.retainAll(selectRowsByText(rowClass, session, columnName, operand));
    						
    					}else if(operators.get(i-1).matches("OR")){
    						binaryOpResult.addAll(selectRowsByText(rowClass, session, columnName, operand));
    					}
    				}
    			}
    			
    			//add what rows remain to the result
    			result.addAll(binaryOpResult);
    		
    		//otherwise, treat the term as a single phrase
    		}else{
    			
    			//if the term begins and ends with quotes, remove them before performing the database query
    			if(currentTerm.startsWith("\"") && currentTerm.endsWith("\"")){
        			currentTerm = currentTerm.substring(1, currentTerm.length()-1);
        		}
    			
    			//query the database for all rows with text in the specified column matching the search term
    			Query query = session.createQuery("from "+rowClass.getName()+" where lower(" + columnName + ") like lower('%"+currentTerm+"%')");
    			
    			List<?> rowsRaw = query.list();
    	       
    			//add the rows received to the result
    	        for (Object obj : rowsRaw) {
    	            result.add(rowClass.cast(obj));
    	        }
    			
    		}   		
    	}
    	
    	textParser.close();	   	
    	
    	return new ArrayList<E>(result);
    }

    /**
	 * Return the sql string for the hibernate criteria
	 * 
	 * @param criteria
	 * @return String - a sql query string
	 */
	@SuppressWarnings("unused")
	private static String toSql(Criteria criteria){

		try{
			CriteriaImpl c = (CriteriaImpl) criteria;
			SessionImpl s = (SessionImpl)c.getSession();
			SessionFactoryImplementor factory = (SessionFactoryImplementor)s.getSessionFactory();
			String[] implementors = factory.getImplementors( c.getEntityOrClassName() );
			CriteriaLoader loader = new CriteriaLoader((OuterJoinLoadable)factory.getEntityPersister(implementors[0]),
					factory, c, implementors[0], s.getLoadQueryInfluencers());
			Field f = OuterJoinLoader.class.getDeclaredField("sql");
			f.setAccessible(true);
			return (String) f.get(loader);
		}
		catch(Exception e){
			throw new RuntimeException(e); 
		}
	}

}
