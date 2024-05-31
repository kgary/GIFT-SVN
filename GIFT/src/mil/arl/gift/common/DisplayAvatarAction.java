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
 * This class contains the base information for displaying an avatar on the TUI.
 *
 * @author mhoffman
 */
public class DisplayAvatarAction implements Serializable, FeedbackAction {

    private static final long serialVersionUID = 1L;

    /** the avatar to display */
    private AvatarData avatarData = null;
    
    /** Whether the TUI should only preload the avatar or if it should display it as well */
    private boolean preloadOnly = false;

    /**
     * Default Constructor
     * 
     * Assumes the default avatar should be used
     *
     * Required to be exist and be public for GWT compatibility
     */
    public DisplayAvatarAction() {
        
    }

    /**
     * Class constructor - set attribute
     *
     * @param avatarData The avatar to display, if null, the default avatar will be used
     */
    public DisplayAvatarAction(AvatarData avatarData) {

        this.avatarData = avatarData;
    }

    /**
     * Return the avatar to display
     * 
     * If null, use the default avatar
     *
     * @return AvatarData The avatar to display
     */
    public AvatarData getAvatar() {

        return avatarData;
    }
    
    /**
     * Sets the avatar to display
     * 
     * If null, the default avatar will be used
     * 
     * @param avatarData The avatar to display
     */
    public void setAvatar(AvatarData avatarData) {
        
        this.avatarData = avatarData;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayAvatarAction: ");
        sb.append("avatar = ").append(getAvatar());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean hasAudio() {

        // Simply displaying an avatar should not produce any audio
        return false;
    }

    /**
     * Gets whether the TUI should only preload the avatar or if it should display it as well
     * 
     * @return whether the TUI should only preload the avatar
     */
    public boolean isPreloadOnly() {
        return preloadOnly;
    }

    /**
     * Sets whether the TUI should only preload the avatar or if it should display it as well
     * 
     * @param preloadOnly whether the TUI should only preload the avatar
     */
    public void setPreloadOnly(boolean preloadOnly) {
        this.preloadOnly = preloadOnly;
    }
}
