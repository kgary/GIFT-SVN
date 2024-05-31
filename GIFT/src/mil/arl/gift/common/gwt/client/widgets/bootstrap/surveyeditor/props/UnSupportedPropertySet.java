/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props;

import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.survey.SurveyItemProperties;

/**
 * The UnSupportedPropertySet is used to catch properties that are not yet exposed in the Survey Editor when
 * loading an AbstractSurveyElement object.  Any property found in the AbstractSurveyElement that does not belong
 * to any PropertySet supported by the Survey Editor is added to this property set so that:
 *   1) Most importantly it is saved back out with the survey and not 'lost'.
 *   2) The property is logged to the console to make it clear that the survey editor will not expose that property yet but
 *      that support could be added to make it exposed in the future.
 *      
 * This property set should remain hidden and the properties should not be exposed in any UI directly in this property set.
 * 
 * @author nblomberg
 *
 */
public class UnSupportedPropertySet extends AbstractPropertySet  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(UnSupportedPropertySet.class.getName());
   

	/**
	 * Constructor (default)
	 */
	public UnSupportedPropertySet() {
	    super();
	    
	    
	    setHiddenPropertySet(true);
	    
	}


    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	
    }

}
