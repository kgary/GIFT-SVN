/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.AvatarData;

/**
 * An action to load an avatar to a client before it is actually displayed
 *
 * @author nroberts
 */
public class PreloadAvatarAction extends AbstractAction implements IsSerializable {
    
    /** The display data for the avatar being loaded */
    private AvatarData avatarData;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    private PreloadAvatarAction() {
        super(ActionTypeEnum.PRELOAD_AVATAR);
    }

    /**
     * Creates an action that notifies a client to load an avatar before it is shown
     *
     * @param avatarData The display data for the avatar being loaded
     */
    public PreloadAvatarAction(AvatarData avatarData) {
        this();
        
        this.avatarData = avatarData;
    }

    /**
     * Gets the display data for the avatar being loaded
     * 
     * @return the avatar display data
     */
    public AvatarData getAvatarData() {
        return avatarData;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[PreloadAvatarAction: ")
        .append(super.toString())
        .append(" avatarData = ")
        .append(avatarData)
        .append("]");
        
        return sb.toString();
    }
}
