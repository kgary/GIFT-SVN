/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;

/**
 * A dialog that presents the user with options to create a new file, edit a file,
 * select an existing one, or use a default file
 * 
 * @author bzahid
 */
public class NewOrExistingFileDialog extends ModalDialogBox {

	private static NewOrExistingFileDialogUiBinder uiBinder = GWT
			.create(NewOrExistingFileDialogUiBinder.class);

	interface NewOrExistingFileDialogUiBinder extends UiBinder<Widget, NewOrExistingFileDialog> {
	}
	
	@UiField
	protected Button createNewButton;
	
	@UiField
	protected Button selectExistingButton;
	
	/** optional message to show above buttons */
	@UiField
	protected HTML optionalMessage;
	
	@UiField
	protected Button editButton;
	
	/** The button used to create a new DKF based on an imported XTSP file. */
	@UiField
    protected Button importXTSPButton;
	
	private Button defaultButton = new Button("Use Default");
		
	private Button cancelButton = new Button("Cancel");
	
	/**
	 * Creates a dialog that allows the user to choose between
	 * selecting an existing file and creating a new one
	 * 
	 * @param title The dialog title
	 * @param createHandler The click handler to execute when 'Create New' is selected
	 * @param selectHandler The click handler to execute when 'Select Existing' is selected
	 */
	public NewOrExistingFileDialog(String title, ClickHandler createHandler, ClickHandler selectHandler) {
		this(title,createHandler, null, selectHandler, null, null);
		
	}
	
	/**
     * Creates a dialog that allows the user to choose between
     * selecting an existing file and creating a new one
     * 
     * @param title The dialog title.  Supports HTML.
     * @param createHandler The click handler to execute when 'Create New' is selected
     * @param selectHandler The click handler to execute when 'Select Existing' is selected
     * @param importHandler The click handler to execute when 'Import from...' is selected, e.g. 
     *         "Import from xTSP". Can be null
     */
    public NewOrExistingFileDialog(String title, ClickHandler createHandler, ClickHandler selectHandler, 
            ClickHandler importHandler) {
        this(title, createHandler, null, selectHandler, null, importHandler);
        
    }
	
	/**
	 * Creates a dialog that allows the user to choose between
	 * selecting an existing file and creating a new one
	 * 
	 * @param title The dialog title.  Supports HTML.
	 * @param createHandler The click handler to execute when 'Create New' is selected
	 * @param editHandler The click handler to execute when 'Edit' is selected. Can be null
	 * @param selectHandler The click handler to execute when 'Select Existing' is selected
	 * @param defaultHandler The click handler to execute when 'Use Default is selected. Can be null
	 * @param importHandler The click handler to execute when 'Import from...' is selected, e.g. 
     *         "Import from xTSP". Can be null
	 */
	public NewOrExistingFileDialog(String title, ClickHandler createHandler, ClickHandler editHandler, 
			ClickHandler selectHandler, ClickHandler defaultHandler, ClickHandler importHandler) {
		setWidget(uiBinder.createAndBindUi(this));
		
		setHtml(title);
		setWidth("600px");
		setGlassEnabled(true);
		
		defaultButton.setVisible(false);
		defaultButton.setWidth("130px");
		defaultButton.addStyleName("footerButtonColor");
		
		cancelButton.setWidth("125px");
		cancelButton.setText("Cancel");
		cancelButton.setType(ButtonType.DANGER);
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		createNewButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		selectExistingButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		if(editHandler != null) {
			showEditButton(editHandler);
		}
		
		if (importHandler != null) {
		    showImportButton(importHandler);
		}
		
		if(defaultHandler != null) {
			showUseDefaultButton(defaultHandler);		
		}
		
		createNewButton.addClickHandler(createHandler);
		selectExistingButton.addClickHandler(selectHandler);
		
		setFooterWidget(cancelButton);	
		setFooterWidget(defaultButton);		
		
	}
	
	private void resizeButtons() {
		
		String width = (!editButton.isVisible()) ? "215px" : "165px";
				
		editButton.setWidth(width);
		createNewButton.setWidth(width);
		selectExistingButton.setWidth(width);
	}
	
	/**
	 *	Shows the edit button on the dialog
	 *
	 * @param clickHandler The edit button click handler
	 */
	public void showEditButton(ClickHandler clickHandler) {
		
		editButton.setVisible(true);
		editButton.addClickHandler(clickHandler);
		editButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
			
		});	
		
		resizeButtons();
	}
	
	/**
	 * Shows the import button on the dialog
	 * 
	 * @param clickHandler The import button click handler
	 */
	public void showImportButton(ClickHandler importHandler) {
	    importXTSPButton.setVisible(true);
	    importXTSPButton.addClickHandler(importHandler);
	    importXTSPButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
	}
	
	/**
	 *	Shows the use default button on the dialog
	 *
	 * @param clickHandler The use default button click handler
	 */
	public void showUseDefaultButton(ClickHandler defaultHandler) {
		
		defaultButton.setVisible(true);
		defaultButton.addClickHandler(defaultHandler);
		defaultButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
			
		});
	}
	
	/**
	 * Shows a dialog that prompts the user to create a new file or select an existing one
	 * 
	 * @param title The title of the dialog
	 * @param createHandler ClickHandler to execute when the create button is clicked
	 * @param selectHandler ClickHandler to execute when the select button is clicked
	 */
	public static void showCreateOrSelect(String title, ClickHandler createHandler, ClickHandler selectHandler) {
		NewOrExistingFileDialog dialog = new NewOrExistingFileDialog(title, createHandler, selectHandler);
		dialog.center();
	}
	
	/**
	 * Shows a dialog that prompts the user to select a file or use a default file.
	 * 
	 * @param title The title of the dialog
	 * @param selectHandler ClickHandler to execute when the select button is clicked
	 * @param defaultHandler ClickHandler to execute when the use default button is clicked
	 */
	public static void showSelectOrUseDefault(String title, 
			ClickHandler selectHandler, ClickHandler defaultHandler) {
		
		NewOrExistingFileDialog dialog = new NewOrExistingFileDialog(title, selectHandler, defaultHandler);
		dialog.createNewButton.setText("Select");
		dialog.selectExistingButton.setText("Use Default");
		dialog.center();
	}

    /**
     * Shows a dialog that prompts the user to create a new file or import from GIFT Wrap
     * 
     * @param title The title of the dialog
     * @param selectHandler ClickHandler to execute when the select button is clicked
     * @param importHandler ClickHandler to execute when the import button is clicked
     */
    public static void showCreateOrImportFromGIFTWrap(String title, ClickHandler selectHandler,
            ClickHandler importHandler) {
        NewOrExistingFileDialog dialog = new NewOrExistingFileDialog(title, selectHandler, importHandler);
        dialog.selectExistingButton.setText("Import from GIFT Wrap");
        dialog.center();
    }
    
    /**
     * Shows a dialog that prompts the user to create a new file or import from xTSP
     * 
     * @param title The title of the dialog. Supports HTML. Can be null or empty.
     * @param selectHandler ClickHandler to execute when the select button is clicked. Cannot be null.
     * @param importHandler ClickHandler to execute when the import button is clicked. Can be null.
     */
    public static void showCreateOrImportFromXTSP(String title, ClickHandler createHandler, ClickHandler selectHandler,
            ClickHandler importHandler) {
        NewOrExistingFileDialog dialog = new NewOrExistingFileDialog(title, createHandler, selectHandler, importHandler);
        dialog.importXTSPButton.setText("Import from xTSP");        
        dialog.center();
    }

	/**
	 * Gets the button used to create a new file
	 * 
	 * @return the button
	 */
	public Button getCreateNewButton(){
		return createNewButton;
	}
	
	/**
	 * Gets the button used to select an existing file
	 * 
	 * @return the button
	 */
	public Button getSelectExistingButton(){
		return selectExistingButton;
	}
	
	/**
	 * Set the text to show on the optional message element.  This message will
	 * appear above the new and existing buttons for this dialog.  
	 * 
	 * @param htmlMessage contains an html string (don't need the html tags) to show in the
	 * message component of this dialog.  If null or empty the message component will not be shown
	 * on the dialog.
	 */
	public void setOptionalMessage(String htmlMessage){
	    
	    if(htmlMessage == null || htmlMessage.isEmpty()){
	        optionalMessage.setVisible(false);
	    }else{
	        optionalMessage.setVisible(true);
	        optionalMessage.setHTML(htmlMessage);
	    }
	    
	}
	
}
