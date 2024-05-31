/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.state.TaskPerformanceState;

/**
 * A UI item representing an objective in the state pane
 * 
 * @author nroberts
 */
public class ObjectiveItem extends Composite {

    private static ObjectiveItemUiBinder uiBinder = GWT.create(ObjectiveItemUiBinder.class);

    interface ObjectiveItemUiBinder extends UiBinder<Widget, ObjectiveItem> {
    }
    
    /** A label use to display the objective name */
    @UiField
    protected HTML objectiveName;
    
    /** The button used to modify an objective's overall assessment */
    @UiField
    protected Button overallAssessmentButton;

    /**
     * Creates a new UI item representing the given objective
     * 
     * @param task the objective to represent. Cannot be null.
     * @param onOverallAssessmentClicked a click handler to invoke when this item is clicked. Can be null.
     */
    public ObjectiveItem(TaskPerformanceState task, ClickHandler onOverallAssessmentClicked) {
        
        if(task == null) {
            throw new IllegalArgumentException("The task for an objective item cannot be null");
        }
        
        initWidget(uiBinder.createAndBindUi(this));
        
        objectiveName.setHTML(SafeHtmlUtils.fromString(task.getState().getName()));
        
        if(onOverallAssessmentClicked != null) {
            overallAssessmentButton.addClickHandler(onOverallAssessmentClicked);
            
        } else {
            overallAssessmentButton.setVisible(false);
        }
    }

}
