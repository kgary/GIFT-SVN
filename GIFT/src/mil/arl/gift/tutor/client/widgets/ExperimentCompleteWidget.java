/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;

import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.iframe.messages.EndCourseMessage;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.TutorUserWebInterface;
import mil.arl.gift.tutor.shared.WidgetInstance;

/**
 * A widget that thanks the user for participating in to an experiment
 *
 * @author nroberts
 */
public class ExperimentCompleteWidget extends Composite implements RequiresResize {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ExperimentCompleteWidget.class.getName());

    private FlowPanel containerPanel = new FlowPanel();

    private final SimplePanel contentWidget = new SimplePanel();
    
    /** The location of the HTML file to use as the completion page */
    private static final String COMPLETE_PAGE_URL = "TutorExperimentComplete.html";
    
    /** The ID of the iframe containing the HTML file in the DOM */
    private static final String COMPLETE_FRAME_ID = "experiment_tool_complete_frame";
    
    private static final String GIFT_EXPERIMENT_SUCCESS_MESSAGE = "GIFT_EXPERIMENT_SUCCESS_MESSAGE";

    /**
     * The ID to look for in the HTML file that corresponds to the button used
     * to go to the dashboard page
     */
    private static final String GO_TO_DASHBOARD_BUTTON_ID = "goToDashboardButton";

    /**
     * Constructor
     *
     * @param instance The instance of the experiment complete widget containing needed property information
     */
    public ExperimentCompleteWidget(WidgetInstance instance) {
        
        this(null, instance);     
    }
    
    /**
     * Constructor
     *
     * @param completionMessage the completion text to show
     * @param instance The instance of the experiment complete widget containing needed property information
     */
    public ExperimentCompleteWidget(final String completionMessage, WidgetInstance instance) {
        
        initWidget(containerPanel);
            
        Frame welcomeFrame = new Frame();
        welcomeFrame.getElement().setId(COMPLETE_FRAME_ID);
        welcomeFrame.getElement().getStyle().setProperty("border", "none");
        welcomeFrame.setSize("100%", "100%");
        welcomeFrame.setUrl(COMPLETE_PAGE_URL);    
        welcomeFrame.addLoadHandler(new LoadHandler() {
			
			@Override
			public void onLoad(LoadEvent event) {
				
				/* 
				 * We need to wait until the frame loads and then check to see if we can find elements within its HTML file. 
				 * The elements are identified by special DOM IDs specified by this class that can be applied to any 
				 * HTML element.  If no elements found, then this widget will use default error text.
				 */

				if(completionMessage != null){
					
					Element messageElement = getFrameElementById(COMPLETE_FRAME_ID, GIFT_EXPERIMENT_SUCCESS_MESSAGE);
					
					if(messageElement != null){
						messageElement.setInnerText(completionMessage);
					}
				}

                if (TutorUserWebInterface.isEmbedded()) {
                    IFrameMessageHandlerChild.getInstance().sendMessage(new EndCourseMessage());
                }

                /* Update 'go to dashboard' button */
                final String experimentReturnUrl = TutorUserWebInterface.getExperimentReturnUrl();
                if (StringUtils.isNotBlank(experimentReturnUrl)) {
                    final Element dashboardButtonElement = getFrameElementById(COMPLETE_FRAME_ID,
                            GO_TO_DASHBOARD_BUTTON_ID);
                    if (dashboardButtonElement != null) {
                        /* Make button visible */
                        dashboardButtonElement.getStyle().setDisplay(Display.INLINE_BLOCK);

                        Event.sinkEvents(dashboardButtonElement, Event.ONCLICK);
                        Event.setEventListener(dashboardButtonElement, new EventListener() {
                            @Override
                            public void onBrowserEvent(Event event) {
                                if (Event.ONCLICK == event.getTypeInt()) {
                                    if (TutorUserWebInterface.isEmbedded()) {
                                        EndCourseMessage endCourseMessage = new EndCourseMessage();
                                        endCourseMessage.setExperimentReturnUrl(experimentReturnUrl);
                                        IFrameMessageHandlerChild.getInstance().sendMessage(endCourseMessage);
                                    }
                                }
                            }
                        });
                    }
                }

                /* Close the user session */
                BrowserSession.getInstance().userEndDomainSessionAndLogout(new AsyncCallback<RpcResponse>() {
                    @Override
                    public void onSuccess(RpcResponse result) {
                        /* do nothing */
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        /* Best effort. The logout should happen on timeout
                         * anyway. */
                    }
                });
			}
		});

        contentWidget.setWidget(welcomeFrame);
        containerPanel.add(contentWidget); 
        Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				contentWidget.setHeight((event.getHeight() - 10) + "px"); 
			}
        	
        });
        
        // Once the experiment complete widget is shown, the client disconnects itself from the server (cleans up the session
        // on the server since the user is done).  This allows the user to keep the webpage open, but allow the server resources
        // to be cleaned up.
        BrowserSession.getInstance().disconnectWebSocket();
        
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
