/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.util.List;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.SurveyItemProperties;


/**
 * The property set relating to the setting that controls the dynamic multi-response fields.
 * 
 * @author sharrison
 *
 */
public class DynamicResponseFieldPropertySet extends AbstractPropertySet  {

    /**
     * Constructor (default)
     * 
     * @param defaultStartingResponseType the response type to push into the
     *        property set as the first element.
     */
	public DynamicResponseFieldPropertySet() {
	    super();

	    properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELDS_PER_LINE, 2);
	    properties.setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_TYPES, "");
	    properties.setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LABELS, "");
	    properties.setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LEFT_ALIGNED, "");
	    
	    // The answer field text box property set is shown to the user.
	    setHiddenPropertySet(false);
	}

    /**
     * Returns the list of response field types (e.g. Numeric, Free Text, etc...)
     * 
     * @return the list of response field types
     */
    public List<String> getResponseFieldTypes() {
        return SurveyItemProperties.decodeListString((String) properties.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_TYPES),
                Constants.COMMA);
    }

    /**
     * Returns the list of response field labels
     * 
     * @return the list of response field labels
     */
    public List<String> getResponseFieldLabels() {
        return SurveyItemProperties.decodeListString((String) properties.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LABELS),
                Constants.PIPE);
    }
    
    /**
     * Returns the list of response field left aligned booleans
     * 
     * @return the list of response field booleans indicating if the label is left aligned
     */
    public List<String> getResponseFieldLeftAligned() {
        return SurveyItemProperties.decodeListString((String) properties.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LEFT_ALIGNED),
                Constants.COMMA);
    }
    
    /**
     * Returns the number of response fields per line.
     * 
     * @return the number of response fields per line.
     */
    public Integer getResponsesPerLine() {
        return properties.getIntegerPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELDS_PER_LINE);
    }

    @Override
    public void load(SurveyItemProperties props) throws LoadSurveyException {
        if (props.getIntegerPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELDS_PER_LINE) != null) {
            properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELDS_PER_LINE,
                    props.getIntegerPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELDS_PER_LINE));
        }

        if (props.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_TYPES) != null) {
            properties.setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_TYPES,
                    props.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_TYPES));
        }

        if (props.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LABELS) != null) {
            properties.setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LABELS,
                    props.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LABELS));
        }
        
        if (props.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LEFT_ALIGNED) != null) {
            properties.setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LEFT_ALIGNED,
                    props.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LEFT_ALIGNED));
        }
    }
}
