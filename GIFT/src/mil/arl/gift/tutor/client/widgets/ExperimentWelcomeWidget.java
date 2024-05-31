/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;

import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.TutorUserWebInterface;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.ExperimentWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * A widget that welcomes users to an experiment and provides them with an interface with which they can begin the experiment's course
 *
 * @author nroberts
 */
public class ExperimentWelcomeWidget extends Composite implements RequiresResize {

    /** Panel that contains the content widget */
    private FlowPanel containerPanel = new FlowPanel();

    /** Contains the welcome frame */
    private final SimplePanel contentWidget = new SimplePanel();
    
    /** The button to start the experiment */
    private final Button continueButton = new Button("Start");
    
    /** The button to resume an experiment */
    private final Button resumeButton = new Button("Return to Experiment");
    
    /** The button to exit */
    private final Button leaveButton = new Button("Get me out of here!");

    /** The panel that contains the action buttons */
    private FlowPanel continuePanel = new FlowPanel();
    
    /** Displays text to the user */
    private Element loadingTextElement = null;
    
    /** The location of the HTML file to use as the welcome page */
    private static final String WELCOME_PAGE_URL = "TutorExperimentWelcome.html";
    
    /** The ID of the iframe containing the welcome page's HTML file in the DOM */
    private static final String WELCOME_FRAME_ID = "experiment_tool_welcome_frame";
    
    /** The ID to look for in the HTML file that corresponds to the button used to start the experiment */
    private static final String START_BUTTON_ID = "startExperimentButton";
    
    /** The ID to look for in the HTML file that corresponds to the loading indicator */
    private static final String GIFT_WELCOME_EXPERIMENT_LOADING_INDICATOR = "GIFT_WELCOME_EXPERIMENT_LOADING_INDICATOR";
    
    /** The ID to look for in the HTML file that corresponds to the loading indicator's text label */
    private static final String GIFT_WELCOME_EXPERIMENT_LOADING_TEXT = "GIFT_WELCOME_EXPERIMENT_LOADING_TEXT";
    
    
    /** The location of the HTML file to use as the resume page */
    private static final String RESUME_PAGE_URL = "TutorExperimentResume.html";
    
    /** The ID of the iframe containing the resume page's HTML file in the DOM */
    private static final String RESUME_FRAME_ID = "experiment_tool_resume_frame";
    
    /** The ID to look for in the HTML file that corresponds to the button used to resume the experiment */
    private static final String RESUME_BUTTON_ID = "GIFT_RESUME_EXPERIMENT_BUTTON";
    
    /** The ID to look for in the HTML file that corresponds to the button used to leave the experiment */
    private static final String LEAVE_BUTTON_ID = "GIFT_LEAVE_EXPERIMENT_BUTTON";
    
    /** The ID to look for in the HTML file that corresponds to the text that should be shown when the back button is pressed */
    private static final String GIFT_LEAVE_EXPERIMENT_BACK_TEXT = "GIFT_LEAVE_EXPERIMENT_BACK_TEXT";

    /**
     * Constructor
     *
     * @param instance The instance of the experiment welcome widget containing needed property information
     */
    public ExperimentWelcomeWidget(WidgetInstance instance) {
    	
    	History.newItem(TutorUserWebInterface.EXPERIMENT_PAGE_TAG, false);
    	
        final WidgetProperties properties = instance.getWidgetProperties();
        initWidget(containerPanel);
        containerPanel.add(contentWidget);
        
        Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				contentWidget.setHeight((event.getHeight() - 10) + "px");
			}
        	
        });
        
        if(BrowserSession.getInstance() != null){
        	
            BrowserSession.getInstance().getActiveDomainSessionName(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                    //TODO: Display an error that the active domain session name can not be retrieved
                }

                @Override
                public void onSuccess(String result) {
                    if (result != null) {
                        // insert new history token so that if we refresh or click 'back' again, we will still have access to the user session id.
                        History.newItem(TutorUserWebInterface.buildUserSessionHistoryTag(BrowserSession.getInstance().getUserSessionKey()), false);
                        displayResumeDomainSession(result, properties);
                    } else {
                        displayWelcomePage(properties);
                    }
                   
                    if(!isAttached()){
                    	addAttachHandler(new AttachEvent.Handler(){

							@Override
							public void onAttachOrDetach(AttachEvent event) {
								if(event.isAttached()){								
									onResize();
								}
							}
                    		
                    	});
                    
                    } else {
                    	onResize();
                    }
                }
            });
            
        } else {
        	
        	displayWelcomePage(properties);
        	
        	 if(!isAttached()){
             	addAttachHandler(new AttachEvent.Handler(){

						@Override
						public void onAttachOrDetach(AttachEvent event) {
							if(event.isAttached()){								
								onResize();
							}
						}
             		
             	});
             
             } else {
             	onResize();
             }
        }
    }
    
    @Override
	public void onResize() {
    	contentWidget.setHeight((Window.getClientHeight() - 10) + "px");
	}
    
    /**
     * Displays the experiment welcome page. This page contains the button to start the experiment.
     * 
     * @param properties the properties of the widget.
     */
    private void displayWelcomePage(final WidgetProperties properties){

        // show non-tested browser dialog
        if (JsniUtility.isIEBrowser()) {
            Document.getInstance().displayDialog("Unsupported browser type",
                    "GIFT has not been thoroughly tested using this browser version.</br></br>Please use a modern version of Chrome, Firefox or Edge for the best experience.");
        }

        if (properties != null) {
            final String experimentId = ExperimentWidgetProperties.getExperimentId(properties);
            final String experimentFolder = ExperimentWidgetProperties.getExperimentFolder(properties);

            /* If experiment folder is not blank then we are using the 'new'
             * pipeline for experiments which go through Dashboard. The welcome
             * page has already been shown there, so skip right into
             * startExperiment(). */
            if (StringUtils.isNotBlank(experimentFolder)) {
                startExperiment(experimentId, experimentFolder);
                return;
            }

            continueButton.getElement().setId("GIFT-Experiment-Start-Button");
            continueButton.addStyleName("guidanceContinueButton");
            
            Frame welcomeFrame = new Frame();
            welcomeFrame.getElement().setId(WELCOME_FRAME_ID);
            welcomeFrame.getElement().getStyle().setProperty("border", "none");
            welcomeFrame.setSize("100%", "100%");
            welcomeFrame.setUrl(WELCOME_PAGE_URL);
            welcomeFrame.addLoadHandler(new LoadHandler() {
				
				@Override
				public void onLoad(LoadEvent event) {
					
					/* 
					 * We need to wait until the frame loads and then check to see if we can find a start button within its HTML file. 
					 * The start button is identified by a special DOM ID specified by this class that can be applied to any 
					 * clickable HTML element.  If no such button is found, then this widget will add its own start button below the 
					 * frame.
					 */
					
					final Element startButtonElement = getFrameElementById(WELCOME_FRAME_ID, START_BUTTON_ID);
					final Element loadingIndicatorElement = getFrameElementById(WELCOME_FRAME_ID, GIFT_WELCOME_EXPERIMENT_LOADING_INDICATOR);
					loadingTextElement = getFrameElementById(WELCOME_FRAME_ID, GIFT_WELCOME_EXPERIMENT_LOADING_TEXT);
			        
			        if(startButtonElement != null){
			        	
			            Event.sinkEvents(startButtonElement, Event.ONCLICK);
			            Event.setEventListener(startButtonElement, new EventListener() {
							
							@Override
							public void onBrowserEvent(Event event) {
								
								if(Event.ONCLICK == event.getTypeInt()){
									startExperiment(experimentId, null);
									
									startButtonElement.setAttribute("disabled", "true");
									startButtonElement.getStyle().setProperty("display", "none");
									
									if(loadingTextElement != null){
										loadingTextElement.setInnerText("Retrieving experiment...");
									}
									
									if(loadingIndicatorElement != null){
										loadingIndicatorElement.getStyle().setProperty("display", "block");
									}
								}
							}
						});
			            
			        } else  {

			            continuePanel.addStyleName("guidanceContinuePanel");
			            continueButton.addAttachHandler(new AttachEvent.Handler() {

			                @Override
			                public void onAttachOrDetach(AttachEvent event) {
			                    
			                    continuePanel.setWidth(continueButton.getOffsetWidth() + "px");
			                }
			            });
			            continuePanel.add(continueButton);
			            containerPanel.add(continuePanel);

			            continueButton.addClickHandler(new ClickHandler() {
			                @Override
			                public void onClick(ClickEvent event) {
			                    continueButton.setEnabled(false);
			                    
			                    startExperiment(experimentId, experimentFolder);
			                }
			            });
			        }
				}
			});

            contentWidget.setWidget(welcomeFrame);
        }
    }

    /**
     * Returns the element with the given ID inside the element with the given frame ID
     * 
     * @param frameId the ID of the frame in which to find the button
     * @param elementId the ID of the element
     * @return the element
     */
    private native Element getFrameElementById(String frameId, String elementId)/*-{
    	return $doc.getElementById(frameId).contentWindow.document.getElementById(elementId);
    }-*/;
    
    /**
     * Starts the experiment represented by this widget
     * 
     * @param experimentId the id of the experiment to start
     * @param experimentFolder the runtime folder of the experiment to start.
     *        Can be null if the experiment is legacy.
     */
    private void startExperiment(String experimentId, String experimentFolder){
    	
    	BrowserSession.startExperimentCourse(experimentId, experimentFolder, new AsyncCallback<RpcResponse>(){

    		@Override
            public void onFailure(Throwable caught) {
    			
				Document.getInstance().displayError("Starting the experiment course", caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if(!result.isSuccess()){
                   Document.getInstance().displayError("Starting the experiment course", result.getResponse(), result.getAdditionalInformation());
                   
                } else {
                	
                	if(loadingTextElement != null){
						loadingTextElement.setInnerText("Loading course...");
					}
                }
            }
    		
    	});
    }
    
    /**
     * Displays the options for a currently active domain session
     *
     * @param activeCourseName The name of the active domain
     * @param properties the widget properties
     */
    private void displayResumeDomainSession(String activeCourseName, final WidgetProperties properties) {

        resumeButton.addStyleName("guidanceContinueButton");
        leaveButton.addStyleName("guidanceContinueButton");
        
        Frame welcomeFrame = new Frame();
        welcomeFrame.getElement().setId(RESUME_FRAME_ID);
        welcomeFrame.getElement().getStyle().setProperty("border", "none");
        welcomeFrame.setSize("100%", "100%");
        welcomeFrame.setUrl(RESUME_PAGE_URL);
        welcomeFrame.addLoadHandler(new LoadHandler() {
			
			@Override
			public void onLoad(LoadEvent event) {
				/* 
				 * We need to wait until the frame loads and then check to see if we can find a start button within its HTML file. 
				 * The start button is identified by a special DOM ID specified by this class that can be applied to any 
				 * clickable HTML element.  If no such button is found, then this widget will add its own start button below the 
				 * frame.
				 */			    
                
                Document.stopLoadingProgress();
				
				final Element resumeButtonElement = getFrameElementById(RESUME_FRAME_ID, RESUME_BUTTON_ID);
				final Element leaveButtonElement = getFrameElementById(RESUME_FRAME_ID, LEAVE_BUTTON_ID);

		        if(resumeButtonElement != null && leaveButtonElement != null){
		        	
		            Event.sinkEvents(resumeButtonElement, Event.ONCLICK);
		            Event.setEventListener(resumeButtonElement, new EventListener() {
						@Override
						public void onBrowserEvent(Event event) {
                            if (Event.ONCLICK == event.getTypeInt()) {
                                disableButtons(resumeButtonElement, leaveButtonElement);
                                BrowserSession.getInstance().resumeDomainSession();
                            }
						}
					});

                    Event.sinkEvents(leaveButtonElement, Event.ONCLICK);
                    Event.setEventListener(leaveButtonElement, new EventListener() {
                        @Override
                        public void onBrowserEvent(Event event) {
                            if (Event.ONCLICK == event.getTypeInt()) {
                                disableButtons(resumeButtonElement, leaveButtonElement);
                                userEndDomainSessionAndLogout();
                            }
                        }
                    });
		            
		        } else  {

		            continuePanel.addStyleName("guidanceContinuePanel");

		            continuePanel.add(resumeButton);
		            continuePanel.add(leaveButton);
		            containerPanel.add(continuePanel);

		            resumeButton.addClickHandler(new ClickHandler() {
		                @Override
		                public void onClick(ClickEvent event) {
		                    resumeButton.setEnabled(false);
		                    leaveButton.setEnabled(false);
		                    BrowserSession.getInstance().resumeDomainSession();
		                }
		            });

		            leaveButton.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            resumeButton.setEnabled(false);
                            leaveButton.setEnabled(false);
                            userEndDomainSessionAndLogout();
                        }
                    });
		        }
		        
		        boolean backButtonPressed = ExperimentWidgetProperties.getBackButtonPressed(properties);
		        if(backButtonPressed){
		        
			        Element backTextElement = getFrameElementById(RESUME_FRAME_ID, GIFT_LEAVE_EXPERIMENT_BACK_TEXT);
			        
			        if(backTextElement != null){
			        	backTextElement.getStyle().clearProperty("display");
			        }
		        }

                if (TutorUserWebInterface.isEmbedded()) {
                    IFrameSimpleMessage courseStartingMsg = new IFrameSimpleMessage(IFrameMessageType.COURSE_STARTING);
                    IFrameMessageHandlerChild.getInstance().sendMessage(courseStartingMsg);
                }
			}

			/**
			 * Disable the resume and leave button elements.
			 * @param resumeButtonElement the resume button element to disable
			 * @param leaveButtonElement the leave button element to disable
			 */
            private void disableButtons(Element resumeButtonElement, Element leaveButtonElement) {
                if (resumeButtonElement != null) {
                    resumeButtonElement.setPropertyString("disabled", "true");
                    resumeButtonElement.getStyle().setCursor(Cursor.DEFAULT);
                    resumeButtonElement.getStyle().setBackgroundImage(
                            "linear-gradient(rgb(100, 200, 100, 0.5), rgb(75, 165, 75, 0.5) 50%, rgb(50, 125, 50, 0.5))");
                }

                if (leaveButtonElement != null) {
                    leaveButtonElement.setPropertyString("disabled", "true");
                    leaveButtonElement.getStyle().setCursor(Cursor.DEFAULT);
                    leaveButtonElement.getStyle().setBackgroundImage(
                            "linear-gradient(rgb(220, 0, 0, 0.5), rgb(185, 0, 0, 0.5) 50%, rgb(130, 0, 0, 0.5))");
                }
            }
		});

        contentWidget.setWidget(welcomeFrame);
    
    }

    /**
     * Exiting the session. Logout user.
     */
    private void userEndDomainSessionAndLogout() {
        /* Close the user session */
        BrowserSession.getInstance().userEndDomainSessionAndLogout(new AsyncCallback<RpcResponse>() {
            @Override
            public void onSuccess(RpcResponse result) {
                /* do nothing */
            }

            @Override
            public void onFailure(Throwable caught) {
                /* Best effort. The logout should happen on timeout anyway. */
            }
        });
    }
}
