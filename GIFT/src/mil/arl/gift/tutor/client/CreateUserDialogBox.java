/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * Dialog box for creating a new GIFT user
 *
 * @author jleonard
 */
public class CreateUserDialogBox extends DialogBox {

    private final ListBox genderListBox = new ListBox();

    final TextBox lmsUsernameTextBox = new TextBox();

    final Button cancelButton = new Button("Cancel");

    final Button createUserButton = new Button("Create user");

    /**
     * For handling clicks on the create user button
     */
    private class CreateUserClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            if (genderListBox.getSelectedIndex() > -1) {

                Boolean isMale = null;

                String selected = genderListBox.getItemText(genderListBox.getSelectedIndex());
                if (selected.equalsIgnoreCase("male")) {
                    isMale = true;
                } else if (selected.equalsIgnoreCase("female")) {
                    isMale = false;
                }

                String lmsUsername = lmsUsernameTextBox.getText();
                if (lmsUsername != null && lmsUsername.isEmpty()) {
                    lmsUsername = null;
                }

                if (isMale != null && lmsUsername != null) {
                    createUserButton.setEnabled(false);
                    cancelButton.setEnabled(false);

                    BrowserSession.createNewUser(isMale, lmsUsername, new AsyncCallback<RpcResponse>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Document.getInstance().displayRPCError("Creating a new user", caught);
                            createUserButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                        }

                        @Override
                        public void onSuccess(RpcResponse result) {
                            if (!result.isSuccess()) {
                                Document.getInstance().displayError("Creating a new user", "The action failed on the server.", result.getResponse());
                            } else {
                                CreateUserDialogBox.this.hide();
                            }
                            createUserButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                        }
                    });
                }
            }
        }
    }

    /**
     * For handling clicks on the cancel button
     */
    private class CancelClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            CreateUserDialogBox.this.hide();
        }
    }

    /**
     * Constructor
     */
    public CreateUserDialogBox() {
        this.setText("Create New User");

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSpacing(5);
        verticalPanel.setSize("100%", "100%");

        Grid grid = new Grid(2, 2);
        verticalPanel.add(grid);
        grid.setWidth("100%");

        Label genderLabel = new Label("Gender");
        grid.setWidget(0, 0, genderLabel);
        grid.getCellFormatter().setWidth(0, 0, "100px");

        genderListBox.addItem("Male");
        genderListBox.addItem("Female");
        grid.setWidget(0, 1, genderListBox);
        genderListBox.setWidth("100%");
        genderListBox.setVisibleItemCount(1);

        Label lmsUsernameLabel = new Label("LMS Username");
        grid.setWidget(1, 0, lmsUsernameLabel);

        lmsUsernameTextBox.setMaxLength(32);
        grid.setWidget(1, 1, lmsUsernameTextBox);
        lmsUsernameTextBox.setWidth("100%");

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(5);
        verticalPanel.add(horizontalPanel);
        verticalPanel.setCellHorizontalAlignment(horizontalPanel, HasHorizontalAlignment.ALIGN_CENTER);
        horizontalPanel.setWidth("100%");

        horizontalPanel.add(cancelButton);
        cancelButton.setWidth("100px");
        cancelButton.addClickHandler(new CancelClickHandler());

        horizontalPanel.add(createUserButton);
        horizontalPanel.setCellHorizontalAlignment(createUserButton, HasHorizontalAlignment.ALIGN_RIGHT);
        createUserButton.setWidth("100px");
        createUserButton.addClickHandler(new CreateUserClickHandler());

        this.setWidget(verticalPanel);
    }
}
