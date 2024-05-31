/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerInformation;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.filter.SensorFilterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is used by writer classes which want to write
 * information to a file.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractFileWriter extends AbstractWriter{

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractFileWriter.class);
    
    /** file writer properties */
    public static final String DIRECTORY = "directory";
    public static final String PREFIX = "prefix";
    
    protected static final String USER_ID_TOKEN = "uId";
    protected static final String DOMAIN_SESSION_ID_TOKEN = "sId";
    
    protected static final String FILE_SEPARATOR = System.getProperty("file.separator");
    protected static final String CSV_EXTENSION = ".csv";   
    protected static final String EOL = System.getProperty("line.separator");
    
    /*
     * The domain session that this writer should write sensor data for. 
     * Used to determine which domain session folder that sensor data files should be written to.
     */
    protected DomainSession domainSession = null;    
    
    /** the directory to create the file in */
    private String directory = PackageUtil.getDomainSessions();
    
    /** file to write data too */
	private String filename;
	
	/** the type of file extension to use */
	private String fileExtension;
	
	/** the prefix to the file name to use */
	private String prefix;
	
	/** file writer */
	protected BufferedWriter output = null;
	
	/**
	 * Class constructor - creates file
	 * 

	 * @param prefix - the file name prefix to use for the file being created and written too
	 * @param fileExtension - the type of file extension to use
	 * @param eventProducerInformation - attributes for which data will be written for
	 */
	public AbstractFileWriter(String prefix, String fileExtension, EventProducerInformation eventProducerInformation){
	    super(eventProducerInformation);
	    
	    this.fileExtension = fileExtension;
	    
	    setPrefix(prefix);
	}
	
	/**
	 * Return the filename (w/o path) for the file that this writer is writing too
	 * 
	 * @return String
	 */
	public String getFileName(){
		return filename;
	}
	
	/**
	 * Return the filename (w/ path) for the file that this writer is writing too
	 * 
	 * @return the file name of the file being written by this writer 
	 * (e.g. output/domainSessions/domainSession22_uId4_eId997b14ca-f69b-466c-8383-325d8d7589e9_2020-07-07_12-45-42/kinect.csv)
	 * If the domain session has not been set this will return null.
	 */
	public String getFullFileName(){
	    return domainSession != null ? directory + FILE_SEPARATOR + domainSession.buildLogFileName() + FILE_SEPARATOR + getFileName() : null;    
	}
	
	/**
	 * Return the folder name of the domain session folder this writer will create files in.
	 * @return e.g. output/domainSessions/domainSession22_uId4_eId997b14ca-f69b-466c-8383-325d8d7589e9_2020-07-07_12-45-42
	 * If the domain session has not been set this will return null.
	 */
	public String getDomainSessionFolder(){
	    return domainSession != null ?  directory + FILE_SEPARATOR + domainSession.buildLogFileName() : null;
	}
    
    /**
	 * Gets the prefix of the output file filename
     *
     * @return The prefix of the output file filename
     */
    public String getPrefix() {
        
        return prefix;
    }
    
    /**
	 * Gets the file extension of the output file
     *
     * @return The file extension of the output file
     */
    public String getFileExtension() {
        
        return fileExtension;
    }
	
	/**
	 * Initialize the file writer 
	 * 
     * @param fileName - the file name (w/o path) to write data too
     * @return fileCreated - whether the file was created
	 */
	public boolean initialize(String fileName){
		
	    boolean fileCreated = false;
	    
	    if(output == null){
	        try{
	            this.filename = fileName;
	            FileWriter fstream = new FileWriter(getFullFileName());
	            output = new BufferedWriter(fstream);

	            fileCreated = true;
	            if(logger.isInfoEnabled()){
	                logger.info("output file named "+fileName+" has been created");
	            }
	            
	        }catch(Exception e){
	            logger.error("Caught exception when trying to initialize AbstractFileWriter", e);
	            System.err.println("Caught exception when trying to initialize AbstractFileWriter");
	            e.printStackTrace();
	        }
	    }else{
	        logger.error("The output stream is already initialized for "+this);
	    }
		
	    return fileCreated;
	}
	
    @Override
    public boolean initialize(int userId, int domainSessionId, String experimentID){

        this.domainSession = new DomainSession(domainSessionId,  userId, DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
        
        if (experimentID != null) {
            this.domainSession.setExperimentId(experimentID);
        }
        
        //construct file name
        String name = prefix + "_" + USER_ID_TOKEN +userId+ "_" + DOMAIN_SESSION_ID_TOKEN + domainSessionId+ "_" + TimeUtil.formatTimeLogFilename(System.currentTimeMillis()) + fileExtension;
        
        return initialize(name);
    }
    
    /**
     * Parses the sensor data file name that this class created for the 
     * user id and domain session id.
     * 
     * @param sensorWriterFile a sensor data file that was written by this class
     * @return domain session object containing the user id and domain session id.  Null will be returned if
     * the ids could not be found successfully.
     */
    public static DomainSession populateIdsFromFileName(File sensorWriterFile){
        
        String fileName = sensorWriterFile.getName();
        int uIdIndex = fileName.indexOf(USER_ID_TOKEN);
        if(uIdIndex != -1){
            
            int uIdIndexStart = uIdIndex + USER_ID_TOKEN.length();
            int uIdIndexEnd = fileName.indexOf(DOMAIN_SESSION_ID_TOKEN, uIdIndexStart) - 1;  // subtract 1 for the "_"
            
            if(uIdIndexEnd > 0){
                int userId = Integer.valueOf(fileName.substring(uIdIndexStart, uIdIndexEnd));
                
                int dsIdIndexStart = uIdIndexEnd + DOMAIN_SESSION_ID_TOKEN.length() + 1; // add 1 for the "_"
                int dsIdIndexEnd = fileName.indexOf("_", dsIdIndexStart);
                
                if(dsIdIndexEnd != -1){
                    int dsId = Integer.valueOf(fileName.substring(dsIdIndexStart, dsIdIndexEnd));
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
    
    
    /**
     * Sensor filter has produced data
     * 
     * @param elapsedTime - elapsed domain session time
     * @param sensorFilterEvent - contains sensor filter data
     */
    @Override
    public void sensorFilterEvent(long elapsedTime, SensorFilterEvent sensorFilterEvent) throws IOException{
        
        if(logger.isInfoEnabled()){
            logger.info("Received sensor filter event to write to file - "+sensorFilterEvent);
        }
        
        //write data
        write(sensorFilterEvent);
    }
        
    /**
     * Sensor has produced data
     * 
     * @param sensorDataEvent - contains sensor data
     */
    @Override
    public void sensorDataEvent(SensorDataEvent sensorDataEvent) throws IOException{
        
        if(logger.isInfoEnabled()){
            logger.info("Received sensor data event to write to file - "+sensorDataEvent);
        }
        
        //write data
        write(sensorDataEvent);
    }
    
    /**
     * Set the prefix to the sensor output file
     * 
     * @param prefix The prefix of the sensor output file
     */
    private void setPrefix(String prefix){
        
        if(prefix == null){
            throw new IllegalArgumentException("the file writer prefix is null");
        }
        
        this.prefix = prefix;
        
    }
	
	/**
	 * Write the string to the file
	 * 
	 * @param str - string to write to the file
	 */
	@Override
    public synchronized void write(String str){

		if(output != null){
			try{
				output.write(str);	
				haveWrittenSomething = true;
			}catch(Exception e){
				logger.error("AbstractFileWriter: Caught exception when trying to write data to file", e);
			}
		}
	}
	
	/**
	 * Flush the writer stream
	 */
	public void flushWriter(){
		
		if(output != null){
			try{
				output.flush();
			}catch(Exception e){
				logger.error("AbstractFileWriter: Caught exception when trying to flush writer", e);
			}
		}
	}
	
	/**
	 * Close the writer stream
	 */
	public void close(){
		
        if(output != null){
            try{
                if(logger.isInfoEnabled()){
                    logger.info("Closing "+this);
                }
                output.close();
            }catch(Exception e){
                logger.error("AbstractFileWriter: Caught exception when trying to close writer", e);
            }
            
            output = null;
        }
	}
	
    @Override
    public void finished(){
        close();
        haveWrittenSomething = false;
    }
	
	@Override
	public String toString(){
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("[AbstractFileWriter:");
	    sb.append(", file name = ").append(getFullFileName());
	    sb.append("]");
	    
	    return sb.toString();
	}

}
