/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.wizard;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.LearnerStateValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is a glorified wrapper around a SelectLearnerStatePaneUiBinder that is
 * displayed below a user-friendly label. I created this class when I thought
 * it might become more complex. If that doesn't turn out to be the case then
 * it would be completely reasonable to delete this class and add the label and
 * SelectLearnerStatePaneUiBinder directly to the
 * BuildLearnerStateInterpreterDialog.
 * 
 * @author elafave
 */
public class SelectLearnerStatePane extends Composite {
	
	/** The ui binder. */
    interface SelectLearnerStatePaneUiBinder extends UiBinder<Widget, SelectLearnerStatePane> {} 
	private static SelectLearnerStatePaneUiBinder uiBinder = GWT.create(SelectLearnerStatePaneUiBinder.class);
	
	@UiField
	protected LearnerStateValueListBox learnerStateValueListBox;
	
    public SelectLearnerStatePane() {		
        initWidget(uiBinder.createAndBindUi(this));
        reset();
    }
    
    public LearnerStateAttributeNameEnum getLearnerState() {
    	return learnerStateValueListBox.getValue();
    }
    
    public void reset() {
    	learnerStateValueListBox.setValue(LearnerStateAttributeNameEnum.ANXIOUS);
    }
}
