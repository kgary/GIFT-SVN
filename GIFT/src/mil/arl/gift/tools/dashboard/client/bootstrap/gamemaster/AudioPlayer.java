/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;

/**
 * A widget providing controls to allow the user to play back recorded audio
 * 
 * @author nroberts
 */
public class AudioPlayer extends Composite {

    private static AudioPlayerUiBinder uiBinder = GWT.create(AudioPlayerUiBinder.class);

    interface AudioPlayerUiBinder extends UiBinder<Widget, AudioPlayer> {
    }
    
    /** The button used to delete media that has been recorded */
    @UiField
    protected Button deleteRecordingButton;
    
    /** The button used to play media that has been recorded */
    @UiField
    protected Button playRecordingButton;
    
    /** The button used to pause media that has been recorded */
    @UiField
    protected Button pauseRecordingButton;
    
    /** An audio HTML element used to play back recorded audio data */
    private AudioElement recordingPlayback;

    /** The URL of the audio to play */
    private String audioUrl;

    /**
     * Creates a new player for playing back recorded audio
     */
    public AudioPlayer() {
        initWidget(uiBinder.createAndBindUi(this));
        
        deleteRecordingButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //clean up the recording so that it is not saved
                setUrl(null);
            }
        });
        
        playRecordingButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                event.stopPropagation();
                
                if(recordingPlayback != null) {
                    recordingPlayback.pause();
                }
                
                //play the recorded audio data when the appropriate button is clicked
                if(audioUrl != null) {
                    recordingPlayback = JsniUtility.playAudio(audioUrl, Dashboard.VolumeSettings.PAST_SESSION_AUDIO.getSetting().getVolume());
                }
            }
        });
        
        pauseRecordingButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                event.stopPropagation();
                
                //pause the recorded audio data when the appropriate button is clicked
                if(recordingPlayback != null) {
                    recordingPlayback.pause();
                }
            }
        });
        
        setUrl(null);
    }
    
    /**
     * Sets the URL of the audio that this player should play
     * 
     * @param url the URL to play. Can be null if no audio should be played.
     */
    public void setUrl(String url) {
        
        if(recordingPlayback != null) {
            recordingPlayback.pause();
            recordingPlayback = null;
        }
        
        audioUrl = url;
        
        onUrlChanged(audioUrl);
    }

    /**
     * Sets whether or not the delete button should be shown and capable of being interacted with
     * 
     * @param enabled whether deleting is enabled
     */
    public void setDeletionEnabled(boolean enabled) {
        deleteRecordingButton.setVisible(enabled);
    }
    
    /**
     * Handles when the player's URL is changed. By default, an audio player will hide itself when
     * it does not have a URL and show itself when it does.
     * <br/><br/>
     * This method can be overridden to change what happens when the player URL changes.
     * 
     * @param newUrl the new URL that this player is now using. Can be null.
     */
    public void onUrlChanged(String newUrl) {
        setVisible(StringUtils.isNotBlank(newUrl));
    }
}
