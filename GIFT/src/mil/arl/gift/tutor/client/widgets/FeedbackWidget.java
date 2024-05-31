/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.InTutor;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayScriptedAvatarAction;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.common.PlayAudioAction;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.enums.TextFeedbackDisplayEnum;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.conversations.AbstractUpdateQueue;
import mil.arl.gift.tutor.client.conversations.UpdateCounterWidget;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.FeedbackWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * Widget for displaying feedback to the user
 *
 * @author jleonard
 */
public class FeedbackWidget extends AbstractUpdateQueue {

    /** The logger for the class */
	private static Logger logger = Logger.getLogger(FeedbackWidget.class.getName());

    /**
     * Timer for flashing new feedback
     */
    private Timer flashTimer = new Timer() {

        private int iterationsLeft = 20;

        @Override
        public void run() {
            highlightNewFeedback(iterationsLeft % 2 == 0);

            if (iterationsLeft > 0) {
                iterationsLeft -= 1;
            } else {
                cancel();
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            WidgetProperties properties = new WidgetProperties();
            FeedbackWidgetProperties.setHasNewFeedback(properties, false);
            SubmitAction submitAction = new SubmitAction(instance.getWidgetId(), properties);

            if (BrowserSession.getInstance() != null) {

                BrowserSession.getInstance().sendActionToServer(submitAction, null);
            }

            highlightNewFeedback(false);

            // Reset iterations
            iterationsLeft = 20;
        }
    };

    private static FeedbackWidgetUiBinder uiBinder = GWT.create(FeedbackWidgetUiBinder.class);

    @UiField
    ScrollPanel feedbackScrollPanel;

    @UiField
    FlowPanel feedbackWrapper;

    @UiField
    FlowPanel feedbackPanel;

    private UpdateCounterWidget counter;

    private final WidgetInstance instance;

    private Widget newFeedbackWidget;

    interface FeedbackWidgetUiBinder extends UiBinder<Widget, FeedbackWidget> {
    }

    /**
     * Constructor
     *
     * @param instance The abstract instance of the widget
     * @param isActive Whether or not this widget will immediately present updates to the client.
     */
    public FeedbackWidget(WidgetInstance instance, boolean isActive) {

        if(logger.isLoggable(Level.INFO)){
            logger.info("Creating Feedback Widget");
        }

        this.instance = instance;
        Widget uiBinderWidget = uiBinder.createAndBindUi(this);
        if(!instance.getWidgetProperties().isTutorTest()){
        	setIsActive(isActive);
        }
        counter = new UpdateCounterWidget();
        setUpdateCounter(counter);

        updateFeedback(instance);

        initWidget(uiBinderWidget);
    }

    public void updateFeedback(WidgetInstance instance) {

        if(logger.isLoggable(Level.INFO)){
            logger.info("Updating feedback widget with:\n"+instance);
        }

    	WidgetProperties properties = instance.getWidgetProperties();

    	if (properties != null) {

    		feedbackWrapper.setVisible(false);

            //Note: this will contain all of the feedback requests thus far
            ArrayList<TutorUserInterfaceFeedback> feedbacks = FeedbackWidgetProperties.getFeedback(properties);

            //whether there are any feedback requests that haven't been handled yet
            //Note: this boolean only relates to the first un-handled feedback if there is more than one.  This is a quick
            //      solution for not implementing every new feedback's delivery settings which could be presented horribly.
            //      In the future, scripting logic maybe created to handle delaying feedback(s) to prevent interleaving over each other.
            boolean hasNewFeedback = FeedbackWidgetProperties.hasNewFeedback(properties);

            if(hasNewFeedback){
                logger.info("Handling new feedback content");
            }

            TutorUserInterfaceFeedback newFeedback = hasNewFeedback ? FeedbackWidgetProperties.getFeedback(properties).get(0) : null;

            if (feedbacks != null) {

                if (newFeedback != null) {

                    //
                    // Decide what audio will be played, trying not to interleave audio tracks if possible
                    //

                    if (newFeedback.getDisplayTextAction() != null && newFeedback.getPlayAudioAction() == null) {
                        //check if 'beep' should be played based on
                        // 1) feedback message is text and there is not other audio file (e.g. mp3) being played [default behavior]
                        // 2) feedback message is text and 'beep' text enhancement was authored

                        InTutor deliverySettings = newFeedback.getDisplayTextAction().getDeliverySettings();

                        /* Determine if the new feedback has a beep command */
                        boolean hasBeep = false;
                        if (deliverySettings != null && deliverySettings.getTextEnhancement() != null) {
                            final String BEEP_ONLY = TextFeedbackDisplayEnum.BEEP_ONLY.getName();
                            final String BEEP_AND_FLASH = TextFeedbackDisplayEnum.BEEP_AND_FLASH.getName();

                            final String textEnhancement = deliverySettings.getTextEnhancement();
                            hasBeep = StringUtils.equalsIgnoreCase(textEnhancement, BEEP_ONLY)
                                    || StringUtils.equalsIgnoreCase(textEnhancement, BEEP_AND_FLASH);

                        }

                        if (hasNewFeedback && (deliverySettings == null || hasBeep)) {
                            Document.playBeep();
                        }
                    }

                    /* Choose to play an avatar scripted action with audio over
                     * playing an audio file, don't do both as the audio can
                     * become interlaced. */
                    if (newFeedback.getDisplayAvatarAction() != null) {

                        DisplayAvatarAction displayAction = newFeedback.getDisplayAvatarAction();

                        AvatarData avatar = displayAction.getAvatar();

                        if (avatar != null) {

                            if (hasNewFeedback) {

                                if (displayAction instanceof DisplayScriptedAvatarAction) {

                                    DisplayScriptedAvatarAction scriptedAction = (DisplayScriptedAvatarAction) displayAction;

                                    if(!AvatarContainer.hasAvatarData()) {
                                    	AvatarContainer.defineSayFeedbackStaticMessage(scriptedAction.getAction());
                                    	AvatarContainer.setAvatarData(avatar);
                                    } else {
                                    	AvatarContainer.sayFeedbackStaticMessage(scriptedAction.getAction());
                                    }

                                } else if (displayAction instanceof DisplayTextToSpeechAvatarAction) {

                                    DisplayTextToSpeechAvatarAction ttsAction = (DisplayTextToSpeechAvatarAction) displayAction;

                                    if(!AvatarContainer.hasAvatarData()) {
                                    	AvatarContainer.defineSayFeedback(stripFeedback(ttsAction.getText()));
                                    	AvatarContainer.setAvatarData(avatar);
                                    } else {
                                    	AvatarContainer.sayFeedback(stripFeedback(ttsAction.getText()));
                                    }

                                } else if(displayAction.getAvatar() != null){

                                	AvatarContainer.setAvatarData(avatar);
                                }
                            }

                        } else {
                            //WHY IS THIS A WARNING?
                            logger.warning("Got null avatar therefore not displaying avatar container or playing avatar action/audio.");
                        }

                    } else if (newFeedback.getPlayAudioAction() != null) {

                        PlayAudioAction playAction = newFeedback.getPlayAudioAction();

                        if (hasNewFeedback) {

                        	AvatarContainer.playAudio(playAction.getMp3AudioFile(), playAction.getOggAudioFile(), getElement());
                        }

                    }


                } else {

                    // Even if there is no new feedback, display the avatar that
                    // was previous seen (if there was one)

                    if (!feedbacks.isEmpty()) {

                        TutorUserInterfaceFeedback previousFeedback = feedbacks.get(0);

                        if (previousFeedback.getDisplayAvatarAction() != null) {

                            DisplayAvatarAction displayAction = previousFeedback.getDisplayAvatarAction();

                            AvatarData avatar = displayAction.getAvatar();

                            AvatarContainer.setAvatarData(avatar);
                        }
                    }
                }

                //Document.getInstance().displayDialog("debug", "there are "+(feedbacks!= null ? feedbacks.size() : 0)+" feedback(s)");

                if(feedbackPanel.getWidgetCount() > 0) {
                    if(FeedbackWidgetProperties.getOldFeedbackStyleEnabled(properties)){
                        feedbackPanel.getWidget(0).addStyleName("oldFeedbackText");
                    }
                	feedbackPanel.getWidget(0).removeStyleName("flashBackground");
                }

                //
                // Apply (other) feedback message text enhancements
                //
                for (TutorUserInterfaceFeedback feedback : feedbacks) {

                    if (feedback.getDisplayTextAction() != null) {

                        logger.info("Adding feedback string to display");
                        InTutor deliverySettings = feedback.getDisplayTextAction().getDeliverySettings();
                        String displayedText = feedback.getDisplayTextAction().getDisplayedText();
                        if (StringUtils.isNotBlank(displayedText)) {
                            HTML feedbackLabel = new HTML(
                                    "<p style=\"font-size:14px;text-align:center;\">" + displayedText + "</p>");
                            feedbackLabel.addStyleName("feedbackText");
                            feedbackPanel.insert(feedbackLabel, 0);

                            if (hasNewFeedback) {

                                newFeedbackWidget = feedbackLabel;

                                /* Determine if the feedback has a 'flash'
                                 * defined */
                                boolean hasFlash = false;
                                if (deliverySettings != null && deliverySettings.getTextEnhancement() != null) {
                                    final String FLASH_ONLY = TextFeedbackDisplayEnum.FLASH_ONLY.getName();
                                    final String BEEP_AND_FLASH = TextFeedbackDisplayEnum.BEEP_AND_FLASH.getName();
                                    String textEnhancement = deliverySettings.getTextEnhancement();

                                    hasFlash = FLASH_ONLY.equals(textEnhancement)
                                            || BEEP_AND_FLASH.equals(textEnhancement);
                                }

                                if (deliverySettings == null || hasFlash) {

                                    // Debugging...
                                    // String settings = deliverySettings != null ? deliverySettings.getTextEnhancement() : "null";
                                    // Window.alert("enabling flash timer;"+settings);

                                    flashTimer.scheduleRepeating(250);

                                } else {

                                    FeedbackWidgetProperties.setHasNewFeedback(properties, false);
                                    SubmitAction submitAction = new SubmitAction(instance.getWidgetId(), properties);

                                    if (BrowserSession.getInstance() != null) {

                                        BrowserSession.getInstance().sendActionToServer(submitAction, null);
                                    }

                                    if (flashTimer.isRunning()) {
                                        flashTimer.cancel();
                                    }

                                    highlightNewFeedback(false);
                                }

                            } else if(FeedbackWidgetProperties.getOldFeedbackStyleEnabled(properties)) {

                                feedbackLabel.addStyleName("oldFeedbackText");
                            }
                        }

                    }

                    //(again) this flag is only associated with the first un-handled 'new' feedback request
                    hasNewFeedback = false;
                }//end for

                //
                // Show HTML feedback
                //
                for (TutorUserInterfaceFeedback feedback : feedbacks) {

                    if(feedback.getDisplayHTMLAction() != null){

                        HTML feedbackLabel = new HTML("<p style=\"font-size:14px;text-align:center;\"><a target=\"_blank\" href=\"" + feedback.getDisplayHTMLAction().getDomainURL() + "\">Click here to view your feedback!</a></p>");
                        feedbackLabel.addStyleName("feedbackText");
                        feedbackPanel.add(feedbackLabel);
                    }
                }

                feedbackWrapper.setVisible(feedbackPanel.getWidgetCount() > 0);
            }

            if(feedbacks != null && !feedbacks.isEmpty()) {
            	for(TutorUserInterfaceFeedback feedback : feedbacks) {
	            	if(!(feedback.getDisplayAvatarAction() == null && feedback.getDisplayTextAction() == null)) {
	            		/* Check if the list of feedback has been cleared.
	            		 * If there is feedback to display, don't use fullscreen mode.*/
	            		properties.setIsFullscreen(false);
	            		break;
	            	}
            	}
            }
        }

    	if(dequeuing) {
    		dequeueUpdates();
    	}else{
    	    logger.info("not dequeuing feedback because flag is false.");
    	}

    }
    
    @Override
    protected void onUnload() {
    	clearFeedback();
    	pauseAllAudio();
    }

    /**
     * Clears all of the received feedback messages from this widget
     */
    public void clearFeedback(){
    	if(feedbackPanel.getWidgetCount() > 0 ) {
    		feedbackPanel.clear();
    		feedbackWrapper.setVisible(false);
    	}
    }
    
    /**
     * Pauses any audio that was started by this widget
     */
    public void pauseAllAudio() {
        
        //get all of the direct child elements appended to this widget
        NodeList<Node> nodes = getElement().getChildNodes();
        
        //look for any audio elements
        for(int i = 0; i < nodes.getLength(); i++) {
            
            Node node = nodes.getItem(i);
            if(Node.ELEMENT_NODE == node.getNodeType()) {
                
                Element element = node.cast();
                if(AudioElement.TAG.equals(element.getTagName())){
                    
                    //if an audio element is found, pause it
                    AudioElement audio = element.cast();
                    audio.pause();
                }
                
            }
        }
    }

    /**
     * Highlight the newest feedback
     *
     * @param highlight If the newest feedback should be highlighted
     */
    private void highlightNewFeedback(boolean highlight) {
        if (newFeedbackWidget != null) {
            if (highlight) {
                newFeedbackWidget.addStyleName("flashBackground");
            } else {
                newFeedbackWidget.removeStyleName("flashBackground");
            }
        }
    }

    /**
     * Remove feedback of unparsable text before sending to the speech engine
     *
     * Removes HTML tags and text in parentheses.
     *
     * @param feedback The feedback to clean up
     * @return String The speech engine parsable text
     */
    private String stripFeedback(String feedback) {
        String newFeedback = feedback;
        newFeedback = newFeedback.replaceAll("<(.|\n)*?>", "");
        newFeedback = newFeedback.replaceAll("\\((.|\n)*?\\)", "");
        return newFeedback;
    }

	@Override
	public void updateServerStatus() {

        if(logger.isLoggable(Level.INFO)){
            logger.info("Notify server that feedback widget is active = "+isActive());
        }
		BrowserSession.setFeedbackWidgetIsActive(isActive(), new AsyncCallback<RpcResponse>() {

			@Override
			public void onFailure(Throwable thrown) {
				logger.warning("Failed to update the FeedbackWidget status on the server: " + thrown.toString());
			}

			@Override
			public void onSuccess(RpcResponse result) {
				if(result.isSuccess()) {
					dequeueUpdates();
				}
			}

		});
	}

	@Override
	public void dequeueUpdates() {
		if(isActive()) {
			dequeuing = true;

			if(logger.isLoggable(Level.INFO)){
			    logger.info("Requesting dequeue feedback widget update(s)");
			}
			BrowserSession.dequeueFeedbackWidgetUpdate(new AsyncCallback<RpcResponse>() {

				@Override
				public void onFailure(Throwable thrown) {
					logger.warning("Failed to retrieve a feedback update from the server: " + thrown.toString());
				}

				@Override
				public void onSuccess(RpcResponse result) {
					if(result.isSuccess()) {
						decrementCounter();
					} else {
						// No more updates, or updates are queued on the server
						counter.setCount(0);
						logger.info("setting dequeing flag to false because server returned false for dequeueFeedbackWidgetUpdate call.");
						dequeuing = false;
					}
				}

			});
		} else {
			dequeuing = false;
		}
	}

    @Override
    public String toString(){

        StringBuilder sb = new StringBuilder();
        sb.append("[FeedbackWidget: ");
        sb.append("]");

        return sb.toString();
    }
}
