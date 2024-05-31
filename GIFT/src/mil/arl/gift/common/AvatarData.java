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
 * This class contains the base information for an avatar
 *
 * @author jleonard
 */
public class AvatarData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** the avatar to display */
    private String avatarURL;

    /** the width of the avatar on the webpage, in pixels */
    private int avatarWidth;

    /** the height of the avatar on the webpage, in pixels */
    private int avatarHeight;

    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public AvatarData() {
    }

    /**
     * Class constructor - set attribute
     *
     * @param avatarURL - the avatar to display
     * @param avatarHeight - the width of the avatar on the webpage, in pixels
     * @param avatarWidth - the height of the avatar on the webpage, in pixels
     */
    public AvatarData(String avatarURL, int avatarHeight, int avatarWidth) {

        if (avatarURL == null) {

            throw new IllegalArgumentException("The avatar URL can't be null");
        }

        this.avatarURL = avatarURL;

        if (avatarWidth <= 0) {

            throw new IllegalArgumentException("The width must be greater than zero");
        }

        this.avatarWidth = avatarWidth;

        if (avatarHeight <= 0) {

            throw new IllegalArgumentException("The height must be greater than zero");
        }

        this.avatarHeight = avatarHeight;
    }

    /**
     * Return the avatar URL
     *
     * @return String The Avatar URL
     */
    public String getURL() {

        return avatarURL;
    }

    /**
     * Gets the width of the avatar on the webpage, in pixels
     *
     * @return int The width of the avatar on the webpage, in pixels
     */
    public int getWidth() {

        return avatarWidth;
    }

    /**
     * Gets the height of the avatar on the webpage, in pixels
     *
     * @return int The height of the avatar on the webpage, in pixels
     */
    public int getHeight() {

        return avatarHeight;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("[AvatarData: ");
        sb.append("URL = ").append(getURL());
        sb.append(", height = ").append(getHeight());
        sb.append(", width = ").append(getWidth());
        sb.append("]");
        return sb.toString();
    }
}
