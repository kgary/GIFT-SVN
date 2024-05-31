/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.util.StringUtils;

/**
 * A dialog used to create or edit shared permissions for a user.
 * 
 * @author sharrison
 */
public class AddSharedPermissionsDialogWidget<T extends AbstractEnum> extends AbstractBsWidget {
    
    private static NewSharedPermissionsDialogUiBinder uiBinder = GWT.create(NewSharedPermissionsDialogUiBinder.class);

    interface NewSharedPermissionsDialogUiBinder extends UiBinder<Widget, AddSharedPermissionsDialogWidget<?>> {
    }

    @UiField
    Modal dialogModal;
    
    @UiField
    Paragraph dialogMessage;
    
    @UiField
    TextBox userTextBox;
    
    @UiField
    Select permissionSelect;

    @UiField
    Button confirmButton;
    
    @UiField
    Button cancelButton;
    
    /** Used to indicate if the dialog is shown or not */
    private boolean isShown = false;
       
    /** the possible values that can be selected as permissions */
    private List<? extends AbstractEnum> permissionChoices;

    /**
     * Creates a new dialog
     * 
     * @param permissionChoices the collection of permissions to choose from to show in the dialog.
     * can't be null or empty.
     */
    public AddSharedPermissionsDialogWidget(List<? extends AbstractEnum> permissionChoices) {
        initWidget(uiBinder.createAndBindUi(this));
        
        if(permissionChoices == null || permissionChoices.isEmpty()){
            throw new IllegalArgumentException("The permission choices can't be null or empty");
        }
        this.permissionChoices = permissionChoices;        

        dialogMessage.setColor("RED");

        populatePermissionsList();
    }

    /**
     * Populates the multi select dropdown with the available permissions that can be chosen.
     */
    private void populatePermissionsList() {

        // remove any options
        while (permissionSelect.getWidgetCount() != 0) {
            permissionSelect.remove(0);
        }

        OptGroup courseOptionGroup = new OptGroup();
        
        for (AbstractEnum permissionEnum : permissionChoices) {

            Option opt = new Option();
            opt.setText(permissionEnum.getDisplayName());
            opt.setValue(permissionEnum.getDisplayName());
            courseOptionGroup.add(opt);
        }

        permissionSelect.add(courseOptionGroup);

        permissionSelect.refresh();
    }
    
    /**
     * Gets the identifier text in the textbox.
     * 
     * @return the identifier text
     */
    public String getUser() {
        return userTextBox.getValue();
    }
    
    /**
     * Retrieves the selected permission type.
     * 
     * @return the permission chosen by the author. Can be null if no permission was selected.
     */
    @SuppressWarnings("unchecked")
    public T getPermission() {
        T permission = null;

        if (permissionSelect.getSelectedItem() != null) {
            try {
                permission = (T) AbstractEnum.valueOf(permissionSelect.getSelectedItem().getValue(), permissionChoices);
            } catch (@SuppressWarnings("unused") EnumerationNotFoundException e) {
                // do nothing
            }
        }

        return permission;
    }
    
    /**
     * Resets the widget.
     */
    public void reset() {
        userTextBox.setValue(null);
        userTextBox.setEnabled(true);
        permissionSelect.setValue(null);
        confirmButton.setText("Add User");
        dialogMessage.setHTML(null);
    }
    
    /**
     * Sets the values of the widget.
     * 
     * @param user the username. Can't be null.
     * @param permission the permission type.
     * @throws IllegalArgumentException if the user is null.
     */
    public void setValues(String user, AbstractEnum permission) {
        if (StringUtils.isBlank(user)) {
            throw new IllegalArgumentException("User must exist.");
        }

        userTextBox.setValue(user);
        userTextBox.setEnabled(false);

        confirmButton.setText("Edit User");

        permissionSelect.setValue(permission == null ? null : permission.getDisplayName(), true);
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
    
    /**
     * Sets the dialog text for the author to see.
     * 
     * @param message the html message for the dialog.
     */
    public void setDialogMessage(String message) {
        dialogMessage.setHTML(message);
    }
}
