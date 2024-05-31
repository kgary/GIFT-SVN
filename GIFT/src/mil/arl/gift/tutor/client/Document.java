/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.iframe.messages.DisplayDialogMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.DisplayNotificationMessage;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.DynamicHeaderScrollPanel;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget;
import mil.arl.gift.tutor.client.widgets.ErrorPageWidget;
import mil.arl.gift.tutor.client.widgets.FeedbackWidget;
import mil.arl.gift.tutor.client.widgets.LogoWidget;
import mil.arl.gift.tutor.client.widgets.IsRemovableTuiWidget;
import mil.arl.gift.tutor.shared.CloseAction;
import mil.arl.gift.tutor.shared.DialogTypeEnum;

/**
 * Represents the document of the web page in the document
 *
 * @author jleonard
 */
public class Document {
    
    private static Logger logger = Logger.getLogger(Document.class.getName());
    
    private static final String DEFAULT_FOOTER = "Generalized Intelligent Framework for Tutoring";
    
    /** The default style of the tutor root container. */
    private static final String DEFAULT_ROOT_STYLE = "rootContainer";
    /** The default style of the document panel. */
    private static final String DEFAULT_PANEL_STYLE = "documentPanel";
    /** The style of the team session root container. */
    private static final String TEAM_SESSION_ROOT_STYLE = "teamSessionBackground";
    /** The style of the team session document panel. */
    private static final String TEAM_SESSION_PANEL_STYLE = "teamSessionDocumentPanel";

    /**
     * The instance of the document container
     */
    private static Document instance;

    /**
     * Gets the active instance of the document
     *
     * @return The active instance of the document
     */
    public static Document getInstance() {
        if (instance == null) {
            instance = new Document();
        }
        return instance;
    }
    
    private DocumentContainer documentContainer = new DocumentContainer();
    private FlowPanel container;
    private BlockerPanel blocker = new BlockerPanel();
    private boolean fullscreen = true;
    private CourseHeaderWidget header = new CourseHeaderWidget() {
    	
    	@Override
		public void setHeaderTitle(String name) {
			header.show();
			header.setName(name);
		}
    	
    	@Override
    	public void handleContinue() {
    		
    		// Disable the header continue button and show the loading indicator
    	    //TODO: Nick - temporarily preventing the continue button from being disabled, since the TUI client sometimes encounters a
    	    //SerializationException upon trying to decode the response to the close action. Until the cause of the serialization issue
    	    //can be determined, the simplest solution is to simply avoid disabling the button.
//    		setContinueButtonEnabled(false);
			Document.getInstance().showLoading(true);
			
			// Notify the browser that the current page should be closed
			BrowserSession.getInstance().sendActionToServer(new CloseAction(pageId), new AsyncCallback<RpcResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					logger.warning("Request to close the guidance widget failed on the server: " + caught.getMessage());
					setContinueButtonEnabled(true);
					Document.getInstance().showLoading(false);
					if(continueCallback != null) {
						continueCallback.onFailure();
					}
				}

				@Override
				public void onSuccess(RpcResponse result) {
					setContinueButtonEnabled(true);
					continueCallback = null;
				}
			});
    	}
    };
    
    
    /**
     * Constructor
     */
    private Document() {
        RootLayoutPanel document = RootLayoutPanel.get();
        document.clear();
        document.addStyleName(DEFAULT_ROOT_STYLE);       
        container = new FlowPanel();
        container.addStyleName(DEFAULT_PANEL_STYLE);
        DynamicHeaderScrollPanel scroller = new DynamicHeaderScrollPanel();
        scroller.setHeader(header);
        scroller.setCenter(documentContainer);
        container.add(scroller);
        document.add(container);
        // Default to full screen mode.
        fullScreenMode();
        hideCourseHeader();
    }
        
    /**
     * Plays a beep sound
     */
    public static void playBeep() {
        playAudio();
    }
    
    /**
     * Plays a audio in the browser
     */
    private static void playAudio() {
        try {
            playAudioHTML5();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Could not play audio with HTML5", e);
        }
    }

    /**
     * Plays a beep in the browser using HTML5
     */
    private static native void playAudioHTML5() /*-{ 
        $doc.getElementById('beepAudioTag').play();
    }-*/;
    
    /**
     * Hide the Training Application placeholder text declared in the TutorUserWebInterface.html
     */
    private static native void hideTrainingAppPlaceholderText() /*-{
        $doc.getElementById('ta-placeholder').style.display="none";
    }-*/;
    
    /**
     * Show the Training Application placeholder text declared in the TutorUserWebInterface.html
     */
    private static native void showTrainingAppPlaceholderText() /*-{
        $doc.getElementById('ta-placeholder').style.display="";
    }-*/;   

    /**
     * Show or hide a loading indicator near the course header widget. 
     * {@link #showLoading(boolean) showLoading(false)} should be called
     * to hide the loading indicator.
     *  
     * @param loading True to show the loading indicator, or false to hide it
     */
    public void showLoading(boolean loading) {
    	if(loading) {
    		documentContainer.add(blocker);
    		blocker.block();
    	} else {
    		blocker.unblock();
    		documentContainer.remove(blocker);
    	}
    	header.showLoading(loading);
    }
    
    /**
     * Hides and resets the course header. This should be called before presenting the next course transition
     * to clear the information message and clear navigation button handlers.
     */
    public void hideCourseHeader() {
    	header.hide();
    }
    
    /**
     * Displays a dialog box in the document
     *
     * @param title The title of the dialog box
     * @param message The message of the dialog box.  This can contain HTML formatting (e.g. <br>) but shouldn't
     *              contain the html tag (i.e. <html> and </html>).
     */
    public void displayDialog(String title, String message) {
        final DialogBox dialogBox = new DialogBox();
        Button closeButton = new Button("Close");
        HTML serverResponseLabel = new HTML();

        dialogBox.setText(title);
        // This is disabled until GWT is upgraded to version 2.5
        //dialogBox.setAnimationEnabled(true);
        // We can set the id of a widget by accessing its Element
        closeButton.getElement().setId("closeButton");
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.addStyleName("dialogVPanel");
        serverResponseLabel.setHTML(message);
        dialogVPanel.add(serverResponseLabel);
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        dialogVPanel.add(closeButton);
        dialogBox.setWidget(dialogVPanel);

        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        dialogBox.center();
        closeButton.setFocus(true);
    }
    
    /**
     * Displays a dialog box in the document. If the TUI is embedded in the dashboard, the dialog will be shown there instead.
     *
     * @param title The title of the dialog box
     * @param message The message of the dialog box.  This can contain HTML formatting (e.g. <br>) but shouldn't
     *              contain the html tag (i.e. <html> and </html>).
     * @param advancedDescription optional additional information about the error to display in the dialog.  Can be null.
     */
    public void displayDialogInDashboard(String title, String message, String advancedDescription) {    	
        
        String text = message;
        if(advancedDescription != null){
            text += "<br/><br/>" + advancedDescription;
        }
        
        if (TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment()) {

            DisplayDialogMessage msgDialog = new DisplayDialogMessage(title, 
                    text,
                    DialogTypeEnum.INFO_DIALOG.name());
            IFrameMessageHandlerChild.getInstance().sendMessage(msgDialog);
            
        } else {     
            
           displayDialog(title, text);
        }
    }
    
    /**
     * Displays a notification in the dashboard (via Bootstrap Notify class).
     * @param title The title for the notification (can be empty to indicate no title).
     * @param message The message for the notification.
     * @param iconCssType Optional bootstrap css icon style name.  Can be empty if no icon should be used.
     */
    public void displayNotificationInDashboard(String title, String message, String iconCssType) {
        if (TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment()) {

            DisplayNotificationMessage msgDialog = new DisplayNotificationMessage(title, 
                    message,
                    iconCssType);
            IFrameMessageHandlerChild.getInstance().sendMessage(msgDialog);
            
        } else {     
            
           displayDialog(title, message);
        }
    }
    
    /**
     * Displays a notification in the dashboard (via Bootstrap Notify class).
     * 
     * @param message The message for the notification.
     * @param iconCssType Optional bootstrap css icon style name.  Can be empty if no icon should be used.
     */
    public void displayNotificationInDashboard(String message, String iconCssType) {
        
        // Pass in an empty string for the title to indicate no title.
        displayNotificationInDashboard("", message, iconCssType);
    }

    /**
     * Create and display a pop-up dialog with the provided error message information.  If the tutor is in embedded
     * mode, this will end the course.
     * 
     * @param title a title to place on the dialog 
     * @param message the message content to display in the error dialog
     * @param advancedDescription optional additional information about the error to display in the dialog 
     */
    public void displayErrorDialog(String title, String message, String advancedDescription) {
        
        String internalTitle = title != null ? title : "Failure";
        if (TutorUserWebInterface.isExperiment() && BrowserSession.getInstance() != null) {
            displayErrorPage(null, message, advancedDescription);
        } else if (TutorUserWebInterface.isEmbedded()) {
            
            DisplayDialogMessage msgDialog = new DisplayDialogMessage(internalTitle, message, advancedDescription,
                                                                      DialogTypeEnum.ERROR_DIALOG.name());
            IFrameMessageHandlerChild.getInstance().sendMessage(msgDialog);
            
        } else {
            internalDisplayErrorDialog(internalTitle, message, advancedDescription);
        }
    }
    
    /**
     * Internal version of the display dialog function.  This should only be called internally to this class.
     * External users should call the displayErrorDialog function.
     * 
     * @param title a title to place on the dialog 
     * @param message the message content to display in the error dialog
     * @param advancedDescription optional additional information about the error to display in the dialog 
     */
    private void internalDisplayErrorDialog(String title, String message, String advancedDescription) {
        final DialogBox dialogBox = new DialogBox();
        Button closeButton = new Button("Close");
        HTML serverResponseLabel = new HTML();

        dialogBox.setText(title);
        // This is disabled until GWT is upgraded to version 2.5
        //dialogBox.setAnimationEnabled(true);
        // We can set the id of a widget by accessing its Element
        closeButton.getElement().setId("closeButton");
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.setSpacing(10);
        dialogVPanel.addStyleName("dialogVPanel");
        serverResponseLabel.setText(message);
        serverResponseLabel.addStyleName("serverResponseError");
        dialogVPanel.add(serverResponseLabel);
        if (advancedDescription != null) {
            HTML errorLabel = new HTML(advancedDescription);
            DisclosurePanel errorPanel = new DisclosurePanel("Advanced Description");
            errorPanel.add(errorLabel);
            dialogVPanel.add(errorPanel);
        }
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        dialogVPanel.add(closeButton);
        dialogBox.setWidget(dialogVPanel);

        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        dialogBox.center();
        closeButton.setFocus(true);
    }

    /**
     * Display in a dialog and log a general error
     *
     * @param action The action that was being preformed when the error occurred
     * @param message The error that occurred
     */
    public void displayError(String action, String message) {
        displayError(action, message, (String)null);
    }
    
        /**
     * Display in a dialog and log a general error
     *
     * @param action The action that was being preformed when the error occurred
     * @param message The error that occurred
     * @param throwable the exception that needs to be handled
     */
    public void displayError(String action, String message, Throwable throwable) {
        String error = "There was an error while " + action.toLowerCase() + ": " + message;
        logger.log(Level.SEVERE, error, throwable);

        displayErrorDialog(null, error, throwable.toString());
    }

    /**
     * Display in a dialog and log a general error
     *
     * @param action The action that was being preformed when the error occurred
     * @param message The error that occurred
     * @param advancedDescription optional additional information about the error
     */
    public void displayError(String action, String message, String advancedDescription) {
        String error = "There was an error while " + action.toLowerCase() + ": " + message;
        if (advancedDescription != null) {
            logger.log(Level.SEVERE, error + " - " + advancedDescription);
        }else{
            logger.severe(error);
        }
        
        displayErrorDialog(null, error, advancedDescription);
    }
    
    /**
     * Display in a dialog and log a RPC error
     * 
     * @param action The action that was being preformed when the error occurred
     * @param throwable the error that needs to be handled
     */
    public void displayRPCError(String action, Throwable throwable) {
        logger.log(Level.SEVERE, "RPC error occurred while " + action.toLowerCase(), throwable);
        
        displayErrorDialog(null, TutorUserWebInterface.generateServerError(action), throwable.toString());
    }
    
    /**
     * Display an error in an error page
     * 
     * @param title the title to use for the page. If null, a generic title will be used
     * @param message the message to display. If null, a generic message will be used
     * @param details the error details. If null, no details will be shown
     */
    public void displayErrorPage(String title, String message, String details){
    	
    	String convertedTitle = null;
    	
    	if(title != null){
    		convertedTitle = DocumentUtil.convertToHtmlString(title);
    	}
    	
    	String convertedMessage = DocumentUtil.convertToHtmlString(message);
    	String convertedDetails = DocumentUtil.convertToHtmlString(details);
    	
    	fullScreenMode();
    	String backgroundUrl = BrowserSession.getInstance().getBackgroundUrl();
    	setArticleWidget(new ErrorPageWidget(convertedTitle, convertedMessage, convertedDetails, backgroundUrl));
    }
    
        /**
     * A callback interface for how the user responds to a confirmation dialog
     */
    public interface ConfirmationDialogCallback {

        /**
         * Called when the dialog is declined by the user
         */
        public void onDecline();

        /**
         * Called when the dialog is accepted by the user
         */
        public void onAccept();
    }

    /**
     * Creates a dialog box for user to confirm or deny an action
     *
     * @param title The title of the dialog box
     * @param message The message of the dialog box
     * @param callback The callback when the dialog box is responded to
     * @return DialogBox A dialog box for the user to confirm or deny an action
     */
    public static DialogBox displayConfirmationDialog(String title, String message, final ConfirmationDialogCallback callback) {
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(title);
        VerticalPanel contents = new VerticalPanel();
        contents.setSpacing(5);
        contents.add(new HTML(message));
        Button yesButton = new Button("Yes");
        yesButton.setWidth("100px");
        yesButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (callback != null) {
                    callback.onAccept();
                }
                dialogBox.hide();
            }
        });
        Button noButton = new Button("No");
        noButton.setWidth("100px");
        noButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (callback != null) {
                    callback.onDecline();
                }
                dialogBox.hide();
            }
        });
        HorizontalPanel yesNoPanel = new HorizontalPanel();
        yesNoPanel.setWidth("100%");
        yesNoPanel.setSpacing(5);
        yesNoPanel.add(noButton);
        yesNoPanel.setCellHorizontalAlignment(noButton, HasHorizontalAlignment.ALIGN_RIGHT);
        yesNoPanel.add(yesButton);
        contents.add(yesNoPanel);
        dialogBox.add(contents);

        dialogBox.center();
        dialogBox.show();
        return dialogBox;
    }

    /**
     * Displays a widget in the document article (center of the page)
     *
     * @param widget The widget to display
     * @param hasFocus If the widget has complete focus
     */
    public void setArticleWidget(Widget widget, boolean hasFocus) {
        documentContainer.showFooter(!hasFocus);
        documentContainer.setArticle(widget);
    }

    /**
     * Displays a widget in the document article (center of the page)
     *
     * @param widget The widget to display
     */
    public void setArticleWidget(Widget widget) {
    	if(widget instanceof LogoWidget) {
			//Ignoring the LogoWidget for now
    		return;
    	}
        setArticleWidget(widget, false);
    }
    
    /**
     * Set the Document background to the image specified.
     * 
     * @param imagePath if null or empty this method does nothing
     */
    public void setBackground(String imagePath){
        
        if(StringUtils.isNotBlank(imagePath)){
            com.google.gwt.dom.client.Document.get().getBody().getStyle().setProperty("background", "url("+imagePath+") no-repeat center center fixed");
            com.google.gwt.dom.client.Document.get().getBody().getStyle().setProperty("backgroundSize", "cover");
        }
    }
    
    /**
     * JSNI function used to hide the 'page loading' icon.
     */
    public static native void stopLoadingProgress()/*-{
        var loadContainer = $wnd.document.getElementById("dashboardLoadContainer");
        if (loadContainer != null) {
            loadContainer.style.display="none";
        } else {
            console.log("SEVERE: Unable to hide the loading icon, the load container was null.");
        }
    }-*/; 
    
    /**
     * Gets the widget current in the document article (center of the page)
     * 
     * @return the widget displaying
     */
     public Widget getArticleWidget() {
         return documentContainer.getArticle();
     }

    /**
     * Displays a widget in the document footer
     *
     * @param widget The widget to display
     */
    public void setFooterWidget(Widget widget) {
        documentContainer.setFooter(widget);
    }

    /**
     * The document will use the entire web browser to display
     */
    public final void fullScreenMode() {
        if (!fullscreen) {
            fullscreen = true;
            documentContainer.setSidebarMode(false);
            container.getElement().getStyle().setProperty("overflow", "auto");
        }
    }

    /**
     * The document will use only use the left side of the browser to display
     */
    public final void sideBarMode() {
        if (fullscreen) {
            fullscreen = false;
            documentContainer.setSidebarMode(true);
            container.getElement().getStyle().setProperty("overflow", "hidden");
        }
    }

    /**
     * Container for the web page
     */
    private class DocumentContainer extends FlowPanel {
        
        private FlowPanel articleContainer = new FlowPanel();
        private SplitLayoutPanel splitPanel = new SplitLayoutPanel();
        private FlowPanel footerContainer = null;
        private Widget footer = null;
        
        boolean articleFocus = false;
        
        public DocumentContainer() {
        	
            /* Setting a size for the articleContainer is unnecessary
             * since the articleContainer is using absolute positioning 
             * (specified in the 'articleContainer' and the 
             * 'articleContainerHeader' style located in the 
             * TutorUserWebInterface.css file).*/
            articleContainer.addStyleName("articleContainer");
            add(articleContainer);
            
            splitPanel.setSize("100%", "100%");
        }
        
        /** 
         * Sets the display mode.
         * 
         * @param sidebarMode whether or not to use the sidebar to display articles. 
         */
        public void setSidebarMode(boolean sidebarMode) {
        	clearArticle();
        	
        	if(sidebarMode) {
        		remove(articleContainer);
        		splitPanel.clear();
        		splitPanel.addWest(articleContainer, 417);
        		splitPanel.add(new FlowPanel());
        		add(splitPanel);
        		addStyleName("documentSideBarMode");
        	} else {
        		removeStyleName("documentSideBarMode");
        		remove(splitPanel);
        		add(articleContainer);
        	}
        }
        
        public void setArticle(Widget widget) {
            
            hideTrainingAppPlaceholderText();
            
            clearArticle();
            if (widget != null) {
                widget.addStyleName("center");
                articleContainer.add(widget);
                
                //only display the TA placeholder text if on default login or training app TUI pages
                if(widget instanceof FeedbackWidget){
                    showTrainingAppPlaceholderText();
                }
                
            }
            
        }
        
        /**
         * Removes the current article widget from its container and, if necessary,
         * notifies said widget that it has been removed by the TUI.
         */
        private void clearArticle() {
            
            //iterate through all the widgets in the article container
            Iterator<Widget> articleItr = articleContainer.iterator();
            
            while(articleItr.hasNext()) {
                
                Widget articleWidget = articleItr.next();
                
                if(articleWidget instanceof IsRemovableTuiWidget) {
                    
                    //if an article widget needs to handle its removal, let it know it was removed by GIFT
                    ((IsRemovableTuiWidget) getArticle()).onRemoval(true);
                }
            }
            
            //remove the current article widget from its container
            articleContainer.clear();
        }
        
        public Widget getArticle(){
        	if(articleContainer.getWidgetCount() > 0) {
        		return articleContainer.getWidget(0);
        	} 
        	return null;
        }

        public void setFooter(Widget widget) {
            if (footerContainer != null) {
                remove(footerContainer);
                footerContainer = null;
                articleContainer.getElement().getStyle().setProperty("bottom", "0px");
            }
            footer = widget;
            if (!articleFocus && widget != null) {
                widget.addStyleName("center");
                footerContainer = new FlowPanel();
                
                footerContainer.setWidth("100%");
                footerContainer.add(widget);
                footerContainer.addStyleName("documentFooter");
                add(footerContainer);
                
                articleContainer.getElement().getStyle().setProperty("bottom", (footerContainer.getOffsetHeight() + 10) + "px");
            }
        }

        public void showFooter(boolean show) {
            articleFocus = !show;
            if (show) {
                setFooter(footer);
            } else if (footerContainer != null) {
                remove(footerContainer);
                footerContainer = null;
                articleContainer.getElement().getStyle().setProperty("bottom", "0px");
            }
        }
    }
}
