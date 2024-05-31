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
 * This class contains a snapshot of a chat window log between the GIFT user and the GIFT tutor.
 * 
 * @author mhoffman
 *
 */
public class ChatLog implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** temporally ordered tutor chat entries */
    private List<String> tutorEntries;
    
    /**  temporally ordered user chat entries */
    private List<String> userEntries;
    
    /** the unique chat id of the current Chat this log applies too */
    private int chatId;

    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public ChatLog(){
        
    }
    
    /**
     * Class constructor - set attributes.
     * 
     * @param chatId - the unique chat id of the current Chat this log applies too. 
     * @param tutorEntries -  temporally ordered tutor chat entries. Can't be null but can be empty.
     * @param userEntries - temporally ordered user chat entries. Can't be null but can be empty.
     */
    public ChatLog(int chatId, List<String> tutorEntries, List<String> userEntries){
        
        setChatId(chatId);
        setTutorEntries(tutorEntries);
        setUserEntries(userEntries);
    }
    
    private void setChatId(int chatId){
        
        if(chatId <= 0){
            throw new IllegalArgumentException("The chat id of "+chatId+" must be greater than zero to be considered a valid id.");
        }
        
        this.chatId = chatId;
    }
    
    /**
     * Return the unique chat id of the current Chat this log applies too.
     * 
     * @return int
     */
    public int getChatId(){
        return chatId;
    }
    
    /**
     * Set the temporally ordered tutor chat entries
     * 
     * @param tutorEntries - the latest entries. Can't be null but can be empty.
     */
    public void setTutorEntries(List<String> tutorEntries){
        
        if(tutorEntries == null){
            throw new IllegalArgumentException("The tutor entries can't be null");
        }
        
        this.tutorEntries = tutorEntries;
    }
    
    /**
     * Get the temporally ordered tutor chat entries
     * 
     * @return won't be null but can be empty
     */
    public List<String> getTutorEntries(){
        return tutorEntries;
    }
    
    /**
     * Set the temporally ordered user chat entries
     * 
     * @param userEntries - the latest entries. Can't be null but can be empty.
     */
    public void setUserEntries(List<String> userEntries){
        
        if(userEntries == null){
            throw new IllegalArgumentException("The user entries can't be null");
        }
        
        this.userEntries = userEntries;
    }
    
    /**
     * Get the temporally ordered user chat entries
     * 
     * @return won't be null but can be empty
     */
    public List<String> getUserEntries(){
        return userEntries;
    }
    
    /**
     * Return the last user's entry.
     * 
     * @return String - can be null if the user hasn't entered anything yet
     */
    public String getLastUserEntry(){
        
        if(userEntries.isEmpty()){
            return null;
        }else{
            return getUserEntries().get(getUserEntries().size()-1);
        }
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ChatLog: ");
        sb.append("id = ").append(getChatId());
        
        sb.append(", # tutor entries = ").append(getTutorEntries().size());        
        if(!getTutorEntries().isEmpty()){
            sb.append(", last tutor entry = ").append(getTutorEntries().get(getTutorEntries().size()-1));
        }
        
        sb.append(", # user entries = ").append(getUserEntries().size());
        if(!getUserEntries().isEmpty()){
            sb.append(", last user entry = ").append(getUserEntries().get(getUserEntries().size()-1));
        }
        
        sb.append("]");
        return sb.toString();
    }
}
