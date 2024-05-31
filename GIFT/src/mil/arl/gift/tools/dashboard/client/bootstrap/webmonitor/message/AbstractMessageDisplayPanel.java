/* 
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.message;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.HelpLink;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleMessageProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleMessageProvider.ModuleMessageHandler;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.AbstractMessageUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageEntryMetadata;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChangedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChoicesUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageHeaderStatusUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageListenChangedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageReceivedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageRemovedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageWatchedDomainSessionUpdate;

/**
 * A panel that is able to display messages received from GIFT's modules. Subclasses can extend this
 * panel in order to handle specific types of messages that may need special logic.
 * 
 * @author nroberts
 */
public abstract class AbstractMessageDisplayPanel extends Composite implements ModuleMessageHandler {
    
    /** Sets the maximum number of messages that can be displayed in the message window */
    private static final int MAX_MESSAGE_BUFFER = UiManager.getInstance().getMessageDisplayBufferSize();
    
    /**
     * Controls the delay(in ms) before updating with new messages, setting this to less than 1000 can cause inconsistencies
     * with selecting messages. 
     */
    private static final int MESSAGE_BUFFER_DELAY = 1000;

    private static AbstractMessageDisplayPanelUiBinder uiBinder = GWT.create(AbstractMessageDisplayPanelUiBinder.class);

    interface AbstractMessageDisplayPanelUiBinder extends UiBinder<Widget, AbstractMessageDisplayPanel> {
    }
    
    /** Interface to allow CSS style name access */
    protected interface Style extends CssResource {
        
        /**
         * Gets the CSS class name to use dropdown panels
         *
         * @return the class name
         */
        public String dropdown();
    }

    /** A set of styles associated with this widget */
    @UiField
    protected Style style;
    
    /** The filter icon that will be used to filter the messages to display */
    @UiField
    protected Icon filterButton;
    
    /** The settings icon that will be used to toggle configurations for the message panel */
    @UiField
    protected Icon gearButton;
    
    /** The listening icon that will be used to toggle message panel listening status */
    @UiField
    protected Icon listenButton;
    
    /** The panel where a selected message's details are rendered  */
    @UiField
    protected MessageDetailsPanel messageDetails;
    
    /** ui element describing the entityMarking filter to users **/
    @UiField
    protected Label entityMarkingLabel;
    
    /** captures user entered entity marking for filter **/
    @UiField
    protected TextBox entityMarkingText;
    
    /** explains the character limit on entity marking to user **/
    @UiField
    protected HelpLink entityMarkingTooltip;
    
    /** A deck panel used to show and hide the message details */
    @UiField
    protected DeckPanel messageDetailsDeck;
    
    /** An empty panel to show when the message details are hidden */
    @UiField
    protected Widget noMessageDetails;
    
    /** The cell table used to display messages */
    @UiField
    protected CellTable<MessageEntryMetadata> messageTable;
    
    /** The popup panel that will appear when the settings button is clicked. */
    private PopupPanel settingsPanel = new PopupPanel();
    
    /** The popup panel that will appear when the filter button is clicked */
    private FilterListPanel filterPanel = new FilterListPanel();
    
    /* The panel containing the list of options to check. */
    private VerticalPanel settingsCheckboxPanel = new VerticalPanel();
    
    /** The data provider for the message metadata celltable **/
    private ListDataProvider<MessageEntryMetadata> messageDataProvider = new ListDataProvider<MessageEntryMetadata>();
    
    /**
     * Create a selector for messages in the cell table and display the selection in the message details panel
     */
    final SingleSelectionModel<MessageEntryMetadata> selectionModel = new SingleSelectionModel<>();
    
    /** The list used to buffer incoming messages so as not to overwhelm the click handler */
    private List<MessageEntryMetadata> messageBuffer = new ArrayList<MessageEntryMetadata>();
    
    /** This timer controls the rate at which messages are added to the celltable. */
    private Timer messageTimer = new Timer() {
        @Override
        public void run() {
            if (!messageBuffer.isEmpty()) {
                messageDataProvider.getList().addAll(messageBuffer);
                messageBuffer.clear();
                
                if (messageDataProvider.getList().size() > MAX_MESSAGE_BUFFER) {
                    int numToRemove = messageDataProvider.getList().size() - MAX_MESSAGE_BUFFER;
                    for (int i=0 ; i < numToRemove ; i++) {
                        
                        /* Remove a message that exceeds the buffer */
                        messageDataProvider.getList().remove(0);
                    }
                }
            }
        }       
    };
    
    /**
     * Creates a new panel for displaying messages
     */
    protected AbstractMessageDisplayPanel() {
                
        initWidget(uiBinder.createAndBindUi(this));
        
        messageTimer.scheduleRepeating(MESSAGE_BUFFER_DELAY);
        
        /** entity marking filter should only appear on domain panels and so are hidden unless implemented for a domain tab **/
        entityMarkingLabel.setVisible(false);
        entityMarkingText.setVisible(false);
        entityMarkingTooltip.setVisible(false);
    
        ModuleMessageProvider.getInstance().addHandler(this);
        initMessageTable();
        
        messageDetailsDeck.showWidget(messageDetailsDeck.getWidgetIndex(noMessageDetails));
        
        /* Add a click handler to the listen button that toggles the listening flag. */
        listenButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Integer dsId = getDomainSessionId();
                
                /* Check if the element is OFF, and control listening based on that value */
                boolean listening = listenButton.getElement().getStyle().getColor().equals("black");
                
                UiManager.getInstance().getDashboardService().setListening(UiManager.getInstance().getSessionId(), listening, 
                        dsId,
                        new AsyncCallback<GenericRpcResponse<Void>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                UiManager.getInstance().displayDetailedErrorDialog("Failed to Toggle Listening", 
                                        "GIFT could not toggle the listening value for the module", 
                                        "An exception was thrown while toggling the listening : " + caught, 
                                        null, null);  
                            }

                            @Override
                            public void onSuccess(GenericRpcResponse<Void> result) {
                                if(!result.getWasSuccessful()) {
                                    UiManager.getInstance().displayDetailedErrorDialog("Failed to Toggle Listening", 
                                            result.getException());
                                    
                                    return;
                                }
                            }
                });
            }           
        });
        
        /* Configure settings panel. */
        settingsPanel.setAutoHideEnabled(true);
        settingsPanel.add(settingsCheckboxPanel);
        settingsPanel.addStyleName(style.dropdown());
        
        /* Configure settings panel. */
        filterPanel.setAutoHideEnabled(true);
        filterPanel.addStyleName(style.dropdown());
        
        /* Configure checkbox that will be added to settings panel. */
        CheckBox advancedHeaderBox = new CheckBox();
        advancedHeaderBox.setText("Advanced Header");
        settingsCheckboxPanel.add(advancedHeaderBox);      
        advancedHeaderBox.addValueChangeHandler(new ValueChangeHandler<Boolean> () {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                Integer dsId = getDomainSessionId();
                
                boolean advancedHeader = event.getValue();
                UiManager.getInstance().getDashboardService().setAdvancedHeader(UiManager.getInstance().getSessionId(), advancedHeader,
                        dsId,
                        new AsyncCallback<GenericRpcResponse<Void>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                UiManager.getInstance().displayDetailedErrorDialog("Failed to Toggle Listening", 
                                        "GIFT could not toggle the advanced header value for the module", 
                                        "An exception was thrown while toggling the listening : " + caught, 
                                        null, null);  
                            }

                            @Override
                            public void onSuccess(GenericRpcResponse<Void> result) {
                                if(!result.getWasSuccessful()) {
                                    UiManager.getInstance().displayDetailedErrorDialog("Failed to Toggle Advanced Header", 
                                            result.getException());
                                    
                                    return;
                                }
                            }
                });
            }  
        });
        
        gearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                settingsPanel.showRelativeTo(gearButton);
            }           
        });  
        
        filterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                filterPanel.showRelativeTo(filterButton);
                
                /* Adjust the height of the filter panel so that it avoids overflowing outside the window */
                int maxHeight = Window.getClientHeight() - (filterButton.getAbsoluteTop() + filterButton.getOffsetHeight());
                filterPanel.getWidget().getElement().getStyle().setProperty("maxHeight", maxHeight + "px");
            }           
        });  
    }

    @Override
    public void onMessageReceived(MessageReceivedUpdate update) {
        
        if(update == null || !isAccepted(update) || !showing) {
            
            /* This panel is not listening to the domain session that the update is part of */
            return;
        }
        
        addMessage(update.getMessage());
    }
    
    @Override
    public void onMessageRemoved(MessageRemovedUpdate update) {
        
        if(update == null || !isAccepted(update) || !showing) {
            
            /* This panel is not listening to the domain session that the update is part of */
            return;
        }
        
        removeMessage(update.getMessage());
    }

    @Override
    public void onMessageFilterChanged(MessageFilterChangedUpdate update) {
        
        if(update == null || !isAccepted(update) || !showing) {
            
            /* This panel is not listening to the domain session that the update is part of */
            return;
        }
        MessageEntryMetadata selected = null;
        
        /* Store the currently selected object, if one is selected */
        if (selectionModel.getSelectedObject() != null) {
            selected = selectionModel.getSelectedObject();
            selectionModel.setSelected(selected, false);
        }

        messageDataProvider.getList().clear();
        messageBuffer.clear();
        
                /* Need to totally reload the displayed messages */
                refreshMessages();
                
                /* Reselect the previously selected message if it is still displayed */
                for(MessageEntryMetadata message : update.getMessages()) {
                    
                    addMessage(message);
                    
            if (selected!= null && message.equals(selected)) {
                selectionModel.setSelected(selected, true);
                    }
                }
            }
    
    @Override
    public void onMessageListenChanged(MessageListenChangedUpdate update) {
        
        if(update == null || !isAccepted(update) || !showing) {
            
            /* This panel is not listening to the domain session that the update is part of */
            return;
        }
        
        /* When the message filter status has changed, toggle the button color. */
        toggleListenButton(update.isListening());
    }
    
    @Override
    public void onMessageHeaderStatusChanged(MessageHeaderStatusUpdate update) {
        
        if(update == null || !isAccepted(update) || !showing) {
            
            /* This panel is not listening to the domain session that the update is part of */
            return;
        }
        
        /* Control the visibility of the panels based on the checkbox toggle. */
        messageDetails.sourceModuleTypePanel.setVisible(update.isAdvancedHeader());
        messageDetails.seqNumPanel.setVisible(update.isAdvancedHeader());
        messageDetails.sourceEventIdPanel.setVisible(update.isAdvancedHeader());
        messageDetails.needsACKPanel.setVisible(update.isAdvancedHeader());
    }
    
    @Override
    public void onMessageWatchedDomainSessionsChanged(MessageWatchedDomainSessionUpdate update) {
        //Nothing to do
    }
    

    /** Whether this panel is currently showing and should process updates */
    private boolean showing;
    
    /**
     * Cell table settings
     */
    private void initMessageTable() {
        messageTable.setPageSize(Integer.MAX_VALUE);
        
        Column<MessageEntryMetadata, String> systemColumn = new Column<MessageEntryMetadata, String>(new TextCell()) {

            @Override
            public String getValue(MessageEntryMetadata message) {
                return message.getType().getDisplayName();
            }
            
        };
        
        messageTable.addColumn(systemColumn);
        messageTable.setColumnWidth(systemColumn, "200px");
        
        messageDataProvider.addDataDisplay(messageTable);
        
        messageTable.setSelectionModel(selectionModel);
        
        /* Nick: Need to totally disable keyboard selection, otherwise, the cell table will 
         * constantly attempt to scroll the last keyboard-selected (or mouse-selected) column 
         * into view, which pretty much makes it impossible for the user to actually use the 
         * scroll bar */
        messageTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                
                Integer dsId = getDomainSessionId();
                
                MessageEntryMetadata selected = selectionModel.getSelectedObject();
                
                /* Show or hide the message details as appropriate*/
                if(selected != null) {
                    messageDetailsDeck.showWidget(messageDetailsDeck.getWidgetIndex(messageDetails));
                    messageDetails.showMessage(dsId, selected);
                }
            }
            
        });
        
    }
    
    /**
     * Adds new messages to the messageBuffer controlled by messageTimer.
     * 
     * @param message the message to add. Cannot be null.
     */
    public void addMessage(final MessageEntryMetadata message) {
        messageBuffer.add(message);
    }
    
    /**
     * Removes a displayed message
     * 
     * @param message the message to remove. Cannot be null.
     */
    public void removeMessage(final MessageEntryMetadata message) {
        messageBuffer.remove(message);
        messageDataProvider.getList().remove(message);
    }
    
    /**
     * Toggle the color of the listen button to indicate whether enabled or diabled
     * 
     * @param status the status of the button, ON (true) or OFF (false)
     */
    public void toggleListenButton(boolean status) {
        listenButton.setColor(status ? "green" : "black");
    }
    
    /**
     * Gets whether this display panel should handle this update
     * 
     * @param update the update to check. Cannot be null.
     * @return whether this update should be accepted.
     */
    protected abstract boolean isAccepted(AbstractMessageUpdate update);
    
    @Override
    public void onMessageFilterChoices(MessageFilterChoicesUpdate update) {
        
        if(update == null || !isAccepted(update) || !showing) {
            return;
        }
        
        filterPanel.onUpdate(update);
    }
    
    /**
     * Gets the ID of the domain session that this panel monitors
     * 
     * @return the domain session ID
     */
    public abstract Integer getDomainSessionId();
    
    /**
     * Unloads the message data currently shown by this panel. This is mainly used to save client 
     * resources when a panel no longer needs to be shown, since the server maintains the ACTUAL
     * information and can simply refresh this panel's data when needed.
     */
    public void clear() {
        messageBuffer.clear();
        messageDataProvider.getList().clear();
    }
    
    /**
     * Sets whether this panel is showing and should receive updates
     * 
     * @param showing whether the panel should show
     */
    public void setShowing(boolean showing) {
        this.showing = showing;
    }
    
    /**
     * Immediately refreshes the list of displayed messages. Normally, the message list updates periodically, but
     * in some circumstances, such as when the filter is changed or the panel is refreshed, it is better to
     * immediately update the displayed messages.
     */
    public void refreshMessages() {
        messageTimer.run();
    }
}
