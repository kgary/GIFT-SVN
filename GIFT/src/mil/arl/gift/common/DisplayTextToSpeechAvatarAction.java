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
 * This class contains the information needed for an avatar to convert text to
 * speech and display on the TUI.
 *
 * @author mhoffman
 */
public class DisplayTextToSpeechAvatarAction extends DisplayAvatarAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** the text to convert to speech */
    private String text;

    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public DisplayTextToSpeechAvatarAction() {
    }

    /**
     * Class constructor - set attributes
     * 
     * Preforms text-to-speech using the default avatar
     *
     * @param text - the text to convert to speech
     */
    public DisplayTextToSpeechAvatarAction(String text) {
        this(null, text);
    }

    /**
     * Class constructor - set attributes
     *
     * @param avatarData The avatar to display
     * @param text - the text to convert to speech
     */
    public DisplayTextToSpeechAvatarAction(AvatarData avatarData, String text) {
        super(avatarData);

        if (text == null) {
            throw new IllegalArgumentException("The text can't be null");
        }

        this.text = text;
    }

    /**
     * Gets the the text to convert to speech
     *
     * @return String The text to convert to speech
     */
    public String getText() {

        return text;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayTextToSpeechAvatarAction: ");
        sb.append(super.toString());
        sb.append(", text = ").append(getText());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean hasAudio() {

        // Generating speech should produce audio
        return true;
    }
}
