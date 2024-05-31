/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.coursewidgets;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.Widget;

import generated.course.BooleanEnum;
import generated.course.DisplayModeEnum;
import generated.course.ImageProperties;
import generated.course.LtiProperties;
import generated.course.YoutubeVideoProperties;
import generated.course.VideoProperties;
import mil.arl.gift.common.enums.VideoCssUnitsEnum;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.io.DetailedException;

/**
 * Widget for displaying different types of content (HTML, URLs, files)
 * 
 * @author bzahid
 */
public class ContentWidget extends Composite {

    private static Logger logger = Logger.getLogger(ContentWidget.class.getName());
    
    private static ContentWidgetUiBinder uiBinder = GWT.create(ContentWidgetUiBinder.class);
    
    // constant strings
    private final String WINDOW_FORM = Document.get().createUniqueId();
    private static final String OPEN = "Open";

    @UiField
    DeckPanel deckPanel;
    
    @UiField
    FlowPanel messagePanel;
    
    @UiField
    HTMLPanel urlPanel;
    
    @UiField
    FlowPanel imagePanel;
    
    @UiField
    Image image;
        
    @UiField
    FlowPanel anchorPanel;
    
    @UiField
    HTML htmlMsg;
    
    @UiField(provided = true)
    NamedFrame frame = new NamedFrame("contentFrame");

    @UiField(provided = true)
    FormPanel frameFormPanel = new FormPanel(frame);

    @UiField(provided = true)
    FormPanel windowFormPanel = new FormPanel(WINDOW_FORM);
    
    @UiField
    Anchor urlAnchor;
    
    /** 
     * References the click handler associated with the urlAnchor. 
     * Used to prevent one hyperlink from opening multiple pages.
     */
    private HandlerRegistration anchorHandler;

    @UiField
    protected Style style;
    
    interface ContentWidgetUiBinder extends UiBinder<Widget, ContentWidget> {
    }

    interface Style extends CssResource {
        String border();
    }
    
    private final String CONSTRAIN_VIDEO_SIZE = "videoContentConstrainSize";
    
    /**
     * Constructor
     *
     * @param instance The abstract instance of the widget
     */
    public ContentWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        deckPanel.showWidget(deckPanel.getWidgetIndex(messagePanel));        
    }
    
    /**
     * Sets the HTML content to display
     * 
     * @param content The HTML content
     */
    public void setMessageHTML(String content) {
        deckPanel.showWidget(deckPanel.getWidgetIndex(messagePanel));
        htmlMsg.setHTML(content);
    }
 
    /**
     * Sets the url of the content to display.
     * 
     * @param url The url
     * @param mediaTypeProperties The MediaTypeProperties associated with the content
     */
    public void setContentUrl(String url, Serializable mediaTypeProperties) {

        boolean isImage = false;
        boolean isLTI = false;

        if (mediaTypeProperties != null) {

            if (mediaTypeProperties instanceof ImageProperties) {
                isImage = true;
            } else if (mediaTypeProperties instanceof LtiProperties) {
                isLTI = true;

                LtiProperties ltiProperties = (LtiProperties) mediaTypeProperties;

                // check for LTI property display mode. If the author chose New Window or Modal,
                // then redirect to the link URL method and exit this method.
                if (DisplayModeEnum.NEW_WINDOW.equals(ltiProperties.getDisplayMode())
                        || DisplayModeEnum.MODAL.equals(ltiProperties.getDisplayMode())) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Opening the LTI in a new window");
                    }

                    setLinkUrl(url, OPEN, ltiProperties);
                    return;
                }
            } else if (mediaTypeProperties instanceof YoutubeVideoProperties) {

                YoutubeVideoProperties videoProps = (YoutubeVideoProperties) mediaTypeProperties;

                allowFullScreen(videoProps.getAllowFullScreen() == BooleanEnum.TRUE);

                if (videoProps.getSize() != null) {

                    String heightUnits = VideoCssUnitsEnum.PIXELS.getName();
                    String widthUnits = VideoCssUnitsEnum.PIXELS.getName();

                    if (videoProps.getSize().getHeightUnits() != null) {
                        heightUnits = videoProps.getSize().getHeightUnits();
                    }

                    if (videoProps.getSize().getWidthUnits() != null) {
                        widthUnits = videoProps.getSize().getWidthUnits();
                    }

                    if (videoProps.getSize().getConstrainToScreen() != null && videoProps.getSize().getConstrainToScreen() == BooleanEnum.TRUE) {

                        BooleanEnum constrain = videoProps.getSize().getConstrainToScreen();
                        logger.info("constrain to screen: " + constrain);

                        frame.addStyleName(CONSTRAIN_VIDEO_SIZE);

                    }

                    setContentUrlHeight(videoProps.getSize().getHeight().toString() + heightUnits);
                    setContentUrlWidth(videoProps.getSize().getWidth().toString() + widthUnits);
                    
                } else {
                    // Default youtube videos to 16:9 ratio and constrained to the screen
                    // width/height.
                    frame.addStyleName(CONSTRAIN_VIDEO_SIZE);
                    
                    final float percentage = 0.75f; // 75-80
                    float height = 56.25f * percentage;
                    float width = 100f * percentage;
                    
                    setContentUrlHeight(height + "vw");
                    setContentUrlWidth(width + "vw");
                }
                
            } else if (mediaTypeProperties instanceof VideoProperties) {
                
                VideoProperties localVideoProps = (VideoProperties) mediaTypeProperties;
                
                allowFullScreen(localVideoProps.getAllowFullScreen() == BooleanEnum.TRUE);
                
                /* Modify the video URL to use the appropriate autoplay setting, if needed. */
                boolean autoPlayEnabled = localVideoProps.getAllowAutoPlay() == BooleanEnum.TRUE;
                if(!url.contains("autoplay=1") && autoPlayEnabled){
                    
                    frame.getElement().setAttribute("allow", "autoplay");
                    
                    // if the AutoPlay property is enabled and the URL does not enable it, change the URL to enable it
                    url.replaceAll("\\Q&\\E*autoplay=0", "");
                    if(url.contains("?")){
                        //when this isn't the only argument after the video id
                        url += "&autoplay=1";   
                    }else{
                        //when this is the first argument after the video id
                        url += "?autoplay=1";   
                    }
                    
                } else if(url.contains("autoplay=1") && !autoPlayEnabled){
                    
                    // if the AutoPlay property is disabled and the URL enables it, change the URL to disable it
                    url.replaceAll("\\Q&\\E*autoplay=1", "");
                }
                
                if (localVideoProps.getSize() != null) {
                    
                    String heightUnits = VideoCssUnitsEnum.PIXELS.getName();
                    String widthUnits = VideoCssUnitsEnum.PIXELS.getName();
                    
                    if (localVideoProps.getSize().getHeightUnits() != null) {
                        heightUnits = localVideoProps.getSize().getHeightUnits();
                    }
                    
                    if (localVideoProps.getSize().getWidthUnits() != null) {
                        widthUnits = localVideoProps.getSize().getWidthUnits();
                    }
                    
                    if (localVideoProps.getSize().getConstrainToScreen() != null && localVideoProps.getSize().getConstrainToScreen() == BooleanEnum.TRUE) {
                        
                        BooleanEnum constrain = localVideoProps.getSize().getConstrainToScreen();
                        logger.info("constrain to screen: " + constrain);
                        
                        frame.addStyleName(CONSTRAIN_VIDEO_SIZE);
                    }
                    
                    setContentUrlHeight(localVideoProps.getSize().getHeight().toString() + heightUnits);
                    setContentUrlWidth(localVideoProps.getSize().getWidth().toString() + widthUnits);
                    
                } else {
                    // Default localvideos to 16:9 ratio and constrained to the screen
                    // width/height.
                    frame.addStyleName(CONSTRAIN_VIDEO_SIZE);
                    
                    final float percentage = 0.75f; // 75-80
                    float height = 56.25f * percentage;
                    float width = 100f * percentage;
                    
                    setContentUrlHeight(height + "vw");
                    setContentUrlWidth(width + "vw");
                }
            } else {
                setContentUrlHeight("100%");
                setContentUrlWidth("100%");
            }
        }

        if (isImage) {
            image.setUrl(url);
            image.addStyleName(style.border());
            deckPanel.showWidget(deckPanel.getWidgetIndex(imagePanel));
        } else {
            if (isLTI) {
                setupFormPanelPost(frameFormPanel, url, mediaTypeProperties, true);
            } else {
                frame.setUrl(url);
            }

            frame.addStyleName(style.border());
            deckPanel.showWidget(deckPanel.getWidgetIndex(urlPanel));
        }
    }
    
    /**
     * Populates the form panel with a clickable button for the user to launch the specified action
     * URL. The form panel action will be sent via a POST request using a URL encoded message.
     * 
     * @param formPanel the form panel to initialize.
     * @param action the action URL.
     * @param mediaTypeProperties the MediaTypeProperties associated with the content
     * @param launchInline true to launch the action inline; false to have a the action opened in a pop up or new window
     */
    private void setupFormPanelPost(final FormPanel formPanel, String action, final Serializable mediaTypeProperties, final boolean launchInline) {
        formPanel.setMethod(FormPanel.METHOD_POST);
        formPanel.setEncoding(FormPanel.ENCODING_URLENCODED);
        
        buildOAuthUrl(action, mediaTypeProperties, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable e) {
                logger.severe("Failed to build the LTI url. Msg: " + e.getMessage() + " Cause: " + e.getCause());
                throw new DetailedException("Building LTI encrypted url failed",
                        "Exception occurred while trying to build the encrypted LTI provider url", e);
            }

            @Override
            public void onSuccess(RpcResponse response) {
                
                if (response.isSuccess()) {
                    // set the returned OAuth URL to the form panel action
                    formPanel.setAction(response.getResponse());
                    if (launchInline) {
                        formPanel.submit();
                    } else {
                        if (mediaTypeProperties instanceof LtiProperties) {
                            LtiProperties ltiProperties = (LtiProperties) mediaTypeProperties;

                            // defaults to new window / tab based on browser preferences.
                            String features = null;
                            if (DisplayModeEnum.MODAL.equals(ltiProperties.getDisplayMode())) {
                                // set features for modal view
                                features = "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no";
                            }

                            openFormPanel(formPanel, WINDOW_FORM, features);
                        }
                    }
                } else {
                    String errorMsg = "Failed to build the LTI url. Reason: " + response.getResponse();
                    logger.severe(errorMsg);
                    throw new DetailedException("Building LTI encrypted url failed", errorMsg, null);
                }
            }
        });
    }
    
    /**
     * Opens a new window using the specified options and submits the form panel.
     * 
     * @param formPanel formPanel that is targeting the window. Cannot be null.
     * @param name name of the window to be displayed. Can be null.
     * @param features optional features for the window. If null is used, will submit an empty
     *            string.
     */
    private void openFormPanel(FormPanel formPanel, String name, String features) {
        Window.open("", name, features == null ? "" : features);
        formPanel.submit();
    }
    
    /**
     * Clears the content urls
     */
    public void clearContent() {
        image.setUrl("");
        frame.setUrl("");
        image.removeStyleName(style.border());
        frame.removeStyleName(style.border());
        frame.removeStyleName(CONSTRAIN_VIDEO_SIZE);
        frameFormPanel.setAction("");
        windowFormPanel.setAction("");
    }
    
    /**
     * Sets the url of content that should be opened in a new window
     * 
     * @param url The url to open
     * @param urlTitle The hyperlink text
     */
    public void setLinkUrl(final String url, String urlTitle) {
        // redirect with null properties
        setLinkUrl(url, urlTitle, null);
    }
    
    /**
     * Sets the url of content that should be opened in a new window
     * 
     * @param url The url to open
     * @param urlTitle The hyperlink text
     * @param mediaTypeProperties The MediaTypeProperties associated with the content
     */
    public void setLinkUrl(final String url, String urlTitle, final Serializable mediaTypeProperties) {
        urlAnchor.setText(urlTitle);
        
        if(anchorHandler != null) {
            anchorHandler.removeHandler();
        }
        
        anchorHandler = urlAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /*
                 * Open the URL in a separate browser.
                 */
                if (mediaTypeProperties != null) {
                    setupFormPanelPost(windowFormPanel, url, mediaTypeProperties, false);
                } else {
                    String options = "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no";
                    Window.open(url, "_blank", options);
                }
            }
        });
        deckPanel.showWidget(deckPanel.getWidgetIndex(anchorPanel));
    }
    
    private void setContentUrlWidth(String width) {
        frame.getElement().getStyle().setProperty("width", width);
    }
    
    private void setContentUrlHeight(String height) {
        frame.getElement().getStyle().setProperty("height", height);
    }
    
    private void allowFullScreen(boolean allowFullScreen) {
        if(allowFullScreen) {
            frame.getElement().setAttribute("fs", "1");
            frame.getElement().setAttribute("webkitallowfullscreen", "");
            frame.getElement().setAttribute("mozallowfullscreen", "");
            frame.getElement().setAttribute("allowfullscreen", "");
        } else {
            frame.getElement().setAttribute("fs", "0");
        }
    }
    
    /**
     * Adds a widget to display beneath the HTML message
     * 
     * @param widget The widget to add
     */
    public void addWidget(Widget widget) {
        messagePanel.add(widget);
    }
    
    @Override
    protected void onDetach() {
        super.onDetach();
        frame.setUrl("");
        htmlMsg.setHTML("");
    }
    
    /**
     * Builds the encrypted OAuth URL that will be used to send the request to the LTI provider.
     * 
     * @param rawUrl the raw media url before it has been protected by OAuth.
     * @param mediaTypeProperties The MediaTypeProperties associated with the content.
     * @param callback the callback used to handle the response or catch any failures.
     */
    public void buildOAuthUrl(String rawUrl, Serializable mediaTypeProperties, AsyncCallback<RpcResponse> callback) {
        // This is handled by an overwritten method when the constructor is initialized.
        // The reason for this is we need to get to a spot where we can access BrowserSession.getInstance()
    }
    
}
