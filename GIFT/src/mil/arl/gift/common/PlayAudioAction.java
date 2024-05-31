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
 * A feedback action to play some audio
 *
 * @author jleonard
 */
public class PlayAudioAction implements FeedbackAction, Serializable {
    
    private static final long serialVersionUID = 1L;

    private String mp3AudioFile;

    private String oggAudioFile;

    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public PlayAudioAction() {
    }

    /**
     * Constructor
     *
     * @param mp3AudioFile The MP3 file to play for feedback
     * @param oggAudioFile The OGG file to play for feedback
     */
    public PlayAudioAction(String mp3AudioFile, String oggAudioFile) {
        
        if(mp3AudioFile == null) {
            
            throw new IllegalArgumentException("The MP3 file path cannot be null");
        }

        this.mp3AudioFile = mp3AudioFile;
        this.oggAudioFile = oggAudioFile;
    }

    /**
     * Gets the MP3 file to play for feedback
     *
     * @return String The MP3 file to play for feedback
     */
    public String getMp3AudioFile() {

        return mp3AudioFile;
    }

    /**
     * Gets the OGG file to play for feedback
     *
     * @return String The OGG file to play for feedback
     */
    public String getOggAudioFile() {

        return oggAudioFile;
    }

    @Override
    public boolean hasAudio() {
        
        // Will always play audio
        return true;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[PlayAudioAction: ");
        sb.append("mp3 = ").append(getMp3AudioFile());
        sb.append(", ogg = ").append(getOggAudioFile());
        sb.append("]");
        return sb.toString();
    }
}
