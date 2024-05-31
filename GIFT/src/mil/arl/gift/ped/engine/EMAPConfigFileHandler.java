/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped.engine;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.ped.EMAP;
import mil.arl.gift.common.course.PedagogyFileValidationException;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.ped.PedagogicalModuleProperties;

/**
 * Responsible for parsing a eMAP XML file into a generated class instance.
 * 
 * @author mhoffman
 *
 */
public class EMAPConfigFileHandler extends AbstractSchemaHandler {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EMAPConfigFileHandler.class);
    /** the eMAP object */
    private EMAP eMAP;
    /** the pedagogy config file */
    private static final File eMAP_CONFIG = new File(PedagogicalModuleProperties.getInstance().getEMAPConfigFileName());
    
    /**
     * Class constructor - parses and validates the eMAP contents and sets the eMAP object
     * 
     * @param eMapXmlContent the contents of the eMAP xml file to use.  If null the default configuration is used.
     * @param schemaFile the schema file to use to validate the eMAP XML file.  If null the default schema is used.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws PedagogyFileValidationException if there was a problem configuration the pedagogical model using the default configuration file.
     * @throws DetailedException if there was a problem configuring the pedagogical model using the content provided
     */
    public EMAPConfigFileHandler(String eMapXmlContent, File schemaFile, boolean failOnFirstSchemaError) throws PedagogyFileValidationException, DetailedException {
        super(schemaFile != null ? schemaFile : EMAP_PEDAGOGICAL_SCHEMA_FILE);
        
        if(eMapXmlContent == null){
            logger.info("Parsing default EMAP configuration from "+eMAP_CONFIG);
            try{
                UnmarshalledFile uFile = parseAndValidate(generated.ped.EMAP.class, eMAP_CONFIG, failOnFirstSchemaError);
                eMAP = (EMAP) uFile.getUnmarshalled();
            }catch(Exception e){
                throw new PedagogyFileValidationException("Failed to parse and validate the eMAP file against the schema.",
                        e.getMessage(),
                        eMAP_CONFIG.getAbsolutePath(),
                        e);
            }
        }else{
            try{
                UnmarshalledFile uFile = parseAndValidate(generated.ped.EMAP.class, eMapXmlContent, failOnFirstSchemaError);
                eMAP = (EMAP) uFile.getUnmarshalled();
            }catch(Exception e){
                throw new DetailedException("Failed to parse and validate the eMAP serialized content against the schema.",
                        e.getMessage(),
                        e);
            }
        }
        
        try{
            checkEMAP(eMAP);
		} catch (Exception e) {
            throw new DetailedException("An error occurred while checking the eMAP configuration.",
                    e.getMessage(),
                    e);
		}

    }
    
    /**
     * Class constructor - parses and validates the eMAP contents and sets the eMAP object
     * 
     * @param file the pedagogical configuration file to use
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws PedagogyFileValidationException if there was a problem configuration the pedagogical model using the configuration file.
     */
    public EMAPConfigFileHandler(FileProxy file, boolean failOnFirstSchemaError) throws PedagogyFileValidationException {
        super(EMAP_PEDAGOGICAL_SCHEMA_FILE);
        
        try{
            UnmarshalledFile uFile = parseAndValidate(generated.ped.EMAP.class, file.getInputStream(), failOnFirstSchemaError);
            eMAP = (EMAP) uFile.getUnmarshalled();
        }catch(Exception e){
            throw new PedagogyFileValidationException("Failed to parse and validate the eMAP file against the schema.",
                    e.getMessage(),
                    file.getFileId(),
                    e);
        }
    }

	/**
     * Return the eMAP generated class instance
     * 
     * @return the eMAP object containing the values from the file
     */
    public EMAP getEMAP(){
        return eMAP;
    }
    
    /**
     * Check the eMAP for errors.
     * 
     * @param eMAP - the content to check
     * @throws Exception - if there was an error discovered during the check
     */
    public static void checkEMAP(generated.ped.EMAP eMAP) throws Exception{
        
        if(eMAP == null){
            throw new IllegalArgumentException("The eMAP can't be null.");
        }
        
        generated.ped.Attributes attributes;
        try{
            attributes = eMAP.getExample().getAttributes();
            checkAttributes(attributes);
        }catch(Exception e){
            logger.error("There was a problem with the Example element.", e);
            throw new Exception("A check of the Example element failed, see log for more details.");
        }
        
        try{
            attributes = eMAP.getPractice().getAttributes();
            checkAttributes(attributes);
        }catch(Exception e){
            logger.error("There was a problem with the Practice element.", e);
            throw new Exception("A check of the Practice element failed, see log for more details.");
        }
        
        try{
            attributes = eMAP.getRecall().getAttributes();
            checkAttributes(attributes);
        }catch(Exception e){
            logger.error("There was a problem with the Recall element.", e);
            throw new Exception("A check of the Recall element failed, see log for more details.");
        }
        
        try{
            attributes = eMAP.getRule().getAttributes();
            checkAttributes(attributes);
        }catch(Exception e){
            logger.error("There was a problem with the Rule element.", e);
            throw new Exception("A check of the Rule element failed, see log for more details.");
        }
    }
    
    /**
     * Check the attributes for errors.
     * 
     * @param attributes - the attributes to check
     */
    public static void checkAttributes(generated.ped.Attributes attributes){
        
        if(attributes == null){
            throw new IllegalArgumentException("The attributes can't be null.");
            
        }else if(attributes.getAttribute() == null){
            throw new IllegalArgumentException("The attributes list can't be null.");
            
        }else if(attributes.getAttribute().isEmpty()){
            throw new IllegalArgumentException("The attributes list can't be empty.");
            
        }
        
        for(generated.ped.Attribute attribute : attributes.getAttribute()){
            
            String typeStr = attribute.getType();
            LearnerStateAttributeNameEnum learnerAttribute = LearnerStateAttributeNameEnum.valueOf(typeStr);
            
            String valueStr = attribute.getValue();
            learnerAttribute.getAttributeValue(valueStr);
            
            checkMetadataAttributes(attribute.getMetadataAttributes());
        }
    }
    
    /**
     * Check the metadata attributes for errors.
     * 
     * @param attributes - the attributes to check
     */
    public static void checkMetadataAttributes(generated.ped.MetadataAttributes attributes){
        
        if(attributes == null){
            throw new IllegalArgumentException("The metadata attributes can't be null.");
            
        }else if(attributes.getMetadataAttribute() == null){
            throw new IllegalArgumentException("The metadata attributes list can't be null.");
            
        }else if(attributes.getMetadataAttribute().isEmpty()){
            throw new IllegalArgumentException("The metadata attributes list can't be empty.");
            
        }
        
        for(generated.ped.MetadataAttribute attribute : attributes.getMetadataAttribute()){
            
            String typeStr = attribute.getValue();
            MetadataAttributeEnum.valueOf(typeStr);
        }
    }

}
