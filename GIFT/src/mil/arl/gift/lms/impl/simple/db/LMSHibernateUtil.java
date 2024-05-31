/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.simple.db;


import java.io.File;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.db.AbstractHibernateUtil;
import mil.arl.gift.lms.impl.simple.db.table.AbstractRawScore;
import mil.arl.gift.lms.impl.simple.db.table.AbstractScoreNode;
import mil.arl.gift.lms.impl.simple.db.table.GradedScoreNode;
import mil.arl.gift.lms.impl.simple.db.table.RawScoreNode;
import mil.arl.gift.lms.impl.simple.db.table.ScoreMetadata;


import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the hibernate util class for LMS database.  It will help configure the connection
 * to the LMS database.
 * 
 * @author mhoffman
 *
 */
public class LMSHibernateUtil extends AbstractHibernateUtil  {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LMSHibernateUtil.class);

    /** name of the hibernate configuration file to use for this util  */
	private static final String CONFIG_FILE = "lms"+File.separator+"lms.hibernate.cfg.xml";
	   
    /** the folder that contains SQL scripts files to run (if needed) to bring the LMS db up to date with the latest development */
    private static final String SQL_SCRIPTS_FOLDER = "config" + File.separator + "lms" + File.separator + "sql" + File.separator;
    
    /** contains the statement to add usernames column to RawScoreNode table */
    private static final File ALTER_RAWSCORENODE_TABLE_WITH_USERNAMES = new File(SQL_SCRIPTS_FOLDER + "RawScoreNode.table.alter.UserNames.sql");
	
	/** the singleton instance of this class */
	private static LMSHibernateUtil instance = null;
	
	/**
	 * Return the singleton instance of this class
	 * 
	 * @return UMSHibernateUtil
	 */
	public static LMSHibernateUtil getInstance(){
		
		if(instance == null){
			instance = new LMSHibernateUtil();
		}
		
		return instance;
	}
	
	/**
	 * Class constructor
	 */
	private LMSHibernateUtil(){
		super(CONFIG_FILE);
	}
	
	@Override
	protected Configuration buildConfiguration(){
		
		try{
			Configuration configuration = new Configuration();
			
			//add all annotated class (@Entity classes)
			//-tells hibernate which classes are to be persisted (create table)
			configuration.addAnnotatedClass(AbstractRawScore.class);
			configuration.addAnnotatedClass(AbstractScoreNode.class);
			configuration.addAnnotatedClass(GradedScoreNode.class);
			configuration.addAnnotatedClass(RawScoreNode.class);
			configuration.addAnnotatedClass(ScoreMetadata.class);
            
			return configuration;
		
	    }catch (Throwable ex) {

	        // Make sure you log the exception, as it might be swallowed
	        logger.error("Initial Configuration creation failed.", ex);

	        throw new ExceptionInInitializerError(ex);
	    }

	}
	
    /**
     * Run additional statements to finish initializing the LMS db.
     *
     * @throws ConfigurationException if there was a problem running a script
     */
    public void postStaticInit() throws ConfigurationException{
        
        //update an existing Raw Score Node table if it does not have the UserNames column (new for GIFT 2021-1)
        try{
            applySqlFileIfColumnNotExist("RAWSCORENODE", "USERNAMES", ALTER_RAWSCORENODE_TABLE_WITH_USERNAMES);
        }catch(Exception e){
            throw new ConfigurationException("Failed to dynamically update the RawScoreNode table.",
                    "There was an exception thrown while trying to run the table update SQL statement from '"+ALTER_RAWSCORENODE_TABLE_WITH_USERNAMES+"'.", e);
        }
    }

}
