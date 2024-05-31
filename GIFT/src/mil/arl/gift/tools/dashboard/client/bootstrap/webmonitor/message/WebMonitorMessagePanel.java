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

import org.gwtbootstrap3.client.shared.event.TabShownEvent;
import org.gwtbootstrap3.client.shared.event.TabShownHandler;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.constants.IconPosition;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleMessageProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleMessageProvider.ModuleMessageHandler;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.WebMonitorStatusProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.WebMonitorStatusProvider.WebMonitorStatusChangeHandler;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChangedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChoicesUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageHeaderStatusUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageListenChangedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageReceivedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageRemovedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageWatchedDomainSessionUpdate;
import mil.arl.gift.tools.dashboard.shared.rpcs.WebMonitorStatus;

/**
 * A section of the web monitor that contains UI components used to view the message
 * traffic for various parts of the GIFT system.
 * 
 * @author nroberts
 */
public class WebMonitorMessagePanel extends Composite implements ModuleMessageHandler, WebMonitorStatusChangeHandler {

    private static WebMonitorMessagePanelUiBinder uiBinder = GWT.create(WebMonitorMessagePanelUiBinder.class);

    interface WebMonitorMessagePanelUiBinder extends UiBinder<Widget, WebMonitorMessagePanel> {
    }
    
    /** The container panel that wraps the list of active sessions */
    @UiField
    protected Widget activeSessionsContainer;
    
    /** A splitter bar that can be used to hide the list of active sessions*/
    @UiField
    protected Widget splitterBar;
    
    /** The area where the tab content is displayed */
    @UiField
    protected TabContent tabContent;
    
    /** The area where the tab headers are displayed */
    @UiField
    protected NavTabs navTabs;
    
    /** A mapping from each domain session to it's associated tab */
    private Map<Integer, DomainSessionMessageDisplayTab> domainSessionToTab = new HashMap<>();

    /** The tab used to show system messages */
    private final MessageDisplayTab systemTab;
    
    /** The tab currently being shown */
    private MessageDisplayTab showingTab = null;

    /**
     * Creates a new panel used to view the message traffic for various parts of the GIFT system.
     */
    public WebMonitorMessagePanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        /* show/hide the active sessions list when the splitter bar is clicked */
        splitterBar.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(StringUtils.isBlank(activeSessionsContainer.getElement().getStyle().getWidth())) {
                    activeSessionsContainer.getElement().getStyle().setWidth(0, Unit.PX);
                    
                } else {
                    activeSessionsContainer.getElement().getStyle().clearWidth();
                }
            }
            
        }, ClickEvent.getType());
        
        /* Create and show the system tab */
        systemTab = new MessageDisplayTab("tab-system", "System", new SystemMessageDisplayPanel());
        
        navTabs.add(systemTab.getHeader());
        tabContent.add(systemTab.getContent());
        
        setShowingTab(systemTab, false);
        
        ModuleMessageProvider.getInstance().addHandler(this);
        WebMonitorStatusProvider.getInstance().addHandler(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedUpdate update) {
        //Nothing to do
    }
    
    @Override
    public void onMessageRemoved(MessageRemovedUpdate update) {
        //Nothing to do
    }

    /**
     * Creates a new message display tab for the domain session with the given ID. This
     * will in turn create a display panel to handle message updates from that session
     * 
     * @param domainSessionId the ID of the domain session to add a display for.
     */
    private void addMessageDisplay(int domainSessionId) {
        
        /* Create a tab for the domain session's messages */
        DomainSessionMessageDisplayTab newTab = new DomainSessionMessageDisplayTab(domainSessionId);
        
        navTabs.add(newTab.getHeader());
        tabContent.add(newTab.getContent());
        
        domainSessionToTab.put(domainSessionId, newTab);
        
        /* Display that tab*/
        setShowingTab(newTab, true);
    }
    
    @Override
    public void onMessageFilterChoices(MessageFilterChoicesUpdate update) {
        //Nothing to do
    }

    @Override
    public void onMessageFilterChanged(MessageFilterChangedUpdate update) {
        //Nothing to do
    }
    
    @Override
    public void onMessageListenChanged(MessageListenChangedUpdate update) {
        // Nothing to do
    }
    
    @Override
    public void onMessageHeaderStatusChanged(MessageHeaderStatusUpdate update) {
        // Nothing to do
    }
    
    @Override
    public void onMessageWatchedDomainSessionsChanged(MessageWatchedDomainSessionUpdate update) {
        
        Set<Integer> domainSessionsToRemove = new HashSet<>(domainSessionToTab.keySet());
        for(Integer domainSessionId : update.getWatchedDomainSessions()) {
            
            if(!domainSessionToTab.containsKey(domainSessionId)) {
                
                /* This is a new domain session, so add a panel for it */
                addMessageDisplay(domainSessionId);
                
            } else {
                
                /* This domain session is still watched, so don't remove it */
                domainSessionsToRemove.remove(domainSessionId);
            }
        }
        
        for(Integer domainSessionId : domainSessionsToRemove) {
            
            /* This domain session is no longer watched, so remove its panel */
            MessageDisplayTab tab = domainSessionToTab.get(domainSessionId);
            navTabs.remove(tab.getHeader());
            tabContent.remove(tab.getContent());
            domainSessionToTab.remove(domainSessionId);
            
            if(tab.equals(showingTab)) {
                
                /* Can't show this domain session's tab anymore, so switch back to system */
                setShowingTab(systemTab, true);
            }
        }
    }
    
    /**
     * The tab and its associated content for message source (i.e. system, domain session)
     * 
     * @author nroberts
     */
    private class MessageDisplayTab{
        
        /** The tab containing the header text */
        private TabListItem header;
        
        /** The content shown by the tab */
        private TabPane content;
        
        /** The panel used to display the received messages */
        private AbstractMessageDisplayPanel displayPanel;
        
        /**
         * Creates a new tab to display messages
         * 
         * @param tabId the unique ID to give the tab. Cannot be null.
         * @param displayName the display name of the tab. Cannot be null.
         * @param panel the panel used to display messages for the tab. Cannot be null.
         */
        public MessageDisplayTab(String tabId, String displayName, AbstractMessageDisplayPanel panel) {
            
            if(tabId == null) {
                throw new IllegalArgumentException("The tab ID cannot be null");
            }
            
            if(displayName == null) {
                throw new IllegalArgumentException("The display name cannot be null");
            }
            
            if(panel == null) {
                throw new IllegalArgumentException("The message display panel cannot be null");
            }
            
            header = new TabListItem(displayName);
            header.setDataTarget("#" + tabId);
            
            displayPanel = panel;
            content= new TabPane();
            content.setId(tabId);
            content.add(displayPanel);
            header.addShownHandler(new TabShownHandler() {
                
                @Override
                public void onShown(TabShownEvent event) {
                    showingTab = MessageDisplayTab.this;
                    onTabShown(getDomainSessionId(), true);
                }
            });
        }

        /**
         * Gets the tab containing the header text
         * 
         * @return the header. Will not be null.
         */
        public TabListItem getHeader() {
            return header;
        }

        /**
         * Gets the content shown by the tab
         * 
         * @return the tab content. Will not be null.
         */
        public TabPane getContent() {
            return content;
        }
        
        /**
         * Gets the panel used to display the received messages
         * 
         * @return the display panel. Will not be null.
         */
        public AbstractMessageDisplayPanel getDisplayPanel() {
            return displayPanel;
        }
        
        /**
         * Gets the domain session id associated with the opened tab. 
         * NOTE: Since the System tab uses this class, there is no domain session 
         * associated, which is why null is returned in this case.
         * 
         * @return the domain session id, which is null for system messages
         */
        public Integer getDomainSessionId() {
            return null;
        }
    }
    
    /**
     * The tab and its associated content for a domain session
     * 
     * @author nroberts
     */
    private class DomainSessionMessageDisplayTab extends MessageDisplayTab {
        
        private int domainSessionId;

        /**
         * Creates a new tab for the domain session with the given ID
         * 
         * @param domainSessionId the ID of the domain session
         */
        public DomainSessionMessageDisplayTab(final int domainSessionId) {
            super("tab-session-" + domainSessionId, 
                    "Session " + domainSessionId, 
                    new DomainSessionMessageDisplayPanel(domainSessionId));
            
            this.domainSessionId = domainSessionId;
            
            getHeader().setIcon(IconType.CLOSE);
            getHeader().setIconPosition(IconPosition.RIGHT);
            
            getHeader().addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    
                    Element element = event.getNativeEvent().getEventTarget().cast();
                    if(element.getTagName().toLowerCase() != "i") {
                        
                        /* User did not click an icon element (i.e. the close icon), so ignore */
                        return;
                    }
                    
                    event.stopPropagation();
                    event.preventDefault();
                    
                    UiManager.getInstance().getDashboardService().unwatchDomainSession(
                        UiManager.getInstance().getSessionId(),
                        domainSessionId,
                        new AsyncCallback<GenericRpcResponse<Void>>() {
    
                            @Override
                            public void onFailure(Throwable caught) {
                                UiManager.getInstance().displayDetailedErrorDialog("Failed to remove domain display panel", 
                                        "GIFT could not close the domain tab", 
                                        "An exception was thrown while closing the domain tab : " + caught, 
                                        null, null);  
                                
                            }
    
                            @Override
                            public void onSuccess(GenericRpcResponse<Void> result) {
                                if(!result.getWasSuccessful()) {
                                    UiManager.getInstance().displayDetailedErrorDialog("Failed to remove domain display panel", 
                                            result.getException());
                                    
                                    return;
                                }
                            }  
                        }
                    );
                }
            });
        }
        
        @Override
        public Integer getDomainSessionId() {
            return domainSessionId;
        }
    }
    
    /**
     * Displays the tab for the given domain session and refreshes its data if
     * requested
     * 
     * @param domainSessionId the ID of the domain session whose tab is shown.
     * Can be null for the system tab.
     * @param refresh whether to refresh the tab's messages when it is shown.
     */
    private void onTabShown(Integer domainSessionId, boolean refresh) {
        
        AbstractMessageDisplayPanel displayPanel;
        if(domainSessionId == null) {
            displayPanel = systemTab.getDisplayPanel();
        } else {
            displayPanel = domainSessionToTab.get(domainSessionId).getDisplayPanel();
        }
        
        displayPanel.setShowing(true);
        
        if(!refresh) {
            
            /* Browser session is not attached yet, so don't refresh */
            return;
        }
        
        /* Refresh the target panel*/
        UiManager.getInstance().getDashboardService().refreshPanel(
                UiManager.getInstance().getUserName(), 
                UiManager.getInstance().getSessionId(), 
                domainSessionId,
                new AsyncCallback<GenericRpcResponse<Void>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        UiManager.getInstance().displayDetailedErrorDialog("Failed to Refresh Panel", 
                                "GIFT could not refresh the panel being displayed", 
                                "An exception was thrown while refreshing the panel : " + caught, 
                                null, null);  
                    }

                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {
                        if(!result.getWasSuccessful()) {
                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Refresh Panel", 
                                    result.getException());
                            
                            return;
                        }
                    }
        });
        
        /* Clear the other panels to save memory */
        if(domainSessionId != null) {
            systemTab.getDisplayPanel().setShowing(false);
            systemTab.getDisplayPanel().clear();
        }
        
        for(Integer otherDomainSession : domainSessionToTab.keySet()) {
            
            if(otherDomainSession.equals(domainSessionId)) {
                
                /* This is the domain session we're trying to show, so don't clear it*/
                continue;
            }
            
            AbstractMessageDisplayPanel panel = domainSessionToTab.get(otherDomainSession).getDisplayPanel();
            panel.setShowing(false);
            panel.clear();
        }
    }
    
    /**
     * Sets the message display tab to show
     * 
     * @param tab the tab to show. Cannot be null.
     * @param refresh whether to refresh the tab when it is shown.
     */
    private void setShowingTab(MessageDisplayTab tab, boolean refresh) {
        
        if(showingTab != null && showingTab.equals(tab)) {
            
            /* Tab is already showing, so just return*/
            return; 
        }
        
        if(showingTab != null) {
            showingTab.getHeader().setActive(false);
            showingTab.getContent().setActive(false);
        }
        
        /*
         * Manually setting the tab active because apparently TabListItem.showTab(true)
         * can't be trusted to do its job since it won't actually fire the 
         * shown event handler
         */
        tab.getHeader().setActive(true);
        tab.getContent().setActive(true);
        
        showingTab = tab;
        
        onTabShown(tab.getDomainSessionId(), refresh);
    }

    @Override
    public void onMonitorStatusChanged(WebMonitorStatus status) {
        
        /* Refresh the currently showing tab, since it's likely that the web monitor service
         * was just registered for this session */
        onTabShown(showingTab.getDomainSessionId(), true);
    }
}
