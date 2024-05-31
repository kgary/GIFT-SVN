/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.List;

/**
 * A request to display a chat window with an update
 *
 * @author jleonard
 */
public class DisplayChatWindowUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /** text to display to the user in the chat window (i.e. a chat entry from the tutor) */
    private String text;
    
    /** (optional) choices for the learner to choose from (i.e. a multiple choice question posed by the tutor) */
    private List<String> choices;
    
    /** whether this update is the last update because the chat session is now closed */
    private boolean chatClosed = false;    
    
    /** whether to allow free response input from the user following the application of this chat update */
    private boolean allowFreeResponse = false;
    
    /** the avatar action (e.g. text to speech) information for this update. */
    private DisplayAvatarAction avatarAction;
    
    /** the unique chat id of the chat this update applies too */
    private int chatId;

    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public DisplayChatWindowUpdateRequest(){
        
    }
    
    /**
     * Class constructor - set attribute(s).
     * 
     * @param chatClosed - whether this update is the last update because the chat session is now closed
     * @param chatId - unique id of a chat this update request is associated with
     */
    public DisplayChatWindowUpdateRequest(boolean chatClosed, int chatId){
        
        this.chatClosed = chatClosed;
        setChatId(chatId);
    }
    
    /**
     * Set the text to show as a chat entry from the tutor.
     * 
     * @param text text to display to the user in the chat window (i.e. a chat entry from the tutor) 
     */
    public void setText(String text){
        
        if(text == null){
            throw new IllegalArgumentException("The text can't be null");
        }
        
        this.text = text;
    }
    
    /**
     * Gets the text to show as a chat entry from the tutor.
     *
     * @return String
     */
    public String getText() {

        return text;
    }
    
    /**
     * Set the choices for the learner to choose from (i.e. a multiple choice question posed by the tutor)
     * 
     * @param choices can be null or empty if there are no choices for the learner
     */
    public void setChoices(List<String> choices){
        this.choices = choices;
    }
    
    /**
     * Return the choices for the learner to choose from (i.e. a multiple choice question posed by the tutor)
     * 
     * @return can be null or empty if there are no choices related to the text the tutor is presenting to the learner
     */
    public List<String> getChoices(){
        return choices;
    }
    
    /**
     * Return whether the chat session is now closed.
     *
     * @return boolean
     */
    public boolean isChatClosed() {
        return chatClosed;
    }
    
    /**
     * Whether to allow the learner to provide a free response answer after this update
     * is applied to the conversation.
     * 
     * @param allowFreeResponse whether this chat update allows the learner to provide a free formed response
     */
    public void setAllowFreeResponse(boolean allowFreeResponse){
        this.allowFreeResponse = allowFreeResponse;
    }
    
    /**
     * Return whether to allow the learner to provide a free response answer after this update
     * is applied to the conversation.
     * 
     * @return whether this chat update allows the learner to provide a free formed response
     */
    public boolean shouldAllowFreeResponse(){
        return allowFreeResponse;
    }

    /**
     * Return the avatar action information for this update.
     *
     * @return can be null if no avatar action is
     * warranted
     */
    public DisplayAvatarAction getAvatarAction() {

        return avatarAction;
    }

    /**
     * Set the avatar action information for this update.
     *
     * @param avatarAction the avatar action (e.g. text to speech) information for this update.
     */
    public void setAvatarAction(DisplayAvatarAction avatarAction) {

        this.avatarAction = avatarAction;
    }
    
    private void setChatId(int chatId){
        
        if(chatId <= 0){
            throw new IllegalArgumentException("The chat id of "+chatId+" must be greater than zero to be considered a valid id.");
        }
        
        this.chatId = chatId;
    }
    
    /**
     * Return the unique chat id of the chat this update applies too.
     *  
     * @return int
     */
    public int getChatId(){
        return chatId;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayChatWindowUpdateRequest: ");
        sb.append("id = ").append(getChatId());
        sb.append(", chatClosed = ").append(isChatClosed());
        sb.append(", allowFreeResponse = ").append(shouldAllowFreeResponse());
        sb.append(", text = ").append(getText());
        sb.append(", choices = ").append(getChoices());
        sb.append(", avatarAction = ").append(getAvatarAction());
        sb.append("]");
        return sb.toString();
    }
}
