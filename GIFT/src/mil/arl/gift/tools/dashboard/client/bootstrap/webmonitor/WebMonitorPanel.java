/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.AbstractWidget;
import mil.arl.gift.tools.dashboard.client.HistoryManager;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.message.WebMonitorMessagePanel;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.status.WebMonitorStatusPanel;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.WebMonitorStatusProvider;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.WebMonitorStatus;

/**
 * The base panel from which the web monitor UI is constructed. This contains all of the 
 * web monitor content and is designed to interact with the UI manager directly to display
 * the web monitor when appropriate.
 * 
 * @author nroberts
 */
public class WebMonitorPanel extends AbstractWidget {
    
    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(WebMonitorPanel.class.getName());

    private static WebMonitorPanelUiBinder uiBinder = GWT.create(WebMonitorPanelUiBinder.class);

    interface WebMonitorPanelUiBinder extends UiBinder<Widget, WebMonitorPanel> {
    }
    
    /** The deck used to switch between modes*/
    @UiField
    protected DeckLayoutPanel mainDeck;
    
    /** The base panel for the status mode */
    @UiField
    protected WebMonitorStatusPanel statusPanel;
    
    /** The base panel for the message mode */
    @UiField
    protected WebMonitorMessagePanel messagePanel;

    /**
     * Creates a new web monitor panel and prepares its core behavior. This mainly 
     * consists of adding handlers to start/stop interacting with the web monitor 
     * service when the page should be usable.
     * 
     * @param mode the mode that the web monitor panel should start in by default.
     * Cannot be null.
     */
    public WebMonitorPanel(MonitorMode mode) {
        initWidget(uiBinder.createAndBindUi(this));
        
        UiManager.getInstance().fillToBottomOfViewport(mainDeck);
        
        setMode(mode);
        
        /* Register to use the monitor service when this panel is attached and 
         * deregister when it is not */
        addAttachHandler(new AttachEvent.Handler() {
            
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if(!event.isAttached()) {
                    deregisterMonitorService();
                    
                } else {
                    registerMonitorService();
                }
            }
        });
    }
    
    /**
     * Set the mode that determines which set of web monitor components should
     * be displayed to the user
     * 
     * @param mode the mode whose UI components should be displayed.
     */
    public void setMode(MonitorMode mode) {
        
        switch(mode) {
            case STATUS:
                HistoryManager.getInstance().addHistory(HistoryManager.WEB_MONITOR_STATUS);
                mainDeck.showWidget(statusPanel);
                    
                    /* Refresh to sync the module statuses with the server */
                    UiManager.getInstance().getDashboardService().refreshModules(
                            UiManager.getInstance().getUserName(), 
                            UiManager.getInstance().getSessionId(),
                            new AsyncCallback<GenericRpcResponse<Void>>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    UiManager.getInstance().displayDetailedErrorDialog("Failed to Refresh Module Statuses", 
                                            "GIFT could not refresh the module statuses being displayed", 
                                            "An exception was thrown while refreshing the module statuses : " + caught, 
                                            null, null);  
                                }

                                @Override
                                public void onSuccess(GenericRpcResponse<Void> result) {
                                    if(!result.getWasSuccessful()) {
                                        UiManager.getInstance().displayDetailedErrorDialog("Failed to Refresh Module Statuses", 
                                                result.getException());
                                        
                                        return;
                                    }
                                }
                    });
                
                break;
                
            case MESSAGE:
                HistoryManager.getInstance().addHistory(HistoryManager.WEB_MONITOR_MESSAGE);
                mainDeck.showWidget(messagePanel);
                break;
                
            default:
                throw new IllegalArgumentException("Unable to switch to mode " + mode + "because there"
                        + "is no accompanying widget associated with it");
        }
    }

    /**
     * An enum describing all of the modes the web monitor can be displayed in. 
     * Swiching modes can be used to show/hide UI components related to specific
     * monitoring features (e.g.viewing status / viewing messages)
     * 
     * @author nroberts
     */
    public static enum MonitorMode{
        
        /** A mode used to view the status of GIFT's modules and manipulate said status */
        STATUS(ScreenEnum.WEB_MONITOR_STATUS),
        
        /** A mode used to view message traffic between GIFT's modules  */
        MESSAGE(ScreenEnum.WEB_MONITOR_MESSAGE);
        
        /** 
         * The screen associated with this mode. Used to sync the client's mode with
         * the user's current screen as tracked on the server
         */
        private ScreenEnum screen;
        
        /**
         * Creates a new web monitor mode that is synchronized with the given sceen
         * on the server
         * 
         * @param screen
         */
        private MonitorMode(ScreenEnum screen) {
            if(screen == null) {
                throw new IllegalArgumentException("A mode for the web monitor panel must provide a screen enum to track"
                        + "the user's position in the web monitor on the server");
            }
            
            this.screen = screen;
        }
        
        /**
         * Gets the server-tracked screen that is associated with this mode. This is used
         * to track the user's history as they change modes and return them to their current
         * mode if they are disconnected
         * 
         * @return the screen. Cannot be null.
         */
        public ScreenEnum getScreen() {
            return screen;
        }
    }
    
    /**
     * Registers this browser sessions to use the web monitor service
     * and to receive updates from it
     */
    private void registerMonitorService() {
        
        UiManager.getInstance().getDashboardService().registerMonitorService(
                UiManager.getInstance().getUserName(), 
                UiManager.getInstance().getSessionId(), 
                new AsyncCallback<GenericRpcResponse<WebMonitorStatus>>() {
                    
                    @Override
                    public void onSuccess(GenericRpcResponse<WebMonitorStatus> result) {
                        
                        if(!result.getWasSuccessful()) {
                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Start Monitoring", 
                                    result.getException());
                            
                            return;
                        }
                        
                        if(!WebMonitorPanel.this.isAttached()) {
                            
                            /* User already navigated away from the web monitor while their browser 
                             * session was registered, so need to deregister immediately*/
                            deregisterMonitorService();
                            
                        } else {
                            
                            /* Load the initial web monitor status that is returned after registering */
                            WebMonitorStatusProvider.getInstance().setMonitorStatus(result.getContent());
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        UiManager.getInstance().displayDetailedErrorDialog("Failed to Start Monitoring", 
                                "GIFT could not begin monitoring because an unexpected error occured", 
                                "An exception was thrown while deregisering this "
                                        + "browser instance for monitoring " + caught, 
                                null, null);
                    }
                });
    }
    
    /**
     * Deregisters this browser sessions from using the web monitor service
     * and stops receiving updates from it
     */
    private void deregisterMonitorService() {
        
        UiManager.getInstance().getDashboardService().deregisterMonitorService(
                UiManager.getInstance().getUserName(), 
                UiManager.getInstance().getSessionId(), 
                new AsyncCallback<GenericRpcResponse<Void>>() {
                    
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {
                        
                        if(!result.getWasSuccessful()) {
                            
                            /* Log to the error console instead of showing the user an error, since this is most
                             * likely to happen when the server has disconnected, which is a separate error */
                            logger.severe("GIFT could not end monitoring because an unexpected error occured." + result.getException());     
                            
                            return;
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        
                        /* Log to the error console instead of showing the user an error, since this is most
                         * likely to happen when the server has disconnected, which is a separate error */
                        logger.severe("GIFT could not end monitoring because an unexpected error occured." + caught);     
                    }
                });
    }
}
