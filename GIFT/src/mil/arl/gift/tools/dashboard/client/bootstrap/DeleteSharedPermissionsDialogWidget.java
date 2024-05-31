/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.io.Serializable;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.util.StringUtils;

/**
 * A dialog used to remove shared permissions for a user.
 * 
 * @author sharrison
 */
public class DeleteSharedPermissionsDialogWidget extends AbstractBsWidget {
    
    private static NewSharedPermissionsDialogUiBinder uiBinder = GWT.create(NewSharedPermissionsDialogUiBinder.class);

    interface NewSharedPermissionsDialogUiBinder extends UiBinder<Widget, DeleteSharedPermissionsDialogWidget> {
    }

    @UiField
    Modal dialogModal;
    
    @UiField
    Paragraph dialogMessage;
    
    @UiField
    Button confirmButton;
    
    @UiField
    Button cancelButton;
    
    /** The permission information for the user that we want to remove */
    private Serializable permissions;
    
    /** Used to indicate if the dialog is shown or not */
    private boolean isShown = false;
    
    /** The confirmation message */
    private final static String DELETE_MSG = "Are you sure you want to unshare this course";

    /**
     * Creates a new dialog
     */
    public DeleteSharedPermissionsDialogWidget() {
        initWidget(uiBinder.createAndBindUi(this));

        reset();
    }

    /**
     * Gets the permission information that we want to remove.
     * 
     * @return the permissions to remove
     */
    public Serializable getPermissions() {
        return permissions;
    }

    /** 
     * Resets this widget
     */
    public void reset() {
        dialogMessage.setHTML(DELETE_MSG + "?");
        permissions = null;
    }

    /**
     * Sets the data for this widget. This does not show the widget.
     * 
     * @param permissions the permission information to remove.
     */
    public void setData(Serializable permissions) {
        if (permissions == null){
            reset();
        } else {
            
            String username = null;
            if(permissions instanceof DomainOptionPermissions){
                username = ((DomainOptionPermissions)permissions).getUser();
            }else if(permissions instanceof DataCollectionPermission){
                username = ((DataCollectionPermission)permissions).getUsername();
            }
            
            if(StringUtils.isBlank(username)) {
                reset();
            }else{
                dialogMessage.setHTML(DELETE_MSG + " with " + username + "?");
            }
        }

        this.permissions = permissions;
    }

    /**
     * Accessor to show the modal dialog.
     */
    public void show() {
        dialogModal.show();
        isShown = true;
    }

    /**
     * Accessor to hide the modal dialog.
     */
    public void hide() {
        reset();
        dialogModal.hide();
        isShown = false;
    }

    /**
     * Accessor to determine if the modal is being shown.
     * 
     * @return true if the modal is being shown, false otherwise.
     */
    public boolean isModalShown() {
        return isShown;
    }

    /**
     * Sets the handler for when the confirm button is clicked. The handler needs to be set for the
     * confirm button to be functional.
     */
    public void setConfirmClickHandler(ClickHandler handler) {
        confirmButton.addClickHandler(handler);
    }
    
    /**
     * Sets the handler for when the cancel button is clicked. The handler needs to be set for the
     * cancel button to be functional.
     */
    public void setCancelClickHandler(ClickHandler handler) {
        cancelButton.addClickHandler(handler);
    }
}
