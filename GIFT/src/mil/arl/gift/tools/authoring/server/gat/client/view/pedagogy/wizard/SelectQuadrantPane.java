/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.wizard;

import java.util.ArrayList;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.QuadrantValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Glorrified wrapper around the QuadrantValueListBox.
 * @author elafave
 *
 */
public class SelectQuadrantPane extends Composite {
	
	/** The ui binder. */
    interface SelectLearnerStatePaneUiBinder extends UiBinder<Widget, SelectQuadrantPane> {} 
	private static SelectLearnerStatePaneUiBinder uiBinder = GWT.create(SelectLearnerStatePaneUiBinder.class);
	
	@UiField
	protected QuadrantValueListBox quadrantValueListBox;
	
    public SelectQuadrantPane() {		
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setAcceptableQuadrants(ArrayList<MerrillQuadrantEnum> quadrants) {
    	if(quadrants.isEmpty()) {
    		return;
    	}
    	quadrantValueListBox.setValue(quadrants.iterator().next());
        quadrantValueListBox.setAcceptableValues(quadrants);
    }
    
    public MerrillQuadrantEnum getQuadrant() {
    	return quadrantValueListBox.getValue();
    }
}
