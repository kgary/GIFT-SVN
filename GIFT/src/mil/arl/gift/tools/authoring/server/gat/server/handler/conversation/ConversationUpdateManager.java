/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import generated.conversation.Conversation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.domain.DomainKnowledgeActionInterface;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationAssessmentHandlerInterface;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeManager.ConversationTreeAction;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeManager.ConversationTreeActions;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;
import mil.arl.gift.tools.authoring.server.gat.shared.model.conversation.ConversationHelper;

/**
 * This class manages running conversations in the GAT.
 * 
 * @author bzahid
 */
public class ConversationUpdateManager {

	/** The logger */
	private static Logger logger = LoggerFactory.getLogger(ConversationUpdateManager.class);

	/** A map of chat ids to conversation managers. Needed to retrieve the appropriate message in an ongoing conversation. */
	private static HashMap<Integer, ConversationManager> idToManagerMap = new HashMap<Integer, ConversationManager>();
	
	/** The callback to execute when an update is retrieved.*/
	private static ConversationUpdateCallback updateCallback;
	
	/**
	 * Gets the next message to present to the user in an ongoing conversation.
	 * 
	 * @param action The action containing information about the conversation
	 * @param callback The callback to execute when the operation is complete.
	 */
	public static void getNextUpdate(final UpdateConversation action, final ConversationUpdateCallback callback) {
		
		updateCallback = callback;
		Conversation conversationObj = null;
		
		if(action.getChatId() == -1) {
			// This is a new conversation; generate the chat id and create the conversation manager. 
			
	        try {
		        JSONObject jsonObj = new JSONObject(action.getConversationJSONStr());
				jsonObj.put(ConversationHelper.TREE_KEY, new JSONObject(action.getConversationTreeJSONStr()));
			    conversationObj = ConversationUtil.fromJSON(jsonObj);
			    
	        } catch(DetailedException e) {
	        	UpdateConversationResult result = new UpdateConversationResult();
	        	result.setSuccess(false);
	        	result.setErrorMsg(e.getReason());
	        	result.setErrorDetails(e.getDetails());
	        	result.setErrorStackTrace(e.getErrorStackTrace());
	        	updateCallback.failure(result);
	        	
	        } catch (Exception e) {
	        	UpdateConversationResult result = new UpdateConversationResult();
	        	result.setErrorMsg("There was a problem reading the conversation file.");
	        	result.setErrorDetails(e.toString());
	        	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	        	updateCallback.failure(result);
	        }
	        
		}
		
		getNextUpdate(action.getChatId(), conversationObj, action.getUserText(), callback);
	}
	
	/**
	 * Gets the next message to present to the user in an ongoing conversation.
	 * 
	 * @param action The action containing information about the conversation
	 * @param callback The callback to execute when the operation is complete.
	 */
	public static void getNextUpdate(int conversationId, Conversation conversation, String userText, final ConversationUpdateCallback callback) {
		
		updateCallback = callback;
		
		
		if(conversationId == -1) {
			// This is a new conversation; generate the chat id and create the conversation manager. 
			
			conversationId = DisplayChatWindowRequest.getNextChatId();
			
			ConversationManager conversationMgr = new ConversationManager();
	        
			// Store this conversation manager in case a user response needs to be entered later.
			idToManagerMap.put(conversationId, conversationMgr);
	        
	        // Handles conversation assessments from choices made in the conversation
	        ConversationAssessmentHandlerInterface conversationAssessmentHandler = new ConversationAssessmentHandlerInterface() {
	            
	            @Override
	            public void assessPerformanceFromConversation(List<ConversationAssessment> assessments) {
	            	// nothing to do
	            }
	        };
	        
	        final int chatId = conversationId;
	        // Needed to check the tutor conversation text that would normally be presented to the learner 
	        DomainKnowledgeActionInterface domainKnowledgeActionHandler = new DomainKnowledgeActionInterface() {
	            
	            @Override
	            public void scenarioStarted() {
	                //nothing to do
	            }
	            
	            @Override
	            public void scenarioCompleted(LessonCompleted lessonCompleted) {
	                //nothing to do                
	            }
	            
	            @Override
	            public void performanceAssessmentCreated(PerformanceAssessment performanceAssessment) {
	                //nothing to do
	            }
	            
	            @Override
	            public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
	                //nothing to do                
	            }
	            
	            @Override
	            public void fatalError(String reason, String details) {
	                //nothing to do                
	            }
	            
	            @Override
	            public void handleDomainActionWithLearner(DomainAssessmentContent information) {
	                
	                if(information instanceof ConversationTreeActions){
	                    
	                    ConversationTreeActions actions = (ConversationTreeActions)information;
	                    List<String> text = new ArrayList<String>();
	                    List<String> choices = null;
	                    boolean endConversation = false;
	                    
	                    for(ConversationTreeAction action : actions.getActions()) {
	                    	text.add(action.getText());
	                    	choices = action.getChoices();
	                    	endConversation = action.isConversationEnd();
	                    }
	                    
	                    updateCallback.notify(chatId, text, choices, endConversation);
	                    
	                    if(endConversation) {
	                    	idToManagerMap.remove(chatId).stopConversation(chatId);
	                    }
	                    
	                }else{
	                    logger.warn("Received unhandled domain assessment content to display to the user in "+information+".");
	                }
	            }
	                        
	            @Override
	            public void displayDuringLessonSurvey(
	            		AbstractSurveyLessonAssessment surveyAssessment, 
	            		SurveyResultListener surveyResultListener) {
	                //nothing to do                
	            }
	        };
	        
	        try {
			    // This holds onto this calling thread until the choice conversation node elements are reached
		        conversationMgr.startConversationTree(chatId, conversation, 
		        		conversationAssessmentHandler, domainKnowledgeActionHandler);
		        
	        } catch(DetailedException e) {
	        	UpdateConversationResult result = new UpdateConversationResult();
	        	result.setSuccess(false);
	        	result.setErrorMsg(e.getReason());
	        	result.setErrorDetails(e.getDetails());
	        	result.setErrorStackTrace(e.getErrorStackTrace());
	        	updateCallback.failure(result);
	        	
	        } catch (Exception e) {
	        	UpdateConversationResult result = new UpdateConversationResult();
	        	result.setErrorMsg("There was a problem reading the conversation file.");
	        	result.setErrorDetails(e.toString());
	        	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	        	updateCallback.failure(result);
	        }
	        
		} else {
			// This is an ongoing conversation with a response from the user.
			
			ConversationManager conversationMgr = idToManagerMap.get(conversationId);
			if(conversationMgr != null) {
			
				 // Add the user's response. This will not create a performance assessment to happen
		        List<String> userEntries = new ArrayList<>();
		        userEntries.add(userText);
		        ChatLog chatLogResponse1 = new ChatLog(conversationId, new ArrayList<String>(0), userEntries);
		        
		        
		        // Update the conversation model with the user's response. This pushes subsequent conversation
		        // messages to domainKnowledgeActionHandler.displayInformationToUser()
		        conversationMgr.addUserResponse(chatLogResponse1);
		        
			}
			
		}
		
	}
	
	/**
	 * Stops the conversation with the matching chat id.
	 * 
	 * @param chatId The id of the conversation to stop.
	 */
	public static void endConversation(int chatId){
		if(idToManagerMap.containsKey(chatId)) {
			idToManagerMap.remove(chatId).stopConversation(chatId);
		}
	}
	
}
