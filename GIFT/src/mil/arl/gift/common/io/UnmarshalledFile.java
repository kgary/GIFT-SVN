/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.ValidationEvent;

import mil.arl.gift.common.util.CollectionUtils;

/**
 * This class contains the results of umarshalling a GIFT XML file.
 *  
 * @author mhoffman
 *
 */
public class UnmarshalledFile {

    /** 
     * whether the serialized unmarshalled object was up-converted to be compliant
     * with the latest GIFT version of that file type
     */
    private boolean upconverted = false;
    
    /**
     * the serialized unmarshalled object
     */
    private Serializable serializableObj;
    
    /**
     * (optional) the serialized unmarshalled object before being converted.
     */
    private Serializable preconvertedObj = null;
    
    /**
     * (optional) the GIFT version number of the unmarshalled object before being converted
     */
    private String preconvertedObjversion;  
    
    /**
     * (optional) contains conversion issues that occurred when converting the unmarshalled contents of the file
     */
    private ConversionIssueList conversionIssues;
    
    /**
     * (optional) contains issues with parsing the XML content against the schema
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     */
    private List<ValidationEvent> validationEvents;
    
    /**
     * Set attribute(s)
     * 
     * @param serializableObj the serialized unmarshalled object. Can't be null.
     */
    public UnmarshalledFile(Serializable serializableObj){
        
        if(serializableObj == null){
            throw new IllegalArgumentException("The serialized object can't be null.");
        }
        
        this.serializableObj = serializableObj;
    }
    
    /**
     * Return the unmarshalled object
     * 
     * @return
     */
    public Serializable getUnmarshalled(){
        return serializableObj;
    }
    
    /**
     * Returns the original unmarshalled object that is not up-converted.
     * May be null if no preconverted object was set.
     * 
     * @return the original unmarshalled object or null if no object was set.
     */
    public Serializable getPreConverted(){
    	return preconvertedObj;
    }
    
    /**
     * Sets the serialized unmarshalled object that was not up-converted.
     * 
     * @param preconvertedObj2 the serialized unmarshalled object that was not up-converted. Can be null.
     */
    public void setPreConverted(Serializable preconvertedObj){
    	this.preconvertedObj = preconvertedObj;
    }
    
    /**
     * Set the conversions issues for the unmarshalled and converted file.  
     * 
     * @param conversionIssues can be null if there was no conversion or no issues during conversion
     */
    public void setConversionIssueList(ConversionIssueList conversionIssues){
        this.conversionIssues = conversionIssues;
    }
    
    /**
     * Return the conversion issues that occurred during converting this file.
     * 
     * @return can be null or empty if there was no conversion or no issues during conversion
     */
    public ConversionIssueList getConversionIssues(){
        return conversionIssues;
    }
    
    /**
     * Set whether the serialized unmarshalled object was up-converted to be compliant
     * with the latest GIFT version of that file type
     * 
     * @param value
     */
    public void setUpconverted(boolean value){
        this.upconverted = value;
    }
    
    /**
     * Return whether the serialized unmarshalled object was up-converted to be compliant
     * with the latest GIFT version of that file type
     * 
     * @return
     */
    public boolean isUpconverted(){
        return upconverted;
    }
    
    /**
     * Sets the GIFT version of the serialized unmarshalled object that was not up-converted.
     * 
     * @param version the version of the serialized unmarshalled object. Can be null.
     */
    public void setPreconvertedVersion(String version) {
		this.preconvertedObjversion = version;
	}
    
    /**
     * Returns the version of the serialized unmarshalled object before it was up-converted or null if no version was set.
     * 
     * @return the version of the serialized unmarshalled object or null if no version was set.
     */
	public String getPreconvertedVersion() {
		return preconvertedObjversion;
	}
    
	/**
	 * Returns the issues with parsing the XML content against the schema
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * 
	 * @return can be null or empty
	 */
    public List<ValidationEvent> getValidationEvents() {
        return validationEvents;
    }

    public void setValidationEvents(List<ValidationEvent> validationEvents) {
        this.validationEvents = validationEvents;
    }

    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[UnmarshalledFile: ");
        sb.append("upconverted = ").append(isUpconverted());
        sb.append(", preconverted version = ").append(getPreconvertedVersion());
        sb.append(", conversion issues = ").append(getConversionIssues() != null && getConversionIssues().isPopulated());
        
        if(CollectionUtils.isNotEmpty(validationEvents)){
            sb.append(", validation events size = ").append(validationEvents.size());
        }
        sb.append("]");
        return sb.toString();
    }
}
