/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;



import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.gwt.client.IFrameMessageHandlerParent;
import mil.arl.gift.common.gwt.client.IFrameMessageListener;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;




/**
 * Bootstrap widget used as the subpanel for the my tools section of the dashboard.  
 * Currently this is a sample showing how we might embed some of the authoring tools.
 * Eventually this may have a subheader which manages what tool the user is selecting.
 *
 * @author nblomberg
 */
public class BsMyToolsWidget extends AbstractBsWidget {

    private static Logger logger = Logger.getLogger(BsMyToolsWidget.class.getName());
    
    /* This trailing slash is important for the /gat & /sas urls in the iframe.
     * The urls /gat becomes /gat/
     *          /sas becomes /sas/
     * This is needed when using an apache proxy (server configuration) so that the jetty
     * server returns a 200 (found) instead of a redirect (302), which is what we were seeing.
    */
    private static final String TRAILING_SLASH = "/";
    
    /** must match {@link #mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility.USER_NAME_URL_PARAM} */
    private static final String USERNAME_GAT_URL_PARAM = "userName";
    
    /** must match {@link #mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility.DEBUG_URL_PARAM} */
    private final static String DEBUG_FLAG = "debug";
    
    /** must match {@link #mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility.BROWSER_SESSION_ID_PARAM} */
    private static final String BROWSER_SESSION_ID_URL_PARAM = "browserSessionId";
    
    /** 
     * Separator character that is used to separate each name/value pair 
     * must match {@link #mil.arl.gift.tools.authoring.server.gat.client.util.PlaceParamParser.TOKEN_SEPARATOR} 
     **/
    private static final String TOKEN_SEPARATOR = "|";
    /** 
     * Separator character that is used to separate a single name/value pair 
     * must match {@link #mil.arl.gift.tools.authoring.server.gat.client.util.PlaceParamParser.PARAM_SEPARATOR} 
     **/
    private static final String PARAM_SEPARATOR = "=";
    
    private static final String CREATE_COURSE_URL_PARAM = "createCourse=true";
    private static final String FILE_PATH_URL_PARAM = "filePath";
    private static final String DEPLOYMENT_MODE_URL_PARAM = "deployMode";
    
    /** The amount of milliseconds it takes for the closing fade animation on a dialog to complete*/
    private static final int DIALOG_FADE_ANIMATION_DURATION = 200;
    
    private static BootstrapMyToolsWidgetUiBinder uiBinder = GWT.create(BootstrapMyToolsWidgetUiBinder.class);
    
    // Base url of the gat host (from the dashboard.properties file.  Should be in the form of 'http://localhost:8080')
    private String gatHostUrl = "";
    
    @UiField
    Container ctrlContents;
    
    @UiField
    Container ctrlLoadPanel;
    
    @UiField
    BsLoadingIcon ctrlLoadIcon;
        
    @UiField
    Paragraph iFrameCtrl;
    
    @UiField
    SimplePanel frameContainer;
    
    @UiField
    DeckPanel toolContainerDeck;
    
    /* Represents whether or not there are files open in the gat that are not read-only. Used to determine if a browser alert should be displayed
     * when the browser is closed. */
    private boolean gatHasOpenFiles = false;
    
    interface BootstrapMyToolsWidgetUiBinder extends UiBinder<Widget, BsMyToolsWidget> {
    }
    
    // This is the token that the gat expects when passing in a course id.  If the course id is an empty string, then
    // the gat will assume it is a new course.
    private final String GAT_DASHBOARD_TOKEN = "#CoursePlace:";
    
    private final String LEAVEGAT_TITLE = "Leave Course Authoring Tool";
    private final String LEAVEGAT_MESSAGE = "Are you sure you want to leave the course authoring tool? <br><br>"+
            "If you have any unsaved changes to your course, please remember to save them before leaving.";
    
    private final String LEAVEGAT_CONFIRM_LABEL = "Leave Page";
    private final String LEAVEGAT_DECLINE_LABEL = "Stay On Page";
    
    private IFrameMessageListener gatListener = null;
    

    /**
     * Constructor
     */
    public BsMyToolsWidget() {
    	
        
        initWidget(uiBinder.createAndBindUi(this));       
        
        initServerProperties();
        
        //dynamically fill tool panels to the bottom of the viewable area
        UiManager.getInstance().fillToBottomOfViewport(frameContainer);
        
    	//initialize GAT frame and
        //default to the new course GAT experience
        ctrlLoadIcon.startLoading();
        ctrlLoadPanel.setVisible(true);
        ctrlContents.getElement().getStyle().setProperty("visibility", "hidden");
        
        listenForGatFileStatus();
        
        logger.fine("Gat iframe url is: " + gatHostUrl);
    }
    
    
    /**
     * Accessor to update the my tools panel based on course data (DomainOption) 
     * that is sent from the dashboard.  
     * 
     * @param data - If the data is an instance of DomainOption, then the panel will be updated to show the gat at a specified domain id.  
     *               If null a new course is created.  Otherwise a warning is logged mentioning the unhandled data type.  
     */
    public void updateMyToolsPanel(Object data) {
       
    	String deploymentModeToken = TOKEN_SEPARATOR + DEPLOYMENT_MODE_URL_PARAM + PARAM_SEPARATOR + 
    	        Dashboard.getInstance().getServerProperties().getDeploymentMode();

    	if (data != null && data instanceof DomainOption) {
    		DomainOption domainData = (DomainOption)data;
    		String path = domainData.getDomainId();
    		if(path.startsWith(TRAILING_SLASH)) {
    			path = path.substring(1);
    		}
    		String gatUrl= gatHostUrl + GAT_DASHBOARD_TOKEN + FILE_PATH_URL_PARAM + PARAM_SEPARATOR + URL.encodeQueryString(path) + deploymentModeToken;
    		updateAndShowGatFrame(gatUrl);

    	}else{
        	String gatUrl= gatHostUrl + GAT_DASHBOARD_TOKEN + CREATE_COURSE_URL_PARAM + deploymentModeToken;
            updateAndShowGatFrame(gatUrl);                        
        }
    }
    
    /**
     * Initializes the server properties that are used by this widget.
     */
    private void initServerProperties() {
        ServerProperties props = Dashboard.getInstance().getServerProperties();
        
        if (props != null) {
            
            /* Need to make sure that the GAT's host address matches the host of the Dashboard, otherwise it will be 
             * treated as a different host even if the actual address itself points to the same machine */
            gatHostUrl = GWT.getHostPageBaseURL() + "GiftAuthoringTool.html";
            gatHostUrl += "?" + USERNAME_GAT_URL_PARAM + PARAM_SEPARATOR + 
                    UiManager.getInstance().getUserName() + Constants.AND + BROWSER_SESSION_ID_URL_PARAM + PARAM_SEPARATOR + 
                    UiManager.getInstance().getSessionId() + Constants.AND + DEBUG_FLAG + PARAM_SEPARATOR +
                    UiManager.getInstance().isDebugMode();
        }
    }
    
    /**
     * Updates and shows the gat frame based on a specified url.
     * 
     * @param url - The full url to the gat.  Should not be null or empty string.
     */
    private void updateAndShowGatFrame(String url) {
        
        logger.fine("Updating gat url: " + url);
        final Frame gatFrame = new Frame(url);
        gatFrame.setWidth("100%");
        gatFrame.setHeight("100%");
        gatFrame.getElement().getStyle().setProperty("border", "none");
        gatFrame.getElement().getStyle().setProperty("display", "block");
        gatFrame.getElement().setAttribute("allow", "autoplay");
        gatFrame.getElement().setAttribute("allowFullScreen", "true");
        gatFrame.getElement().setId(UiManager.getInstance().getGatFrameId());
        
        toolContainerDeck.showWidget(toolContainerDeck.getWidgetIndex(frameContainer));
        frameContainer.setWidget(gatFrame);
        iFrameCtrl.setVisible(true);   
    }

    /**
     * Adds a message listener to keep track of whether or not editable files are open in the GAT.
     */
    private void listenForGatFileStatus() {
    	
    	if(gatListener != null) {
    		IFrameMessageHandlerParent.getInstance().removeMessageListener(gatListener);
    	}
    	
    	gatListener = new IFrameMessageListener() {

    		@Override
    		public boolean handleMessage(AbstractIFrameMessage message) {

    			logger.fine("receiving message: " + message.getMsgType());

    			if(message instanceof IFrameSimpleMessage){

    				IFrameMessageType type = message.getMsgType();

    				if(type != null && type.equals(IFrameMessageType.GAT_LOADED)){
    						
    					ctrlLoadIcon.stopLoading();
    			        ctrlLoadPanel.setVisible(false);
    			        ctrlContents.getElement().getStyle().setProperty("visibility", "visible");
    					
    				} else if(type != null && type.equals(IFrameMessageType.GAT_FILES_OPEN)){
    					gatHasOpenFiles = true;
    					
    				} else if(type != null && type.equals(IFrameMessageType.GAT_FILES_CLOSED)){
    					gatHasOpenFiles = false;
    					
    				} else if (type != null && type.equals(IFrameMessageType.GO_TO_DASHBOARD)){
    					
    				    UiManager.getInstance().displayScreen(ScreenEnum.MYCOURSES);
    				    
    				} else if (type != null && type.equals(IFrameMessageType.WRAP_OPEN)){
    				    UiManager.getInstance().lockNavTabs();
    				    
    				} else if (type != null && type.equals(IFrameMessageType.WRAP_CLOSED)){
    				    UiManager.getInstance().unlockNavTabs();
    				    
    				}
    			}

    			return false;
    		}
    	};

    	IFrameMessageHandlerParent.getInstance().addMessageListener(gatListener);
    }
    
    /**
     * Function to determine if an authoring tool is visible (based on the tool url).
     * 
     * @param toolUrl - The url of the tool to check (cannot be null).
     * @return - true if the authoring tool is visible/active.  False otherwise.
     */
    private boolean isAuthoringToolActive(String toolUrl) {
        boolean isActive = false;
        
        Widget widget = frameContainer.getWidget();
        if (widget instanceof Frame) {
            Frame frame = (Frame)widget;
            
            if (frameContainer.isVisible() && frame.getUrl().contains(toolUrl)) {
                isActive = true;
            }
        }
        return isActive;
    }
    
    /**
     * Public function to allow external widgets to check if the gat authoring tool
     * is active (visible) and has files open. 
     * 
     * @return true if the gat authoring tool is visible/active and has open files that are not read-only.  False otherwise.
     */
    public boolean isGatAuthoringToolActive() {
    	
        return isAuthoringToolActive(gatHostUrl) && gatHasOpenFiles;
    }

    /**
     * Shows the leave gat dialog.  If the user confirms to leave, then 
     * the user will be taken to the screenId that is specified.
     * 
     * @param screenId - The screen id that the user will be taken to if the confirm option is selected. Cannot not be null.
     */
    public void showLeaveGatDialog(final ScreenEnum screenId) {   
    	showLeaveGatDialog(screenId, -1, null);
    }

    /**
     * Shows the leave gat dialog.  If the user confirms to leave, then the user will be taken 
     * to the screenId, widget index, or frame that is specified.
     * 
     * @param screenId - The screen id that the user will be taken to if the confirm option is selected.
     * @param widgetIndex - The widget index that the user will be taken to if the confirm option is selected.
     * @param frame - The iframe to display if the confirm option is selected.
     */
    public void showLeaveGatDialog(final ScreenEnum screenId, final int widgetIndex, final Frame frame) {   
        
        final ConfirmationDialogCallback backButtonCallback = new ConfirmationDialogCallback() {

            @Override
            public void onDecline() {
                listenForGatFileStatus();
            }

            @Override
            public void onAccept() {
            	
            	Timer timer = new Timer(){

					@Override
					public void run() {
						
						if(screenId == null) {

							if(frame != null) {
								toolContainerDeck.showWidget(toolContainerDeck.getWidgetIndex(frameContainer));
								frameContainer.setWidget(frame);
								iFrameCtrl.setVisible(true);

							} else {
								toolContainerDeck.showWidget(widgetIndex);
							}

						} else {
							UiManager.getInstance().displayScreen(screenId);
						}
					}
					
				};
				
				/*
				 * Nick: In order to keep the animation for closing the dialog from becoming choppy, we need to 
				 * delay unloading the DOM until the animation completes, which takes about 0.2s. 
				 * 
				 * Note that if the duration on Boostrap's 'fade' style class is changed to a higher value, then 
				 * this value will also need to be changed accordingly to prevent the choppy animation
				 */
				timer.schedule(DIALOG_FADE_ANIMATION_DURATION);
            }
            
        };
        
        if(gatHasOpenFiles) {
			UiManager.getInstance().displayConfirmDialog(LEAVEGAT_TITLE, LEAVEGAT_MESSAGE, LEAVEGAT_CONFIRM_LABEL, LEAVEGAT_DECLINE_LABEL, backButtonCallback);
		}
		
    }
    
    /*
	 * Nick: This method exists specifically to address a longstanding issue with all of our frame elements and
	 * Internet Explorer 9, 10, and 11. The gist of the problem is that IE doesn't properly clean up a frame's elements after it is 
	 * removed from the DOM. Because of this, if the user edits a field in a frame and then removes the frame from the DOM, IE's 
	 * focus stays on the text box they edited, preventing ANY other text fields throughout the entire interface from being edited. 
	 * 
	 * To prevent this from happening, I'm using a 2 step approach:
	 * 1) Remove the frame's inner HTML from the DOM
	 * 		- This causes the DOM to remove the textbox the user was editing
	 * 2) Change the frame's URL so that it's current DOM is unloaded
	 * 		- This ensures that the frame's old DOM doesn't keep have the page's focus
	 * 
	 * All of these steps together seem to handle all cases of this problem occurring.
	 * 
	 * For more info on this problem, check out 
	 * http://stackoverflow.com/questions/19150008/ie-9-and-ie-10-cannot-enter-text-into-input-text-boxes-from-time-to-time
	 */
    private void unlockFrameTextBoxes(){
    	
    	Widget widget = frameContainer.getWidget();
    	
        if (widget != null && widget instanceof Frame) {
        	
            Frame frame = (Frame)widget;
            
            String url = frame.getUrl();
            
            //need to reset URL and inner HTML
            frame.getElement().setInnerHTML("");
    		frame.setUrl("about:blank");
    		
    		frame.setUrl(url);
        }
    }
    
    @Override
    public void onPreDetach() {
    	unlockFrameTextBoxes();
    }
}