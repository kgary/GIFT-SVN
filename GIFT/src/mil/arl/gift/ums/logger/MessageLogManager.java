/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.logger;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.usersession.AbstractUserSessionDetails;
import mil.arl.gift.common.usersession.LtiUserSessionDetails;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.ums.UMSModule;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.table.DbDataCollection;
import mil.arl.gift.ums.db.table.DbDataCollectionResultsLti;
import mil.arl.gift.ums.db.table.DbExperimentSubject;

/**
 * This class manages where a particular message type will be logged.  There are two types
 * of message log files: administrative/system and domain session.
 * 
 * @author mhoffman
 *
 */
public class MessageLogManager {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MessageLogManager.class);
    
    /** instance of the admin message logger */
	private AdminMessageLogger adminLogger;
	
	/** map of domain session id to an instance of a domain session logger */
	private Map<Integer, DomainSessionLogger> domainSessionToLogger = new HashMap<Integer, DomainSessionLogger>();
	
	/**
	 * Class constructor
	 * 
	 * @param systemMsgLogDuration the duration in hours that a system message log file should contain
     * messages for.
	 */
	public MessageLogManager(int systemMsgLogDuration){
		
		adminLogger = new AdminMessageLogger(systemMsgLogDuration);
	}
	
	/**
	 * Log the message in the appropriate log file
	 * 
     * @param message - the message to log
     * @param rawMsg - the raw message to log
     * @param encodingType - the type of codec used on this message
	 */
    public void handleMessage(Message message, String rawMsg, MessageEncodingTypeEnum encodingType){
		
		//determine the type of message (Admin/System or Domain)
		if(message instanceof DomainSessionMessage){
		    
            DomainSessionMessage dMessage = (DomainSessionMessage)message;
            
            //get domain session logger instance
            DomainSessionLogger domainLogger = domainSessionToLogger.get(dMessage.getDomainSessionId());
            
            if(domainLogger != null){
                domainLogger.handleMessage(message, rawMsg, encodingType);                
                
            }else{
                logger.error("Can't log message = "+dMessage+" - Unable to get domain session logger for domain session id = "+dMessage.getDomainSessionId());
            }

			
        }else{
			
            adminLogger.handleMessage(message, rawMsg, encodingType);
            
            //We check to see if it is coming from the UMS Module because it is the one that generates the original Domain Selection Reply
            //Other modules forward it but we are concerned if the UMS sends the same Domain Selection Reply twice
            if(message.getMessageType() == MessageTypeEnum.DOMAIN_SELECTION_REPLY && message.getSenderModuleType() == ModuleTypeEnum.UMS_MODULE){
                //domain session was just created, create new domain session logger
                
                DomainSession dsMessage = (DomainSession)message.getPayload();
                if(domainSessionToLogger.containsKey(dsMessage.getDomainSessionId())){
                    logger.error("Received another "+message.getMessageType()+" message type after handling the first one by creating a domain session message log.");
                }else{

                    DomainSessionLogger sessionLogger = new DomainSessionLogger(dsMessage);
                    domainSessionToLogger.put(dsMessage.getDomainSessionId(), sessionLogger);
                    
                    //notify UMS to update db entries
                    UMSModule.getInstance().eventFileCreatedNotification(sessionLogger.getFile().getName(), dsMessage.getDomainSessionId());
                    
                    if(dsMessage.getExperimentId() != null){
                        //update subject entry with message log file name
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Updating subject entry with new domain session message log file name of '"+sessionLogger.getFile().getName()+"' for experiment represented by experiment id of '"+dsMessage.getExperimentId()+"'.");
                        }
                        DbExperimentSubject dbSubject = UMSDatabaseManager.getInstance().getExperimentSubject(dsMessage.getExperimentId(), dsMessage.getUserId());
                        dbSubject.setMessageLogFilename(dsMessage.buildLogFileName() + File.separator + sessionLogger.getFile().getName());
                        UMSDatabaseManager.getInstance().updateRow(dbSubject);
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Updated new experiment subject of "+dbSubject);
                        }
                    } else if (dsMessage.isSessionType(UserSessionType.LTI_USER)){
                        
                        AbstractUserSessionDetails abstractDetails = dsMessage.getSessionDetails();
                        if (abstractDetails != null && abstractDetails instanceof LtiUserSessionDetails) {
                            LtiUserSessionDetails details = (LtiUserSessionDetails)abstractDetails;
                            
                            if (details.getDataSetId() != null) {
                                // The LTI session and it belongs to a data collection data set, so it needs to be tracked in the database.
                               DbDataCollectionResultsLti dataCollectionRecord = new DbDataCollectionResultsLti(details.getLtiUserId(), 
                                       details.getDataSetId(), 
                                       dsMessage.buildLogFileName() + File.separator + sessionLogger.getFile().getName(), 
                                       new Date());
                               
                               try {
                                   UMSDatabaseManager.getInstance().insertRow(dataCollectionRecord);
                                } catch (ConfigurationException e) {
                                    logger.error(
                                            "ConfigurationException caught while trying  to log a data collection result for the lti session: "
                                                    + details,
                                            e);
                                } catch (Exception e) {
                                    logger.error(
                                            "Exception caught while trying to log a data collection result for the lti session: "
                                                    + details,
                                            e);
                                }
                            }
                               
                        }
                    } else if(dsMessage.isSessionType(UserSessionType.GIFT_USER)){
                        // update the inherited 'course tile' publish course that all courses have.
                        
                        // get path to course folder path (e.g. mhoffman/new course) from course id (E.g. mhoffman/new course/new course.course.xml)
                        String courseFilePath = dsMessage.getDomainSourceId();
                        String courseFolderPath = courseFilePath.substring(0, courseFilePath.lastIndexOf("/"));
                        String courseFolderName = courseFolderPath.substring(courseFolderPath.indexOf("/")+1, courseFolderPath.length());
                        
                        // check if this course already has a 'course tile' published course
                        DbDataCollection dataCollection;
                        List<DbDataCollection> dataCollections = 
                                UMSDatabaseManager.getInstance().getPublishedCoursesOfType(courseFolderPath, DataSetType.COURSE_DATA, true, null);
                        if(dataCollections.isEmpty()){
                            // create the published course row
                            
                            // look up the course in the db to get the owner
                            CourseRecord courseRecord = UMSDatabaseManager.getInstance().getCourseByPath(courseFilePath);
                            if(courseRecord == null) {
                                // A course can not be found if the course was manually copied to GIFT workspace folder and has never been saved.
                                logger.error("Unable to create a new subject entry in the database for the course at '"+courseFolderPath+
                                        "' because that course was not found in the table with all the courses.  Was this course manually copied "+
                                        "into a workspace folder?  If so, try saving the course in the course creator first.");
                                return;
                            }
                            
                            if(courseRecord.isPublicOwner()){
                                // don't create publish course automatically for Public courses since we won't know who the author is
                                return;
                            }
                            
                            String owner = courseRecord.getOwnerName();
                            try{
                                dataCollection = UMSDatabaseManager.getInstance().createExperiment(courseFolderName, 
                                        null, owner, DataSetType.COURSE_DATA, null);
                            }catch(Exception e){
                                logger.error("Failed to auto create published course db row for "+dsMessage, e);
                                return;
                            }
                        }else{
                            dataCollection = dataCollections.get(0);
                        }
                        
                            
                        if(logger.isInfoEnabled()){
                            logger.info("Updating subject entry with new domain session message log file name of '"+sessionLogger.getFile().getName()+"' for "+DataSetType.COURSE_DATA+" published course type represented by id of '"+dataCollection.getId()+"'.");
                        }
                        DbExperimentSubject dbSubject = UMSDatabaseManager.getInstance().getExperimentSubject(dataCollection.getId(), dsMessage.getSubjectId());
                        dbSubject.setMessageLogFilename(dsMessage.buildLogFileName() + File.separator + sessionLogger.getFile().getName());
                        UMSDatabaseManager.getInstance().updateRow(dbSubject);
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Updated subject of "+dbSubject+" for published course "+dataCollection);
                        }

                    }

                    if(logger.isInfoEnabled()){
                        logger.info("Created domain session logger for domain session id = "+dsMessage.getDomainSessionId());
                    }
                }
            }
			
		}

	}
	
}
