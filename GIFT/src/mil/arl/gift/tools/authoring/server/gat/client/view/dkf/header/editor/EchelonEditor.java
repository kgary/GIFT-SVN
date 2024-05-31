/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Team;
import mil.arl.gift.common.enums.EchelonEnum;

/**
 * Widget for editing the echelon values in the GAT
 * 
 * @author cpadilla
 *
 */
public class EchelonEditor extends Composite {

    /** The UI Binder */
    private static EchelonEditorUiBinder uiBinder = GWT.create(EchelonEditorUiBinder.class);

    /** Interface for the UI Binder to this class */
    interface EchelonEditorUiBinder extends UiBinder<Widget, EchelonEditor> {
    }
    
    /**
     * A callback used to handle when this editor's optional cancel button is pressed
     * 
     * @author nroberts
     */
    public static interface CancelCallback{
        
        /**
         * Handles when the user has cancelled editing for this editor
         * 
         * @param backupCopy the backup copy of the learner ID being edited that this editor will revert to
         */
        public void onEditingCancelled(EchelonEnum backupCopy);
    }
    
    /** The cancel button */
	@UiField
	protected Button cancelButton;
    
    /** A backup of the learner ID's original value that is used when editing is cancelled */
    private EchelonEnum backupEchelon;

    /** The selector to specify an echelon value */
	@UiField(provided=true)
	protected ValueListBox<EchelonEnum> echelonSelector = new ValueListBox<EchelonEnum>(new Renderer<EchelonEnum>() {

		@Override
		public String render(EchelonEnum value) {
		    if (value == null) return "None";
			
			return value.getDisplayName();
		}

		@Override
		public void render(EchelonEnum value, Appendable appendable) throws IOException {
			
			appendable.append(render(value));
		}
	});
    
	/**
	 * Constructor
	 * 
	 * @param team the team whose echelon value is being edited
	 * @param cancelCallback the callback for when the cancel button is clicked
	 */
    public EchelonEditor(final Team team, final CancelCallback cancelCallback) {
        initWidget(uiBinder.createAndBindUi(this));
        
        echelonSelector.setValue(null);

        if (team.getEchelon() != null)  {
            echelonSelector.setValue(EchelonEnum.valueOf(team.getEchelon(), EchelonEnum.VALUES()));
            backupEchelon = echelonSelector.getValue();
        }
        
        List<EchelonEnum> acceptableValues = new ArrayList<EchelonEnum>();
        acceptableValues.add(null);
        acceptableValues.addAll(EchelonEnum.VALUES());
        
        echelonSelector.setAcceptableValues(acceptableValues);

        if(cancelCallback != null) {
            
            //if the appropriate callback is provided, allow the author to cancel editing
            cancelButton.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    event.stopPropagation();
                    
                    echelonSelector.setValue(backupEchelon);
                    
                    cancelCallback.onEditingCancelled(backupEchelon);
                }
            });
            
            cancelButton.setVisible(true);
            
        } else {
            cancelButton.setVisible(false);
        }
        
        echelonSelector.addValueChangeHandler(new ValueChangeHandler<EchelonEnum>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<EchelonEnum> value) {
                if (value.getValue() != null) {
                    team.setEchelon(value.getValue().getName());
                }
            }
        });
        
    }
    
    /**
     * Gets the value of the echelon selector
     * 
     * @return the {@link EchelonEnum} value
     */
    public EchelonEnum getValue() {
        return echelonSelector.getValue();
    }
    
    /**
     * Saves the backup value of the echelon selector
     */
    public void saveBackupValue() {
        backupEchelon = echelonSelector.getValue();
    }

    /**
     * Sets whether this editor is read-only
     * 
     * @param readOnly if the editor is read-only
     */
    public void setReadOnly(boolean readOnly) {
        echelonSelector.setEnabled(!readOnly);
    }

}
