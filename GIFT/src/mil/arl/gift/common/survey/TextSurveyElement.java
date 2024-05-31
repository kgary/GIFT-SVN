/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;

/**
 * A survey element that displays text (possibly formatted as HTML) in a survey
 *
 * @author jleonard
 */
public class TextSurveyElement extends AbstractSurveyElement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required for GWT compatibility
     */
    public TextSurveyElement() {
        super();
    }

    /**
     * Constructor
     *
     * @param surveyElementId The ID of this survey element
     * @param pageId The ID of the page this text element is in
     * @param properties The properties of this text element
     */
    public TextSurveyElement(int surveyElementId, int pageId, SurveyItemProperties properties) {
        super(surveyElementId, pageId, properties);
    }

    @Override
    public SurveyElementTypeEnum getSurveyElementType() {

        return SurveyElementTypeEnum.TEXT_ELEMENT;
    }

    /**
     * Gets the text (or HTML) to display in the text block
     *
     * @return String The text (or HTML) to display in the text block
     */
    public String getText() {

        return (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.TEXT);
    }

    /**
     * Sets the text (or HTML) to display in the text block
     *
     * @param text The text (or HTML) to display in the text block
     */
    public void setText(String text) {

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.TEXT, text);
    }
}
