/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.logger;


import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;

import generated.proto.common.ProtobufLogMessageProto.ProtobufLogMessage;
import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;


/**
 * This class is responsible for writing a single domain session's messages sent
 * between modules to a log file.
 *  
 * @author mhoffman
 *
 */
public class DomainSessionLogger extends AbstractMessageLogger  {
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(DomainSessionLogger.class);
    
    /** strings used to create file name */
    private static final String PREFIX = "domainSession";
    private static final String USER_PREFIX = "_uId";

    /** directory to place the log file */
    private static final String DIR = PackageUtil.getDomainSessions();
    
    /** the epoch time when the domain session started */
    private Long domainSessionStartTime = null;
    
    /** information about the domain session whose messages will be logged */
    private DomainSession domainSession;
	
	/**
	 * Class constructor - create the log file
	 *    
	 * @param domainSession - information about the domain session whose messages will be logged
	 */
    public DomainSessionLogger(DomainSession domainSession){
    	super(DIR, buildFileName(domainSession));
    	
    	this.domainSession = domainSession;
    }
    
    /**
     * Return a generated log file name from the domain session info provided.
     * 
     * @param domainSession contains information about a domain session.  Can't be null.
     * @return the generated log file name for the domain session. E.g. domainSession697_uId1\domainSession697_uId1
     */
    private static String buildFileName(DomainSession domainSession){
        File domainSessionsFolder = new File(DIR);
        domainSessionsFolder.mkdir();
        
        String str = domainSession.buildLogFileName();
        
        // create the folder where all of the output files for this domain session will be written
        File domSession = new File(DIR + File.separator + str);
        domSession.mkdir();
                
        return str + File.separator + str;
    }
    
    /**
     * Parses the domain session message log file name that this class created for the 
     * user id and domain session id.
     * 
     * @param domainSessionFile a domain session message log file that was written by this class
     * @return domain session object containing the user id and domain session id.  Null will be returned if
     * the ids could not be found successfully.
     */
    public static DomainSession populateIdsFromFileName(File domainSessionFile){
        
        String fileName = domainSessionFile.getName();
        int prefixIndex = fileName.indexOf(PREFIX);
        if(prefixIndex == 0){
            
            int dsId;
            int userId;
            
            int dsIdIndexStart = prefixIndex + PREFIX.length();
            int dsIdIndexEnd = fileName.indexOf(USER_PREFIX, dsIdIndexStart);
            
            if(dsIdIndexEnd != -1){
                dsId = Integer.valueOf(fileName.substring(dsIdIndexStart, dsIdIndexEnd));
                
                int userIdIndexStart = dsIdIndexEnd + USER_PREFIX.length();
                int userIdIndexEnd = fileName.indexOf("_", userIdIndexStart);
                
                if(userIdIndexEnd != -1){
                    userId = Integer.valueOf(fileName.substring(userIdIndexStart, userIdIndexEnd));
                    return new DomainSession(dsId, userId, DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
                }else{
                    //ERROR
                    return null;
                }
            }else{
                //ERROR
                return null;
            }
        }else{
            //ERROR
            return null;
        }
    }
    
    @Override
    public void handleMessage(Message message, String rawMsg, MessageEncodingTypeEnum encodingType) {
        if (message.getMessageType() == MessageTypeEnum.START_DOMAIN_SESSION) {
            domainSessionStartTime = Long.valueOf(message.getTimeStamp());
        }

        String elapsedDSTime, elapsedWriteTime;
        if (domainSessionStartTime != null) {
            // first display the relative time from start to message creation
            elapsedDSTime = TimeUtil.formatTimeRelative(domainSessionStartTime.longValue(),
                    message.getTimeStamp());

            // second display the relative time from start to log write
            elapsedWriteTime = TimeUtil.formatTimeRelative(domainSessionStartTime.longValue(),
                    System.currentTimeMillis());
        } else {
            elapsedDSTime = TimeUtil.getDefaultRelativeTime();
            elapsedWriteTime = TimeUtil.getDefaultRelativeTime();
        }

        if (MessageEncodingTypeEnum.BINARY.equals(encodingType)) {
            ProtobufMessage protobufMessage = new ProtobufMessageProtoCodec().map(message);
            ProtobufLogMessage logMessage = ProtobufLogMessage.newBuilder().setMessage(protobufMessage)
                    .setElapsedDsTime(DoubleValue.of(Double.valueOf(elapsedDSTime)))
                    .setElapsedWriteTime(DoubleValue.of(Double.valueOf(elapsedWriteTime))).build();
            writeToFile(logMessage);
        } else {
            logger.error("Tried to log a message with an unsupported encoding type: " + encodingType);
        }
        
        if (message.getMessageType() == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
            // since we have no domain session ended message, use the fact that a module is 
            // requesting to close the domain session as an indication that session is being terminated
            // Allow 5 seconds before closing the writer to account for any domain session messages 
            // that come in after this
            // If we don't close the log here, the Java process will maintain a handle on the file
            // and the file can't be changed in any way until the Java process is ended.
            new Thread(new Runnable() {
                
                @Override
                public void run() {

                    try{
                        Thread.sleep(5000);
                        close();
                    }catch (Exception e){
                        logger.warn("There was a problem while closing the domain session log for "+domainSession, e);
                    }
                }
            }).start();
        }
    }

}
