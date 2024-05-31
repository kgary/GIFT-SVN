/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.arl.gift.common.enums.MessageTypeEnum;

/**
 * Contains information about initialize gateway interop plugins.  This information can be 
 * used to determine what services might be available in the gateway at this point in the course execution.
 * E.g. will feedback be presented in the external training application, can the external training application
 * answer a survey question being presented on the tutor.
 * 
 * @author mhoffman
 *
 */
public class InteropConnectionsInfo {

    /** collection of message types the current gateway interop plugins can handle */
    private List<MessageTypeEnum> supportedMessages;
    
    public InteropConnectionsInfo(){
        supportedMessages = new ArrayList<>();
    }
    
    /**
     * Return the collection of message types the current gateway interop plugins can handle 
     * 
     * @return can be empty but not null
     */
    public List<MessageTypeEnum> getSupportedMessages(){
        return supportedMessages;
    }
    
    /**
     * Add a supported message type that can be handled by one or more gateway interop plugins.
     * 
     * @param type can't be null
     */
    public void addSupportedMessageType(MessageTypeEnum type){
        
        if(type == null){
            throw new IllegalArgumentException("The type can't be null.");
        }
        
        supportedMessages.add(type);
    }
    
    /**
     * Add a collection of supported message type that can be handled by one or more gateway interop plugins.
     * 
     * @param types can't be null
     */
    public void addSupportedMessageTypes(Collection<MessageTypeEnum> types){
        
        if(types == null){
            throw new IllegalArgumentException("The types can't be null.");
        }
        
        supportedMessages.addAll(types);
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[InteropConnectionsInfo: ");
        
        sb.append("supportedMessages = {");
        for(MessageTypeEnum type : getSupportedMessages()){
            sb.append(" ").append(type).append(",");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
}
