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

import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.ums.db.survey.Surveys;

/**
 * This is the XML Editor custom node dialog for the Survey Reply element in the DKF schema.
 * The dialog allows the user to specify which GIFT Survey Reply from the Survey Database for a survey referenced by
 * a survey question to use in a DKF being developed using the DAT.
 * 
 * @author mhoffman
 *
 */
public class SurveyReplyDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SurveyReplyDialog.class);

    /** title of the dialog */
    private static final String TITLE = "GIFT Survey Reply Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing Reply entry from the GIFT Survey Database\n"+
                "for the associated GIFT survey question.\n" +
                "\nNote: the survey question must be set before this dialog will be populated with available replies\n" +
                "\nNote: the 'use database' property must be set to true and a connection successfully established to use this dialog";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    private static final String KEY = "key";
    private static final String REPLY = "reply";
    
    //the following block checks to make sure the class structure of Reply->key from the xsd hasn't changed
    //as this class uses that structure to to find node elements in the tree.
    //If changes are made to this structure or the names of class fields, then the code that walks/searches nodes in this class
    //will need to be changed accordingly.
    static{         
        generated.dkf.Question question = new generated.dkf.Question();
        question.getKey();
        question.getReply();
        generated.dkf.Reply reply = new generated.dkf.Reply();
        reply.getKey();
    }
    
    /**
     * Class constructor - create dialog
     */
    public SurveyReplyDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object getData(){
        
        if(selectedValue != null && selectedValue instanceof ReplyItem){
            int replyId = ((ReplyItem)selectedValue).getReplyId();
            return replyId;
        }
        
        return selectedValue;
    }
    
    @Override
    public Object[] getCustomValues() {
        
        List<ReplyItem> items = new ArrayList<>();        
        
        if(CommonProperties.getInstance().shouldUseDBConnection()){
            
            //
            // get all the replies for the associated question for the currently selected reply node
            //
            
            Integer surveyContextId = SurveyContextDialog.getCurrentSurveyContextId();
            if(surveyContextId != null){
                
                //this logic needs the reply key attribute element (unless someone has changed its use in an xsd)
                FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
                if(selectedNode != null){
                    
                    if(selectedNode.getLabelText().equals(KEY)){
                      //found reply key attribute
                        
                        //find question key
                        //traverses up XML tree (topDown): question->replies->reply->key, currently at "key", need "question" element
                        FToggleNode questionNode = (FToggleNode) selectedNode.getParent().getParent().getParent();
                        
                        for(int i = 0; i < questionNode.getChildCount(); i++){
                            
                            FToggleNode childNode = (FToggleNode) questionNode.getChildAt(i);
                            if(childNode.getLabelText().equals(KEY)){
                                
                                //found question key element
                                Object sQuestionKey = childNode.getValue();
                                
                                if(sQuestionKey != null){

                                    //
                                    // get survey question
                                    //
                                    // MH - Note: for some reason the sQuestionKey recently changed to a String
                                    int sQuestionKeyId;
                                    if(sQuestionKey instanceof String){
                                        sQuestionKeyId = Integer.valueOf((String) sQuestionKey);
                                    }else{
                                        sQuestionKeyId = (int)sQuestionKey;
                                    }
                                    
                                    AbstractSurveyQuestion<?> sQuestion = Surveys.getSurveyQuestion(sQuestionKeyId);
                                    
                                    if(sQuestion != null){

                                        //get replies available for survey question
                                        OptionList replies = Surveys.getOptionList(sQuestion);

                                        if (replies != null) {

                                            for (ListOption reply : replies.getListOptions()) {

                                                items.add(new ReplyItem(reply.getId(), reply.getText()));
                                            }
                                        }
                                    }
                                }
                            }
                        }//end for
                        
                    }else if(selectedNode.getLabelText().equals(REPLY)){
                        logger.info("Waiting for selected node to change from "+selectedNode+" to "+KEY+" before attempting to gather survey reply ids for the list");
                    }else{
                        logger.error("The currently selected node is not a "+KEY+" labeled node, therefore unable to provided survey reply ids");
                        return null;
                    }
                }
                
            }else{
                logger.error("The survey context id has not be set yet, therefore unable to provided survey reply ids");
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
     * Wrapper class for Survey Database Question Reply information.  This class is responsible for 
     * handling the toString implementation used to display a string representing an instance of this class on the dialog.
     * 
     * @author mhoffman
     *
     */
    public class ReplyItem implements Comparable<ReplyItem>{
        
        int replyId;
        String text;
        
        public ReplyItem(int replyId, String text){
            this.replyId = replyId;
            this.text = text;
        }
        
        public int getReplyId(){
            return replyId;
        }
        
        @Override
        public String toString(){
            return replyId + " " + text;
        }
        
        @Override
        public int compareTo(ReplyItem otherReplyItem) {
            
            if(otherReplyItem == null){
                return -1;
            }else{
                
                if(getReplyId() < otherReplyItem.getReplyId()){
                    return -1;
                }else if(getReplyId() > otherReplyItem.getReplyId()){
                    return 1;
                }
            }
            
            return 0;
        }
    } 
  
}
