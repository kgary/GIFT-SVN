/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to handle the storing and retrieval of conversation variables.
 * 
 * @author mhoffman
 *
 */
public class ConversationVarsHandler {
    
    /**
     * mapping of conversation variable name to values that have been provided during
     * course execution.
     */
    private Map<String, String> conversationVariableMap = new ConcurrentHashMap<>();
    
    /**
     * Store a variable using the key and value provided.
     * 
     * @param key used to retrieve the value.  can't be null or empty.
     * @param value the value to use in the conversation.  can't be null.
     * @return the previous value associated with that key.  will be null if there was no previous value.
     */
    public String setVariable(String key, String value){
        
        if(key == null || key.isEmpty()){
            throw new IllegalArgumentException("The key can't be null or empty.");
        }else if(value == null){
            throw new IllegalArgumentException("The value can't be null.");
        }
        
        return conversationVariableMap.put(key, value);
    }
    
    /**
     * Return the value associated with the key.
     * 
     * @param key used to retrieve the value that was previously associated with that key.  can't be null.
     * @return the value associated with the key.  Can be null if a value was not associated with the key.
     */
    public String getValue(String key){
        return conversationVariableMap.get(key);        
    }

}
