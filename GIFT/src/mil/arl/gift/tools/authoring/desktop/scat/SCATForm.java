/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.scat;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import generated.sensor.SensorsConfiguration;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.sensor.AbstractEventProducer;
import mil.arl.gift.sensor.SensorsConfigFileHandler;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolForm;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.IdGenerator;

/**
 * This class extends the XML authoring tool form by providing SCAT specific parameters.
 * In addition it can validate the sensor configuration xml using the sensor configuration file handler.
 * 
 * @author mhoffman
 *
 */
public class SCATForm extends XMLAuthoringToolForm {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SCATForm.class);
    
    /** default title of form and prefix used when xml has been loaded*/
    private static final String TITLE_PREFIX = "Sensor Configuration Authoring Tool"; 
    
    /** the documentation */
    private static URI DOC_FILE = HelpDocuments.getConfigSettingsDoc();
    
    /** the root generated class for the sensor config content*/
    private static final Class<?> ROOT_CLASS = SensorsConfiguration.class;
    
    private static final String FILTERS = "Filters";
    private static final String FILTER  = "Filter";
    private static final String WRITERS = "Writers";
    private static final String WRITER  = "Writer";
    private static final String SENSORS = "Sensors";
    private static final String SENSOR  = "Sensor";
    private static final String ID      = "id";
    
    //the following block checks to make sure the class structure of Filter, Sensor and Writer from the xsd hasn't changed
    //as this class uses that structure to to find node elements in the tree.
    //If changes are made to this structure or the names of class fields, then the code that walks/searches nodes in this class
    //will need to be changed accordingly.
    static{       
          generated.sensor.Filter filter = new generated.sensor.Filter();
          filter.getId();
          generated.sensor.Filters filters = new generated.sensor.Filters();
          filters.getFilter();
          generated.sensor.Writer writer = new generated.sensor.Writer();
          writer.getId();
          generated.sensor.Writers writers = new generated.sensor.Writers();
          writers.getWriter();
          generated.sensor.Sensor sensor = new generated.sensor.Sensor();
          sensor.getId();
          generated.sensor.Sensors sensors = new generated.sensor.Sensors();
          sensors.getSensor();
    }
    
    private static final String FILE_COMMENT = "\n\tThis file was created with the GIFT Sensor Configuration Authoring Tool (SCAT).\n" +
    		"\tThis file contains the configuration for sensors for the sensor module.\n"+  
    		"\tMultiple sensors, filters and writers can be configured using this file.\n"+  
    		"\tFor specific sensor configuration examples look at the various sensor configuration xml files (e.g. SensorConfiguration.Mouse.xml).\n";

    /**
     * Default constructor - use default schema file
     * @throws DetailedException if there was a problem with the sensor configuration schema file
     */
    public SCATForm() throws DetailedException{
        this(AbstractSchemaHandler.SENSOR_SCHEMA_FILE);

    }
    
    /**
     * Class constructor - use custom schema file
     * 
     * @param schemaFile - the sensor config schema file
     * @throws DetailedException if there was a problem with the sensor configuration schema file
     */
    public SCATForm(File schemaFile) throws DetailedException{
        super(schemaFile, ROOT_CLASS, SCATProperties.getInstance().getWorkspaceDirectory(), TITLE_PREFIX, FILE_COMMENT, DOC_FILE, AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION);
    }
    
    /**
     * Initialize and show the authoring tool form
     */
    public void init(){
        super.setVisible(true);
    }
    
    @Override
    protected Object executeConversion(FileProxy fileToConvert) throws Exception{
    	
    	// Get the conversion util based off of which version is being converted.
    	AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(fileToConvert, true);
    	
    	if (conversionUtil != null) {    	
    		UnmarshalledFile uFile = conversionUtil.convertSensorConfiguration(fileToConvert, true, true);
    		generated.sensor.SensorsConfiguration newSensorsConfig = (SensorsConfiguration) uFile.getUnmarshalled();
        
    		return newSensorsConfig;
    	}
    	
    	return null;
    }
    
    @Override
    public void giftValidate(FileProxy contentFile) throws Throwable{

        Collection<AbstractEventProducer> producers = null;
        try{
            SensorsConfigFileHandler handler = new SensorsConfigFileHandler(contentFile);
            producers = handler.getEventProducers();
            
            //
            //test the sensors 
            //
            if(SCATProperties.getInstance().shouldTestSensors()){
                
                handler.testSensors();
                
            }else{
                logger.info("Skipped testing sensors based on SCAT configuration property.  Check scat.properties for more details");
            }
            
        } finally{
            
            if(producers != null){
                //if there were any producers instantiated, they need to be cleaned up
                
                for(AbstractEventProducer eProducer : producers){
                    
                    eProducer.dispose();
                }
            }
        }
    }
    
    @Override
    protected boolean newXML(){
        
        if(super.newXML()){
            
            //reset id generator for new file
            IdGenerator.setGlobalId(0);
            
            return true;
        }
        
        return false;
    }
    
    @Override
    protected boolean loadXML(File file){        
        
        if(super.loadXML(file)){
            
            //
            // get highest id among sensors, writers and filters
            //
            int highestId = 0;
            int id;
            
            //SENSORS-
            
            Element rootNode = getRootNode();
            
            NodeList nl = rootNode.getElementsByTagName(SENSORS);
            if(nl != null && nl.getLength() == 1){
                //found sensors element, the root of the sensors list
                
                Element sensors = (Element)nl.item(0);
                
                nl = sensors.getElementsByTagName(SENSOR);
                if(nl != null){
                    
                    //get info for each Sensor
                    for(int index = 0; index < nl.getLength(); index++){
                        
                        Element sensor = (Element)nl.item(index);
                        String idStr = sensor.getAttribute(ID);
                        if(idStr == null || idStr.length() == 0){
                            continue;
                        }
                        id = Integer.valueOf(idStr);
                        
                        if(id > highestId){
                            highestId = id;
                        }
                    }
                }
            }
            
            //FILTERS-
            
            nl = rootNode.getElementsByTagName(FILTERS);
            if(nl != null && nl.getLength() == 1){
                //found filters element, the root of the filters list
                
                Element filters = (Element)nl.item(0);
                
                nl = filters.getElementsByTagName(FILTER);
                if(nl != null){
                    
                    //get info for each Filter
                    for(int filterIndex = 0; filterIndex < nl.getLength(); filterIndex++){
                        
                        Element filter = (Element)nl.item(filterIndex);
                        String idStr = filter.getAttribute(ID);
                        if(idStr == null || idStr.length() == 0){
                            continue;
                        }
                        id = Integer.valueOf(idStr);
                        
                        if(id > highestId){
                            highestId = id;
                        }
                    }
                }
            }
            
            //WRITERS-
            
            nl = rootNode.getElementsByTagName(WRITERS);
            if(nl != null && nl.getLength() == 1){
                //found writers element, the root of the writers list
                
                Element writers = (Element)nl.item(0);
                
                nl = writers.getElementsByTagName(WRITER);
                if(nl != null){
                    
                    //get info for each Sensor
                    for(int index = 0; index < nl.getLength(); index++){
                        
                        Element writer = (Element)nl.item(index);
                        String idStr = writer.getAttribute(ID);
                        if(idStr == null || idStr.length() == 0){
                            continue;
                        }
                        id = Integer.valueOf(idStr);
                        
                        if(id > highestId){
                            highestId = id;
                        }
                    }
                }
            }
            
            IdGenerator.setGlobalId(highestId);
            
            return true;
        }
        
        return false;
    }
}
