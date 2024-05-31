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
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.tutor.shared.AbstractAction;
import mil.arl.gift.tutor.shared.ActionTypeEnum;
import mil.arl.gift.tutor.shared.ChatWindowEntry;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.WidgetLocationEnum;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.AvatarContainerWidgetProperties;
import mil.arl.gift.tutor.shared.properties.ChatWindowWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * The state information for an active request to display a chat window
 *
 * @author jleonard
 */
public class ActiveChatWindowRequest {
    
    private static Logger logger = LoggerFactory.getLogger(ActiveChatWindowRequest.class);

    private final DomainWebState webState;

    private final DisplayChatWindowRequest request;

    /** the map of unique domain session id to all the (unfinished) chats for that course */
    private static final Map<Integer, DomainSessionChats> DOMAIN_SESSION_TO_CHAT_DATA = new HashMap<>();
    
    private final WidgetProperties properties = new WidgetProperties();

    private final ActionListener parentListener;

    private final ActionListener listener = new ActionListener() {
        @Override
        public void onAction(AbstractAction action) {

            if (action.getActionType() == ActionTypeEnum.SUBMIT) {

                SubmitAction submitAction = (SubmitAction) action;

                WidgetProperties properties = submitAction.getProperties();
                
                ChatWindowWidgetProperties.setUserName(properties, request.getUserName());
                
                String enteredText = ChatWindowWidgetProperties.getEnteredText(properties);
                
                if(enteredText != null && !enteredText.isEmpty()) {
                    
                    int dsId = webState.getDomainSessionId();
                    int chatId = ChatWindowWidgetProperties.getChatId(properties);
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("User entered chat text of: '"+enteredText+"' for chat "+chatId+" in domain session: "+webState+".");
                    }
                    
                    DomainSessionChats dsChats = DOMAIN_SESSION_TO_CHAT_DATA.get(dsId);
                    ChatData chatData = dsChats.get(chatId);
                    
                	ChatLog chatLog = chatData.getChatLog();
                	LinkedList<ChatWindowEntry> chatWindowEntries = chatData.getChatWindowEntries();
                	
                	chatLog.getUserEntries().add(enteredText);
                	
                	chatWindowEntries.add(new ChatWindowEntry(request.getUserName(), enteredText));

                    TutorModule.getInstance().sendChatMessage(chatLog, webState, new MessageCollectionCallback() {
                        @Override
                        public void success() {
                        }

                        @Override
                        public void received(Message msg) {
                        }

                        @Override
                        public void failure(Message msg) {
                        }

                        @Override
                        public void failure(String why) {
                        }
                    });
                }
            }

            if (parentListener != null) {

                parentListener.onAction(action);
            }
        }
    };

    /**
     * Constructor - set attributes
     *
     * @param webState The domain web state the chat window belongs to.
     * @param request The chat window request
     * @param parentListener The listener for actions in this chat session
     */
    public ActiveChatWindowRequest(DomainWebState webState, DisplayChatWindowRequest request, ActionListener parentListener) {
        
        if(webState == null){
            throw new IllegalArgumentException("The session can't be null.");
        }else if(request == null){
            throw new IllegalArgumentException("The request can't be null.");
        }
        
        this.webState = webState;
        this.request = request;
        this.parentListener = parentListener;
        
        DomainSessionChats dsChats = DOMAIN_SESSION_TO_CHAT_DATA.get(webState.getDomainSessionId());
        if(dsChats == null){
            dsChats = new DomainSessionChats();
            DOMAIN_SESSION_TO_CHAT_DATA.put(webState.getDomainSessionId(), dsChats);
        }
        
        ChatData chatData = dsChats.get(request.getChatId());
        if(chatData == null) {
            dsChats.put(request.getChatId(), new ChatData(request.getChatId()));
        }
        
        ChatWindowWidgetProperties.setChatId(properties, request.getChatId());
        ChatWindowWidgetProperties.setChatName(properties, request.getChatName());
        ChatWindowWidgetProperties.setDescription(properties, request.getDescription());
        ChatWindowWidgetProperties.setAvatar(properties, request.getAvatar().getAvatar());
        ChatWindowWidgetProperties.setFullscreen(properties, request.isFullscreen());
        ChatWindowWidgetProperties.setAllowEarlyExit(properties, request.shouldProvideBypass());
    }

    /**
     * Displays the chat window in the client
     * 
     * @param chatId the id of the chat to display.
     */
    public void displayWidget(final int chatId) {

        DomainSessionChats dsChats = DOMAIN_SESSION_TO_CHAT_DATA.get(webState.getDomainSessionId());
    	ChatData chatData = dsChats.get(chatId);
    	LinkedList<ChatWindowEntry> chatLog = chatData.getChatWindowEntries();
    	
    	properties.setShouldUpdate(true);
    	AvatarContainerWidgetProperties.setWidgetType(properties, WidgetTypeEnum.CHAT_WINDOW_WIDGET);
    	ChatWindowWidgetProperties.setChatId(properties, chatId);
        ChatWindowWidgetProperties.setChatLog(properties, chatLog);
        
        if(ChatWindowWidgetProperties.getAvatar(properties) != null && ChatWindowWidgetProperties.getEnteredText(properties) != null
                && !ChatWindowWidgetProperties.getEnteredText(properties).isEmpty()){
            webState.characterBusyNotification();
        }

        webState.displayWidget(WidgetTypeEnum.AVATAR_CONTAINER_WIDGET, properties, WidgetLocationEnum.ARTICLE, listener);
        
        //clean up the finished conversation memory (in the future we may hold on to this info for an AAR or something else)
        if(ChatWindowWidgetProperties.isFinished(properties)){
            dsChats.remove(chatId);
        }
    }
    
    /**
     * Clean up the chat log information for the closing course.
     * 
     * @param domainSessionId the unique id of the course that is closing
     */
    public static void domainSessionClosing(int domainSessionId){
        DOMAIN_SESSION_TO_CHAT_DATA.remove(domainSessionId);
    }

    /**
     * Displays the chat window in the client
     * 
     * @param chatId the id of the chat to display.
     */
    public void displayUpdateNotification(final int chatId, final int updateCount) {
    	
    	WidgetProperties properties = new WidgetProperties();
    	properties.setShouldUpdate(true);
    	
    	AvatarContainerWidgetProperties.setWidgetType(properties, WidgetTypeEnum.CHAT_WINDOW_WIDGET);
    	ChatWindowWidgetProperties.setChatId(properties, chatId);
        ChatWindowWidgetProperties.setUpdateCount(properties, updateCount);
        
        webState.displayWidget(WidgetTypeEnum.AVATAR_CONTAINER_WIDGET, properties, WidgetLocationEnum.ARTICLE, listener);
    }
    
    /**
     * Handles an update to the chat window
     *
     * @param update The update to the chat window
     */
    public void handleUpdate(DisplayChatWindowUpdateRequest update) {

        DomainSessionChats dsChats = DOMAIN_SESSION_TO_CHAT_DATA.get(webState.getDomainSessionId());
        ChatData chatData = dsChats.get(update.getChatId());
        chatData.getChatLog().getTutorEntries().add(update.getText());
        
        if(update.getText() != null && !update.getText().isEmpty()) {
        	
        	ChatWindowEntry chatEntry = new ChatWindowEntry(request.getTutorName(), update.getText());
        	
        	if(update.getChoices() != null && !update.getChoices().isEmpty()) {
            	chatEntry.setChoices(update.getChoices());
            }
        	
        	LinkedList<ChatWindowEntry> chatWindowEntries = chatData.getChatWindowEntries();        	
        	chatWindowEntries.add(chatEntry);
        }

        if (update.isChatClosed()) {

            ChatWindowWidgetProperties.setIsFinished(properties, true);

        } else {

            ChatWindowWidgetProperties.setIsFinished(properties, false);
        }
        
        ChatWindowWidgetProperties.setAllowFreeResponse(properties, update.shouldAllowFreeResponse());

        ChatWindowWidgetProperties.setEnteredText(properties, update.getText());

        if (update.getAvatarAction() != null) {

            ChatWindowWidgetProperties.setAvatar(properties, update.getAvatarAction().getAvatar());

            if (update.getAvatarAction() instanceof DisplayTextToSpeechAvatarAction) {

                DisplayTextToSpeechAvatarAction ttsAvatarAction = (DisplayTextToSpeechAvatarAction) update.getAvatarAction();

                ChatWindowWidgetProperties.setEnteredText(properties, ttsAvatarAction.getText());
            }
        }
    }
    
    /**
     * Return the unique conversation id (created by the server) for the request to display
     * a chat window.
     * 
     * @return conversation id of the chat to display
     */
    public int getChatId(){
        return request.getChatId();
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[ActiveChatWindowRequest: ");
        sb.append("chatId = ").append(request.getChatId());
        sb.append("webState = ").append(webState);
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Map of chat id (unique to a domain module instance but not among domain modules
     * or restarts of the same domain module) to the chat log information.
     * 
     * @author mhoffman
     *
     */
    private class DomainSessionChats extends HashMap<Integer, ChatData>{

        /**
         * default
         */
        private static final long serialVersionUID = 1L;
        
    }
    
    /**
     * Contains the data for a single chat.
     * 
     * @author mhoffman
     *
     */
    private class ChatData{
        
        /** 
         * unique to a domain module instance but not among domain modules
         * or restarts of the same domain module
         */ 
        private int chatId;
        
        /** contains the history of both the tutor and learner chat entries */
        private ChatLog chatLog;
        
        /** the ordered chat entries of all parties in the chat */
        private LinkedList<ChatWindowEntry> chatWindowEntries = new LinkedList<ChatWindowEntry>();
        
        /**
         * Set attribute(s)
         * 
         * @param chatId the id of the chat
         */
        public ChatData(int chatId){
            this.chatId = chatId;
            chatLog = new ChatLog(chatId, new ArrayList<String>(), new ArrayList<String>());
        }
        
        /**
         * Return the ordered chat entries of all parties in the chat
         * 
         * @return will not be null but will be empty if the chat has no entries yet
         */
        public LinkedList<ChatWindowEntry> getChatWindowEntries(){
            return chatWindowEntries;
        }
        
        /**
         * Return the history of both the tutor and learner chat entries 
         * 
         * @return will not be null
         */
        public ChatLog getChatLog(){
            return chatLog;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ChatData: chatId = ");
            builder.append(chatId);
            builder.append("]");
            return builder.toString();
        }        
        
    }
}
