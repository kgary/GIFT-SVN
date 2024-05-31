/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.domain.DomainKnowledgeActionInterface;

/**
 * This class contains logic for interacting with different types of conversation engines in GIFT.
 * It is meant to be the central location for creating and updating conversation models.
 * 
 * @author mhoffman
 *
 */
public class ConversationManager {
    
    /** 
     * used as an artifically created task name to contain assessments of concepts which have
     * no direct correlation to authored course concepts
     */
    public static final String CONVERSATION_TASK_NAME = "Conversation Concepts";

    /**
     * The different conversation manager types
     */
    
    /** used to manage AutoTutor conversations */
    private AutoTutorManager autoTutorManager = new AutoTutorManager();
    
    /** used to manage conversation tree conversations */
    private ConversationTreeManager conversationTreeManager = new ConversationTreeManager();
    
    /** 
     * mapping of unique conversation id (to this domain module instance) and the conversation manager
     * responsible for managing that conversation 
     */
    private Map<Integer, ConversationManagerInterface> chatManagerMap = new HashMap<>();
    
    /**
     * Return the handler responsible for managing conversation variables
     * 
     * @return the handler for conversation variables
     */
    public ConversationVarsHandler getConversationVarHandler(){
        return conversationTreeManager.getConversationVarHandler();
    }
    
    /**
     * Create and start a conversation tree conversation instance.
     * Note: this will hold onto the calling thread until the first conversation tree
     * choice is presented to the learner.  At that point the thread is released since the logic
     * is waiting on the next chat update message for this conversation instance.
     * 
     * @param chatId unique id for the chat among all other chats in the domain module.
     * @param conversation contains the conversation elements authored
     * @param conversationAssessmentHandler used for updating performance assessments for performance nodes based on conversation assessments
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner
     * @throws DetailedException if there was a problem creating or starting the conversation
     */
    public void startConversationTree(int chatId, generated.conversation.Conversation conversation, 
            ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        conversationTreeManager.createConversationModel(chatId, conversation, conversationAssessmentHandler, domainKnowledgeActionInterface);
        
        chatManagerMap.put(chatId, conversationTreeManager);
        conversationTreeManager.startConversation(chatId);
    }
    
    /**
     * Create and start an AutoTutor conversation instance.
     * 
     * @param chatId unique id for the chat among all other chats in the domain module.
     * @param configuration the reference to the AutoTutor SKO
     * @param courseFolder used to retrieve an AutoTutor SKO GIFT file
     * @param conversationAssessmentHandler used for updating performance assessments for performance nodes based on conversation assessments
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner
     * @throws DetailedException if there was a problem creating or starting the conversation
     */
    public void startAutoTutor(int chatId, generated.dkf.AutoTutorSKO configuration, 
            AbstractFolderProxy courseFolder, ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        autoTutorManager.createAutoTutorModel(chatId, configuration, courseFolder, conversationAssessmentHandler, domainKnowledgeActionInterface);
        
        chatManagerMap.put(chatId, autoTutorManager);
        autoTutorManager.startConversation(chatId);
    }
    
    /**
     * Create and start an AutoTutor conversation instance.
     * 
     * @param chatId unique id for the chat among all other chats in the domain module.
     * @param configuration the reference to the AutoTutor SKO
     * @param courseFolder used to retrieve an AutoTutor SKO GIFT file
     * @param conversationAssessmentHandler used for updating performance assessments for performance nodes based on conversation assessments
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner
     * @throws DetailedException if there was a problem creating or starting the conversation
     */
    public void startAutoTutor(int chatId, generated.course.AutoTutorSKO configuration, 
            AbstractFolderProxy courseFolder, ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        autoTutorManager.createAutoTutorModel(chatId, configuration, courseFolder, conversationAssessmentHandler, domainKnowledgeActionInterface);
        
        chatManagerMap.put(chatId, autoTutorManager);
        autoTutorManager.startConversation(chatId);
    }
    
    /**
     * Stop the specified conversation.
     * 
     * @param chatId a unique identifier of a conversation in this domain module instance.
     */
    public void stopConversation(int chatId){
        
        //find the underlying conversation model
        ConversationManagerInterface conversationMgr = chatManagerMap.get(chatId);
        if(conversationMgr == null){
            throw new IllegalArgumentException("Can't find the conversation model for the id "+chatId+" among "+chatManagerMap.size()+" known conversations.");
        }else{
            conversationMgr.stopConversation(chatId);
        }
    }
    
    /**
     * Stop all conversations that have been created for this domain session instance.
     */
    public void stopAllConversations(){
        
        autoTutorManager.stopAllConversations();
        conversationTreeManager.stopAllConversations();
    }
    
    /**
     * Update the underlying conversation model with the latest learner's input.
     * 
     * @param chatLog contains the latest learner's input to the conversation
     * @throws DetailedException if there was a problem applying the conversation update
     */
    public void addUserResponse(ChatLog chatLog) throws DetailedException{
        
        ConversationManagerInterface mgr = chatManagerMap.get(chatLog.getChatId());
        if(mgr != null){
            mgr.addUserResponse(chatLog);
        }else{
            throw new DetailedException("Unable to apply the learner's latest conversation response.", "The conversation with id "+chatLog.getChatId()+" could not be found among the "+chatManagerMap.size()+" conversations currently known.", null);
        }
    }
    
    /**
     * Contains an assessment of a concept based on how the conversation is progressing with the learner.
     * 
     * @author mhoffman
     *
     */
    public static class ConversationAssessment{
        
        private String concept;
        
        /** the level of confidence of this concept assessment [0, 1.0]*/
        private float confidence = 1.0f;

        private AssessmentLevelEnum assessmentLevel;
        
        /**
         * Set attributes.
         * 
         * @param concept a concept being assessed in the conversation.  Can't be null or empty.
         * @param assessmentLevel the assessment level to associate with that concept.  Can't be null.
         */
        public ConversationAssessment(String concept, AssessmentLevelEnum assessmentLevel){
            
            if(concept == null || concept.isEmpty()){
                throw new IllegalArgumentException("The concept can't be null or empty.");
            }else if(assessmentLevel == null){
                throw new IllegalArgumentException("The assessment level can't be null.");
            }
            
            this.concept = concept;
            this.assessmentLevel = assessmentLevel;
        }
        
        /**
         * Return the concept being assessed in the conversation.
         * 
         * @return won't be null or empty.
         */
        public String getConcept(){
            return concept;
        }
        
        /**
         * Return the assessment level for this concept.
         * 
         * @return won't be null.
         */
        public AssessmentLevelEnum getAssessmentLevel(){
            return assessmentLevel;
        }
        
        /**
         * Return the confidence of the assessment on this concept.
         * 
         * @return a value from 0 to 1.0.  Default is 1.0.
         */
        public float getConfidence() {
            return confidence;
        }

        /**
         * Set the confidence of the assessment on this concept.
         * 
         * @param confidence a value between 0 and 1.0
         */
        public void setConfidence(float confidence) {
            
            if(confidence < 0 || confidence > 1.0){
                throw new IllegalArgumentException("The confidence value must be between 0 and 1.0.");
            }
            
            this.confidence = confidence;
        }
        
        /**
         * Create a new instance of this class from a conversation tree assessment object.
         * 
         * @param assessment used to populate a new instance of ConversationAssessment.  Can't be null.
         * @return a new instance
         */
        public static ConversationAssessment createInstance(generated.conversation.Assessment assessment){
            
            if(assessment == null){
                throw new IllegalArgumentException("The assessment can't be null.");
            }
            
            AssessmentLevelEnum assessmentLevel = AssessmentLevelEnum.valueOf(assessment.getLevel());
            ConversationAssessment conversationAssessment = new ConversationAssessment(assessment.getConcept(), assessmentLevel);
            conversationAssessment.setConfidence(assessment.getConfidence().floatValue());
            return conversationAssessment;
        }
        
        /**
         * Create a collection of new instances of this class from a list of conversation tree assessment objects.
         * 
         * @param assessments used to populate a list of new instances of ConversationAssessment.  Can't be null.
         * @return won't be null but can be empty.
         */
        public static List<ConversationAssessment> createListInstance(List<generated.conversation.Assessment> assessments){
            
            if(assessments == null){
                throw new IllegalArgumentException("The assessments list can't be null.");
            }
            
            List<ConversationAssessment> conversationAssessments = new ArrayList<>();
            for(generated.conversation.Assessment assessment : assessments){
                conversationAssessments.add(ConversationAssessment.createInstance(assessment));
            }
            
            return conversationAssessments;
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[ConversationAssessment: ");
            sb.append("concept = ").append(getConcept());
            sb.append(", assessmentLevel = ").append(getAssessmentLevel());
            sb.append(", confidence = ").append(getConfidence());
            sb.append("]");
            return sb.toString();
        }
    }
}
