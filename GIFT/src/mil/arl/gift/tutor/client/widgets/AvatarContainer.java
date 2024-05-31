/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.logging.Logger;

import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.conversations.ChatWidget;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.AvatarContainerWidgetProperties;
import mil.arl.gift.tutor.shared.properties.UserActionWidgetProperties;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * A container to present the Avatar above any other widgets.
 * 
 * @author bzahid
 */
public class AvatarContainer extends Composite implements RequiresResize, IsUpdateableWidget {
    
    interface AvatarContainerUiBinder extends UiBinder<Widget, AvatarContainer> {
    }
    
    private static Logger logger = Logger.getLogger(AvatarContainer.class.getName());
    
    private static AvatarContainerUiBinder uiBinder = GWT.create(AvatarContainerUiBinder.class);
    
    private static AvatarContainer instance;
    
    @UiField
    protected FlowPanel avatarContainer;
    
    @UiField
    protected FlowPanel headerPanel;
    
    @UiField
    protected FlowPanel widgetContainer;
    
    @UiField(provided = true)
    protected SimpleLayoutPanel feedbackPanel = new SimpleLayoutPanel(){
        
        @Override
        public void onResize() {
            super.onResize();
            
            repositionFrame();
        }
    };
    
    @UiField
    protected SplitLayoutPanel splitPanel;
    
    @UiField
    protected Widget mainPanel;
    
    /**
     * An iframe used to render the avatar. Note that this iframe is attached to the root of the document, 
     * not a particular instance of this widget. Whenever an instance of this widget is shown, the avatar frame is 
     * repositioned so that it appears on top of this widget's content.
     * <br/><br/>
     * This allows the avatar to load prematurely before an instance of this widget is shown.
     */
    private static Frame avatarFrame = new Frame();
    
    /** Whether or not the learner's browser is capable of rendering the current avatar and playing its audio */
    private static boolean browserSupportsAvatar = true;
    
    /** A dialog used to notify the learner if their browser does not support the current avatar */
    private static ModalDialogBox compatibilityWarningDialog = new ModalDialogBox();
    
    static {
        
        //initialize the native JavaScript method used to allow avatars to speak
        defineEmptySayFeedback();
        
        //initialize the styling and positioning of the avatar frame
        avatarFrame.getElement().setId("avatarContainer");
        avatarFrame.getElement().setAttribute("scrolling", "no");
        avatarFrame.getElement().setAttribute("frameborder", "0");
        avatarFrame.getElement().setAttribute("allow", "autoplay");
        avatarFrame.getElement().setAttribute("onload", "sayFeedback();");
        
        avatarFrame.setVisible(false);
        avatarFrame.getElement().getStyle().setPosition(Position.FIXED);
        avatarFrame.getElement().getStyle().setTop(0, Unit.PX);
        avatarFrame.getElement().getStyle().setLeft(0, Unit.PX);
        avatarFrame.getElement().getStyle().setProperty("border", "none");
        
        RootPanel.get().add(avatarFrame);
        
        //initialize the dialog that warns the learner about browser compatibility issues
        compatibilityWarningDialog.setText("Incompatible Browser");
        
        HTML compatibilityWarning = new HTML(
            "The authored character is not supported by your browser.<br/><br/>"
            + "To show this character, please run this course again using Google Chrome, Mozilla Firefox, or Microsoft Edge instead."
        );
        
        compatibilityWarningDialog.setWidget(compatibilityWarning);
        compatibilityWarningDialog.setAnimationEnabled(true);
        compatibilityWarningDialog.setGlassEnabled(true);
        compatibilityWarningDialog.setModal(true);
        compatibilityWarningDialog.setCloseable(true);
    }
    
    /**
     * Class constructor
     */
    public AvatarContainer() {
        initWidget(uiBinder.createAndBindUi(this));
        updateContainerInstance();
        
        //resizes the avatar frame whenever the static instance of this widget is attached
        addAttachHandler(new Handler() {

            @Override
            public void onAttachOrDetach(AttachEvent event) {
                
                boolean attached = event.isAttached();
                
                if(attached) {
                    
                    //whatever instance is currently attached should be used for this class' static operations
                    updateContainerInstance();
                    
                    //wait for layout operations in GWT's event loop to finish before positioning the app
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            
                            //wait for CSS animations and sizing to be applied before positioning the app
                            AnimationScheduler.get().requestAnimationFrame(new AnimationCallback() {
                                
                                @Override
                                public void execute(double timestamp) {
                                    
                                    //redraw and reposition the avatar once this widget's size is available
                                    redrawAvatarContainer();
                                    repositionFrame();
                                    
                                    if(!browserSupportsAvatar) {
                                        
                                        //notify the server that the avatar is idle, since it isn't supported
                                        BrowserSession.getInstance().idleAvatarNotification();
                                        
                                        //notify the learner that their browser doesn't support the avatar
                                        compatibilityWarningDialog.getWidget().setWidth(mainPanel.getOffsetWidth() + "px");
                                        compatibilityWarningDialog.center();
                                        compatibilityWarningDialog.setPopupPosition(
                                                mainPanel.getAbsoluteLeft(),
                                                compatibilityWarningDialog.getAbsoluteTop()
                                        );
                                    }
                                }
                            });
                        }
                    });
                    
                } else {
                    
                    //hide the avatar so it doesn't appear without an accompanying instance of this widget
                    avatarFrame.setVisible(false);
                    
                    //hide the browser compatibility warning dialog if it is showing so it doesn't block other course objects
                    compatibilityWarningDialog.hide();
                }
            }
            
        });
        
        Window.addResizeHandler(new ResizeHandler() {
            
            @Override
            public void onResize(ResizeEvent event) {
                AvatarContainer.this.onResize();
            }
        });
    }
    
    /**
     * Creates a new avatar container widget.
     * 
     * @param instance Instance of the widget.
     */
    public AvatarContainer(WidgetInstance instance) {
        this();
        update(instance);
    }
    
    /**
     * Gets the current instance of the AvatarContainer widget.
     * 
     * @return the instance of the AvatarContainer. Can be null.
     */
    public static AvatarContainer getInstance() {
        return instance;
    }
    
    /**
     * Sets the widget below the avatar.
     * 
     * @param widget The widget to display
     */
    public void setContainerWidget(Widget widget) {
        widgetContainer.clear();
        widgetContainer.add(widget);
    }
    
    /**
     * Gets the current widget being displayed beneath the Avatar.
     * 
     * @return the widget beneath the Avatar. Can be null if there is no widget currently displayed.
     */
    public Widget getContainerWidget() {
        if(widgetContainer == null) {
            return null;
        }
        return (widgetContainer.getWidgetCount() > 0) ? widgetContainer.getWidget(0) : null;
    }
    
    @Override
    public void update(WidgetInstance instance) {
        
        WidgetTypeEnum widgetType = AvatarContainerWidgetProperties.getWidgetType(instance.getWidgetProperties());
        boolean tutorTest =  instance.getWidgetProperties().isTutorTest();      

        if (widgetType == WidgetTypeEnum.USER_ACTION_WIDGET) {
                        
            TutorActionsWidget actionsWidget = TutorActionsWidget.getInstance();
            
            if(actionsWidget == null) {
                actionsWidget = new TutorActionsWidget(instance);
                setContainerWidget(actionsWidget);
            }
            
            if(!UserActionWidgetProperties.shouldUsePreviousActions(instance.getWidgetProperties())) {
                actionsWidget.setUserActions(new UserActionWidget(instance));   
            }                
            
        } else if (widgetType == WidgetTypeEnum.FEEDBACK_WIDGET) {
                        
            if(getContainerWidget() != null && getContainerWidget() instanceof TutorActionsWidget) {
                
                TutorActionsWidget actionsWidget = TutorActionsWidget.getInstance();
                if(actionsWidget == null) {
                    actionsWidget = new TutorActionsWidget(instance);
                    setContainerWidget(actionsWidget);
                }
                
                actionsWidget.handleFeedbackUpdate(instance);
                
                // Issue 3414: avoids a situation where there is no avatar displayed but
                // the server thinks that the avatar is busy because it has not received 
                // an idle notification. This prevents updates to the AvatarContainer with 
                // additional feedback messages or messages to display the avatar. By sending 
                // an idle notification when there is no avatar displayed, we can ensure that 
                // feedback messages won't back up on the server.
                if(!hasAvatarData() && !tutorTest) {
                    BrowserSession.getInstance().idleAvatarNotification();
                }
            } else if(widgetContainer != null) {
                FeedbackWidget feedbackWidget = new FeedbackWidget(instance, true);
                setContainerWidget(feedbackWidget);
                if(!tutorTest) {
                    BrowserSession.setTutorActionsAvailable(false);
                }
            }
                        
        } else if (widgetType == WidgetTypeEnum.CHAT_WINDOW_WIDGET) {
            
            if(getContainerWidget() != null && getContainerWidget() instanceof TutorActionsWidget) {
                TutorActionsWidget.getInstance().handleChatUpdate(instance);
                
            }else if(getContainerWidget() != null && getContainerWidget() instanceof ChatWidget){
                ((ChatWidget)getContainerWidget()).updateChat(instance.getWidgetProperties());
                
            } else if(widgetContainer != null) {
                //trying to update a chat but the current widget doesn't contain a chat type widget
                //therefore need to create a chat widget and set it as the current widget
                setContainerWidget(new ChatWidget(instance));                   
                if(!tutorTest) {
                    BrowserSession.setTutorActionsAvailable(false);
                }
            }
        }
            
    }
    
    @Override
    public WidgetTypeEnum getWidgetType() {
        return WidgetTypeEnum.AVATAR_CONTAINER_WIDGET;
    }
    
    /**
     * Sets the avatar data to display right now.
     * 
     * @param avatar contains the HTML of the avatar to display now.  If null the avatar container will
     * be cleared of any existing avatar.
     * @return True if the avatar data was set, false if the avatar data was already set.
     */
    public static boolean setAvatarData(AvatarData avatar) {
        
        if(avatar != null) {
            
            boolean loadedNewAvatar = false;
            
            if(!hasAvatarData()) {
                
                //load the new avatar data into the avatar frame
                loadAvatar(avatar);
                loadedNewAvatar = true;
            }
            
            redrawAvatarContainer();
            
            repositionFrame();
            
            return loadedNewAvatar;
            
        }else{
            
            //clear the avatar data - this is needed so the avatar will be reloaded in the future thereby causing
            //the avatar idle notification to be sent to the server.  Then the server can start to send queued chat entries.
            logger.info("Clearing avatar container of any existing avatar and notifying the server that the avatar is busy to prevent avatar updates.");
            avatarFrame.getElement().removeAttribute("src");
            
            BrowserSession.getInstance().busyAvatarNotification();
            
            return true;
        }
    }
    
    /**
     * Redraws the avatar and the elements that visually contain it. If GIFT has detected that the learner's browser does not
     * support the current avatar, this method will also hide the avatar.
     */
    private static void redrawAvatarContainer() {
        
        avatarFrame.setVisible(browserSupportsAvatar && hasAvatarData());
        
        if(!browserSupportsAvatar && avatarFrame.isAttached()) {
            
            //detatch the avatar frame from the page if it isn't supported so that we don't waste time loading it
            avatarFrame.removeFromParent();
            
        } else if(browserSupportsAvatar && !avatarFrame.isAttached()){
            
            //reattach the avatar frame to the page if it is supported
            RootPanel.get().add(avatarFrame);
        }
        
        if(instance != null) {
            
            if(hasAvatarData() && browserSupportsAvatar) {
                
                //resize the avatar container in case its size does not match the avatar frame (which can happen if the avatar is preloaded)
                instance.avatarContainer.setWidth(avatarFrame.getElement().getStyle().getWidth());
                instance.avatarContainer.setHeight(avatarFrame.getElement().getStyle().getHeight());
                instance.headerPanel.setHeight(avatarFrame.getElement().getStyle().getHeight());
                
            } else {
                
                instance.avatarContainer.setWidth("0px");
                instance.avatarContainer.setHeight("0px");
                instance.headerPanel.setHeight("0px");
            }
        }
    }

    /**
     * Returns whether or not the avatar data has been set
     * 
     * @return True if avatar data exists, false otherwise
     */
    public static boolean hasAvatarData() {
        return (instance != null && avatarFrame.getUrl() != null && !avatarFrame.getUrl().trim().isEmpty());
    }
    
    /**
     * Loads the avatar data into the web page, even if the avatar container is not showing. Calling this method on its
     * own can be used to preload the avatar before it is shown.
     * 
     * @param avatar The avatar data to load
     */
    public static void loadAvatar(AvatarData avatar) {

        //check to see if the avatar uses Unity and the browser supports it
        checkUnityAvatarCompatibility(avatar.getURL());
        
        avatarFrame.setUrl(avatar.getURL());
        avatarFrame.setSize(avatar.getWidth() + "px", avatar.getHeight() + "px");
    }
    
    /**
     * Defines the sayNewFeedback function to do nothing
     */
    public static native void defineEmptySayFeedback() /*-{ 
        $wnd.sayFeedback = function() { }
    }-*/;

    /**
     * Defines the 'sayFeedback' function that is called as soon as the avatar data loads. 
     *
     * @param newFeedback The new feedback to say when the avatar is loaded
     */
    public static native void defineSayFeedback(String newFeedback) /*-{ 
        $wnd.sayFeedback = function() {
        
            if(@mil.arl.gift.tutor.client.widgets.AvatarContainer::browserSupportsAvatar){
            
                if(newFeedback != null) {
                    $doc.getElementById('avatarContainer').contentWindow.postMessage(JSON.stringify({method:"msSpeak",key:newFeedback}),"*");
                }
                
            } else {
            
                //if the browser doesn't support the avatar, always treat the avatar as idle
                $wnd.notifyGIFT();
            }
            
            @mil.arl.gift.tutor.client.widgets.AvatarContainer::defineEmptySayFeedback()();
        }
    }-*/;
    
    /**
     * Calls the speak method for the avatar
     * 
     * @param newFeedback The feedback to say.
     */
    public static native void sayFeedback(String newFeedback) /*-{
        $wnd.sayNewFeedback = function() {
        
            if(@mil.arl.gift.tutor.client.widgets.AvatarContainer::browserSupportsAvatar){
        
                if(newFeedback != null) {
                    $doc.getElementById('avatarContainer').contentWindow.postMessage(JSON.stringify({method:"msSpeak",key:newFeedback}),"*");
                }
            
            } else {
            
                //if the browser doesn't support the avatar, always treat the avatar as idle
                $wnd.notifyGIFT();
            }
        }
         $wnd.sayNewFeedback();
    }-*/;
    
    /**
     * Defines the 'sayFeedback' function that is called as soon as the avatar data loads. 
     *
     * @param messageKey The key of the message to speak
     */
    public static native void defineSayFeedbackStaticMessage(String messageKey) /*-{ 
        $wnd.sayFeedback = function() {
        
            if(@mil.arl.gift.tutor.client.widgets.AvatarContainer::browserSupportsAvatar){
        
                if(messageKey != null) {
                    $doc.getElementById('avatarContainer').contentWindow.postMessage(JSON.stringify({method:"msPlay",key:messageKey}),"*");
                }
            
            } else {
            
                //if the browser doesn't support the avatar, always treat the avatar as idle
                $wnd.notifyGIFT();
            }
            
            @mil.arl.gift.tutor.client.widgets.AvatarContainer::defineEmptySayFeedback()();
        }
    }-*/;

    /**
     * Calls the play method for the avatar
     * 
     * @param messageKey The key of the message to speak
     */
    public static native void sayFeedbackStaticMessage(String messageKey) /*-{ 
        $wnd.sayNewFeedback = function() {
        
            if(@mil.arl.gift.tutor.client.widgets.AvatarContainer::browserSupportsAvatar){
        
                if(messageKey != null) {
                    $doc.getElementById('avatarContainer').contentWindow.postMessage(JSON.stringify({method:"msPlay",key:messageKey}),"*");
                }
            
            } else {
            
                //if the browser doesn't support the avatar, always treat the avatar as idle
                $wnd.notifyGIFT();
            }
        }
        $wnd.sayNewFeedback();
    }-*/;
    
    /**
     * Plays the given MP3 audio file or, if provided, falls back to the given OGG file 
     * using the provided element
     * 
     * @param mp3File the URL of the MP3 audio file to play
     * @param oggFile an optional URL for an OGG file to fall back to if the MP3 file cannot be played
     * @param element the element to attach the audio element to when it is played.
     * This can be used to clean up the audio when the element is removed from the DOM.
     */
    public static native void playAudio(String mp3File, String oggFile, Element element) /*-{ 
        var new_audio = document.createElement("audio");
        var source = document.createElement('source');
        source.type = 'audio/ogg';
        source.src = oggFile;
        new_audio.appendChild(source);
        source = document.createElement('source');
        source.type = 'audio/mpeg';
        source.src = mp3File;
        new_audio.appendChild(source);
        element.appendChild(new_audio);
        new_audio.load();
        new_audio.play();
    }-*/;
    
    @Override 
    protected void onLoad() {
        super.onLoad();
        
        Scheduler.get().scheduleFinally(new ScheduledCommand() {
            
            @Override
            public void execute() {
                
                //need to check the size of this widget once it finishes loading to ensure its children are resized properly
                onResize();
            }
        });
    }
    
    @Override
    public void onResize() {
        //update the position of the avatar frame whenever the viewport changes size
        repositionFrame();
    }
    
    /**
     * Assigns this particular avatar container as the main instance that should be used throughout the TUI.
     */
    protected void updateContainerInstance() {
        instance = this;
    }
    
    /** 
     * Repositions and resizes the avatar frame so it overlays the screen space allocated for it by this widget
     */
    private static void repositionFrame() {
        
        if(instance != null) {
            
            avatarFrame.getElement().getStyle().setTop(instance.avatarContainer.getElement().getAbsoluteTop(), Unit.PX);
            avatarFrame.getElement().getStyle().setLeft(instance.avatarContainer.getElement().getAbsoluteLeft(), Unit.PX);
            avatarFrame.getElement().getStyle().setProperty("maxWidth", instance.feedbackPanel.getOffsetWidth() + "px");
        }
    }
    
    /**
     * Checks to see if the avatar at the given URL uses Unity and if the browser supports the features that Unity
     * avatars need
     * 
     * @param avatarUrl the avatar's URL
     */
    private static native void checkUnityAvatarCompatibility(String avatarUrl)/*-{
    
        if(!$wnd.AudioContext){
    
            // Avatars that use Unity (such as Virtual Human) need the Web Audio API in order to function properly,
            // so if we detect that the current browser does not support this API (which is the case for IE) 
            // then we need to notify the user and hide the avatar.
            //
            // To detect if the avatar is using Unity, we check the avatar's .html file to see if it has a reference
            // to the 'UnityLoader' object used by the build output for Unity WebGL projects. The .html file
            // is retrieved using a simple HTTP GET via XmlHttpRequest.
            $wnd.jQuery.get(avatarUrl, function(data) {
            
              var text = data;
              @mil.arl.gift.tutor.client.widgets.AvatarContainer::notifyAvatarBrowserIncompatibility(Z)(
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
    private static void notifyAvatarBrowserIncompatibility(boolean incompatible) {
        
        browserSupportsAvatar = !incompatible;
        
        redrawAvatarContainer();
    }
    
    /**
     * Gets the the Iframe element being used to show the avatar
     * 
     * @return the avatar Iframe
     */
    private static Element getAvatarFrameElement() {
        
        if(avatarFrame != null) {
            return avatarFrame.getElement();
            
        } else {
            return null;
        }
    }

}
