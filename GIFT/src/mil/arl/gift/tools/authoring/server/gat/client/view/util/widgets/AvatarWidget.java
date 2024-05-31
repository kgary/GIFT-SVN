/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets;

import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.FetchCharacterServerStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalHeader;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;

import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * This widget presents the default avatar.
 * 
 * @author bzahid
 */
public class AvatarWidget extends Composite {
    
    /** logger instance */
    private static Logger logger = Logger.getLogger(AvatarWidget.class.getName());

    /** The URL that will be used to load the avatar */
    private static String avatarUrl = null;
	
	private HTML avatarContainer = new HTML();
	
	/** whether the character is ready to speak */
	private static boolean isIdle = true;
	
	/** whether the character server is running (and ready) */
	private static boolean isAvatarOnline = false;
	
	/** whether this widget is attached or not */
	private boolean attached = false;
	
	/** whether the character has finished be loaded */
	private boolean finishedLoadingCharacter = false;
	
	private static AvatarIdleCallback idleCallback = null;
	
	/** Whether or not the learner's browser is capable of rendering the current avatar and playing its audio */
    private static boolean browserSupportsAvatar = true;
    
    /** A dialog used to notify the learner if their browser does not support the current avatar */
    private Modal compatibilityWarningDialog = new Modal();
    
	/**
	 * Callback interface for executing logic when the avatar idle notification is received 
	 */
	public static interface AvatarIdleCallback {
		public void onIdle();
	}
	
	/**
	 * Initializes a new default avatar.
	 */
	public AvatarWidget() {
	    
	    FlowPanel mainPanel = new FlowPanel();
	    mainPanel.add(avatarContainer);
	    
	    //initialize the dialog that warns the learner about browser compatibility issues
        HTML compatibilityWarning = new HTML(
            "The authored character is not supported by your browser.<br/><br/>"
            + "To show this character, please run this course again using Google Chrome, Mozilla Firefox, or Microsoft Edge instead."
        );
        
        ModalHeader compatibilityHeader = new ModalHeader();
        compatibilityHeader.setTitle("Incompatible Browser");
        
        ModalBody compatibilityBody = new ModalBody();
        compatibilityBody.add(compatibilityWarning);
        
        Button okButton = new Button("OK", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
               compatibilityWarningDialog.hide();
            }
        });
        okButton.setType(ButtonType.PRIMARY);
        
        ModalFooter compatibilityFooter = new ModalFooter();
        compatibilityFooter.add(okButton);
        
        compatibilityWarningDialog.add(compatibilityHeader);
        compatibilityWarningDialog.add(compatibilityBody);
        compatibilityWarningDialog.add(compatibilityFooter);
        compatibilityWarningDialog.setClosable(false);
        compatibilityWarningDialog.setDataBackdrop(ModalBackdrop.STATIC);
        compatibilityWarningDialog.setFade(true);
        compatibilityWarningDialog.getElement().getStyle().setTextAlign(TextAlign.LEFT);
        
        mainPanel.add(compatibilityWarningDialog);
	    
		initWidget(mainPanel);
		
		exposeNativeFunctions();
		defineEmptySayFeedback();
		
		avatarContainer.getElement().getStyle().setProperty("marginLeft", "auto");
		avatarContainer.getElement().getStyle().setProperty("marginRight", "auto");
		
		addAttachHandler(new AttachEvent.Handler() {
            
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                
                if(event.isAttached()) {                    
                    attached = true;
                    
                    if(!finishedLoadingCharacter){
                        // the character has not been added to the HTML yet, attempt to do so now
                        finishLoadingCharacter();
                    }
                }
            }
        });
		
		SharedResources.getInstance().getDispatchService().execute(new FetchCharacterServerStatus(), new AsyncCallback<GatServiceResult>() {

			@Override
			public void onFailure(Throwable thrown) {			    
			    logger.log(Level.INFO, "There was a problem retrieving the character server status from the server.  The avatar will be set as offline.", thrown);
				isAvatarOnline = false;
			}

			@Override
			public void onSuccess(GatServiceResult result) {
				logger.info("The server reported the character server is " + (result.isSuccess() ? "" : "NOT") +" online.");
			    isAvatarOnline = result.isSuccess();
			    if(isAvatarOnline){
			        if(attached){
			            finishLoadingCharacter();
			        }
			    }else {
				    // remove the avatar
				    avatarContainer.removeFromParent();
				}
			}
			
		});
	}
	
	/**
	 * Attempt to add the character panel to the HTML
	 */
	private synchronized void finishLoadingCharacter(){
	    
	    logger.info("Attempting to finish loading character");
	    	    
        if(avatarUrl == null && isAvatarOnline) {
            // the character has not been added and the character server is online
            
            finishedLoadingCharacter = true;

            //initialize the avatar URL only when this widget is attached, since the server properties will be loaded by then
            avatarUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.TUI_URL) 
                    + "/" + GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.DEFAULT_CHARACTER);
            
            setAvatarData(new AvatarData(avatarUrl, 200, 250));
        }
    
    
        if(!browserSupportsAvatar) {
            
            //notify the server that the avatar is idle, since it isn't supported
            idleAvatarNotification();
            
            //notify the learner that their browser doesn't support the avatar
            compatibilityWarningDialog.show();
        }
	}
	
	
	/**
	 * Sets the avatar data to display.
	 * 
	 * @param avatar The avatar display properties
	 */
	public void setAvatarData(AvatarData avatar) {
	    
	    logger.info("Setting avatar to "+avatar);
	    
	    //check to see if the avatar uses Unity and the browser supports it
        checkUnityAvatarCompatibility(avatar.getURL());
	    
		MediaHtml avatarHtml = generateAvatar(avatar.getURL(), avatar.getWidth() + "px", avatar.getHeight() + "px");
		avatarContainer.setWidth(avatarHtml.getWidth());
		avatarContainer.setHeight(avatarHtml.getHeight());
		avatarContainer.setHTML(avatarHtml.getHtml());
	}
	
	/**
	 * Generates the avatar HTML to display.
	 * 
	 * @param avatarToUse The avatar source to use.
	 * @param avatarWidth The width of the avatar.
	 * @param avatarHeight The height of the avatar
	 * @return The avatar HTML
	 */
    private static MediaHtml generateAvatar(String avatarToUse, String avatarWidth, String avatarHeight) {

        StringBuilder avatarHtml = new StringBuilder();
        
        avatarHtml.append("<iframe src=\"").append(avatarToUse).append("\" ");
        avatarHtml.append("id=\"").append("avatarContainer").append("\" ");
        avatarHtml.append("scrolling=\"").append("no").append("\" ");
        avatarHtml.append("frameborder=\"").append(0).append("\" ");
        avatarHtml.append("width=\"").append(avatarWidth).append("\" ");
        avatarHtml.append("height=\"").append(avatarHeight).append("\" ");
        avatarHtml.append("onload=\"").append("sayFeedback();").append("\" ");
        avatarHtml.append("allow=\"").append("autoplay").append("\" ");
        avatarHtml.append("></iframe>");
        
        return new MediaHtml("avatar", avatarHtml.toString(), avatarWidth, avatarHeight);
    }
    
    /**
     * Defines the sayNewFeedback function to do nothing
     */
    public static native void defineEmptySayFeedback() /*-{ 
    	
    	var baseWindow = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();
    	
        baseWindow.sayFeedback = function() { }
    }-*/;

    /**
     * Calls the speak method for the avatar
     * 
     * @param newFeedback The feedback to say.
     */
    public static native void sayFeedback(String newFeedback) /*-{
    	
    	var that = this;
    	
    	var baseWindow = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();
    	
	    baseWindow.sayNewFeedback = function() {
                
            if(@mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.AvatarWidget::browserSupportsAvatar){
        
                if(newFeedback != null &&
                    baseWindow.document.getElementById('avatarContainer') != null){
                    baseWindow.document.getElementById('avatarContainer').contentWindow.postMessage(JSON.stringify({method:"msSpeak",key:newFeedback}),"*");
                }
                
            } else {
            
                //if the browser doesn't support the avatar, always treat the avatar as idle
                baseWindow.notifyGIFT();
            }
        }
		
		@mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.AvatarWidget::busyAvatarNotification()();
		
		baseWindow.sayNewFeedback();
    }-*/;

    /**
     * Remove feedback of unparsable text before sending to the speech engine
     * 
     * Removes HTML tags and text in parentheses.
     *
     * @param feedback The feedback to clean up
     * @return String The speech engine parsable text
     */
    public static String stripFeedback(String feedback) {
        String newFeedback = feedback;
        newFeedback = newFeedback.replaceAll("<(.|\n)*?>", "");
        newFeedback = newFeedback.replaceAll("\\((.|\n)*?\\)", "");
        return newFeedback;
    }
    
    /**
     * Exposes a javascript function to be used by the GIFT Media Semantics Avatar html
     * to notify GIFT that the avatar is idle.
     */
    public native void exposeNativeFunctions()/*-{
    	
    	var baseWindow = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();
    
    
        if(typeof baseWindow.notifyGIFT !== "function"){
        
            //only add an event listener the first time native functions are exposed, otherwise we get duplicate idle notifications
            baseWindow.addEventListener("message", function(e) {
                baseWindow.notifyGIFT();
            }, false);
        }
        
       	baseWindow.notifyGIFT = $entry(function(){
            @mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.AvatarWidget::idleAvatarNotification()();
        });
        
    }-*/;
    
    /**
     * The method called by the native javascript function {@link #exposeNativeFunctions()}
     * to indicate that the avatar is idle.
     */
    public static void idleAvatarNotification(){
    	isIdle = true;
    	if(idleCallback != null) {
    		idleCallback.onIdle();
    	}
    }
    
    /**
     * The method called by the native javascript function {@link #sayFeedback()}
     * to indicate that the avatar is busy.
     */
    public static void busyAvatarNotification() {
    	isIdle = false;
    	if(!isAvatarOnline) {
    		idleAvatarNotification();
    	}
    }
    
    /**
     * Gets whether or not the avatar is idle.
     * 
     * @return true if the avatar is idle, false if it is speaking.
     */
    public boolean isIdle() {
    	return isIdle;
    }
    
    /**
     * Sets the callback to execute when the avatar idle notification is received.
     * @param callback the callback to execute when the avatar is idle
     */
    public void setAvatarIdleCallback(AvatarIdleCallback callback) {
    	idleCallback = callback;
    }
    
    /**
     * Unloads the contents of the iframe containing the avatar and stops any scripts it is currently executing
     */
    public static native void unloadAvatar()/*-{
    	
    	var baseWindow = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();
    	
    	//Nick: Assigning an empty URL to the iframe is the simplest way to unload its contents
    	if(baseWindow.document.getElementById('avatarContainer') != null){
		    baseWindow.document.getElementById('avatarContainer').src = "";
		}
	}-*/;
    
    /**
     * Unloads and reloads the contents of the iframe containing the avatar, reseting its state back to when it was first initialized.
     */
    public void reloadAvatar(){
    	
    	//unload the contents of the iframe and stop any avatar scripts that are currently running
    	unloadAvatar();
    	
    	avatarUrl = null;
    }
    
    /**
     * Checks to see if the avatar at the given URL uses Unity and if the browser supports the features that Unity
     * avatars need
     * 
     * @param avatarUrl the avatar's URL
     */
    private native void checkUnityAvatarCompatibility(String avatarUrl)/*-{
    
        if(!$wnd.AudioContext){
            
            var that = this;
    
            // Avatars that use Unity (such as Virtual Human) need the Web Audio API in order to function properly,
            // so if we detect that the current browser does not support this API (which is the case for IE) 
            // then we need to notify the user and hide the avatar.
            //
            // To detect if the avatar is using Unity, we check the avatar's .html file to see if it has a reference
            // to the 'UnityLoader' object used by the build output for Unity WebGL projects. The .html file
            // is retrieved using a simple HTTP GET via XmlHttpRequest.
            $wnd.jQuery.get(avatarUrl, function(data) {
            
              var text = data;
              that.@mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.AvatarWidget::notifyAvatarBrowserIncompatibility(Z)(
                  text.indexOf('UnityLoader') >= 0
              );
              
            });
        }
    }-*/;
    
    /**
     * Notifies GIFT whether or not the learner's current browser cannot support a Unity-based avatar
     * 
     * @param incompatible whether or not the learner's current browser cannot support a Unity-based avatar
     */
    private void notifyAvatarBrowserIncompatibility(boolean incompatible) {
        
        browserSupportsAvatar = !incompatible;
        
        avatarContainer.setVisible(browserSupportsAvatar); 
        
        if(isAttached() && !browserSupportsAvatar) {
            
            //notify the server that the avatar is idle, since it isn't supported
            idleAvatarNotification();
            
            //notify the learner that their browser doesn't support the avatar
            compatibilityWarningDialog.show();
        }
    }
    
}