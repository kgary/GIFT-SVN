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

import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.ums.db.survey.Surveys;

/**
 * This is the XML Editor custom node dialog for the Survey Context element in the DKF schema.
 * The dialog allows the user to specify which Survey Context Id from the Survey Database to use in a DKF file being developed using the DAT.
 * 
 * @author mhoffman
 *
 */
public class SurveyContextDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;

    /** title of the dialog */
    private static final String TITLE = "Survey Context Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing Survey Context entry from the GIFT Survey Database.\n"+
                "\nNote: the 'use database' property must be set to true and a connection successfully established to use this dialog";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    private static Integer currentSurveyContextId;
    
    /**
     * Return the current survey context id value in the course XML model.
     * 
     * @return id
     */
    public static Integer getCurrentSurveyContextId(){
        return currentSurveyContextId;
    }
    
    public static void resetCurrentSurveyContextId(){
        currentSurveyContextId = null;
    }
    
    public static void setCurrentSurveyContextId(int surveyContextId){
        currentSurveyContextId = surveyContextId;
    }
    
    /**
     * Class constructor - create dialog
     */
    public SurveyContextDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object getData(){
        
        if(selectedValue != null && selectedValue instanceof SurveyContextItem){
            int surveyContextId = ((SurveyContextItem)selectedValue).getSurveyContextId();
            currentSurveyContextId = surveyContextId;
            return surveyContextId;
        }
        
        return selectedValue;
    }
    
    @Override
    public Object[] getCustomValues() {
        
        List<SurveyContextItem> items = new ArrayList<SurveyContextItem>();        
        
        if(CommonProperties.getInstance().shouldUseDBConnection()){
            
            List<SurveyContext> surveyContexts = Surveys.getSurveyContexts(null);
            
            for(SurveyContext sContext : surveyContexts){
                
                items.add(new SurveyContextItem(sContext));
            }
        }
        
        Collections.sort(items);
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        //no custom entries allowed for user history for this dialog implementation
    }
    
    /**
     * Wrapper class for Survey Database Survey Context information.  This class is responsible for 
     * handling the toString implementation used to display a string representing an instance of this class on the dialog.
     * 
     * @author mhoffman
     *
     */
    public class SurveyContextItem implements Comparable<SurveyContextItem>{
        
        SurveyContext sContext;
        
        public SurveyContextItem(SurveyContext sContext){
            this.sContext = sContext;
        }
        
        public int getSurveyContextId(){
            return sContext.getId();
        }
        
        @Override
        public String toString(){
            return sContext.getId() + " " + sContext.getName();
        }
        
        @Override
        public int compareTo(SurveyContextItem otherSurveyContextItem) {
            
            if(otherSurveyContextItem == null){
                return -1;
            }else{
                
                if(getSurveyContextId() < otherSurveyContextItem.getSurveyContextId()){
                    return -1;
                }else if(getSurveyContextId() > otherSurveyContextItem.getSurveyContextId()){
                    return 1;
                }
            }
            
            return 0;
        }
    }    
}
