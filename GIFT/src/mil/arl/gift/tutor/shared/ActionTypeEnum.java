/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

/**
 * The types of actions that can be taken
 *
 * @author jleonard
 */
public enum ActionTypeEnum {

    INVALID(),
    DISPLAY_WIDGET(),
    DISPLAY_DIALOG(),
    SUBMIT(),
    CLOSE(),
    DEACTIVATE(),
    END_COURSE(),
    START_COURSE(),
    AVATAR_IDLE(),
    AVATAR_BUSY(),
    NO_UPDATE(),
    INIT_APP(),
    SEND_APP_MESSASGE(),
    LESSON_STATUS(),
    SYNCHRONIZE_CLIENT_STATE(),
    KNOWLEDGE_SESSIONS_UPDATED(),
    PRELOAD_AVATAR();
    
    
    private ActionTypeEnum() {
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ActionTypeEnum: ");
        sb.append("value = ").append(this.name());
        sb.append("]");
        
        return sb.toString();
    }
}
