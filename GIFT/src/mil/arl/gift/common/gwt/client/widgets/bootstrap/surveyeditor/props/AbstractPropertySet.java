/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props;

import java.io.Serializable;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SaveSurveyException;
import mil.arl.gift.common.survey.SurveyItemProperties;


/**
 * The AbstractPropertySet class encapsulates a list of related SurveyItemProperties.
 * In the properties panel, these properties are grouped according to the property set that
 * they are contained in.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(AbstractPropertySet.class.getName());
   

    /** The list of properties that make up the property set. */
    protected SurveyItemProperties properties = new SurveyItemProperties();
    
    
    /** Determines if the propertyset should be displayed in the UI or if the properties are hidden from the user. */
    private boolean hiddenPropertySet = false;
    
    
	/**
	 * Constructor (default)
	 */
	public AbstractPropertySet() {
	    

	}
	
	/**
	 * Accessor to get the entire property list for the property set.
	 * 
	 * @return SurveyItemProperties - The properties for the property set.
	 */
	public SurveyItemProperties getProperties() {
	    return properties;
	}
	
	/**
	 * Accessor to get an Integer property value from the property set.
	 * 
	 * @param key - The property key to lookup.
	 * @return Integer - The Integer value of the property (if found).  If it cannot be found, null is returned.
	 */
	public Integer getIntegerPropertyValue(SurveyPropertyKeyEnum key) {
	    return properties.getIntegerPropertyValue(key);
	}
	
	/**
     * Accessor to get a Boolean property value from the property set.
     * 
     * @param key - The property key to lookup.
     * @return Boolean - The Boolean value of the property (if found).  If it cannot be found, null is returned.
     */
	public Boolean getBooleanPropertyValue(SurveyPropertyKeyEnum key) {
	    return properties.getBooleanPropertyValue(key);
	}
	
	/**
     * Accessor to get a Serializable property value from the property set.
     * 
     * @param key - The property key to lookup.
     * @return Serializable - The Serializable value of the property (if found).  If it cannot be found, null is returned.
     */
	public Serializable getPropertyValue(SurveyPropertyKeyEnum key) {
	    return properties.getPropertyValue(key);
	}
	
	/**
	 * Load handler for loading the AbstractPropertySet with the specific properties from the survey database.
	 * 
	 * @param props - The survey item properties from the survey database.
	 */
	public abstract void load(SurveyItemProperties props) throws LoadSurveyException;

	/**
	 * Returns true if the property set is a hidden property set, which means the property
	 * set should not be displayed in the UI.  However the properties will still be loaded
	 * and saved to the database.
	 * 
	 * @return
	 */
    public boolean isHiddenPropertySet() {
        return hiddenPropertySet;
    }

    /**
     * Sets if the property set should be hidden property set, which means the property 
     * set should not be displayed in the UI.  However the properties will still be loaded
     * and saved to the database.
     * 
     * @param hiddenPropertySet
     */
    public void setHiddenPropertySet(boolean hiddenPropertySet) {
        this.hiddenPropertySet = hiddenPropertySet;
    }

    /**
     * Saves the property set to the database by converting to server side database classes.
     * 
     * @param props - The property set that will be saved to the database.
     */
    public void save(SurveyItemProperties props) throws SaveSurveyException {
        properties.copyInto(props);
        
    }
	
	

}
