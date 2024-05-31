/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;

/**
 * A widget containing UI controls to play, pause, and loop the history playback for a timeline
 * 
 * @author nroberts
 */
public class TimelinePlayControls extends Composite {
    
    /** the color to use for the background color of the play button when playing back the session */
    private static final String PLAYING_BUTTON_BACKGROUND_COLOR = "#e49505";
    
    /** the style name that should be applied to the loop button when loop is on */
    private static final String LOOP_ON_STYLE_NAME = "loopOn";

    private static TimelinePlayControlsUiBinder uiBinder = GWT.create(TimelinePlayControlsUiBinder.class);

    interface TimelinePlayControlsUiBinder extends UiBinder<Widget, TimelinePlayControls> {
    }
    
    /** Interface to allow CSS style name access */
    protected interface Style extends CssResource {

        /**
         * Gets the CSS class name used to make the play button flash colors
         *
         * @return the class name
         */
        public String playButtonFlash();
    }

    /** A set of styles associated with this widget */
    @UiField
    protected Style style;
    
    /** A button used to resume/pause the timeline playback */
    @UiField
    protected Icon playPauseButton;
    
    /** a tooltip to show on the play button when landing on the timeline for the first time */
    @UiField
    protected ManagedTooltip playLandingTooltip;
    
    /** the icon used to enable/disable loop playback capability */
    @UiField
    protected Icon loopButton;

    /** 
     * The timeline's implementation of the play/pause control. This should be invoked
     * when the user interacts with the proper UI component 
     */
    private Command playPauseImpl;

    /** 
     * The timeline's implementation of the loop control. This should be invoked
     * when the user interacts with the proper UI component 
     */
    private Command loopImpl;

    /**
     * Creates a new widget containing play controls for a timeline chart
     */
    public TimelinePlayControls() {
        initWidget(uiBinder.createAndBindUi(this));
        
        playPauseButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(playPauseImpl != null) {
                    playPauseImpl.execute();
                }
            }
        });
        
        loopButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(loopImpl != null) {
                    loopImpl.execute();
                }
            }
        });
    }
    
    /**
     * Sets whether the controls should indicate that a timeline playback is currently playing
     * 
     * @param playing whether the timeline is playing.
     */
    public void setPlaying(boolean playing) {
        
        if(playing) {
            
            playPauseButton.setType(IconType.PAUSE);
            
            // stop flashing background animation
            playPauseButton.removeStyleName(style.playButtonFlash());
            
            // set background to playing color
            playPauseButton.getElement().getStyle().setBackgroundColor(PLAYING_BUTTON_BACKGROUND_COLOR); 
            
        } else {
            
            playPauseButton.setType(IconType.PLAY);
            
            // start flashing background animation
            playPauseButton.addStyleName(style.playButtonFlash());
        }
    }
    
    /**
     * Sets whether the controls should indicate that a timeline playback is currently looping
     * 
     * @param looping whether the timeline is looping
     */
    public void setLooping(boolean looping) {
        
        if(looping) {
            loopButton.addStyleName(LOOP_ON_STYLE_NAME);
            
        } else {
            loopButton.removeStyleName(LOOP_ON_STYLE_NAME);
        }
    }
    
    /**
     * Gets whether the timeline playback is paused based on the current state of the controls
     * 
     * @return whether the timeline playback is paused
     */
    public boolean isPaused() {
        return IconType.PLAY.equals(playPauseButton.getType());
    }
    
    /**
     * Sets whether the play and pause controls should be enabled
     * 
     * @param enable whether the controls should be enabled
     * @param playPauseDisabled whether the timeline playback has currently disabled playing
     */
    public void setPlayPauseEnabled(boolean enable, boolean playPauseDisabled) {
        
        if(enable && playPauseDisabled){
            // enabling the currently disabled button, 
            // e.g. going from the end of the timeline (paused) to not the end of the timeline (paused)
            
            playPauseButton.addStyleName(style.playButtonFlash());
            
        }else if(!enable){
            playPauseButton.removeStyleName(style.playButtonFlash());
            playPauseButton.getElement().getStyle().clearBackgroundColor();
        }
        
        playPauseButton.getElement().getStyle().setOpacity(enable ? 1 : 0.5); 
    }
    
    /** 
     * Sets the timeline's implementation of the play/pause control. This will be invoked
     * when the user interacts with the proper UI component 
     * 
     * @param impl a command implementing the logic for the control. Can be null, if the 
     * control should do nothing
     */
    public void setPlayPauseImpl(Command impl) {
        this.playPauseImpl = impl;
    }
    
    /** 
     * Sets the timeline's implementation of the loop control. This will be invoked
     * when the user interacts with the proper UI component 
     * 
     * @param impl a command implementing the logic for the control. Can be null, if the 
     * control should do nothing
     */
    public void setLoopImpl(Command impl) {
        this.loopImpl = impl;
    }
    
    /**
     * Show any tooltips that should appear when loading the timeline for the first time.<br/>
     * Currently the tooltips are:<br/>
     * 1. play button - hide after 4 seconds
     */
    public void showInitialTooltips(){
        
        playLandingTooltip.show();
        
        //shows the tooltip for a duration
        Timer showTimer = new Timer() {
            @Override
            public void run() {         
                playLandingTooltip.hide();
            }
        };
        showTimer.schedule(4000);
    }

}
