/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;

import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.util.StringUtils;

/**
 * A modal dialog box with a progress indicator progress bar.
 * 
 * @author mhoffman
 *
 */
public class ProgressModalDialogBox extends ModalDialogBox {
    
    /**
     * contains the latest progress from the server
     */
    private ProgressIndicator progressIndicator;
    
    /**
     * The progress UI component for the main task progress information
     */
    ProgressBarListEntry progress = new ProgressBarListEntry();
    
    /**
     * The progress UI component for the subtask progress information
     */
    ProgressBarListEntry subtaskProgress = new ProgressBarListEntry();

    /**
     * Build and show the progress modal dialog box.
     * 
     * @param title the title to use on the modal
     * @param progressIndicator contains the initial progress information.
     * @param showSubprogress whether to show the subtask progress in the widget
     */
    public ProgressModalDialogBox(String title, ProgressIndicator progressIndicator, boolean showSubprogress){
        super();
        
        if(StringUtils.isBlank(title)){
            title = "Progress";
        }
        
        setText(title);
        
        setGlassEnabled(true);
        setCloseable(false);
        
        this.progressIndicator = progressIndicator;
        
        init();
        
        subtaskProgress.setVisible(showSubprogress);
    }
    
    /** 
     * Initializes the dialog's UI components.
     */
    private void init() {        
        
        FlowPanel wrapper = new FlowPanel();
        
        progress.updateProgress(progressIndicator);        
        wrapper.add(progress);
        wrapper.setWidth("400px");
        wrapper.getElement().getStyle().setProperty("wordBreak", "break-all");        
        wrapper.add(subtaskProgress);
        
        setWidget(wrapper);
        
        center();
    }
    
    /**
     * Updates the dialog's progress bar. The dialog will close if the progress
     * is marked as complete or reaches 100% (AFTER 1 second in order to have time to show the progress).
     * 
     * @param updatedProgressIndicator A update to the progress indicator retrieved from the server.  If null, nothing happens.
     */
    public void updateProgress(ProgressIndicator updatedProgressIndicator) {
        
        if(updatedProgressIndicator == null) {
            return;
        }

        this.progressIndicator = updatedProgressIndicator;
        progress.updateProgress(updatedProgressIndicator);
        if(updatedProgressIndicator.getSubtaskProcessIndicator() != null) {
            subtaskProgress.updateProgress(updatedProgressIndicator.getSubtaskProcessIndicator());
            if(!subtaskProgress.isVisible()) {
                subtaskProgress.setVisible(true);
            }
        }else if(subtaskProgress.isVisible()) {
            // the previous progress indicator had subtask but the new one doesn't
            // therefore the subtask progress UI elements need to be hidden.
            subtaskProgress.setVisible(false);
        }
        
        if (updatedProgressIndicator.isComplete() || updatedProgressIndicator.getPercentComplete() >= 100) {
            
            Timer timer = new Timer() {
                
                @Override
                public void run() {
                    hide();
                }
            };
            
            timer.schedule(1000);

        }else{
            this.show();
            center();
        }
        
    }
}
