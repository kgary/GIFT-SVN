/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.simple.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.db.AbstractHibernateDatabaseManager;
import mil.arl.gift.common.score.AbstractRawScore;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.simple.db.table.GradedScoreNode;
import mil.arl.gift.lms.impl.simple.db.table.RawScoreNode;
import mil.arl.gift.lms.impl.simple.db.table.ScoreMetadata;

/**
 * This class is the main interface to the LMS database back-end.  It contains methods to 
 * select, insert, update and delete rows in the database tables.
 * 
 * @author mhoffman
 *
 */
public class LMSDatabaseManager extends AbstractHibernateDatabaseManager {
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(LMSDatabaseManager.class);

	//singleton instance of this class
	private static LMSDatabaseManager instance = null;
	
	/**
	 * Return the singleton instance of this class
	 * 
	 * @return DatabaseManager
	 */
	public static LMSDatabaseManager getInstance(){
		
		if(instance == null){
			instance = new LMSDatabaseManager();
		}
		
		return instance;
	}
	
    /**
     * Class constructor
     * 
     * @throws ConfigurationException if there was a problem initializing the LMS database
     */
    private LMSDatabaseManager() throws ConfigurationException{
        super(LMSHibernateUtil.getInstance());
        
        try{
            initLMSDb();
        }catch(Exception e){
            throw new ConfigurationException("Failed to initialize LMS database connection.", "An exception was thrown that reads:\n"+e.getMessage(), e);
        }
    }
    
    /**
     * Perform any additional LMS database initialization.
     */
    private void initLMSDb(){
        
        LMSHibernateUtil.getInstance().postStaticInit();
    }
	
	@Override
    public List<Class<? extends Object>> getUserDataTableClasses() {
        List<Class<? extends Object>> userDataTables = new ArrayList<>();
        
        userDataTables.add(AbstractRawScore.class);
        userDataTables.add(AbstractScoreNode.class);

        userDataTables.add(ScoreMetadata.class);
        userDataTables.add(GradedScoreNode.class);

        
        userDataTables.add(RawScoreNode.class);

        
        return userDataTables;
    }
	
	/**
	 * Return the records for the course specified in the GIFT LMS database this class is connected too.
	 * 
	 * @param userId unique id of the GIFT user to retrieve records for
	 * @param domainName the specific course name to look for.  If null than this is not used as a query parameter.
	 * e.g. Public/TSP 07-GFT-0137 Vignettes/TSP 07-GFT-0137 ClearBldg.jtc_shakarat.course.xml
	 * @param domainSessionIds used to filter for specific LMS records with the given domain session ids.  Can be null or empty to indicate that 
     * this field is not used.
	 * @param pageStart the index of the first record returned in this request. 
	 * For example if the request should return the 5th and onward records, the value should be 4 (zero based index). 
	 * Must be non-negative. Zero indicates to start with the first record that satisfies the request requirements.
	 * @param pageSize how many records to return, must be non-negative. Zero indicates to return all records that satisfy the request requirements.
	 * @param orderByDescendingDate whether to sort the records by date with the latest records first. False will sort for ascending date (oldest first)
	 * @return the metadata for the records found.  Can be empty but not null.
	 */
	public List<ScoreMetadata> getCourseRecords(int userId, String domainName, Set<Integer> domainSessionIds, int pageStart, int pageSize, boolean orderByDescendingDate){
	    
        Session session = getCurrentSession();
        session.beginTransaction();
        try{
            String queryString = "from ScoreMetadata where userId = "+userId;
            Map<String, Object> params = new HashMap<>();
            if(StringUtils.isNotBlank(domainName)){
                //need to build a query here to handle special characters (e.g. single quotes) in the course name
                queryString += " and domainName = :domainNameValue";
                
                params.put("domainNameValue", domainName);
            }
            
            if(CollectionUtils.isNotEmpty(domainSessionIds)){
                //need to build a query here for domain session ids
                queryString += " and domainSessionId in :domainSessionIds";
                params.put("domainSessionIds", domainSessionIds);
            }
            
            // Order By must go at the end
            queryString += " order by time";
            if(orderByDescendingDate){
                queryString += " desc";
            }
            
            Query query = session.createQuery(queryString);
            for(String paramName : params.keySet()){
                if(params.get(paramName) instanceof java.util.Collection<?>){
                    query.setParameterList(paramName, (java.util.Collection<?>) params.get(paramName)); 
                }else{
                    query.setParameter(paramName, params.get(paramName)); 
                }
            } 
            return selectRowsByQuery(ScoreMetadata.class, query, pageStart <= 0 ? -1 : pageStart, pageSize <= 0 ? -1 : pageSize, session);

        }finally{
            
            if(session.isOpen()){
                session.close();
            }
        }

	}
	
    @Override
    public void clearUserData() throws HibernateException{
        
        StringBuffer sb = new StringBuffer();
        sb.append("Clear User Data results:\n");
        
        Session session = createNewSession();
        
        session.beginTransaction();
        
        for(Class<?> tableClass : getUserDataTableClasses()){
                       
            //Note: can't use SQL Delete query here because Graded Score Node and AbstractScoreNode have circular foreign key relationships
            //      therefore it will cause a foreign key constraint exception no matter the order of the 'delete *' operation on these 2 classes.
            List<?> rows = selectAllRows(tableClass);
            for(Object row : rows){
                try{
                    deleteRow(row, session);
                }catch(Exception e){
                    sb.append("FAILED to delete row: ").append(row).append("because ").append(e.getMessage());
                }
            }
            
            sb.append("Deleted all rows from ").append(tableClass.getName()).append("\n");
            
            //Check for user primary key id tables
            for(String tableName : getTableGeneratorTableName(tableClass)){
                SQLQuery query = session.createSQLQuery("DELETE FROM app."+tableName);
                query.executeUpdate();
            }
        }

        session.getTransaction().commit();
        session.close();

        logger.info(sb.toString());
    }
    
    /**
     * Used to interact with the LMS database via scripts or outside of the UMS module.
     * 
     * @param args - used to identify which logic to execution.  To see the list of arguments, run w/o an argument.
     */
    public static void main(String[] args){
        
        //use LMS log4j
        PropertyConfigurator.configure(PackageUtil.getConfiguration() + "/lms/lms.log4j.properties");
        
        boolean displayHelp = false;
        
        if(args.length == 1){
            
            switch(args[0]){
            
            case "clear":
                //Clear the user data from the database
                
                try{
                    System.out.println("Starting to clear user data...");
                    LMSDatabaseManager.getInstance().clearUserData();
                    System.out.println("Finished clearing user data. ");
                    
                }catch(Exception e){
                    System.out.println("Caught exception while trying to clear user data.");
                    e.printStackTrace();
                    displayHelp = true;
                }
                
                break;
                
            case "recreate-db":
                LMSDatabaseManager.getInstance().recreateDB();
                break;
                
            default:
                displayHelp = true;
            }
            
        }else{
            displayHelp = true;
        }
        
        if(displayHelp){
            System.out.println("Usage: LMSDatabaseManager <option>\n" +
                    "\t clear\t\t clear rows from tables created by executing user and domain sessions.\n" +
                    "\t recreate-db\t\t recreate the LMS database table schema."); 
        }
    }

}
