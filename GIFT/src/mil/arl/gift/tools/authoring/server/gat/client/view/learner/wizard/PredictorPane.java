/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.wizard;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.view.learner.PredictorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.PredictorValueListBox;

/**
 * This is a glorified wrapper around a PredictorValueListBox that is
 * displayed below a user-friendly label. I created this class when I thought
 * it might become more complex. If that doesn't turn out to be the case then
 * it would be completely reasonable to delete this class and add the label and
 * PredictorValueListBox directly to the BuildLearnerStateInterpreterDialog.
 * 
 * @author elafave
 */
public class PredictorPane extends Composite {
	
	/** The ui binder. */
    interface PredictorPaneUiBinder extends UiBinder<Widget, PredictorPane> {} 
	private static PredictorPaneUiBinder uiBinder = GWT.create(PredictorPaneUiBinder.class);
	
	@UiField
	protected PredictorValueListBox predictorValueListBox;
	
    public PredictorPane() {		
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public PredictorTypeEnum getPredictorType() {
    	return predictorValueListBox.getValue();
    }
    
    public void setPredictorType(PredictorTypeEnum predictorType) {
    	predictorValueListBox.setValue(predictorType);
    }
    
    public void setAcceptablePredictorTypes(List<PredictorTypeEnum> acceptableValues) {
    	predictorValueListBox.setAcceptableValues(acceptableValues);
    }
}
