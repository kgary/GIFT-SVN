/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.constants.Alignment;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.MobileApp;
import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.iframe.messages.ControlApplicationMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.shared.LessonStatusAction.LessonStatus;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.DisplayContentWidgetProperties;

/**
 * An extension of {@link AvatarContainer} to be used in the TUI's 
 * {@link mil.arl.gift.tutor.web.shared.WidgetTypeEnum#USER_ACTION_WIDGET user action widget}. This class provides some additional features
 * on top of the basic AvatarContainer logic, such as the ability to show and communicate with embedded web applications.
 * 
 * @author nroberts
 */
public class UserActionAvatarContainer extends AvatarContainer {
	
	/** The HTML used in the message shown while waiting for a web application to load */
	private static final String LOADING_MESSAGE_HTML = 
			"<div style='color: white; font-size: 24px; padding: 10px 20px; "
			+ 		"text-shadow: -1px -1px 0 black, 1px -1px 0 black, -1px 1px 0 black, 1px 1px 0 black;'>"
			+ 	"Loading..."
			+ "</div>";

	/** The logger for the UserActionAvatarContainer class */
	private static Logger logger = Logger.getLogger(UserActionAvatarContainer.class.getName());
	
	/** The URL used to clear out the iframe next to the avatar when no web application is being shown. */
	private static String EMPTY_WEB_APPLICATION_URL = "about:blank";
	
	/** 
	 * The static instance of this class. The TUI needs an instance of this class at all times, since the domain may need to 
	 * send initialization information before this widget is shown if it decides to show an embedded web application.
	 */
	private static UserActionAvatarContainer instance;
	
	/** The frame used to host embedded web applications */
	private Frame webApplicationFrame = new Frame();
	
	/** A message shown while waiting for a web application to load */
	private HTML loadingMessage = new HTML(LOADING_MESSAGE_HTML);
	
	/** A queue used to store messages sent to the embedded application before it is ready to process them */
	private Queue<String> appMessageQueue = new LinkedList<String>();
	
	/** Whether or not an embedded web application is currently loaded */
	private boolean webAppHasLoaded = false;
	
	/** Popup that is displayed while handling Siman Stop message */
	private ModalDialogBox simanStopPopup = new ModalDialogBox();
	
	/** 
	 * The lesson status of the current training application. If a lesson is not inactive, the state of this widget will be maintained 
	 * even while it is detached from the page, ensuring that the learner doesn't lose feedback messages or embedded app state.
	 */
	private LessonStatus lessonStatus = LessonStatus.INACTIVE;
	
	/** A deck panel used to switch between loading guidance messages and embedded web applications */
	private final DeckPanel embeddedPanel = new DeckPanel();
	
	/** The panel containing the frame used to host embedded web applications */
	private final FlowPanel embeddedAppPanel = new FlowPanel();
	
	/** A container widget used to embed frames for web-based training applications */
    protected LayoutPanel embeddedWidgetContainer = new LayoutPanel();
    
    /** A panel used to overlay a message over the right side of this widget while a training application is loading */
    protected SimpleLayoutPanel loadingOverlayPanel = new SimpleLayoutPanel();
    
    /** The widget instance used to create the widget currently being used as a loading message */
    protected WidgetInstance loadingWidgetInstance = null;
	
	/**
	 * A string defining the implementation to use when setting up the training application. So, far, the implementations
	 * supported include the following:
	 * 
	 *  URI - a training application embedded within an iframe, such as Unity
	 *  "generated.course.MobileApp" - the GIFT Mobile app
	 */
	private String applicationImpl;
	
	/**
	 * Creates a new instance of this class and readies its UI elements
	 */
	private UserActionAvatarContainer() {
		super();
		
		embeddedWidgetContainer.setSize("100%", "100%");
		
		//move the main avatar components to a panel on the left and use the remaining space to embed training apps like Unity
        splitPanel.remove(feedbackPanel);
        splitPanel.addWest(feedbackPanel, 417);
        splitPanel.add(embeddedWidgetContainer);
        
		
		embeddedPanel.setVisible(false);
		embeddedPanel.getElement().getStyle().setPosition(Position.FIXED);
		embeddedAppPanel.setSize("100%", "100%");
		
		//add a frame to hold web applications
		webApplicationFrame.setSize("100%", "100%");
		webApplicationFrame.getElement().getStyle().setBorderStyle(BorderStyle.NONE);
		
		embeddedAppPanel.add(webApplicationFrame);
		
		//add a loading message to the top-left corner of the embedded panel that appears while a web application is loading
		loadingMessage.setVisible(false);
		loadingMessage.getElement().getStyle().setPosition(Position.ABSOLUTE);
		loadingMessage.getElement().getStyle().setTop(0, Unit.PX);
		loadingMessage.getElement().getStyle().setLeft(0, Unit.PX);
		
		embeddedAppPanel.add(loadingMessage);
		
		embeddedPanel.add(embeddedAppPanel);
		embeddedPanel.showWidget(embeddedPanel.getWidgetIndex(embeddedAppPanel));
		
		/* 
		 * Attach the panel that will contain embedded web applications to the <body> element of the page instead of adding it to this widget.
		 * This ensures that the iframe containing the embedded app will remain attached to the page even if this widget is detached to display
		 * another article widget, which in turn prevents the embedded app from unloading until the training application scenario is complete.
		 * 
		 * This allows the embedded application to maintain its state even when other articles, such as mid lesson surveys, are shown during a 
		 * training app scenario so that the application can resume from where it left off once this widget is shown again.
		 * 
		 * To ensure that the embedded application still appears in the appropriate area of the screen allocated by this widget, the application's
		 * position is changed using 'position: fixed' so that it can overlay intended area and resize as it does.
		 */
		RootPanel.get().add(embeddedPanel);
		
		registerHandler(webApplicationFrame.getElement());
		
		//Constructs the Siman Stop Popup panel
		Heading text1 = new Heading(HeadingSize.H2, "The scenario has ended.");
		Heading text2 = new Heading(HeadingSize.H2, "GIFT is now waiting on the training application to finish closing.");
		
		BsLoadingIcon loadIcon = new BsLoadingIcon();
		loadIcon.setType(IconType.SPINNER);
		loadIcon.setSize(IconSize.TIMES5);
		loadIcon.startLoading();
		
		Paragraph paragraph = new Paragraph();
		paragraph.setAlignment(Alignment.CENTER);
		paragraph.getElement().getStyle().setPadding(8, Unit.PX);
		paragraph.add(text1);
		paragraph.add(text2);
		paragraph.add(loadIcon);
		
		simanStopPopup.add(paragraph);
		simanStopPopup.setGlassEnabled(true);
		simanStopPopup.setText("Embedded Application Closing");
		simanStopPopup.setWidget(paragraph);
		simanStopPopup.setGlassEnabled(true);
		simanStopPopup.setModal(true);
		
		//Removes the popup when the widget is no longer attached
		addAttachHandler(new Handler() {

            @Override
            public void onAttachOrDetach(AttachEvent event) {
            	
            	boolean attached = event.isAttached();
            	
                if(attached) {
                    
                    //wait for layout operations in GWT's event loop to finish before positioning the app
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            
                            //wait for CSS animations and sizing to be applied before positioning the app
                            AnimationScheduler.get().requestAnimationFrame(new AnimationCallback() {
                                
                                @Override
                                public void execute(double timestamp) {
                                    repositionWebApp();
                                }
                            });
                        }
                    });
                	
                } else {
                	
                    simanStopPopup.hide();
                    
                    if(lessonStatus.isLessonInactive()){
                    	
                    	//if the web app has stopped, unload it when this widget is detached
                    	unloadWebApplication();
                    }
                }
                
                embeddedPanel.setVisible(event.isAttached());
            }
		    
		});
		
		SimpleLayoutPanel resizePanel = new SimpleLayoutPanel(){
			
			@Override
			public void onResize() {
				super.onResize();
				
				if(embeddedPanel.isVisible()){
					
					//update the position of the embedded app whenever its allocated space changes size
					repositionWebApp();
				}
			}
		};
		
		Window.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				
				if(embeddedPanel.isVisible()){
					
					//update the position of the embedded app whenever the viewport changes size
					repositionWebApp();
				}
			}
		});
		
		embeddedWidgetContainer.add(resizePanel);
	}
	
	/**
     * Loads the web application at the given URL into an iframe next to the avatar. If the URL is null or empty, the iframe
     * will be cleared of its content.
     * 
     * @param applicationUrl
     */
    public void loadWebAppliation(String applicationUrl){
        
        applicationImpl = applicationUrl;
        
        if(loadingWidgetInstance == null 
                || DisplayContentWidgetProperties.getDisplayDuration(loadingWidgetInstance.getWidgetProperties()) == 0) {
            
            // if the displayed overlay widget isn't a guidance message with a fixed duration, hide it when a web application is loaded,
            // otherwise it may be stuck on the screen while the web application loads
            setLoadingOverlay(null, null);
        }
        
        if(MobileApp.class.getName().equals(applicationImpl)) {
            
            //initialize the mobile app wrapping this window this widget is part of
            webAppHasLoaded = true;
            
            return;
            
        } else {
        
            //initialize an embedded application within this widget's iframe
        	webAppHasLoaded = false;
        	if(applicationImpl != null && !applicationImpl.isEmpty()){
        		webApplicationFrame.setUrl(applicationImpl);
        		
        	} else {
        		webApplicationFrame.setUrl(EMPTY_WEB_APPLICATION_URL);
        	}
        }
    }
    
    /**
     * Unloads the web application currently loaded into the iframe next to the avatar.
     */
    public void unloadWebApplication(){
    	
    	//loading an empty web application will unload the previous one
    	loadWebAppliation(null);
    }

    /**
     * Gets the singleton instance of this class
     * 
     * @return the singleton instance
     */
    public static UserActionAvatarContainer getInstance(){
    	
    	if(instance == null){
    		instance = new UserActionAvatarContainer();
    	}
    	
    	return instance;
    }
    
    /**
     * Registers a message handler on the current window to listen for messages from embedded training applications
     * 
     * @param embeddedAppFrame the Iframe containing the training applications where messages should come from
     */
    private native void registerHandler(Element embeddedAppFrame) /*-{
		var that = this;
		$wnd.addEventListener("message", function(event) {
		
		    if(event.source === embeddedAppFrame.contentWindow){
		    
		        //only process messages that come from the Iframe that embeds the training application
			    that.@mil.arl.gift.tutor.client.widgets.UserActionAvatarContainer::handleMessageFromWebApplication(Ljava/lang/String;)(event.data);
		    }
		});
	}-*/;
	
    /**
     * Handles an outgoing embedded training application message so it can be sent to the Tutor server. This method also handles the
     * initial handshake sent from the embedded application and sends any messages currently queued up for the embedded application
     * upon receiving said handshake.
     * 
     * @param message the message from the embedded training application. If null or empty, the message will still be handled 
     * based on if the training application has loaded or if zero errors occured while starting. If the message starts with '!', 
     * the embedded application returned an error while initializing which will trigger course end. Text following '!' represents 
     * the details of the error, dependent on the training application. Lastly, if the message is not null, empty, or without a '!' 
     * at the beginning, then the message is valid and will be passed to the server to be handled accordingly.
     */
	private void handleMessageFromWebApplication(String message) {
	
	    /* If the message received is blank, log a WARNING statement to the console to inform the user of
	     * the blank message. */
	    if (StringUtils.isBlank(message)) {
	        String addlMsg = !webAppHasLoaded 
	                ? "If running Unity Web Application, the embedded application may still be in the process of initializing."
                    : "Ensure that the embedded application is sending messages with the desired contents.";        
	        logger.warning("Received empty message from the embedded application. " + addlMsg);
	    }
	    
	    /* Handle the case where the embedded application returns an error. Additionally, safeguard against messages that are shorter 
	       than one character. */
	    if(message.length() > 1 && message.charAt(0) == '!') {
	        Document.getInstance().displayErrorDialog(
	                "Embedded Application Error", 
	                "The embedded application encountered an error which requires the course to end. "
	                        + "Send this message to the course author for further steps to resolve the issue.", 
	                message.substring(1));
	        return;
	    }
	    
	    //If the siman stop response has come back hide the siman stop popup
	    if(simanStopPopup.isShowing() && isSimanStopResponseMessage(message)) {
	        simanStopPopup.hide();
	    }
	    
		//If this is the first message, transmit all the queued messages to the web app
		//Otherwise pass the message to the server
		if(!webAppHasLoaded) {
			
			loadingMessage.setVisible(false); //remove the loading message, since the handshake was made
			
			webAppHasLoaded = true;
			
			//send all of the messages that were received while the web application was loading in FIFO order
			while(!appMessageQueue.isEmpty()){
				sendMessageToWebApplication(appMessageQueue.poll());
			}
			
		} else {
			
			//Need to wrap the raw message into a new string created by GWT Java code, since strings created
			//from native JavaScript can't be sent over RPC communications. This is because the strings created
			//by JavaScript aren't actually proper Java strings; they just get cast as Strings by GWT and, as such,
			//only support the subset of the String API needed for client-side code. This prevents them from being
			//serialized properly for RPCs, since they won't translate to proper Java strings on the server end. 
			//Using GWT Java code to create a string avoids this problem by creating a proper Java string that 
			//can be sent over RPCs just fine.
			StringBuilder newMessage = new StringBuilder(message);
			
			BrowserSession.getInstance().sendEmbeddedAppState(newMessage.toString());
		}
	}
	
	/**
	 * Sends a message to the embedded training application. If the embedded application is not yet ready to process messages, then 
	 * the message will be queued for sending later.
	 * 
	 * @param message the message to send
	 */
	public void sendMessageToWebApplication(String message) {
		
		if(webAppHasLoaded) {
		    
		    if(isSimanStopMessage(message)) {
		        simanStopPopup.center();
		    }
		    
		    if(MobileApp.class.getName().equals(applicationImpl)) {
		    
    		    //send a control message to the GIFT Mobile App
		        IFrameMessageHandlerChild.getInstance().sendMessage(new ControlApplicationMessage(message));
		    
		    } else {
		        
		        //send a message to the app embedded within the iframe
		        sendMessageNative(message, instance.webApplicationFrame.getElement());
		    }
			
		} else {
			
			if(!loadingMessage.isVisible()){
				
				//show a loading message until the initial handshake occurs
				loadingMessage.setVisible(true);
			}
			
			appMessageQueue.add(message);
		}
	}
	
	/**
	 * Natively sends the given message to the given iframe using Window.postMessage().
	 * 
	 * @param message the message to send
	 * @param iframe the iframe to send the message to
	 */
	private native void sendMessageNative(String message, Element iframe)/*-{
	    iframe.contentWindow.postMessage(message, "*");
	}-*/;
	
	/**
	 * Determines if a JSON string is a Siman Stop message
	 * @param message The message, as JSON, to be tested
	 * @return true if the message is a Siman Stop message, false otherwise
	 */
	private native boolean isSimanStopMessage(String message) /*-{
	    //Parse the JSON as a JavaScript object
	    try {
	        var msg = JSON.parse(message);
    	    var payload = JSON.parse(msg.payload);
    	    //Check if Siman_Type is "Stop"
    	    if(payload.Siman_Type === "Stop") {
    	        return true;
    	    } else {
    	        return false;
    	    }
	    } catch(err) {
	        return false;
	    }
	    
	}-*/;
	
	/**
	 * Determines if a JSON string is a response to a Siman Stop message
	 * @param message the message, as JSON, to be tested
	 * @return true if the message is a response to a Siman Stop message, false otherwise
	 */
	private native boolean isSimanStopResponseMessage(String message) /*-{
	    try {
	        var msg = JSON.parse(message);
	        
	        //Check if message is a SimanResponse
	        if(msg.type === "SimanResponse" && msg.payload === "Stop") {
	            return true;
	        } else {
	            return false;
	        }
	    } catch(err) {
	        return false;
	    }
	}-*/;
	
	/**
	 * Sets what lesson status the current training application is in and updates this widget accordingly.
	 * 
	 * @param status the training application lesson status
	 */
	public void setLessonStatus(LessonStatus status){
		
		lessonStatus = status;
		
		if(LessonStatus.INITIALIZING.equals(lessonStatus)) {
            
		    //show an overlay where loading guidance messages can be displayed
		    embeddedPanel.add(loadingOverlayPanel);
		    embeddedPanel.showWidget(embeddedPanel.getWidgetIndex(loadingOverlayPanel));

		} else if(LessonStatus.ACTIVE.equals(lessonStatus)) {
		    
		    if(loadingWidgetInstance == null 
		            || DisplayContentWidgetProperties.getDisplayDuration(loadingWidgetInstance.getWidgetProperties()) == 0) {
		        
		        //if the displayed overlay widget isn't a guidance message with a fixed duration, hide it when the lesson becomes active
	            setLoadingOverlay(null, null);
		    }
		    
		} else {
		    
		    if(!isAttached()){
	            
	            //unload the iframe used for embedded apps if this widget is detached and no training app lesson is active
	            unloadWebApplication();
	            
	            if(getContainerWidget() instanceof TutorActionsWidget){
	                
	                //unload this widget's feedback messages if this widget is detached and no training app lesson is active
	                ((TutorActionsWidget) getContainerWidget()).clearFeedback();
	            }
	        }
		}
	}
	
	/**
	 * Gets the lesson status of the current training application
	 * 
	 * @return the lesson status of the current training application
	 */
	public LessonStatus getLessonStatus(){
		return lessonStatus;
	}
	
	/** 
	 * Repositions and resize the embedded application so it overlays the screen space allocated for it by this widget
	 */
	private void repositionWebApp() {
	    embeddedPanel.getElement().getStyle().setTop(embeddedWidgetContainer.getElement().getAbsoluteTop(), Unit.PX);
        embeddedPanel.getElement().getStyle().setLeft(embeddedWidgetContainer.getElement().getAbsoluteLeft(), Unit.PX);
        embeddedPanel.getElement().getStyle().setHeight(embeddedWidgetContainer.getOffsetHeight(), Unit.PX);
        embeddedPanel.getElement().getStyle().setWidth(embeddedWidgetContainer.getOffsetWidth(), Unit.PX);
	}
	
	@Override
	public void onResize() {
	    super.onResize();
	}
	
	/**
	 * Sets the widget that should be shown while a training application is loading. This widget will automatically be
	 * hidden and detached once the training application has finished loading.
	 * <br/><br/>
	 * If the provided widget is null, then the current loading widget will be immediately hidden.
	 * 
	 * @param instance the widget instance containing the properties of the widget to show
	 * @param widget the widget to show
	 */
	public void setLoadingOverlay(WidgetInstance instance, Widget widget) {
	    
	    if(widget != null) {
	        
	        loadingOverlayPanel.setWidget(widget);
	        loadingWidgetInstance = instance;
	        
	    } else {
	        
	        //hide the overlay for loading guidance messages
	        embeddedPanel.showWidget(embeddedPanel.getWidgetIndex(embeddedAppPanel));
            loadingOverlayPanel.clear();
            loadingOverlayPanel.removeFromParent();
            
            loadingWidgetInstance = null;
	    }
	}
	
	/**
	 * Gets the widget instance containing the properties of the widget that will be used as a loading message while a training
	 * is loading
	 * 
	 * @return the widget instance of the loading message widget
	 */
	public WidgetInstance getLoadingOverlay() {
	    return loadingWidgetInstance;
	}
	
	/**
	 * Gets the widget that will be used as a loading message while a training app is loading
	 * 
	 * @return the loading message widget
	 */
	public Widget getLoadingOverlayWidget() {
        return loadingOverlayPanel.getWidget();
    }
}
