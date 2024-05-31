/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;

import generated.proto.common.ProtobufLogMessageProto.ProtobufLogMessage;
import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;

/**
 * This class is responsible for writing administrative or system level message sent 
 * between modules to a log file.
 * 
 * @author mhoffman
 *
 */
public class AdminMessageLogger extends AbstractMessageLogger {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AdminMessageLogger.class);

    /** strings used to create file name */
    private static final String PREFIX = "system";
    
    /** directory to place the log file */
    private static final String DIR = PackageUtil.getMessageLog();
    
    /** the duration in milliseconds that a system message log file should contain messages for. */
    private int systemMsgLogDurationMs;
	
    /**
     * Class constructor - create the log file
     * 
     * @param systemMsgLogDuration the duration in hours that a system message log file should contain
     * messages for.
     */
	public AdminMessageLogger(int systemMsgLogDuration){
		super(DIR, PREFIX);		
	      
        if(systemMsgLogDuration <= 0){
            throw new IllegalArgumentException("The system log duration of "+systemMsgLogDuration+" must be greater than zero.");
        }
        
        //convert hours to milliseconds
        this.systemMsgLogDurationMs = systemMsgLogDuration * 60 * 60 * 1000;
		
		ModuleStatusListener moduleStatusListener = new ModuleStatusListener() {

            @Override
            public void moduleStatusRemoved(StatusReceivedInfo status) {
                handleMsgLogDuration();
                writeModuleStatusChange(System.currentTimeMillis(), status.getModuleStatus(), false);   
            }

            @Override
            public void moduleStatusChanged(long sentTime, ModuleStatus status) {
            }

            @Override
            public void moduleStatusAdded(long sentTime, ModuleStatus status) {
                handleMsgLogDuration();
                writeModuleStatusChange(sentTime, status, true);      
            }
        };

        ModuleStatusMonitor.getInstance().addListener(moduleStatusListener);
	}
	
	@Override
    public void handleMessage(Message message, String rawMsg, MessageEncodingTypeEnum encodingType) {
        handleMsgLogDuration();
        if (MessageEncodingTypeEnum.BINARY.equals(encodingType)) {
            ProtobufMessage protobufMessage = new ProtobufMessageProtoCodec().map(message);
            writeToFile(ProtobufLogMessage.newBuilder().setMessage(protobufMessage).build());
        } else {
            logger.error("Tried to log a message with an unsupported encoding type: " + encodingType);
        }
    }
	
	/**
	 * Check if the log file duration has been reached.  If so, create a new log file with the same prefix
	 * and in the same directory as the original file.
	 */
	private void handleMsgLogDuration(){
	    
        if(systemMsgLogStart != 0 && (System.currentTimeMillis() - systemMsgLogStart) > systemMsgLogDurationMs){
            //check the duration since the first logged event, if passed the configured duration
            //create a new log file            
            createLogFile();
        }
	}
	
	/**
	 * Write a module status change message to the log
	 * 
	 * @param timestamp - the time to record for the module status change event
	 * @param status - the module's status
	 * @param isNewModule - whether the module has been discovered or been removed
	 */
    private void writeModuleStatusChange(long timestamp, ModuleStatus status, boolean isNewModule) {
        final String customString;
        if (isNewModule) {
            // write module discovered message
            customString = "Module Discovered: " + status.getModuleName() + ", " + status;

        } else {
            // write module removed message
            customString = "Module Removed: " + status.getModuleName() + ", " + status;
        }

        final ProtobufLogMessage.Builder logMessage = ProtobufLogMessage.newBuilder();
        logMessage.setElapsedWriteTime(DoubleValue.of(timestamp));
        writeToFile(logMessage.setCustomString(StringValue.of(customString)).build());
    }
}
