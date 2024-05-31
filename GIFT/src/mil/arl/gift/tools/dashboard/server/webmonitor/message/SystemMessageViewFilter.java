/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.webmonitor.message;

import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;

/**
 * This message filter accepts all messages except domain session messages.
 * 
 * @author nroberts
 */
public class SystemMessageViewFilter extends MessageViewFilter {

    @Override
    public boolean acceptMessage(Message msg) {
        
        boolean isSelected = super.acceptMessage(msg);
        if(!isSelected && !rawList.contains(msg.getMessageType())){
            //message type is not in the filter's list
            
            if(!(msg instanceof DomainSessionMessage)){
                //add the new system session message type to the list
                addChoice(msg.getMessageType());
            }
        }
        
        return isSelected;
    }
}
