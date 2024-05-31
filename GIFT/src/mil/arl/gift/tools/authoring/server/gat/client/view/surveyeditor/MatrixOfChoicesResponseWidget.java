/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;

/**
 * A widget used to display and edit responses for matrix-of-choices questions
 * 
 * @author nroberts
 */
public class MatrixOfChoicesResponseWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MatrixOfChoicesResponseWidget.class.getName());
	
	private static MultipleChoiceResponseWidgetUiBinder uiBinder = GWT
			.create(MultipleChoiceResponseWidgetUiBinder.class);

	interface MultipleChoiceResponseWidgetUiBinder extends
			UiBinder<Widget, MatrixOfChoicesResponseWidget> {
	}
	
	@UiField
	protected Widget optionsGroup;
	
	@UiField
	protected Button menuButton;
	
	@UiField
	protected DropDownMenu optionsMenu;
	
	@UiField(provided=true)
	protected EditableInlineLabel label = new EditableInlineLabel();
	
	@UiField
	protected AnchorListItem moveUpItem;
	
	@UiField
	protected AnchorListItem moveDownItem;
	
	@UiField
	protected AnchorListItem removeChoiceItem;
	
	/**
	 * Flag to indicate if the label value is from a shared list (immutable).
	 */
	private boolean isLabelShared = false;
	
	/** A wrapper for the editable label that prevents direct access to the label's methods (such as setValue) */
	private RestrictedEditableLabel restrictedLabel;
	
	/**
	 * Creates a new widget for entering responses to matrix-of-choices questions
	 */
	public MatrixOfChoicesResponseWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		
		menuButton.addMouseDownHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				event.preventDefault();
			}
		});
		
		menuButton.addMouseUpHandler(new MouseUpHandler() {
			
			@Override
			public void onMouseUp(MouseUpEvent event) {
				
				event.preventDefault();
			}
		});
		
		label.addMouseDownHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {		
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					
					@Override
					public void execute() {
						
						// Only show the popup, if the widget is editable.
						if (menuButton.isEnabled()) {
						    menuButton.setVisible(true);
						}
						
					}
				});
			}
		});
		
		label.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				menuButton.setVisible(false);
			}
		});
	}
	
	/**
	 * Gets this response's label
	 * 
	 * @return the label
	 */
	public RestrictedEditableLabel getLabel(){
	    if (restrictedLabel == null) {
	        restrictedLabel = new RestrictedEditableLabel(label);
	    }
	    
		return restrictedLabel;
	}
	
	/**
	 * Determines if the label is part of a shared answer set
	 * 
	 * @return if the label is part of a shared answer set
	 */
	public boolean isShared() {
	    return isLabelShared;
	}
	
    /**
     * Sets the text value to the editable label.
     * 
     * @param value the text value to set.
     * @param isShared true if the text value is from a shared list.
     */
    public void setLabelValue(String value, boolean isShared) {
        label.setValue(value);
        isLabelShared = isShared;
    }

	/**
	 * Sets whether or not this choice is temporary and shouldn't be treated like a real choice
	 * 
	 * @param temporary whether or not this choice is temporary
	 */
	public void setTemporary(boolean temporary){
		optionsGroup.setVisible(!temporary);
	}

	/**
	 * Gets the menu item used to move this choice up
	 * 
	 * @return the menu item used to move this choice up
	 */
	public AnchorListItem getMoveUpItem() {
		return moveUpItem;
	}

	/**
	 * Gets the menu item used to move this choice down
	 * 
	 * @return the menu item used to move this choice down
	 */
	public AnchorListItem getMoveDownItem() {
		return moveDownItem;
	}

	/**
	 * Gets the menu item used to remove this choice
	 * 
	 * @return the menu item used to remove this choice
	 */
	public AnchorListItem getRemoveChoiceItem() {
		return removeChoiceItem;
	}
	
	/**
	 * Sets whether or not this response's text label should be editable
	 * 
	 * @param editable whether or not this response's text label should be editable
	 */
	public void setLabelEditable(boolean editable){
		
	    boolean setEditable = editable && !isLabelShared;
	    
		label.setEditingEnabled(setEditable);
		if(setEditable) {
			label.setTooltipText("Click to edit");
		} else {
			label.setTooltipText("Switch to Writing Mode to edit choice text");
		}
	}

	/**
	 * Refresh the widget based on the mode of the survey editor.
	 * 
	 * @param readOnly - Determines if the widget is editable
	 * @param mode - The mode of the survey editor (scoring or writing mode).
	 */
    public void refresh(boolean readOnly, SurveyEditMode mode) {
        
        boolean editable = true;
        if (readOnly || isLabelShared || mode == SurveyEditMode.ScoringMode) {
            editable = false;
        }
        setLabelEditable(editable);
        menuButton.setEnabled(editable);
        
        // If the widget isn't editable, then also hide any popup menu that was opened.
        if (!editable) {
            menuButton.setVisible(false);
        }
        
    }
    
    /**
     * Triggers the editable label to change to edit mode. 
     */
    public void startEditing() {
        if (!isLabelShared) {
            label.startEditing();
        }
    }
}
