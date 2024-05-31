/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This action is used to indicate that the TUI avatar is idle or busy.
 * 
 * @author mhoffman
 *
 */
public class AvatarStatusAction extends AbstractAction implements IsSerializable {

    /**
     * Default Constructor
     *
     * Required for GWT - DO NOT USE
     */
    public AvatarStatusAction() {
        super(ActionTypeEnum.AVATAR_IDLE);
        
    }
    
    /**
     * Set the avatar state.
     * 
     * @param idle true if the state of the avatar is idle, false if busy
     */
    public AvatarStatusAction(boolean idle){
        super(idle ? ActionTypeEnum.AVATAR_IDLE : ActionTypeEnum.AVATAR_BUSY);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[AvatarStatusAction: ");
        sb.append(super.toString());
        return sb.toString();
    }
}
