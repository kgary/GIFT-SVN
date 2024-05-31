/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.domain.DomainModuleProperties;

/**
 * This class is used to process HTML tags found in Survey elements.  In some cases URLs may need to be
 * handled differently when the content is hosted by the domain module and not the tutor module.
 * 
 * @author mhoffman
 *
 */
public class SurveyHTMLElementsHandler {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SurveyHTMLElementsHandler.class);
    
    /** important keys to search for in survey element hyperlinks */
    private static final String HYPERLINK_URL_ELEMENT = "href=";
    private static final String FILE_LINK_PREFIX = "file://";
    private static final String HYPERLINK_URL_FILE_KEY = HYPERLINK_URL_ELEMENT + "'" + FILE_LINK_PREFIX;
    
    /** server prefix to network resources */
    private static String networkURL;
    
    static{
    	
        try {
            networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + "/";
        } catch (Exception ex) {
            logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
            networkURL = DomainModuleProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
        }
    }

    /**
     * Process any found HTML tags in the various survey elements provided.
     * For example, if the survey has a text block that contains HTML hyperlink, this method will
     * process that URL.  If that URL has a reference to a domain hosted file, the appropriate network prefix
     * will be added in order for the tutor to be able to show that file in the browser.
     * 
     * @param survey the survey to process
     */
    public static void processSurveyHTMLElements(Survey survey){
        
        for(SurveyPage surveyPage : survey.getPages()){
            
            for (AbstractSurveyElement element : surveyPage.getElements()) {
                
                if (element.getSurveyElementType() == SurveyElementTypeEnum.TEXT_ELEMENT){
                    
                    TextSurveyElement textElement = (TextSurveyElement)element;
                    
                    String displayedText = textElement.getText();

                    if (displayedText != null) {
                        
                        //
                        // find hyperlinks that reference GIFT files (as opposed to websites) and $FILE_LINK_PREFIX value with $networkURL
                        //
                        if(displayedText.contains(HYPERLINK_URL_FILE_KEY)){
                            displayedText = displayedText.replaceAll(HYPERLINK_URL_FILE_KEY, HYPERLINK_URL_ELEMENT + "'" + networkURL);
                            textElement.setText(displayedText);
                        }
                        
                    }
                }
            }
        }
    }

}
