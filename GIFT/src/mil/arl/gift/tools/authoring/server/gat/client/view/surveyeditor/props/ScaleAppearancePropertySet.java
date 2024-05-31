/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.survey.SurveyItemProperties;

/**
 * The ScaleAppearancePropertySet contains the set of properties that allow for customization
 * of the layout of the rating scale question.
 * 
 * @author nblomberg
 *
 */
public class ScaleAppearancePropertySet extends AbstractPropertySet  {

    /** The logger for the class */
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(ScaleAppearancePropertySet.class.getName());

	/**
	 * Constructor (default)
	 */
	public ScaleAppearancePropertySet() {
	    super();
	    
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY, false);
	    properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY, false);
	    
	    properties.setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, "");
        properties.setPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY, "");
        
        properties.setPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY,"");
	}

    /**
     * Gets whether or not the scale labels should be shown for the question
     * 
     * @return the value that has been set for the property, false if the
     * property is not set
     */
    public boolean getIsDisplayScaleLabels() {
        return properties.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY, false);
    }

    /**
     * Gets whether or not the reply option labels should be shown for the
     * question
     * 
     * @return the value that has been set for the property, false if the
     * property is not set
     */
    public boolean getReplyOptionLabelsAreHidden() {
        return properties.getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY, false);
    }

    /**
     * Gets the text that should be shown at the far left of the scale
     * 
     * @return the value of the text that has been set for the property, null if
     * the property is not set
     */
    public String getLeftExtremeLabel() {
        Serializable propValue = properties.getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
        return propValue instanceof String ? (String) propValue : null;
    }

    /**
     * Gets the list of labels that should be shown in the middle of the scale
     * 
     * @return the list of labels that has been set for the property, null if
     * the property is not set
     */
    public List<String> getMidScaleLabels() {
        return properties.getStringListPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY);
    }

    /**
     * Gets the text that should be shown at the far right of the scale
     * 
     * @return the value of the text that has been set for the property, null if
     * the property is not set
     */
    public String getRightExtremeLabel() {
        Serializable propValue = properties.getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY);
        return propValue instanceof String ? (String) propValue : null;
    }

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY));
    	}
    	if(props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY) != null){
    		properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY, props.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY));
    	}
    	if(props.getPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY) != null){
    		properties.setPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY, props.getPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY));
    	}
    }
}