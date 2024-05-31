/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import org.gwtbootstrap3.client.ui.Icon;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter.OverallAssessmentNoteEditor;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;

/**
 * A widget that visually indicates an overall assessment level in a bar-like presentation
 * 
 * @author nroberts
 */
public class OverallAssessmentBar extends Composite {
    
    private static OverallAssessmentBarUiBinder uiBinder = GWT.create(OverallAssessmentBarUiBinder.class);

    interface OverallAssessmentBarUiBinder extends UiBinder<Widget, OverallAssessmentBar> {
    }
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {

        String fill();
        
        String vertical();
        
        String readOnly();
        
        String popup();
    }
    
    /** An accessor for this widget's CSS styling rules */
    @UiField
    protected Style style;
    
    /** The first star indicating the lowest assessment level */
    @UiField
    protected Icon star1;
    
    /** The second star indicating a middle assessment level */
    @UiField
    protected Icon star2;
    
    /** The third star indicating the highest assessment level */
    @UiField
    protected Icon star3;
    
    @UiField
    protected ManagedTooltip belowTooltip;
    
    @UiField
    protected ManagedTooltip atTooltip;
    
    @UiField
    protected ManagedTooltip aboveTooltip;
    
    /** The assessment score that this bar is currently displaying */
    private ScoreNodeUpdate assessment = null;

    /** A command to invoke whenever the observer controller changes the assessment level of the score */
    private Command onAssessmentChange;
    
    /** An editor used to modify notes associated with the assessment score.*/
    private OverallAssessmentNoteEditor noteEditor;

    /** A popup used to display the note editor */
    private PopupPanel notePopup;
    
    /** Whether the OC has interacted with the UI to change the assessment */
    private boolean hasAssessmentChanged = false;

    /** 
     * Creates a new overall assessment bar with an empty assessment
     */
    public OverallAssessmentBar() {
        this(false);
    }
    
    /**
     * Creates a new overall assessment bar with the given read-only state
     * 
     * @param readOnly whether this bar should be read-only
     */
    public OverallAssessmentBar(boolean readOnly) {
        
        initWidget(uiBinder.createAndBindUi(this));
        
        if(readOnly) {
            
            star1.addStyleName(style.readOnly());
            star2.addStyleName(style.readOnly());
            star3.addStyleName(style.readOnly());
            
        } else {
            
            star1.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    hasAssessmentChanged = true;
                    
                    setAssessmentLevel(AssessmentLevelEnum.BELOW_EXPECTATION);
                    onAssessmentChange();
                }
            });
            
            star2.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    hasAssessmentChanged = true;
                    
                    setAssessmentLevel(AssessmentLevelEnum.AT_EXPECTATION);
                    onAssessmentChange();
                }
            });
            
            star3.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    hasAssessmentChanged = true;
                    
                    setAssessmentLevel(AssessmentLevelEnum.ABOVE_EXPECTATION);
                    onAssessmentChange();
                }
            });
            
            /*Create the note editor and the popup used to display it */
            noteEditor = new OverallAssessmentNoteEditor();
            noteEditor.setWidth("300px");
            noteEditor.getElement().getStyle().setProperty("padding", "10px 5px 5px");
            
            notePopup = new PopupPanel(true, false);
            notePopup.addStyleName(style.popup());
            notePopup.add(noteEditor);
        }
    }
    
    /**
     * Redraws the bar so that the displayed assessment level matches the underlying performance node's state
     */
    public void redraw() {
        
        boolean fill1 = true, fill2 = true, fill3 = true;
                
        if(assessment == null) {
            fill1 = false;
            fill2 = false;
            fill3 = false;
            
        } else if(!AssessmentLevelEnum.ABOVE_EXPECTATION.equals(assessment.getAssessment())) {
            fill3 = false;
            
            if(!AssessmentLevelEnum.AT_EXPECTATION.equals(assessment.getAssessment())) {
                fill2 = false;
                
                if(!AssessmentLevelEnum.BELOW_EXPECTATION.equals(assessment.getAssessment())) {
                    fill1 = false;
                }
            }
        }
        
        String fillStyle = style.fill();
        
        star1.setStyleName(fillStyle, fill1);
        star2.setStyleName(fillStyle, fill2);
        star3.setStyleName(fillStyle, fill3);
    }
    
    /**
     * Updates this assessment bar's assessment level to the given valye
     * 
     * @param assessment the assessment level to assign. Cannot be null.
     */
    public void setAssessmentLevel(AssessmentLevelEnum assessment) {
        
        if(this.assessment == null){ 
            
            /* This is a parent node with no editable score, so simply wrap the assessment */
            setScoreNodeState(new ScoreNodeUpdate(assessment));
            
        } else {
            
            /* Otherwise, modify the existing assessment score */
            this.assessment.setAssessment(assessment);
        }
        
        redraw();
    }
    
    /**
     * Gets the assessment level currenly displayed by this bar
     * 
     * @return the assessment level. Can be null if no assessment has been
     * picked by the user.
     */
    public AssessmentLevelEnum getAssessmentLevel() {
        
        if(this.assessment == null) {
            return null;
        }
        
        return this.assessment.getAssessment();
    }
    
    /**
     * Sets the state of the score modified by this assessment bar
     * 
     * @param state the score state. This state will be modified as the OC changes
     * the assessment and adds notes to the assessment score.
     */
    public void setScoreNodeState(ScoreNodeUpdate state) {
        this.assessment = state;
        
        if(noteEditor != null) {
            noteEditor.setScoreNodeState(assessment);
        }
        
        redraw();
    }
    
    /**
     * Gets the state of the score being modified by this assessment bar
     * 
     * @return the modified score state, complete with the current assessment level
     * assigned by the OC and any notes they have added.
     */
    public ScoreNodeUpdate getScoreNodeState() {
        return this.assessment;
    }
    
    /**
     * Set a command to handle when an observer controller manually changes
     * this bar's assessment (i.e. by clicking on it)
     * 
     * @param command the command to invoke. Can be null.
     */
    public void setAssessmentChangedCommand(Command command) {
        this.onAssessmentChange = command;
    }
    
    /**
     * Handles when the assessment has been changed by an observer using
     * whatever command, if any, was assigned
     */
    private void onAssessmentChange() {
        if(this.onAssessmentChange != null) {
            this.onAssessmentChange.execute();
        }
    }
    
    /**
     * Shows any metadata associated with the assessment score. This includes
     * any comments or recordings made by the OC.
     * 
     * @param element the element that initiated the show. If clicked again, this
     * metadata will remain visible rather than being re-shown
     */
    public void showScoreMetadata(final Element element) {
        if(notePopup != null && !notePopup.isShowing()) {
            
            /* The metadata is not currently showing, so display it and keep it open as long as the OC
             * clicks on it or the panel that opened it */
            notePopup.addAutoHidePartner(element);
            notePopup.addCloseHandler(new CloseHandler<PopupPanel>() {
                
                @Override
                public void onClose(CloseEvent<PopupPanel> event) {
                    notePopup.removeAutoHidePartner(element);
                }
            });
            
            notePopup.setPopupPosition(this.getAbsoluteLeft() + this.getOffsetWidth() + 20, getAbsoluteTop());
            notePopup.show();
        }
    }
    
    /**
     * Gets whether the OC has made a recording associated with the assessment score that 
     * needs to be saved
     * 
     * @return whether there is a recording to save.
     */
    public boolean hasObserverMediaChanged() {
        if(noteEditor == null) {
            return false;
        }
        
        return noteEditor.hasObserverMediaChanged();
    }
    
    /**
     * Saves the recording associated with the assessment score
     * 
     * @param params the parameters needed to save the recording. Cannot be null.
     * @param callback the callback used to handle when the save completes. Cannot be null.
     */
    public void saveObserverMedia(RecorderParams params, final AsyncCallback<String> callback) {
        if(noteEditor == null) {
            return;
        }
        
        noteEditor.saveObserverMedia(params, callback);
    }
    
    /**
     * Gets whether the OC has interacted with the UI to change the assessment 
     * 
     * @return whether the assessment has changed
     */
    public boolean hasAssessmentChanged() {
        return hasAssessmentChanged;
    }
}
