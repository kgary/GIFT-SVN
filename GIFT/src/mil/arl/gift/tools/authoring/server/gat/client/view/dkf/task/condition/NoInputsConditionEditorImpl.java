/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class NoInputsConditionEditor.
 */
public class NoInputsConditionEditorImpl extends Composite {

    /** The ui binder. */
    private static NoInputsConditionEditorUiBinder uiBinder = GWT.create(NoInputsConditionEditorUiBinder.class);

    /**
     * The Interface NoInputsConditionEditorUiBinder.
     */
    interface NoInputsConditionEditorUiBinder extends UiBinder<Widget, NoInputsConditionEditorImpl> {
    }

    /**
     * Default Constructor
     * 
     * Required to be public for GWT UIBinder compatibility.
     */
    public NoInputsConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    /**
     * Setup dirty handlers.
     */
    protected void setupDirtyHandlers() {

    }

    /**
     * Setup help handlers.
     */
    protected void setupHelpHandlers() {
        
    }
}