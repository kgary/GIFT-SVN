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

import mil.arl.gift.tools.authoring.server.gat.client.view.learner.TranslatorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.TranslatorValueListBox;

/**
 * This is a glorified wrapper around a TranslatorPaneUiBinder that is
 * displayed below a user-friendly label. I created this class when I thought
 * it might become more complex. If that doesn't turn out to be the case then
 * it would be completely reasonable to delete this class and add the label and
 * TranslatorPaneUiBinder directly to the BuildLearnerStateInterpreterDialog.
 * 
 * @author elafave
 */
public class TranslatorPane extends Composite {
	
	/** The ui binder. */
    interface TranslatorPaneUiBinder extends UiBinder<Widget, TranslatorPane> {} 
	private static TranslatorPaneUiBinder uiBinder = GWT.create(TranslatorPaneUiBinder.class);
	
	@UiField
	protected TranslatorValueListBox translatorValueListBox;
	
    public TranslatorPane() {		
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public TranslatorTypeEnum getTranslatorType() {
    	return translatorValueListBox.getValue();
    }
    
    public void setTranslatorType(TranslatorTypeEnum translatorType) {
    	translatorValueListBox.setValue(translatorType);
    }
    
    public void setAcceptableTranslatorTypes(List<TranslatorTypeEnum> acceptableValues) {
    	translatorValueListBox.setAcceptableValues(acceptableValues);
    }
}
