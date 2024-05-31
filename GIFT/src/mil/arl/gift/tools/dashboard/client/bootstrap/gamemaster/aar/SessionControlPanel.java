/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.VolumeSettingSlider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.AssessmentDisplayMode;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.SummativeAssessmentChangeHandler;

/**
 * The panel containing the controls that are used to adjust how knowledge sessions are played. This includes
 * things like volume controls, as well as the timeline controls that are only used for past sessions.
 * 
 * @author nroberts
 */
public class SessionControlPanel extends Composite implements SummativeAssessmentChangeHandler{

    private static SessionControlPanelUiBinder uiBinder = GWT.create(SessionControlPanelUiBinder.class);

    interface SessionControlPanelUiBinder extends UiBinder<Widget, SessionControlPanel> {
    }
    
    /** Interface to allow CSS style name access */
    protected interface Style extends CssResource {
        
        /**
         * Gets the CSS class name used to hide controls related to the timeline
         *
         * @return the class name
         */
        public String timelineControlsHidden();
    }

    /** A set of styles associated with this widget */
    @UiField
    protected Style style;

    /** The controls that adjust how past sessions are played, paused, and looped */
    @UiField
    protected TimelinePlayControls playControls;
    
    /** A label used to display the current time in a past session's playback */
    @UiField
    protected Label playbackTimeLabel;
    
    /** The controls that adjust how the timeline is visually scaled*/
    @UiField
    protected TimelineScaleControls scaleControls;
    
    /** A button used to toggle whether summative or formative assessments are shown*/
    @UiField
    protected Button summativeButton;
    
    /** Icon used to show the legend for the timeline*/
    @UiField
    protected Icon timelineLegendHelpIcon;
    /** A tooltip that shows helpful information about the button that toggles summative and formative assessments */
    @UiField
    protected Tooltip summativeTooltip;
    
    /** New instance of timeline help dialog used to show the dialog within the legend*/
    private TimelineLegendDialog timelineLegendDialog = new TimelineLegendDialog();
    
    /** A command used to toggle summative mode on and off */
    private Command summativeCommand = null;
    
    /** The controls used to modify the volume settings */
    @UiField(provided = true)
    protected VolumeSettingSlider volumeControls = new VolumeSettingSlider(Dashboard.VolumeSettings.ALL_SOUNDS.getSetting());
    
    /** An interface for other widgets to interact with the timeline controls */
    protected TimelineControls timelineControls = new TimelineControls() {
        
        @Override
        public void refreshPlaybackTimeLabel(String text) {
            playbackTimeLabel.setText(text);
        }
        
        @Override
        public TimelineScaleControls getScaleControls() {
            return scaleControls;
        }
        
        @Override
        public TimelinePlayControls getPlayControls() {
            return playControls;
        }

        @Override
        public void setVisible(boolean visible) {
            if(visible) {
                removeStyleName(style.timelineControlsHidden());
                
            } else {
                addStyleName(style.timelineControlsHidden());
            }
        
        }

        @Override
        public void setSummativeButtonImpl(Command command) {
            summativeCommand = command;
        }
    };

    /** 
     * Creates a new session control panel that provides the user with controls to interact with the timeline
     */
    public SessionControlPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        /* Toggle the summative button on initially and make it switch modes on click */
        summativeButton.toggle();
        summativeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(summativeCommand != null) {
                    summativeCommand.execute();
                }
                
                updateAssessmentAppearance();
            }
        });
        
        timelineLegendHelpIcon.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                timelineLegendDialog.setPopupPosition(event.getClientX(), event.getClientY());
                timelineLegendDialog.show();
            }
        });
        
        SummativeAssessmentProvider.getInstance().addManagedHandler(this);
        
        updateAssessmentAppearance();
    }
    
    /**
     * Refreshes the appearance of the summative assessment toggle button and its tooltip to match
     * the current assessment display mode
     */
    private void updateAssessmentAppearance() {
        
        if(AssessmentDisplayMode.SUMMATIVE.equals(SummativeAssessmentProvider.getInstance().getDisplayMode())) {
            summativeTooltip.setTitle("Click to display <i>formative</i> assessments that reflect learners' performance over time."
                    + "<br/><br/>Currently showing summative assessments.");
        } else {
            summativeTooltip.setTitle("Click to display <i>summative</i> assessments that reflect learners' final overall performance."
                    + "<br/><br/>Currently showing formative assessments.");
        }
    }

    /**
     * Gets an interface that can be used to interact with this panel's timeline controls
     * 
     * @return the timeline controls interface. Will not be null.
     */
    public TimelineControls getTimelineControls() {
        return timelineControls;
    }

    @Override
    public void onSummativeAssessmentsChanged(Map<String, AssessmentLevelEnum> perfNodeNameToSummativeAssessment) {
        // Nothing to do
    }

    @Override
    public void onDisplayModeChanged(AssessmentDisplayMode displayMode) {
        updateAssessmentAppearance();
    }
}
