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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.util.StringUtils;

/**
 * Defines a dialog with: 'Delete', 'Remove', and 'Cancel' options. 
 * The dialog is created and displayed with the static method 'show'
 */
public class DeleteRemoveCancelDialog extends ModalDialogBox {
       
    private static final DeleteRemoveCancelDialog dialog = new DeleteRemoveCancelDialog();
    
    /**
     * Displays a dialog and modifies it based off of the parameters passed to the function
     * @param title The title to be displayed for the dialog. Can not be null
     * @param msgHTML The details of the dialog to display. It can include HTML formatting tags to style the message to the caller's liking. Can not be null
     * @param callback Contains the three click handlers for each of the buttons. Can not be null.
     */
    public static void show(String title, String msgHTML, DeleteRemoveCancelCallback callback) {
        
        //Perform the null checks for the passed values
        if(title == null || title.equals(""))
            throw new IllegalArgumentException("The title of a DeleteRemoveCancelDialog can not be null or empty");
        if(msgHTML == null || msgHTML.equals(""))
            throw new IllegalArgumentException("The msgHTML of a DeleteRemoveCancelDialog can not be null or empty");
        if(callback == null)
            throw new IllegalArgumentException("The callback of a DeleteRemoveCancelDialog can not be null. The dialog must have click handlers defined.");
        
        //Sets the parameters of the dialog
        dialog.setText(title == null ? "" : title);
        dialog.setMessage(msgHTML);
        dialog.setCallback(callback);
        dialog.center();
    }
    
    /**
     * Displays a dialog and modifies it based off of the parameters passed to the function.  This instance allows
     * the delete button label to be customized.
     * 
     * @param title The title to be displayed for the dialog. Can not be null
     * @param msgHTML The details of the dialog to display. It can include HTML formatting tags to style the message to the caller's liking. Can not be null
     * @param callback Contains the three click handlers for each of the buttons. Can not be null.
     * @param deleteButtonText the text to show on the delete button.  Can't be null or empty.
     */
    public static void show(String title, String msgHTML, DeleteRemoveCancelCallback callback, String deleteButtonText) {
        
        if(StringUtils.isBlank(deleteButtonText)){
            throw new IllegalArgumentException("The delete button text can't be null or empty.");
        }
        
        dialog.deleteButton.setText(deleteButtonText);
        
        show(title, msgHTML, callback);
    }
    
    /**
     * Displays a dialog and modifies it based off of the parameters passed to the function.  This instance allows
     * the delete and remove button labels to be customized.
     * 
     * @param title The title to be displayed for the dialog. Can not be null
     * @param msgHTML The details of the dialog to display. It can include HTML formatting tags to style the message to the caller's liking. Can not be null
     * @param callback Contains the three click handlers for each of the buttons. Can not be null.
     * @param deleteButtonText the text to show on the delete button.  Can't be null or empty.
     * @param removeReferenceButtonText the text to show on the remove reference button.  Can't be null or empty.
     */
    public static void show(String title, String msgHTML, DeleteRemoveCancelCallback callback, String deleteButtonText, String removeReferenceButtonText) {
        
        if(StringUtils.isBlank(deleteButtonText)){
            throw new IllegalArgumentException("The delete button text can't be null or empty.");
        }else if(StringUtils.isBlank(removeReferenceButtonText)){
            throw new IllegalArgumentException("The remove reference button text can't be null or empty.");
        }
        
        dialog.deleteButton.setText(deleteButtonText);
        dialog.removeButton.setText(removeReferenceButtonText);
        
        show(title, msgHTML, callback);
    }
    
    /** The callback. */
    private DeleteRemoveCancelCallback callback;
    
    /** The HTML. */
    private HTML html = new HTML();
    
    /** The flow panel. */
    private FlowPanel flowPanel = new FlowPanel();
    
    /** The delete button. */
    private Button deleteButton = new Button("Delete Content & Save");
    
    /** The remove button. */
    private Button removeButton = new Button("Remove Reference");
    
    /** The cancel button. */
    private Button cancelButton = new Button("Cancel");
    
    private DeleteRemoveCancelCallback getCallback() {
        return this.callback;
    }
    
    private void setCallback(DeleteRemoveCancelCallback callback) {
        this.callback = callback;
    }
    
    private void setMessage(String msgHTML) {
        this.html.setHTML(msgHTML);
    }
    
    private DeleteRemoveCancelDialog() {
        setGlassEnabled(true);
        
        //Sets the behavior of the buttons
        deleteButton.setType(ButtonType.WARNING);
        deleteButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                getCallback().delete();
                hide();
            }
            
        });
        
        removeButton.setType(ButtonType.PRIMARY);
        removeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                getCallback().remove();
                hide();
            }
            
        });
        
        cancelButton.setType(ButtonType.DANGER);
        cancelButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                getCallback().cancel();
                hide();
            }
            
        });
        
        VerticalPanel vPanel = new VerticalPanel();
        
        vPanel.getElement().getStyle().setProperty("maxWidth", "700px");
        vPanel.getElement().getStyle().setProperty("padding", "10px 10px 0px 10px");
        vPanel.setSpacing(10);
        
        vPanel.add(html);
        vPanel.add(flowPanel);
        
        setFooterWidget(cancelButton);
        setFooterWidget(removeButton);
        setFooterWidget(deleteButton);
        
        setWidget(vPanel);
    }
}