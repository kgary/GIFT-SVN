/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metadata;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;

/**
 * This is the base class for metadata file handler.  It includes a reference to the default metadata schema.
 * 
 * @author mhoffman
 *
 */
public class AbstractMetadataFileHandler extends AbstractSchemaHandler {
    
    /**
     * Default constructor - use default schema
     */
    public AbstractMetadataFileHandler(){
        super(METADATA_SCHEMA_FILE);
        
    }
    
    /**
     * Class constructor - use specified schema
     * 
     * @param schemaFile - a metadata schema file
     */
    public AbstractMetadataFileHandler(String schemaFile){
        super(schemaFile);
        
    }
  
    /**
     * Return a string representation of the metadata attributes content in XML format.
     * 
     *  @param attributes - metadata attributes element
     * @return String - metadata attributes element as a string
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when unmarshalling
     */
    public static String getRawAttributes(generated.metadata.Attributes attributes) throws SAXException, JAXBException{
        return getAsXMLString(attributes, generated.metadata.Attributes.class, METADATA_SCHEMA_FILE);        
    }
    
    /**
     * Return an Attributes object from the raw XML actions element string provided.
     * 
     * @param rawAttributes - metadata attributes element as a string
     * @return Attributes - new Attributes object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when unmarshalling
     */
    public generated.metadata.Attributes getAttributes(String rawAttributes) throws JAXBException, SAXException{      
        UnmarshalledFile uFile = parseAndValidate(generated.metadata.Attributes.class, rawAttributes, true);
        return (generated.metadata.Attributes)uFile.getUnmarshalled();
    }
    
    /**
     * Return a string representation of the metadata attribute content in XML format.
     * 
     *  @param attribute - metadata attribute element
     * @return String - metadata attribute element as a string
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when unmarshalling
     */
    public static String getRawAttribute(generated.metadata.Attribute attribute) throws SAXException, JAXBException{
        return getAsXMLString(attribute, generated.metadata.Attribute.class, METADATA_SCHEMA_FILE);        
    }
    
    /**
     * Return an Attribute object from the raw XML actions element string provided.
     * 
     * @param rawAttribute - metadata attribute element as a string
     * @return Attribute - new Attribute object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when unmarshalling
     */
    public generated.metadata.Attribute getAttribute(String rawAttribute) throws JAXBException, SAXException{  
        UnmarshalledFile uFile = parseAndValidate(generated.metadata.Attribute.class, rawAttribute, true);
        return (generated.metadata.Attribute)uFile.getUnmarshalled();
    }
}
