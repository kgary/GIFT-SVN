/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.lms.impl.common.AbstractLms;

/**
 * This handler is responsible for parsing an LMS connections file and then initializing any connections
 * it specifies.
 * 
 * @author mhoffman
 *
 */
public class LMSConnectionsFileHandler extends AbstractSchemaHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LMSConnectionsFileHandler.class);
    
    /** the default LMS connections file name */
    private static final String DEFAULT_FILE_NAME = PackageUtil.getConfiguration() + File.separator+"lms"+File.separator+"LMSConnections.xml";
    
    /** the LMS connections file being used */
    private File file;
    
    /** 
     * map of enabled and initialized LMS connections 
     * 
     * Key: name of connection from LMS connections xml
     * Value: the LMS connection implementation
     */
    private Map<String, AbstractLms> lmsImplementations = new HashMap<>(); 
    
    /**
     * Class constructor - use default LMS connections file
     * @throws FileValidationException if there was a problem parsing and validating the LMS connections file
     */
    public LMSConnectionsFileHandler() throws FileValidationException{
        this(DEFAULT_FILE_NAME);
    }
    
    /**
     * Class constructor - use specified LMS connections file to initialize LMS connections.
     * 
     * @param connectionsFileName the LMS connections file to parse in order to initialize LMS connection(s).
     * @throws FileValidationException if there was a problem parsing and validating the LMS connections file
     */
    public LMSConnectionsFileHandler(String connectionsFileName) throws FileValidationException{
        super(LMS_CONNECTIONS_SCHEMA_FILE);
        
        if(connectionsFileName == null){
            throw new IllegalArgumentException("The connections file name can't be null");
        }
        
        file = new File(connectionsFileName);
        if(!file.exists()){
            throw new IllegalArgumentException("Unable to find connections file named "+connectionsFileName);
        }
        
        configure();
    }
    
    /**
     * Return the collection of initialized LMS connections.
     * 
     * @return Map<String, AbstractLms> initialized LMS connection(s).
     */
    public Map<String, AbstractLms> getLMSImplementations(){
        return lmsImplementations;
    }
    
    /**
     * Parse the LMS connections file and create the specified connections.
     * @throws FileValidationException if there was a problem parsing and validating the LMS connections file
     */
    public void configure() throws FileValidationException{
        
        generated.lms.LMSConnections connections = null;
        try{
            UnmarshalledFile uFile = parseAndValidate(generated.lms.LMSConnections.class, file, true);
            connections = (generated.lms.LMSConnections) uFile.getUnmarshalled();
            initializeConnections(connections);
        }catch(Throwable e){
            logger.error("Caught exception while parsing LMS connections file "+file.getAbsolutePath(), e);
            throw new FileValidationException("Failed to parse and validate the LMS connections file.",
                    e.getMessage(),
                    file.getName(), 
                    e);
        }       
        
    }
    
    /**
     * Instantiate and connect to the LMS connections specified.
     * 
     * @param connections the LMS connections information
     * @throws IllegalArgumentException if the connections parameter is invalid
     * @throws ConfigurationException if there was a problem configuring any of the connections
     */
    private void initializeConnections(generated.lms.LMSConnections connections) throws IllegalArgumentException, ConfigurationException{
        
        if(connections == null){
            throw new IllegalArgumentException("The LMS connections is null.");
        }
        
        for(generated.lms.Connection connection : connections.getConnection()){
            
            if(!connection.isEnabled()){
                if(logger.isInfoEnabled()){
                    logger.info("Skipping LMS connection named "+connection.getName()+" because it is NOT enabled.");
                }
                continue;
            }
            
            try{
                AbstractLms lms = (AbstractLms)Class.forName(PackageUtil.getRoot()+ "." + connection.getImpl()).getDeclaredConstructor().newInstance();
                lms.setName(connection.getName());
                lms.connect(connection.getParameters());
                
                lmsImplementations.put(connection.getName(), lms);
                
                if(logger.isInfoEnabled()){
                    logger.info("Initialized LMS of "+lms+".");
                }
                
            }catch(Exception e){
                logger.error("Caught exception while trying to initialize LMS connection named "+connection.getName()+" with implementation class of "+connection.getImpl()+".", e);
                throw new ConfigurationException("Unable to initialize LMS connection named "+connection.getName()+".",
                        e.getMessage(),
                        e);                
            }
        }
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[LMSConnectionsFileHandler: ");
        
        sb.append("connections = {");
        for(String name : getLMSImplementations().keySet()){
            sb.append(" ").append(getLMSImplementations().get(name)).append(",");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
