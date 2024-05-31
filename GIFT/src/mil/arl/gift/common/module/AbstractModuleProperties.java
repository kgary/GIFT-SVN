/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.io.CommonProperties;

/**
 * This is the base class for module property classes and contains the property file parser
 * and common module properties.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractModuleProperties extends CommonProperties {  
    
    /** common property names */
    protected static final String MODULE_NAME = "ModuleName";
    protected static final String ACTIVEMQ_ADDRESS = "ActiveMQURL";
    protected static final String MESSAGE_ENCODING_TYPE = "MessageEncodingType";
    protected static final String IGNORE_IPADDR_ALLOCATION = "IgnoreIPAddrAllocation";

    /** The python server port number that will be used.  */
    protected static final String PYTHON_SERVER_PORT = "XMLRpcPythonServerPort";
    /** The python class name that will be used.  Methods that are called, must be in this class. */
    protected static final String PYTHON_SERVER_CLASS_NAME = "XMLRpcPythonServerClassName";
    /** Flag to enable/disable the starting of the python server.  If this is set to false, the module will not attempt 
     *  to start the python server.  If this is set to true, then the python server will be started using the other python xmlrpc
     *  parameters in this file.
     */
    protected static final String START_PYTHON_SERVER = "StartXMLRpcPythonServer";
    /** A value to indicate an invalid port for the python server */
    public static final int PYTHON_SERVER_INVALID_PORT = 0;
    
    private static final String COMMON_MODULE_PROP_FILE = "module.common.properties";
    
    /** message encoding type for this module */
    private MessageEncodingTypeEnum encodingType = null;
    
    /**
     * Class constructor - create parser instance
     * 
     * @param propertyFileName the module property file name relative to the GIFT/config directory
     */
    public AbstractModuleProperties(String propertyFileName){
        super(propertyFileName, COMMON_MODULE_PROP_FILE);
    }
    
    /**
     * Class constructor used in Server mode when working with remote gateway zip - create parser instance
     * 
     * @param propertyFileName the module property file name relative to the GIFT/config directory
     * @param remotePropertyFileName the module property file name found within the remote gateway zip
     */
    public AbstractModuleProperties(String propertyFileName, String remotePropertyFileName){
        super(propertyFileName, remotePropertyFileName, COMMON_MODULE_PROP_FILE);
    }
        
    /**
     * Return the message broker URL
     * 
     * @return String
     */
    public String getBrokerURL(){
        return getPropertyValue(ACTIVEMQ_ADDRESS);
    }
    
    /**
     * Return the module name
     * 
     * @return String
     */
    public String getModuleName(){
        return getPropertyValue(MODULE_NAME);
    }

    /**
     * Return the enumerated message encoding type for this module.
     * 
     * @return MessageEncodingTypeEnum
     */
    public MessageEncodingTypeEnum getMessageEncodingType(){
        
        if(encodingType == null){
            encodingType = MessageEncodingTypeEnum.valueOf(getPropertyValue(MESSAGE_ENCODING_TYPE));
        }
        
        return encodingType;
    }
    
    /**
     * Return whether or not to ignore any ip address filtering when it comes to module allocation
     * for message clients.
     * 
     * @return boolean
     */
    public boolean shouldIgnoreIPAddrAllocation(){
        return getPropertyBooleanValue(IGNORE_IPADDR_ALLOCATION);
    }
    
    /**
     * Return if the learner module should start the xml rpc python server.  Default is false.
     * 
     * @return boolean - true if the learner module should start the xml rpc python server.
     */
    public boolean getStartXMLRpcPythonServer() {
        return getPropertyBooleanValue(START_PYTHON_SERVER, false);
    }
    
    
    /**
     * Return the port that the XML Rpc python server is started on.  A valid port (non zero) must be specified.  Default is 0 (invalid).
     * 
     * @return int - port that the server should be configured to start on.
     */
    public int getXMLRpcPythonServerPort() {
        return getPropertyIntValue(PYTHON_SERVER_PORT, PYTHON_SERVER_INVALID_PORT);
    }
    
    /**
     * Return the class that the python server will register for the learner module.  If no class is specified, an empty string is returned.
     * 
     * @return String - Return the name of the python server class name.  An empty string is returned if the property cannot be found.
     */
    public String getXMLRpcPythonServerClassName() {
        return getPropertyValue(PYTHON_SERVER_CLASS_NAME, "");
        
    }
    
    @Override
    protected void setCommandLineArgs(List<Option> optionsList, String[] args) {
        
        List<Option> moduleOptionsList = new ArrayList<>();
        
        OptionBuilder.withArgName("name");
        
        OptionBuilder.hasArg();
        
        OptionBuilder.withDescription("The name of the module");
        
        moduleOptionsList.add(OptionBuilder.create(MODULE_NAME));
        
        OptionBuilder.withArgName("url");
        
        OptionBuilder.hasArg();
        
        OptionBuilder.withDescription("The URL of the ActiveMQ to connect to");
        
        moduleOptionsList.add(OptionBuilder.create(ACTIVEMQ_ADDRESS));
        
        OptionBuilder.withArgName("encoding");
        
        OptionBuilder.hasArg();
        
        OptionBuilder.withDescription("The message encoding type");
        
        moduleOptionsList.add(OptionBuilder.create(MESSAGE_ENCODING_TYPE));
                
        OptionBuilder.withArgName("boolean");
        
        OptionBuilder.hasArg();
        
        OptionBuilder.withDescription("To ignore any ip address filtering when it comes to module allocation");
        
        moduleOptionsList.add(OptionBuilder.create(IGNORE_IPADDR_ALLOCATION));
        
        if(optionsList != null) {
        
            moduleOptionsList.addAll(optionsList);
        }
        
        super.setCommandLineArgs(moduleOptionsList, args);
    }
}
