/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.status;

import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.PanelCollapse;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;

/**
 * A collapsible panel containing detailed information about all modules running for
 * a particular module type.
 * 
 * @author nroberts
 */
public class ModuleStatusCollapsePanel extends Composite implements HasText {

    private static ModuleStatusCollapsePanelUiBinder uiBinder = GWT.create(ModuleStatusCollapsePanelUiBinder.class);

    interface ModuleStatusCollapsePanelUiBinder extends UiBinder<Widget, ModuleStatusCollapsePanel> {
    }
    
    /** Interface to allow CSS style name access */
    protected interface Style extends CssResource {

        public String on();
    }

    /** A set of styles associated with this widget */
    @UiField
    protected Style style;
    
    /** The heading containing the display text that can be clicked on to trigger the collapse */
    @UiField
    protected Heading heading;
    
    /** The collapsible area*/
    @UiField
    protected PanelCollapse collapse;
    
    /** The icon used to indicate the module status */
    @UiField
    protected Icon statusIndicator;
    
    /** The queue deck that is used to show the running module queues */
    @UiField
    protected DeckPanel queueDeck;
    
    /** The list group where the names of queues with the same module type will be listed */
    @UiField
    protected ListGroup queueListGroup;
    
    /** 
     * The message that displays to indicate to the user that no instances of module
     * are running
     */
    @UiField
    protected HTML noQueueMessage;
    
    /** The most recent list of queue names that were received for this module */
    private List<String> queueNames = null;

    /** 
     * Whether this panel should remain permanently collapsed. Useful 
     * for modules that should only ever have one instance. 
     */
    private boolean stayCollapsed;
    
    /** The module whose status is shown by this panel */
    private ModuleTypeEnum targetModule;

    /**
     * Creates a new collapsible panel for the given module type and hooks
     * up the needed logic to display its queues and stop it
     * 
     * @param module the module that this panel should represent. Cannot be null;
     */
    public ModuleStatusCollapsePanel(final ModuleTypeEnum module) {
        
        if(module == null) {
            throw new IllegalArgumentException("The module type this collapse should represent cannot be null");
        }
        
        targetModule = module;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        /* Show/hide the queue names when the header is clicked */
        heading.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                if(stayCollapsed) {
                    return;
                }
                
                collapse.setIn(!collapse.isShown());
            }
        }, ClickEvent.getType());
        
        /* Kill the module when the status indicator is clicked */
        statusIndicator.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                event.stopPropagation();
                
                killModule(null);
            }
        });
        
        redrawQueues();
    }

    /**
     * Sets the queue names that should be displayed by this panel. If the list
     * of queue names has changed, the UI will change to reflect it. If no queues
     * are available, the module will be shown as not running.
     * 
     * @param queueNames the list of queue names to display. Cannot be null.
     */
    public void setQueueNames(List<String> queueNames) {
        
        if(this.queueNames != null && this.queueNames.equals(queueNames)) {
            return;
        }
        
        this.queueNames = queueNames;
        
        redrawQueues();
    }

    /**
     * Redraws the rendered list of queues to match the latest state received from the server
     */
    private void redrawQueues() {
        
        /* Update the status indicator to indicate if the module is running */
        if(queueNames == null || queueNames.isEmpty()) {
            statusIndicator.removeStyleName(style.on());
            queueDeck.showWidget(queueDeck.getWidgetIndex(noQueueMessage));
            
        } else {
            statusIndicator.addStyleName(style.on());
            queueDeck.showWidget(queueDeck.getWidgetIndex(queueListGroup));
        }
        
        /* Update the displayed list of queue names*/
        queueListGroup.clear();
        
        if(queueNames != null) {
            for(final String queueName : queueNames) {
                
                /* Add this queue to the displayed list */
                ListGroupItem queueItem = new ListGroupItem();
                queueItem.setText(queueName);
                
                /* Add an button icon that stops this queue specifically */
                Icon stopIcon = new Icon(IconType.TIMES);
                stopIcon.setSize(IconSize.LARGE);
                stopIcon.addClickHandler(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        killModule(queueName);
                    }
                });
                
                ManagedTooltip tooltip = new ManagedTooltip("Click to stop this specific instance of the " + targetModule.getDisplayName());
                tooltip.setWidget(stopIcon);
                
                queueItem.add(stopIcon);
                
                queueListGroup.add(queueItem);
            }
        }
    }

    @Override
    public String getText() {
        return heading.getText();
    }

    @Override
    public void setText(String text) {
        heading.setText(text);
    }
    
    /**
     * Sets whether this panel should remain collapsed permanently and ignore
     * the user's attempts to open it. Useful for modules that should only 
     * ever have one instance. 
     * 
     * @param stayCollapsed whether the panel should stay collapsed.
     */
    public void setStayCollapsed(boolean stayCollapsed) {
        this.stayCollapsed = stayCollapsed;
        
        if(stayCollapsed) {
            heading.getElement().getStyle().setCursor(Cursor.DEFAULT);
        } else {
            heading.getElement().getStyle().clearCursor();
        }
    }
    
    /**
     * Kills the module associated with this panel
     * 
     * @param queueName the queue name of the specific module instance to kill. If null,
     * ALL instances of the module associated with this panel will be killed.
     */
    private void killModule(final String queueName) {
        
        if(queueName == null && queueNames.isEmpty()) {
            
            /* No instances of this module are running, so it makes no sense to initiate a kill request */
            return;
        }
        
        UiManager.getInstance().displayConfirmDialog(
                "Kill Confirmation", 
                "Are you sure you want to kill " + (queueName != null ? queueName : " all instances of this module") + "?", 
                new ConfirmationDialogCallback() {
                    
                    @Override
                    public void onDecline() {
                        //Nothing to do
                    }
                    
                    @Override
                    public void onAccept() {
                        UiManager.getInstance().getDashboardService().killModules(
                                UiManager.getInstance().getUserName(),
                                UiManager.getInstance().getSessionId(), 
                                Arrays.asList(targetModule),
                                queueName,
                                new AsyncCallback<GenericRpcResponse<Void>>() {
                                
                                    @Override
                                    public void onSuccess(GenericRpcResponse<Void> result) {
                                        
                                        if(!result.getWasSuccessful()) {
                                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Kill Module(s)", 
                                                    result.getException());
                                            
                                            return;
                                        }
                                    }
                                
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        UiManager.getInstance().displayDetailedErrorDialog("Failed to Kill Modules", 
                                                "GIFT could not kill the specified modules(s) because an unexpected error occured", 
                                                "An exception was thrown while killing " + targetModule + ": " + caught, 
                                                null, null);
                                    }
                            });
                    }
                });
        
        
    }
}
