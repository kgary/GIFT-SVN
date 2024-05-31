/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

/**
 * Contains properties that the client-side SAS needs
 *
 * @author jleonard
 */
public class GwtSurveySystemProperties {
    
	/** URL of the servlet handling files uploaded for importing. This is configurable in src/mil/arl/gift/tools/sas/war/WEB-INF/web.xml */
	public static final String IMPORT_SERVLET_URL = "surveyImports/";
	
    public static final String SURVEY_IMAGE_UPLOAD_URL = "surveyWebResources/uploadedImages/";    
    
    /** 
     * this is the GIFT Key used in a survey context which has concept related questions added
     * to it.  Those questions are placed in a survey associated with this key and that survey
     * represents a question bank for this survey context.
     * 
     * Note: this must match mil.arl.gift.common.survey.Constants.java KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY class attribute value
     */
    public static final String KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY = "Knowledge Assessment Question Bank";
    
    /**
     * This post fix is appended to the survey name for knowledge assessment question bank surveys.
     */
    public static final String KNOWLEDGE_ASSESSMENT_QBANK_GIFT_SURVEY_NAME_POST_FIX = " Survey Context - " + KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY;
    
    /**
     * this is a regular expression describing the GIFT KEY used in a survey context which 
     * has concept related questions that have been used in a knowledge assessment, concept based survey. 
     * These generated concept surveys are associated with the key described by this regular expression. 
     * 
     * Note: this must match mil.arl.gift.common.survey.Constants.java KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX class attribute 
     * value
     */
    public static final String KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX = 
            KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY + " : [0-9]+";
    
}
