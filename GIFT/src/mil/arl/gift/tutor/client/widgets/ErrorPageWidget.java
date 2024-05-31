/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A widget that welcomes users to an experiment and provides them with an interface with which they can begin the experiment's course
 *
 * @author nroberts
 */
public class ErrorPageWidget extends Composite implements RequiresResize {

    private FlowPanel containerPanel = new FlowPanel();

    private final SimplePanel contentWidget = new SimplePanel();

    private FlowPanel continuePanel = new FlowPanel();
    
    private TextBox bugTextBox = new TextBox();
    
    /** The location of the HTML file to use as the error page */
    private static final String ERROR_PAGE_URL = "TutorError.html";
    
    /** The ID of the iframe containing the HTML file in the DOM */
    private static final String ERROR_FRAME_ID = "experiment_tool_error_frame";
    
    /** The ID to look for in the HTML file that corresponds to the title element */
    private static final String GIFT_EXPERIMENT_ERROR_TITLE = "GIFT_EXPERIMENT_ERROR_TITLE";
    
    /** The ID to look for in the HTML file that corresponds to the main message element */
    private static final String GIFT_EXPERIMENT_ERROR_HELP_MESSAGE = "GIFT_EXPERIMENT_ERROR_HELP_MESSAGE";
    
    /** The ID to look for in the HTML file that corresponds to the details message element */
    private static final String GIFT_EXPERIMENT_ERROR_DETAILS_MESSAGE = "GIFT_EXPERIMENT_ERROR_DETAILS_MESSAGE";

    /**
     * Constructor
     *
     * @param title the title of the error page, shouldn't be null or empty.
     * @param message the high level error message to show on the page, shouldn't be null or empty.
     * @param details the low level details about the error to show on the page, shouldn't be null or empty.
     * @param backgroundUrl the path to the background image to use, shouldn't be null or empty.
     */
    public ErrorPageWidget(final String title, final String message, final String details, final String backgroundUrl) {
        initWidget(containerPanel);
            
        Frame welcomeFrame = new Frame();
        welcomeFrame.getElement().setId(ERROR_FRAME_ID);
        welcomeFrame.setSize("100%", "100%");
        welcomeFrame.setUrl(ERROR_PAGE_URL);
        welcomeFrame.getElement().getStyle().setBackgroundImage("linear-gradient(transparent, rgba(255,0,0,0.6)), url('"+backgroundUrl+"')");
        welcomeFrame.addLoadHandler(new LoadHandler() {
			
			@Override
			public void onLoad(LoadEvent event) {
				
				/* 
				 * We need to wait until the frame loads and then check to see if we can find elements within its HTML file. 
				 * The elements are identified by special DOM IDs specified by this class that can be applied to any 
				 * HTML element.  If no elements found, then this widget will use default error text.
				 */
				
				if(title != null){
					
					Element titleElement = getFrameElementById(ERROR_FRAME_ID, GIFT_EXPERIMENT_ERROR_TITLE);
					
					if(titleElement != null){
						titleElement.setInnerSafeHtml(SafeHtmlUtils.fromTrustedString(title));
					}
				}
				
				if(message != null){
					
					Element messageElement = getFrameElementById(ERROR_FRAME_ID, GIFT_EXPERIMENT_ERROR_HELP_MESSAGE);
					
					if(messageElement != null){
						messageElement.setInnerSafeHtml(SafeHtmlUtils.fromTrustedString(message));
					}
				}
				
				if(details != null){
					
					Element detailsElement = getFrameElementById(ERROR_FRAME_ID, GIFT_EXPERIMENT_ERROR_DETAILS_MESSAGE);
					
					if(detailsElement != null){
						detailsElement.setInnerSafeHtml(SafeHtmlUtils.fromTrustedString(details));
					}
				}
		        
			}
		});

        contentWidget.setWidget(welcomeFrame);
        containerPanel.add(contentWidget);
        containerPanel.add(bugTextBox); 
        
        // This text box fixes a bug that prevents text from being selected.
        ScheduledCommand command = new ScheduledCommand() {
			@Override
			public void execute() {
				bugTextBox.setFocus(true);
				bugTextBox.setVisible(false);
			}
		};
        Scheduler.get().scheduleDeferred(command);
        
        Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				contentWidget.setHeight((event.getHeight() - 10) + "px");
			}
        	
        });
                
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        onResize();
    }

    @Override
    public void onResize() {
    	contentWidget.setHeight((Window.getClientHeight() - 10) + "px");
    }
    
    /**
     * Returns the element with the given ID inside the element with the given frame ID
     * 
     * @param frameId the ID of the frame in which to find the element
     * @param elementId the ID of the frame element
     * @return the element
     */
    private native Element getFrameElementById(String frameId, String elementId)/*-{
    	return $doc.getElementById(frameId).contentWindow.document.getElementById(elementId);
    }-*/;
}
