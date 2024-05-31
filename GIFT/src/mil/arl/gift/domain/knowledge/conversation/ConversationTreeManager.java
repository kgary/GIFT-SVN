/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.domain.DomainKnowledgeActionInterface;

/**
 * This class is responsible for managing conversation tree(s) for a single domain session.
 * 
 * @author mhoffman
 *
 */
public class ConversationTreeManager implements ConversationManagerInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ConversationTreeManager.class);
    
    /** 
     * mapping of unique conversation id (chat id) to the data model responsible for maintaining state 
     * about that conversation 
     */
    private Map<Integer, ConversationTreeModel> conversationIdToModel = new HashMap<>();
    
    /**
     * used to manage conversation variables
     */
    private ConversationVarsHandler conversationVarHandler = new ConversationVarsHandler();


    /**
     * Create the conversation tree data model for the conversation tree authored.  The data model is responsible
     * for maintaining state about the conversation.
     * 
     * Note: conversation tree models should be created by the ConversationManager class, hence the lack of constructor modifier
     * 
     * @param chatId unique id for the chat among all other chats in the domain module.
     * @param conversation contains the conversation elements authored
     * @param conversationAssessmentHandler used for updating performance assessments for performance nodes based on conversation assessments
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner
     * @throws DetailedException if there was a validation issue with the conversation
     */
    void createConversationModel(int chatId, generated.conversation.Conversation conversation, 
            ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        ConversationTreeModel model = new ConversationTreeModel(chatId, conversation, conversationVarHandler, conversationAssessmentHandler, domainKnowledgeActionInterface);
        
        conversationIdToModel.put(chatId, model);
    }
    
    /**
     * Return the handler responsible for managing conversation variables
     * 
     * @return the handler for conversation variables
     */
    public  ConversationVarsHandler getConversationVarHandler(){
        return conversationVarHandler;
    }
    
    @Override
    public void addUserResponse(ChatLog chatLog){
        
        int chatId = chatLog.getChatId();
        
        //determine if this is a conversation managed in this class
        if(chatId < 1){
            throw new IllegalArgumentException("Invalid chat id of "+chatId+".  Chat id must be a value greater than zero.");
        }
        
        ConversationTreeModel model = conversationIdToModel.get(chatId);
        
        if(model == null){
            return;
        }
        
        String learnerText = chatLog.getLastUserEntry();        
        model.selectedQuestionChoice(learnerText);  
        
        //display next conversation elements
        //TODO: do this after a pedagogical request to continue the conversation, maybe leverage course state msg?
        model.deliverNextActions();
    }
    

    @Override
    public void startConversation(int chatId) {

        //determine if this is a conversation managed in this class
        if(chatId < 1){
            throw new IllegalArgumentException("Invalid chat id of "+chatId+".  Chat id must be a value greater than zero.");
        }
        
        ConversationTreeModel model = conversationIdToModel.get(chatId);
        
        if(model == null){
            return;
        }
        
        model.start();
    }

    @Override
    public void stopConversation(int chatId) {
        
        //determine if this is a conversation managed in this class
        if(chatId < 1){
            throw new IllegalArgumentException("Invalid chat id of "+chatId+".  Chat id must be a value greater than zero.");
        }
        
        ConversationTreeModel model = conversationIdToModel.get(chatId);
        
        if(model == null){
            return;
        }
        
        model.stop();        
    }
    
    @Override
    public void stopAllConversations(){
        
        for(ConversationTreeModel model : conversationIdToModel.values()){
            
            try{
                model.stop();
            }catch(Exception e){
                logger.error("Caught exception from misbehaving Conversation Tree model when trying to forcefully stop it.", e);
            }
        }
    }
    
    /**
     * Contains conversation tree actions to present to the learner for a conversation
     * instance.
     * 
     * @author mhoffman
     *
     */
    public static class ConversationTreeActions implements DomainAssessmentContent{
        
        private List<ConversationTreeAction> actions;
        
        private int chatId;
        
        /**
         * Set attributes.
         * 
         * @param chatId the unique conversation id for all users of this domain module instance
         * @param actions the list of conversation tree actions to present to the learner
         * (e.g. show a question).  Can't be null or empty.
         */
        public ConversationTreeActions(int chatId, List<ConversationTreeAction> actions){
            
            if(chatId < 1){
                throw new IllegalArgumentException("The chat id must be greater than 0");
            }else if(actions == null || actions.isEmpty()){
                throw new IllegalArgumentException("The actions list can't be null or empty.");
            }
            
            this.chatId = chatId;
            this.actions = actions;
        }
        
        /**
         * Return the unique conversation id for all users of this domain module instance
         * 
         * @return the conversation id
         */
        public int getChatId(){
            return chatId;
        }
        
        /**
         * Return the list of conversation tree actions to present to the learner
         * (e.g. show a question)
         * 
         * @return won't be null or empty.
         */
        public List<ConversationTreeAction> getActions(){
            return actions;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ConversationTreeActions: ");
            builder.append("chatId = ").append(chatId);
            builder.append(", actions = {");
            for(ConversationTreeAction action : actions){
                builder.append("\n").append(action).append(",");
            }
            
            builder.append("}]");
            return builder.toString();
        }      
    }
    
    /**
     * Contains a single conversation tree action to present to the learner
     * (e.g. show a message)
     * 
     * @author mhoffman
     *
     */
    public static class ConversationTreeAction implements DomainAssessmentContent{
        
        private String text;
        
        private List<String> choices;
        
        /** whether this action is indicating the conversation is over */
        private boolean conversationEnd = false;
        
        /**
         * Set attributes.
         * 
         * @param text to display to the learner.  Can't be null but can be empty.
         * @param choices choices to display for a question.  Can be null or empty.
         */
        public ConversationTreeAction(String text, List<String> choices){

            if(text == null){
                throw new IllegalArgumentException("The text can't be null.");
            }
            
            this.text = text;
            this.choices = choices;
        }
        
        /**
         * Return the text to display to the learner. 
         * 
         * @return Can't be null but can be empty.
         */
        public String getText(){
            return text;
        }
        
        /**
         * Return the choices to display for a question.
         * 
         * @return Can be null or empty.
         */
        public List<String> getChoices(){
            return choices;
        }
        
        /**
         * Set whether this conversational tree action is indicating that the conversation
         * is over.
         * 
         * @param conversationEnd true if the conversation is now over
         */
        public void setConversationEnd(boolean conversationEnd){
            this.conversationEnd = conversationEnd;
        }
        
        /**
         * Return whether this action is indicating that the conversation is over.
         * 
         * @return true if the conversation is now over
         */
        public boolean isConversationEnd(){
            return conversationEnd;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ConversationTreeAction: text=").append(text)
                    .append(", choices=").append(choices)
                    .append(", conversationEnd=").append(conversationEnd)
                    .append("]");
            return builder.toString();
        }
    }
   
}
