/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChoicesUpdate;

/**
 * A panel used to display the available message types that can be filtered
 * 
 * @author nroberts
 */
public class FilterListPanel extends PopupPanel {

    private static FilterListPanelUiBinder uiBinder = GWT.create(FilterListPanelUiBinder.class);

    interface FilterListPanelUiBinder extends UiBinder<Widget, FilterListPanel> {
    }
    
    /** A mapping from each filter choice to the checkbox used to enable it */
    private Map<MessageTypeEnum, CheckBox> choiceToCheckBox = new HashMap<>();
    
    /** A panel where the filter choice checkboxes are added */
    @UiField
    protected FlowPanel filterList;

    /** The domain session ID that this filter is associated with*/
    private Integer domainSessionId;

    /**
     * Creates a new panel for displaying the list of message types to filter by
     */
    public FilterListPanel() {
        setWidget(uiBinder.createAndBindUi(this));
    }
    
    /**
     * Loads the given filter update into this widget to update its
     * displayed list of filter choices
     * 
     * @param update an update containing the list of choices. Cannot be null.
     */
    public void onUpdate(MessageFilterChoicesUpdate update) {
        
        this.domainSessionId = update.getDomainSessionId();
        
        Set<MessageTypeEnum> toRemove = new HashSet<>(choiceToCheckBox.keySet());
        
        for(MessageTypeEnum choice : update.getAllChoices()) {
            
            CheckBox choiceBox;
            
            if(toRemove.contains(choice)) {
                
                /* This choice still exists, so just update the existing one */
                toRemove.remove(choice);
                choiceBox = choiceToCheckBox.get(choice);
                
            } else {
                
                /* A new choice is available, so need to add it */
                choiceBox = addChoice(choice);
            }
            
            choiceBox.setValue(update.getSelectedChoices().contains(choice));
        }
        
        /* Remove any choices that are no longer present */
        for(MessageTypeEnum removeChoice : toRemove) {
            CheckBox choiceBox = choiceToCheckBox.remove(removeChoice);
            filterList.remove(choiceBox);
        }
    }
    
    /**
     * Adds a new filter check box based on the given message type choice
     * 
     * @param choice the message type choice that the check box will represent. Cannot be null.
     * @return the check box representing the choice. Will not be null.
     */
    public CheckBox addChoice(final MessageTypeEnum choice) {
        
        final CheckBox choiceBox = new CheckBox(choice.getDisplayName());
        choiceBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
                
                if(!event.isControlKeyDown()) {
                    
                    /* Deselect the other choices unless the Ctrl key is pressed */
                    for(MessageTypeEnum otherChoice : choiceToCheckBox.keySet()) {
                        if(!otherChoice.equals(choice)) {
                            choiceToCheckBox.get(otherChoice).setValue(false);
                        }
                    }
                }
                
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        
                        /* Select this choice */
                        choiceBox.setValue(true);
                                
                        /* Gather the selected choices */
                        Set<MessageTypeEnum> selectedChoices = new HashSet<>();
                        for(MessageTypeEnum thisChoice : choiceToCheckBox.keySet()) {
                            CheckBox box = choiceToCheckBox.get(thisChoice);
                            if(box.getValue()) {
                                selectedChoices.add(thisChoice);
                            }
                        }
                      
                        /* Update the list of selected choices on the server */
                        UiManager.getInstance().getDashboardService().setMessageFilter(
                              UiManager.getInstance().getUserName(), 
                              UiManager.getInstance().getSessionId(), 
                              domainSessionId, 
                              selectedChoices, 
                              new AsyncCallback<GenericRpcResponse<Void>>() {
                          
                                  @Override
                                  public void onFailure(Throwable caught) {
                                      UiManager.getInstance().displayDetailedErrorDialog("Failed to Filter Message Types", 
                                              "GIFT could not filter the selected message types", 
                                              "An exception was thrown while filtering the message types : " + caught, 
                                                  null, null);  
                                      }
  
                                      @Override
                                      public void onSuccess(GenericRpcResponse<Void> result) {
                                          if(!result.getWasSuccessful()) {
                                              UiManager.getInstance().displayDetailedErrorDialog("Failed to Filter Message Types", 
                                                  result.getException());
                                          
                                          return;
                                      }
                            }
                        });
                    }
                });
                
                
                
            }
        });
        
        choiceToCheckBox.put(choice, choiceBox);
        
        filterList.add(choiceBox);
        
        return choiceBox;
    }

}
