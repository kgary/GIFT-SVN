/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.TutorUserInterfaceFeedback;

/**
 * A class that stores updates for inactive widgets on the client
 * 
 * @author bzahid
 */
public class UpdateQueueManager {

	private static Map<Integer, Boolean> userIdHasActiveFeedback;
	
    /** Keeps track of the active chat id for each user. */
    private static Map<Integer, Integer> userIdToActiveChatId;
    
    /** Stores feedback updates for each user. */
    private static Map<Integer, ArrayList<TutorUserInterfaceFeedback>> userIdToFeedbackQueue;
    
    /** Keeps track of chat updates for each user. */
    private static Map<Integer, Map<Integer, ArrayList<DisplayChatWindowUpdateRequest>>> userIdToChatQueue;
    
    /** Instance of the UpdateQueueManager */
    private static UpdateQueueManager instance;
    
    /**
     * Class constructor
     */
    private UpdateQueueManager() {
    	
    	userIdToActiveChatId = new HashMap<Integer, Integer>();
    	userIdHasActiveFeedback = new HashMap<Integer, Boolean>();
    	userIdToFeedbackQueue = new HashMap<Integer, ArrayList<TutorUserInterfaceFeedback>>();
    	userIdToChatQueue = new HashMap<Integer, Map<Integer, ArrayList<DisplayChatWindowUpdateRequest>>>();
    	instance = this;
    }
    
    /**
     * Gets the instance of the UpdateQueueManager
     * 
     * @return the instance of the UpdateQueueManager.
     */
    public static UpdateQueueManager getInstance() {
    	
    	if(instance == null) {
    		instance = new UpdateQueueManager();
    	}
    	
    	return instance;
    }
    
    /**
     * Sets the active chat id for a user.
     * 
     * @param userSessionId The user session id
     * @param activeChatId The id of the chat that is currently active
     */
    public void setActiveChatId(int userSessionId, int activeChatId) {
    	userIdToActiveChatId.put(userSessionId, activeChatId);
    }
    
    /**
     * Gets the id of the active chat for the given user.
     * 
     * @param userSessionId The user session id.
     * @return The id of the active chat or -1 if no chats are active for that user.
     */
    public int getActiveChatId(int userSessionId) {
    	
    	if(userIdToActiveChatId.containsKey(userSessionId)) {
    		return userIdToActiveChatId.get(userSessionId);
    		
    	} else {
    		return -1;
    	}
    }
    
    /**
     * If the chat id provided matches the user's currently active chat id, then that chat id will
     * be removed from the mapping of active chat id for that user.  Otherwise this method has no effect.
     *   
     * @param userSessionId The user session id used to retrieve the currently active chat id.
     * @param inactiveChatId The unique id of the conversation (created by the server) that is now idle.  
     * An inactive conversation means that the server should NOT deliver updates to
     * the chat until it becomes active again.  The client would need to notify the server if the conversation
     * becomes active again.  
     */
    public void setInactiveChatId(int userSessionId, int inactiveChatId) {
        
        Integer activeChatId = userIdToActiveChatId.get(userSessionId);
        if(activeChatId != null && activeChatId == inactiveChatId){
            userIdToActiveChatId.put(userSessionId, -1);
        }
    }
    
    /**
     * Flags the user as having the feedback widget active or inactive.
     * 
     * @param userSessionId The user session id
     * @param feedbackIsActive Whether or not the feedback widget is active for this user
     */
    public void setFeedbackIsActive(int userSessionId, boolean feedbackIsActive) {
    	userIdHasActiveFeedback.put(userSessionId, feedbackIsActive);
    }
    
    /**
     * Gets whether or not the user has an active feedback widget.
     * 
     * @param userSessionId The user session id
     * @return True if the feedback widget is active, false otherwise.
     */
    public boolean isFeedbackActive(int userSessionId) {
    	
    	if(userIdHasActiveFeedback.containsKey(userSessionId)) {
    		return userIdHasActiveFeedback.get(userSessionId);
    		
    	} else {
    		return false;
    	}
    }
    
    /**
     * Adds feedback to the update queue for the given user.
     * 
     * @param userSessionId The user session id
     * @param feedback The feedback update to enqueue
     */
    public void enqueueFeedbackUpdate(int userSessionId, TutorUserInterfaceFeedback feedback) {
    	
    	if(userIdToFeedbackQueue.containsKey(userSessionId)) {
    		userIdToFeedbackQueue.get(userSessionId).add(feedback);
    		
    	} else {
    		ArrayList<TutorUserInterfaceFeedback> feedbackArray = new ArrayList<TutorUserInterfaceFeedback>();
    		feedbackArray.add(feedback);
    		userIdToFeedbackQueue.put(userSessionId, feedbackArray);
    	}
    	
    }
    
    /**
     * Gets a feedback update from the user's queue.
     * 
     * @param userSessionId The user session id
     * @return The feedback update for the user or null if no update is available
     */
    public TutorUserInterfaceFeedback dequeueFeedbackUpdate(int userSessionId) {
    	
    	if(userIdToFeedbackQueue.containsKey(userSessionId)) {
    		if(!userIdToFeedbackQueue.get(userSessionId).isEmpty()) {
    			return userIdToFeedbackQueue.get(userSessionId).remove(0);
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Adds a chat update to the update queue for the given user.
     * 
     * @param userSessionId The user session id
     * @param chatUpdate The chat update to enqueue
     */
    public void enqueueChatUpdate(int userSessionId, DisplayChatWindowUpdateRequest chatUpdate){
    	
        synchronized(userIdToChatQueue){
        	if(userIdToChatQueue.containsKey(userSessionId)) {
        		Map<Integer, ArrayList<DisplayChatWindowUpdateRequest>> chatIdToChatQueue = userIdToChatQueue.get(userSessionId);
        		
        		if(chatIdToChatQueue.containsKey(chatUpdate.getChatId())) {
        			chatIdToChatQueue.get(chatUpdate.getChatId()).add(chatUpdate);
        			
        		} else {
        			ArrayList<DisplayChatWindowUpdateRequest> chatArray = new ArrayList<DisplayChatWindowUpdateRequest>();
        			chatArray.add(chatUpdate);
        			chatIdToChatQueue.put(chatUpdate.getChatId(), chatArray);
        		}
        		
        	} else {
        		
        		Map<Integer, ArrayList<DisplayChatWindowUpdateRequest>> chatIdToChatQueue = new HashMap<Integer, ArrayList<DisplayChatWindowUpdateRequest>>();
        		ArrayList<DisplayChatWindowUpdateRequest> chatArray = new ArrayList<DisplayChatWindowUpdateRequest>();
    			
        		chatArray.add(chatUpdate);
    			chatIdToChatQueue.put(chatUpdate.getChatId(), chatArray);
    			userIdToChatQueue.put(userSessionId, chatIdToChatQueue);    		
        	}
        }
    }
    
    /**
     * Gets a chat update from the user's queue that matches the active chat id.
     * 
     * @param userSessionId The user session id
     * @return The chat update for the active chat widget, or null if there is no update available.
     */
    public DisplayChatWindowUpdateRequest dequeueChatUpdate(int userSessionId) {
    	
        synchronized(userIdToChatQueue){
        	// Make sure both maps contain an entry for this user
        	if(userIdToActiveChatId.containsKey(userSessionId) && userIdToChatQueue.containsKey(userSessionId)) {
        	    
        	    Map<Integer, ArrayList<DisplayChatWindowUpdateRequest>> usersChatQueue = userIdToChatQueue.get(userSessionId);
        	
        		// Get the map of update queues, then get the update queue for the active chat id
        		ArrayList<DisplayChatWindowUpdateRequest> chatUpdateArray = 
        		        usersChatQueue.get(userIdToActiveChatId.get(userSessionId));

                if(chatUpdateArray != null && !chatUpdateArray.isEmpty()) {
                    // If there is a queue available, dequeue the update
                    return chatUpdateArray.remove(0);
                }
        	}
        }
    	
    	return null;
    }
    
    /**
     * Returns true if the next update for a given user should be enqueued.
     * 
     * @param userSessionId The user session id
     * @param chatId The chat id of the next update
     * @return true if the next update should be enqueued, false otherwise.
     */
    public boolean shouldEnqueueNextChatUpdate(int userSessionId, int chatId) {
    	
    	if(userIdToActiveChatId.containsKey(userSessionId) && userIdToActiveChatId.get(userSessionId) != chatId) {
    		// If the id of the active chat widget doesn't match, the next update should be enqueued
    		return true;
    	}
    	
    	synchronized(userIdToChatQueue){
        	if(userIdToActiveChatId.containsKey(userSessionId) && userIdToChatQueue.containsKey(userSessionId)) {
        	
        		// Get the map of update queues, then get the update queue for the active chat id
        		ArrayList<DisplayChatWindowUpdateRequest> chatUpdateArray = 
        				userIdToChatQueue.get(userSessionId)
        				.get(userIdToActiveChatId.get(userSessionId));
        		
        		// If the chat update array contains something, the next update should be enqueued
        		return (chatUpdateArray != null && !chatUpdateArray.isEmpty());
        	}
    	}
    	
    	return false;
    }
    
    /**
     * Returns true if the next update for a given user should be enqueued.
     * 
     * @param userSessionId The user session id
     * @return true if the next update should be enqueued, false otherwise.
     */
    public boolean shouldEnqueueNextFeedbackUpdate(int userSessionId) {
    	
    	if(userIdHasActiveFeedback.containsKey(userSessionId) && !userIdHasActiveFeedback.get(userSessionId)) {
    		// If the user's feedback widget is inactive, feedback should be enqueued
    		return true;
    	}
    	
    	if(userIdToFeedbackQueue.containsKey(userSessionId)) {
    		return !userIdToFeedbackQueue.get(userSessionId).isEmpty();
    	}
    	
    	return false;
    }
    
    public int getChatUpdateQueueLength(int userSessionId, int chatId) {
    	
        synchronized(userIdToChatQueue){
        	if(userIdToChatQueue.containsKey(userSessionId)) {
        		ArrayList<DisplayChatWindowUpdateRequest> chatUpdateArray = userIdToChatQueue.get(userSessionId).get(chatId);
        		if(chatUpdateArray != null && !chatUpdateArray.isEmpty()) {
        			return chatUpdateArray.size();
        		}
        	}
        }
    	
    	return 0;
    }
    
    public int getFeedbackUpdateQueueLength(int userSessionId) {
    	
    	if(userIdToFeedbackQueue.containsKey(userSessionId)) {
    		return userIdToFeedbackQueue.get(userSessionId).size();
    	}
    	
    	return 0;
    }
    
    /**
     * Discards all queues for the given user.
     * 
     * @param userSessionId The user session id
     */
    public void discardUserQueues(int userSessionId) {
    	
        synchronized(userIdToChatQueue){
            userIdToChatQueue.remove(userSessionId);
        }
    	userIdToActiveChatId.remove(userSessionId);
    	userIdToFeedbackQueue.remove(userSessionId);
    	userIdHasActiveFeedback.remove(userSessionId);
    }
}
