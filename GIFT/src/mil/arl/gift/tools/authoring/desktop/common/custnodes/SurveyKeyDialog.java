/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.ums.db.survey.Surveys;

/**
 * This is the XML Editor custom node dialog for the Survey Key element in the Course schema.
 * The dialog allows the user to specify which GIFT Survey Key from the Survey Database to use in  
 * a course or dkf file being developed using the CAT or DAT respectively.
 * 
 * @author mhoffman
 *
 */
public class SurveyKeyDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SurveyKeyDialog.class);

    /** title of the dialog */
    private static final String TITLE = "GIFT Survey Key Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing GIFT Survey Key entry from the GIFT Survey Database\n for the current survey context.\n" +
                "\nNote: the survey context must be set before this dialog will be populated with available GIFT Survey keys\n" +
                "\nNote: the 'use database' property must be set to true and a connection successfully established to use this dialog";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    /**
     * Class constructor - create dialog
     */
    public SurveyKeyDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object getData(){
        
        if(selectedValue != null){
            return selectedValue;
        }
        
        return selectedValue;
    }
    
    @Override
    public Object[] getCustomValues() {
        
        List<String> items = new ArrayList<String>();        
        
        if(CommonProperties.getInstance().shouldUseDBConnection()){
            
            Integer surveyContextId = SurveyContextDialog.getCurrentSurveyContextId();
            if(surveyContextId != null){
                
                List<SurveyContextSurvey> surveyContextSurveys = Surveys.getSurveyContextSurveys(surveyContextId);
                if(surveyContextSurveys != null){                    
                
                    for(SurveyContextSurvey surveyContextSurvey : surveyContextSurveys){
                    
                        String key = surveyContextSurvey.getKey();
                        
                        //Prevent showing the GIFT key associated with a survey whose name is the EMAP reserved survey name
                        //The reserved survey name is for internal use only
                        if(!key.equals(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY)
                        		&& !key.matches(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX)){
                            items.add(key);
                        }
                    }
                }
                
            }else{
                logger.error("The survey context id has not be set yet, therefore there are no GIFT survey keys to show");
            }
        }
        
        Collections.sort(items);
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        //no custom entries allowed for user history for this dialog implementation
    }
  
}
