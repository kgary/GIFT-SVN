/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import java.util.List;

/**
 * A dialog that has a list of settings files
 *
 * @author jleonard
 */
public class SettingsListDialog extends DialogBox {
    
    private String selectedFile = null;
    
    public SettingsListDialog(List<String> settingsFiles) {
        
        FlowPanel container = new FlowPanel();
        
        final ListBox settingsFilesListBox = new ListBox();
        
        settingsFilesListBox.setWidth("300px");
        
        settingsFilesListBox.setVisibleItemCount(5);
        
        for(String settingsFile : settingsFiles) {
            settingsFilesListBox.addItem(settingsFile);
        }
        
        container.add(settingsFilesListBox);
        
        FlowPanel buttonContainer = new FlowPanel();
        
        Button cancelButton = new Button("Cancel");
        
        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                selectedFile = null;
                
                hide();
            }
        });
        
        buttonContainer.add(cancelButton);
        
        Button selectButton = new Button("Select");
        
        selectButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                int index = settingsFilesListBox.getSelectedIndex();

                if (index > -1) {

                    selectedFile = settingsFilesListBox.getItemText(index);

                    hide();

                } else {

                    CommonResources.displayDialog("Select File Error", "No file selected");
                }
            }
        });
        
        buttonContainer.add(selectButton);
        
        container.add(buttonContainer);

        this.setWidget(container);
    }
    
    public String getSelectedFile() {
        
        return selectedFile;
    }
    
}
