/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * This class contains information that is used by the Tutor User Interface to display a chat window.
 * 
 * @author mhoffman
 *
 */
public class DisplayChatWindowRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_TUTOR_NAME = "GIFT";
    public static final String DEFAULT_USER_NAME = "me";

    /** information of the avatar to present when first showing the chat window */
    private DisplayAvatarAction avatar;
    
    /** the label to give to the tutor's chat window entries */
    private String tutorName = DEFAULT_TUTOR_NAME;
    
    /** the label to give to the user's chat window entries */
    private String userName = DEFAULT_USER_NAME;
    
    /** flag used to indicate whether the user of the chat window should be allowed to continue w/o having to complete the chat */
    private boolean provideBypass = false;
        
    /** whether or not the chat should be presented in fullscreen mode */
    private boolean fullscreen = false;
    
    /** unique id of the chat used to match/sync updates and chat history */
    private int chatId;
    
    /** the name of this conversation */
    private String chatName = "Unknown";
    
    /** the conversation description */
    private String description = "Unknown";
    
    /** used to keep track of the next unique chat session id to give */
    private static int SHARED_ID = 0;
    
    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility.
     * 
     * Note: Should not be used for any other purpose.  This constructor will not generate a valid chat id.
     */
    public DisplayChatWindowRequest(){

    }
    
    /**
     * Class constructor - set attributes.
     * Note: this constructor generates a new unique chat id.
     * 
     * @param avatar - information of the avatar to present when first showing the chat window
     * @param tutorName - the label to give to the tutor's chat window entries 
     * @param userName - the label to give to the user's chat window entries
     */
    public DisplayChatWindowRequest(DisplayAvatarAction avatar, String tutorName, String userName){
        this(getNextChatId(), avatar, tutorName, userName);
    }
    
    /**
     * Class constructor - set attributes.
     * Note: this constructor generates a new unique chat id.
     * 
     * @param avatar - information of the avatar to present when first showing the chat window
     */
    public DisplayChatWindowRequest(DisplayAvatarAction avatar){
        this(getNextChatId(), avatar);
    }
    
    /**
     * Class constructor - set attributes.
     * 
     * @param chatId - unique chat id that can be used used to match/sync updates and chat history  
     * @param avatar - information of the avatar to present when first showing the chat window
     * @param tutorName - the label to give to the tutor's chat window entries 
     * @param userName - the label to give to the user's chat window entries
     */
    public DisplayChatWindowRequest(int chatId, DisplayAvatarAction avatar, String tutorName, String userName){
        setChatId(chatId);
        setAvatar(avatar);
        setTutorName(tutorName);
        setUserName(userName);
    }
    
    /**
     * Class constructor - set attributes.
     * 
     * @param chatId - unique chat id that can be used used to match/sync updates and chat history  
     * @param avatar - information of the avatar to present when first showing the chat window
     */
    public DisplayChatWindowRequest(int chatId, DisplayAvatarAction avatar){
        setChatId(chatId);
        setAvatar(avatar);
    }    
    
    /**
     * Return the next new unique chat id
     * 
     * @return int
     */
    public synchronized static int getNextChatId(){
        return ++SHARED_ID;
    }
    
    /**
     * Return the unique chat id that can be used used to match/sync updates and chat history 
     * 
     * @return int
     */
    public int getChatId(){
        return chatId;
    }
    
    /**
     * Set the unique chat id that can be used used to match/sync updates and chat history 
     * 
     * @param chatId - unique id for a given chat session
     */
    private void setChatId(int chatId){
        
        if(chatId <= 0){
            throw new IllegalArgumentException("The chat id of "+chatId+" must be greater than zero to be considered a valid id.");
        }
        
        this.chatId = chatId;
    }
    
    /**
     * Return whether or not to allow the user to continue w/o having to complete the chat
     * 
     * @return boolean
     */
    public boolean shouldProvideBypass(){
        return provideBypass;
    }
    
    /**
     * Set whether or not to allow the user to continue w/o having to complete the chat.
     * 
     * @param value flag used to indicate whether the user of the chat window should be allowed to continue w/o having to complete the chat
     */
    public void setProvideBypass(boolean value){
        this.provideBypass = value;
    }
        
    /**
     * Returns whether or not this this conversation should be displayed in fullscreen mode
     * 
     * @return whether this conversation should be presented in fullscreen mode
     */
    public boolean isFullscreen() {
    	return fullscreen;
    }
    
    /**
     * Whether to display this conversation in fullscreen mode
     * 
     * @param fullscreen whether this conversation should be presented in fullscreen mode
     */
    public void setFullscreen(boolean fullscreen) {
    	this.fullscreen = fullscreen;
    }
    
    /**
     * Gets the information of the avatar to present when first showing the chat window
     *
     * @return The information of the avatar to present when first showing the chat window. Can be null.
     */
    public DisplayAvatarAction getAvatar() {

        return avatar;
    }

    /**
     * Sets the information of the avatar to present when first showing the chat window
     *
     * @param avatar The information of the avatar to present when first showing the chat window. Can be null.
     */
    public void setAvatar(DisplayAvatarAction avatar) {

        this.avatar = avatar;
    }
    
    /**
     * Gets the label to give to the tutor's chat window entries
     *
     * @return String - the label to give to the tutor's chat window entries
     */
    public String getTutorName() {

        return tutorName;
    }

    /**
     * Sets the label to give to the tutor's chat window entries
     *
     * @param tutorName the label to give to the tutor's chat window entries
     */
    public void setTutorName(String tutorName) {

        if(tutorName == null){
            throw new IllegalArgumentException("The tutor name can't be null");
        }
        
        this.tutorName = tutorName;
    }
    
    /**
     * Gets the label to give to the user's chat window entries
     *
     * @return String - the label to give to the user's chat window entries
     */
    public String getUserName() {

        return userName;
    }

    /**
     * Sets the label to give to the user's chat window entries
     *
     * @param userName the label to give to the user's chat window entries
     */
    public void setUserName(String userName) {

        if(userName == null){
            throw new IllegalArgumentException("The user name can't be null");
        }
        
        this.userName = userName;
    }
    
    /**
     * Gets the name of the conversation
     *
     * @return the name of the conversation. Won't be null.
     */
    public String getChatName() {

        return chatName;
    }

    /**
     * Sets the name of the conversation
     *
     * @param chatName the name of the conversation.  Can't be null.
     */
    public void setChatName(String chatName) {

        if(chatName == null){
            throw new IllegalArgumentException("The chat name can't be null");
        }
        
        this.chatName = chatName;
    }
    
    /**
     * Gets the description of the conversation
     *
     * @return the description of the conversation
     */
    public String getDescription() {

        return description;
    }

    /**
     * Sets the description of the conversation
     *
     * @param description the conversation description.  Can't be null.
     */
    public void setDescription(String description) {

        if(description == null){
            throw new IllegalArgumentException("The conversation description can't be null");
        }
        
        this.description = description;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DisplayChatWindow: ");
        sb.append("id = ").append(getChatId());
        sb.append(", avatar = ").append(getAvatar());
        sb.append(", user = ").append(getUserName());
        sb.append(", tutor = ").append(getTutorName());
        sb.append(", name = ").append(getChatName());
        sb.append(", description = ").append(getDescription());
        sb.append(", provideBypass = ").append(shouldProvideBypass());
        sb.append(", fullscreen = ").append(isFullscreen());
        sb.append("]");
        
        return sb.toString();            
    }
}
