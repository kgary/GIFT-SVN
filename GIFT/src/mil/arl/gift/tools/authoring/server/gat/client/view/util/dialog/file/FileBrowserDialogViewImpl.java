/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.file;

import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.util.ShowingStateChangedEvent;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;

// TODO: Auto-generated Javadoc
/**
 * The Class FileBrowserDialogViewImpl.
 *
 * @author nroberts
 */
public class FileBrowserDialogViewImpl extends DialogBox implements FileBrowserDialogView{
	
	/** The dialog content. */
	FileBrowserDialogContent dialogContent = new FileBrowserDialogContent();
	
	/** The value. */
	String value;
	
	private boolean valueChangeHandlerInitialized = false;
	
	/**
	 * Instantiates a new file browser dialog view impl.
	 */
	public FileBrowserDialogViewImpl(){
		
		this.add(dialogContent);
		
		this.setWidth("400px");
		this.setHeight("400px");
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasValue#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
	 */
	@Override
	public void setValue(String value, boolean fireEvents) {
		this.value = value;
		ValueChangeEvent.fire(this, value);
	}
	
	@Override 
	public HasValue<String> getFileNameInput(){
		return dialogContent.getFileNameInput();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
	 */
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		    
		// Initialization code
		if (!valueChangeHandlerInitialized) {

			valueChangeHandlerInitialized = true;
			addDomHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {
					ValueChangeEvent.fire(FileBrowserDialogViewImpl.this, value);
				}
				
			}, ChangeEvent.getType());
		}
		
		return addHandler(handler, ValueChangeEvent.getType());
	}


	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.FileSelectionView#getConfirmButton()
	 */
	@Override
	public HasClickHandlers getConfirmButton() {
		return dialogContent.getConfirmButton();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.FileSelectionView#getConfirmButtonHasEnabled()
	 */
	@Override
	public HasEnabled getConfirmButtonHasEnabled() {
		return dialogContent.getConfirmButtonHasEnabled();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.FileSelectionView#getCancelButton()
	 */
	@Override
	public HasClickHandlers getCancelButton() {
		return dialogContent.getCancelButton();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.FileSelectionView#getFileNameDataDisplay()
	 */
	@Override
	public HasData<FileTreeModel> getFileNameDataDisplay() {
		return dialogContent.getFileNameDataDisplay();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.FileSelectionView#setUpArrowCommand(com.google.gwt.core.client.Scheduler.ScheduledCommand)
	 */
	@Override
	public void setUpArrowCommand(ScheduledCommand command) {
		dialogContent.setUpArrowCommand(command);
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.FileSelectionView#getUpArrow()
	 */
	@Override
	public HasEnabled getUpArrow() {
		return dialogContent.getUpArrow();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.FileSelectionView#getDirectoryName()
	 */
	@Override
	public HasText getDirectoryName() {
		return dialogContent.getDirectoryName();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.Showable#showContent()
	 */
	@Override
	public void showContent(){		
		super.center();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.Showable#hideContent()
	 */
	@Override
	public void hideContent(){
		
		super.hide();	
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.Showable#showContent(boolean)
	 */
	@Override
	public void showContent(boolean fireEvents){
		
		super.center();
		
		if(fireEvents){
			SharedResources.getInstance().getEventBus().fireEvent(new ShowingStateChangedEvent(this, true));
		}
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.dialog.Showable#hideContent(boolean)
	 */
	@Override
	public void hideContent(boolean fireEvents){
		
		super.hide();
		
		if(fireEvents){
			SharedResources.getInstance().getEventBus().fireEvent(new ShowingStateChangedEvent(this, false));		
		}
	}

}
