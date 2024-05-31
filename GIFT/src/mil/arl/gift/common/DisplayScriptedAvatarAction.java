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
 * This class contains the information needed for an avatar to execute a
 * pre-rendered action such as recorded audio.
 *
 * @author mhoffman
 */
public class DisplayScriptedAvatarAction extends DisplayAvatarAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The key of the avatar action (e.g. message to play for feedback) */
    private String action;

    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public DisplayScriptedAvatarAction() {
    }

    /**
     * Class constructor - set attributes
     *
     * @param avatarData The avatar to display
     * @param action - the key of the avatar action
     */
    public DisplayScriptedAvatarAction(AvatarData avatarData, String action) {
        super(avatarData);

        setAvatar(avatarData);

        if (action == null) {

            throw new IllegalArgumentException("The action can't be null");
        }

        this.action = action;
    }

    /**
     * Return the action to execute by the avatar
     *
     * @return The action to execute by the avatar
     */
    public String getAction() {

        return action;
    }
    
    @Override
    public final void setAvatar(AvatarData avatarData) {

        // Cannot have null avatar, default avatar is not scripted
        if (avatarData == null) {

            throw new IllegalArgumentException("The avatar cannot be null for a scripted action");
        }

        super.setAvatar(avatarData);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayScriptedAvatarAction: ");
        sb.append(super.toString());
        sb.append(", action = ").append(getAction());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean hasAudio() {

        // The action may or may not have audio associated with it, assume that
        // it does
        return true;
    }
}
