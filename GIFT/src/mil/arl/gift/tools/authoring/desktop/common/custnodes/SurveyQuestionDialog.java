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

import com.fg.ftreenodes.FToggleNode;

import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.ums.db.survey.Surveys;

/**
 * This is the XML Editor custom node dialog for the Survey Question element in the DKF schema.
 * The dialog allows the user to specify which GIFT Survey Question from the Survey Database for a survey referenced by
 * a survey key and a survey context to use in a DKF being developed using the DAT.
 * 
 * @author mhoffman
 *
 */
public class SurveyQuestionDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SurveyQuestionDialog.class);

    /** title of the dialog */
    private static final String TITLE = "GIFT Survey Question Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing GIFT Survey Question entry from the GIFT Survey Database\n"+
                "for the associated GIFT survey key and survey context.\n" +
                "\nNote: the survey key and survey context must be set before this dialog will be populated with available GIFT Survey Questions\n" +
                "\nNote: the 'use database' property must be set to true and a connection successfully established to use this dialog";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    private static final String KEY = "key";
    private static final String GIFT_SURVEY_KEY = "GIFTSurveyKey";
    private static final String QUESTION = "question";
    
    //the following block checks to make sure the class structure of Survey->Questions->Question->key from the xsd hasn't changed
    //as this class uses that structure to to find node elements in the tree.
    //If changes are made to this structure or the names of class fields, then the code that walks/searches nodes in this class
    //will need to be changed accordingly.
    static{         
        generated.dkf.Assessments.Survey survey = new generated.dkf.Assessments.Survey();
        survey.getGIFTSurveyKey();
        survey.getQuestions();
        generated.dkf.Question question = new generated.dkf.Question();
        question.getKey();       
    }
    
    /**
     * Class constructor - create dialog
     */
    public SurveyQuestionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object getData(){
        
        if(selectedValue != null && selectedValue instanceof QuestionItem){
            int questionId = ((QuestionItem)selectedValue).getQuestionId();
            return questionId;
        }
        
        return selectedValue;
    }
    
    @Override
    public Object[] getCustomValues() {
        
        List<QuestionItem> items = new ArrayList<>();        
        
        if(CommonProperties.getInstance().shouldUseDBConnection()){
            
            //
            // get all the questions for the associated survey for the currently selected question node
            //
            
            Integer surveyContextId = SurveyContextDialog.getCurrentSurveyContextId();
            if(surveyContextId != null){
                
                //this should be the question element (unless someone has changed its use in an xsd)
                FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
                if(selectedNode != null){
                    
                    if(selectedNode.getLabelText().equals(KEY)){
                      //found question key attribute
                        
                        //get survey key
                        FToggleNode surveyNode = (FToggleNode) selectedNode.getParent().getParent().getParent().getParent();
                        for(int i = 0; i < surveyNode.getChildCount(); i++){

                            FToggleNode childNode = (FToggleNode) surveyNode.getChildAt(i);
                            if(childNode.getLabelText().equals(GIFT_SURVEY_KEY)){
                                //found survey key element
                                
                                String surveyKey = (String)childNode.getValue();
                                
                                if(surveyKey != null){
                                    Survey survey = Surveys.getSurveyContextSurvey(surveyContextId, surveyKey);
                                    
                                    if(survey != null){
                                       
                                        for(SurveyPage sPage : survey.getPages()){
                                            
                                            for(AbstractSurveyElement sQuestion : sPage.getElements()){
                                                
                                                items.add(new QuestionItem(sQuestion, Surveys.getQuestion(sQuestion.getId())));
                                            }
                                        }
                                    }
                                }
                                
                                break;
                            }
                        }//end for 
                        
                    }else if(selectedNode.getLabelText().equals(QUESTION)){
                        logger.info("Waiting for selected node to change from "+selectedNode+" to "+KEY+" before attempting to gather survey questions for the list");
                    }else{
                        logger.error("The currently selected node is not a "+KEY+" labeled node, therefore unable to provided survey questions");
                        return null;
                    }
                }             
                
            }else{
                logger.error("The survey context id has not be set yet, therefore unable to provided question ids");
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
     * Wrapper class for Survey Database Question information.  This class is responsible for 
     * handling the toString implementation used to display a string representing an instance of this class on the dialog.
     * 
     * @author mhoffman
     *
     */
    public class QuestionItem implements Comparable<QuestionItem>{
        
        AbstractSurveyElement surveyQuestion;
        AbstractQuestion question;
        
        public QuestionItem(AbstractSurveyElement surveyQuestion, AbstractQuestion question){
            this.surveyQuestion = surveyQuestion;
            this.question = question;
        }
        
        public int getQuestionId(){
            return surveyQuestion.getId();
        }
        
        @Override
        public String toString(){
            return getQuestionId() + " " + question.getText();
        }

        @Override
        public int compareTo(QuestionItem otherQuestionItem) {
            
            if(otherQuestionItem == null){
                return -1;
            }else{
                
                if(getQuestionId() < otherQuestionItem.getQuestionId()){
                    return -1;
                }else if(getQuestionId() > otherQuestionItem.getQuestionId()){
                    return 1;
                }
            }
            
            return 0;
        }
    } 
  
}
