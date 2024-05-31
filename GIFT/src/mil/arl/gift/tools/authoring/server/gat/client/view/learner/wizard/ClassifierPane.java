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

import mil.arl.gift.tools.authoring.server.gat.client.view.learner.ClassifierTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.ClassifierValueListBox;

/**
 * This is a glorified wrapper around a ClassifierValueListBox that is
 * displayed below a user-friendly label. I created this class when I thought
 * it might become more complex. If that doesn't turn out to be the case then
 * it would be completely reasonable to delete this class and add the label and
 * ClassifierValueListBox directly to the BuildLearnerStateInterpreterDialog.
 * 
 * @author elafave
 */
public class ClassifierPane extends Composite {
	
	/** The ui binder. */
    interface ClassifierPaneUiBinder extends UiBinder<Widget, ClassifierPane> {} 
	private static ClassifierPaneUiBinder uiBinder = GWT.create(ClassifierPaneUiBinder.class);
	
	@UiField
	protected ClassifierValueListBox classifierValueListBox;
	
    public ClassifierPane() {		
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public ClassifierTypeEnum getClassifierType() {
    	return classifierValueListBox.getValue();
    }
    
    public void setClassifierType(ClassifierTypeEnum classifierType) {
    	classifierValueListBox.setValue(classifierType);
    }
    
    public void setAcceptableClassifierTypes(List<ClassifierTypeEnum> acceptableValues) {
    	classifierValueListBox.setAcceptableValues(acceptableValues);
    }
}
