/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.proto.common.ProtobufLogMessageProto.ProtobufLogMessage;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.logger.ProtobufMessageLogReader;
import mil.arl.gift.net.api.message.Message;

/**
 * This class is the base class for message loggers.  It handles creating and writing to the log file.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractMessageLogger {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractMessageLogger.class);
	
    protected static final String EXTENSION = ".log"; 
    
	/** file writer */
    protected OutputStream output = null;
	
	/** the log file */
	private File file;
	
	private String directory;
	private String filePrefix;	
    
    /** when the first message was written */
    protected long systemMsgLogStart;
    
    /** The class-level lock that the file writer will synchronize on. This is 
     *  to prevent the file writer being closed while a message is still being 
     *  written to the log file. */
    private final Object lock = new Object();
	
	/**
	 * Class constructor - create the log file
	 * 
	 * @param directory - the directory to create the file in
	 * @param filePrefix - the file name's prefix
	 */
	protected AbstractMessageLogger(String directory, String filePrefix){
	    
	    this.directory = directory;
	    this.filePrefix = filePrefix;
	    
		createLogFile();
	}
	
	/**
	 * Create the log file in the given directory and with the given prefix in the name
	 */
	protected void createLogFile(){
		
		try{
            systemMsgLogStart = 0;  //reset to start time for this log file
            file = new File(directory + File.separator + filePrefix + "_"+ TimeUtil.formatTimeLogFilename(System.currentTimeMillis()) + ProtobufMessageLogReader.PROTOBUF_LOG_FILE_EXTENSION);
            if(logger.isInfoEnabled()){
                logger.info("Creating message logger at " + file);
            }
            output = new FileOutputStream(file);
		}catch(Exception e){
			logger.error("Caught exception when trying to create log file",e);
		}
	}	
    
    /**
     * Return the log file
     * 
     * @return the log file
     */
    public File getFile(){
    	return file;
    }
	
	/**
     * Writes the message to the protobuf logger file. writeDelimitedTo() is
     * used since it appends the size of the message before encoding it,
     * allowing for multiple messages to be written to a file.
     * 
     * @param protoMsg
     *            the protobuf log message that will be written to the file.
     */
    public void writeToFile(ProtobufLogMessage protoMsg) {
        try {
            /* Check if the file writer is open before attempting to write messages. 
             * Synchronizing here protects against the case where the file writer 
             * is closed before messages are finished being written to the log file. */
            synchronized (lock) {
                if (output != null) {
                    protoMsg.writeDelimitedTo(output);
                }               
            }            
        } catch (IOException e) {
            logger.error("Caught exception when trying to write data to log file", e);
        }
    }
	
	/**
     * Close both writers. If the writers were closed previously, this method
     * does nothing.
     * 
     * @throws IOException if there was a problem closing the writer.
     */
	public void close() throws IOException{
	    synchronized (lock) {
            if (output != null) {
                 output.close();
                 output = null;
            }
        }
	}
	
   /**
     * Log the provided message accordingly
     * 
     * @param message - the message to log
     * @param rawMsg - the raw message to log
     * @param encodingType - the type of codec used on this message
	 */
    public abstract void handleMessage(Message message, String rawMsg, MessageEncodingTypeEnum encodingType);
}
