/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import org.gwtbootstrap3.client.ui.TextArea;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.RecordingBooth;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;

/**
 * An editor used to modify metadata associated with overall assessments, namely notes
 * and recordings
 * 
 * @author nroberts
 */
public class OverallAssessmentNoteEditor extends Composite {

    private static OverallAssessmentNoteEditorUiBinder uiBinder = GWT.create(OverallAssessmentNoteEditorUiBinder.class);

    interface OverallAssessmentNoteEditorUiBinder extends UiBinder<Widget, OverallAssessmentNoteEditor> {
    }
    
    /** A wrapper around the text area that allows the user to submit a recording in its place */
    @UiField
    protected RecordingBooth recorder;
    
    /** A text area used to enter a comment describing an overall assessment */
    @UiField
    protected TextArea descriptionTextArea;

    /** The state of the score being modified. This is what will be changed as input fields are interacted with. */
    private ScoreNodeUpdate scoreState;

    /**
     * Creates a new score metadata editor with no data loaded inside of it
     */
    public OverallAssessmentNoteEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        descriptionTextArea.addDomHandler(new InputHandler() {
            
            @Override
            public void onInput(InputEvent event) {

                /* Whenever the OC edits the comment box, update the comment in the score state */
                ScoreNodeUpdate scoreState = OverallAssessmentNoteEditor.this.scoreState;
                scoreState.setObserverComment(descriptionTextArea.getText());
                scoreState.setObserverMedia(null);
            }
        }, InputEvent.getType());
    }
    
    /**
     * Sets the state of the score being modified. This is what will be changed as input fields are interacted with.
     * 
     * @param scoreState the score state. Cannot be null.
     */
    public void setScoreNodeState(ScoreNodeUpdate scoreState) {
        this.scoreState = scoreState;
        
        descriptionTextArea.setValue(scoreState.getObserverComment());
        recorder.setExtistingRecordingUrl(scoreState.getObserverMedia());
    }
    
    /**
     * Gets whether the OC has made a recording associated with the assessment score that 
     * needs to be saved
     * 
     * @return whether there is a recording to save.
     */
    public boolean hasObserverMediaChanged() {
        return recorder.hasRecording();
    }
    
    /**
     * Saves the recording associated with the assessment score
     * 
     * @param params the parameters needed to save the recording. Cannot be null.
     * @param callback the callback used to handle when the save completes. Cannot be null.
     */
    public void saveObserverMedia(RecorderParams params, final AsyncCallback<String> callback) {
        recorder.saveRecording(params, new AsyncCallback<String>() {
            
            @Override
            public void onSuccess(String result) {
                
                ScoreNodeUpdate scoreState = OverallAssessmentNoteEditor.this.scoreState;
                scoreState.setObserverComment(null);
                scoreState.setObserverMedia(result);
                
                callback.onSuccess(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }
}
