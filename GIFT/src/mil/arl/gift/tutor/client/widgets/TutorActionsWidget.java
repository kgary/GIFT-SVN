/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.conversations.ConversationsListWidget;
import mil.arl.gift.tutor.client.conversations.UpdateCounterWidget;
import mil.arl.gift.tutor.shared.ScenarioControls;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.LessonStatusAction.LessonStatus;
import mil.arl.gift.tutor.shared.properties.FeedbackWidgetProperties;
import mil.arl.gift.tutor.shared.properties.UserActionWidgetProperties;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.TabShowEvent;
import org.gwtbootstrap3.client.shared.event.TabShowHandler;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget containing a toolbar that can be used to switch views.
 * 
 * @author bzahid
 */
public class TutorActionsWidget extends Composite {

	interface TutorActionsWidgetUiBinder extends UiBinder<Widget, TutorActionsWidget> {
	}
		
	private static TutorActionsWidgetUiBinder uiBinder = GWT.create(TutorActionsWidgetUiBinder.class);
	
	private static Logger logger = Logger.getLogger(TutorActionsWidget.class.getName());
		
	@UiField
    protected TabListItem feedbackTab;
    
    @UiField
    protected TabPane feedbackTabPane;
    
    @UiField
    protected FlowPanel feedbackPanel;
    
    @UiField
    protected TabListItem conversationsTab;    
    
    @UiField
    protected TabPane conversationsTabPane;
    
    @UiField
    protected FlowPanel conversationsPanel;
    
    @UiField
    protected TabListItem tutorMeTab;    
    
    @UiField
    protected TabPane tutorMeTabPane;
    
    @UiField
    protected static FlowPanel tutorMePanel;
    
    @UiField
    protected TabListItem learnerActionsTab;
    
    @UiField
    protected TabPane learnerActionsTabPane;
    
    @UiField
	protected FlowPanel learnerActionsPanel;
    
    @UiField
    protected TabListItem finishTab;
    
    @UiField
    protected NavTabs navtabs;
	
    private FeedbackWidget feedbackWidget;
    
    private ConversationsListWidget conversationsWidget;
    
    private UpdateCounterWidget feedbackUpdateCounter;
    
    private UpdateCounterWidget conversationsUpdateCounter;
    
    private boolean refreshServerStatusOnLoad = false;
    
    private static TutorActionsWidget instance;
    
    /**
     * Class constructor
     * 
     * @param widgetInstance Instance of the widget
     */
    public TutorActionsWidget(WidgetInstance widgetInstance) {
    	
    	initWidget(uiBinder.createAndBindUi(this));
    	
    	feedbackWidget = new FeedbackWidget(widgetInstance, true){
    		
    		@Override
    		protected void onUnload() {
    			
    			if(LessonStatus.INACTIVE.equals(UserActionAvatarContainer.getInstance().getLessonStatus())){
    				
    				// if a training application lesson is active, then we don't want to unload feedback messages when the feedback panel is
    				// temporarily detached.
    				super.onUnload();
    			}
    		}
    	};
    	conversationsWidget = new ConversationsListWidget();
    	
    	conversationsUpdateCounter = new UpdateCounterWidget();
    	conversationsUpdateCounter.setVisible(false);
    	conversationsUpdateCounter.getElement().getStyle().setProperty("margin", "-47px 2px 0 14px");
    	conversationsUpdateCounter.getElement().getStyle().setProperty("position", "absolute");
    	conversationsUpdateCounter.getElement().getStyle().setProperty("zIndex", "1");
    	conversationsTab.add(conversationsUpdateCounter);
    	
    	feedbackUpdateCounter = new UpdateCounterWidget();
    	feedbackUpdateCounter.setVisible(false);
    	feedbackUpdateCounter.getElement().getStyle().setProperty("margin", "-47px 2px 0 17px");
    	feedbackUpdateCounter.getElement().getStyle().setProperty("position", "absolute");
    	feedbackUpdateCounter.getElement().getStyle().setProperty("zIndex", "1");
    	feedbackTab.add(feedbackUpdateCounter);
    	
    	feedbackPanel.add(feedbackWidget);
    	conversationsPanel.add(conversationsWidget);
    	
    	instance = this;
    	
    	feedbackTab.addShowHandler(new TabShowHandler() {

			@Override
			public void onShow(TabShowEvent event) {
				feedbackWidget.setIsActive(true);
				feedbackUpdateCounter.setVisible(false);
				conversationsWidget.setIsActive(false);
				conversationsUpdateCounter.setVisible(conversationsWidget.getUpdateCount() > 0);
			}
    		
    	});
    	
    	conversationsTab.addShowHandler(new TabShowHandler() {

			@Override
			public void onShow(TabShowEvent event) {
				feedbackWidget.setIsActive(false);
				conversationsWidget.setIsActive(true);
				conversationsUpdateCounter.setVisible(false);
				feedbackUpdateCounter.setVisible(feedbackUpdateCounter.getCount() > 0);
			}
    		
    	});
    	
    	tutorMeTab.addShowHandler(new TabShowHandler() {

			@Override
			public void onShow(TabShowEvent event) {
				feedbackWidget.setIsActive(false);
				conversationsWidget.setIsActive(false);
				feedbackUpdateCounter.setVisible(feedbackUpdateCounter.getCount() > 0);
				conversationsUpdateCounter.setVisible(conversationsWidget.getUpdateCount() > 0);
			}
    		
    	});
    	
    	learnerActionsTab.addShowHandler(new TabShowHandler() {

			@Override
			public void onShow(TabShowEvent event) {
				feedbackWidget.setIsActive(false);
				conversationsWidget.setIsActive(false);
				feedbackUpdateCounter.setVisible(feedbackUpdateCounter.getCount() > 0);
				conversationsUpdateCounter.setVisible(conversationsWidget.getUpdateCount() > 0);
			}
    		
    	});
    	
    	finishTab.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                event.preventDefault();
                event.stopPropagation();
                
                BrowserSession.getInstance().stopTrainingAppScenario();
            }
        });
    	
    	if(widgetInstance != null 
    	        && widgetInstance.getWidgetProperties() != null
    	        && UserActionWidgetProperties.getScenarioControls(widgetInstance.getWidgetProperties()) != null) {
    	    
    	    ScenarioControls scenarioControls = UserActionWidgetProperties.getScenarioControls(widgetInstance.getWidgetProperties());
    	    
    	    finishTab.setVisible(scenarioControls == null || scenarioControls.isManualStopEnabled());
    	    
    	} else {
    	    finishTab.setVisible(true);
    	}
    	
    	BrowserSession.setTutorActionsAvailable(true);
    }
    
    /**
     * Refreshes the status of the active widget when the TutorActionsWidget is displayed.
     */
    public void refreshServerStatusOnLoad() {
    	refreshServerStatusOnLoad = true;
    }
    
    @Override
    protected void onLoad() {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("Received onLoad callback.  Setting tutor actions available to true.");
        }

    	BrowserSession.setTutorActionsAvailable(true);
    	
    	if(refreshServerStatusOnLoad) {
    		
    		if(feedbackTab.isActive()) {
				feedbackTab.showTab(true);
				
    		} else if (conversationsTab.isActive()) {
    			conversationsTab.showTab(true);
    			
    		} else if (tutorMeTab.isActive()) {
    			tutorMeTab.showTab(true);
    			
    		} else if(learnerActionsTab.isActive()) {
    			learnerActionsTab.showTab(true);
    		}
    		
    		refreshServerStatusOnLoad = false;
    	}
    	
    }
    
    @Override 
    protected void onUnload() {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("Received onUnload callback.  Setting tutor actions available to false.");
        }
        BrowserSession.setTutorActionsAvailable(false);
    }
    
    /**
     * Gets the current instance of the TutorActionsWidget.
     * 
     * @return the instance of the TutorActionsWidget. Can be null.
     */
    public static TutorActionsWidget getInstance() {
    	return instance;
    }
    
    /**
     * Sets the available learner actions.
     * 
     * @param userActionsWidget The UserActionWidget containing available learner actions.
     */
    public void setUserActions(UserActionWidget userActionsWidget) {
    	
    	learnerActionsPanel.clear();
    	learnerActionsPanel.add(userActionsWidget);
    	
    	if(userActionsWidget.getTutorMeActions().getWidgetCount() > 0) {
	    	tutorMePanel.clear();
	    	tutorMePanel.add(userActionsWidget.getTutorMeActions());
    	}
    }
    
    /**
     * Automatically displays the incoming conversation in the conversations tab.
     * 
     * @param name The name of the incoming conversation.  Can be null.
     */
    public void selectConversation(String name) {
    
        if(logger.isLoggable(Level.INFO)){
            logger.info("The conversation named '"+name+"' was selected and should now be displayed.  Setting the following parameters:\n"
                    + "tutorMeTab.active = false, conversationsTab.active = true, feedbackwidget.active = false, conversationWidget.active = true");
        }
        
    	tutorMeTab.setActive(false);
    	tutorMeTabPane.setIn(false);
    	conversationsTab.showTab();
    	conversationsTab.setActive(true);
    	conversationsTabPane.setIn(true);
    	feedbackWidget.setIsActive(false);
		conversationsWidget.setIsActive(true);
		conversationsUpdateCounter.setVisible(false);    	
    	conversationsWidget.setIncomingChatActive(name);
    	
    }
    
    /**
     * Handles a chat update request.
     * 
     * @param instance The instance of the chat widget to update
     */
    public void handleChatUpdate(WidgetInstance instance) {    	
    	conversationsWidget.updateOrCreateConversation(instance);
    	conversationsUpdateCounter.setCount(conversationsWidget.getUpdateCount());
    	conversationsUpdateCounter.setVisible(!conversationsTab.isActive() && (conversationsWidget.getUpdateCount() > 0));    	
    }
    
    /**
     * Handles a feedback update request.
     * 
     * @param instance The instance of the widget
     */
    public void handleFeedbackUpdate(WidgetInstance instance) {
    	
        if(instance.getWidgetProperties().isTutorTest()) {
            // If this is a test, switch to the feedback panel if a display text action is available
            
            ArrayList<TutorUserInterfaceFeedback> feedback = FeedbackWidgetProperties.getFeedback(instance.getWidgetProperties());
            if(feedback != null && !feedback.isEmpty()) {
                TutorUserInterfaceFeedback newFeedback = feedback.get(0);
                if(newFeedback != null && newFeedback.getDisplayTextAction() != null) {
                    feedbackTab.showTab();
                    feedbackTabPane.setIn(true);
                    feedbackTab.setActive(true);
                }   
            }else{
                logger.warning("There was no feedback to handle in the tutor test");
            }
        } 
        
    	if(feedbackTab.isActive()) {
    		feedbackWidget.updateFeedback(instance);
    		feedbackUpdateCounter.decrementCount();
    	} else { 
    		feedbackUpdateCounter.setCount(FeedbackWidgetProperties.getUpdateCount(instance.getWidgetProperties()));

    		if(feedbackUpdateCounter.getCount() == 0) {
    			// If the server's update count is 0, the feedback queue has been cleared
    			feedbackWidget.updateFeedback(instance);
    		} else if (conversationsWidget != null 
        			&& conversationsWidget.isActive() 
        			&& !conversationsWidget.hasOngoingConversations()) {

        		feedbackTab.showTab();
    		}
    	}
    	
    	feedbackUpdateCounter.setVisible(!feedbackTab.isActive() && (feedbackUpdateCounter.getCount() > 0));
    }
    
    /**
     * Gets whether or not the feedback tab has updates available.
     * 
     * @return True if there are remaining updates for the Feedback tab, false otherwise.
     */
    public boolean feedbackWidgetHasUpdates() {
    	return (feedbackWidget != null && feedbackWidget.isActive() && (feedbackWidget.getUpdateCount() > 0)); 
    }
    
    /**
     * Reset this (currently) singleton widget so that it will be recreated
     * for the next course object that needs it.
     */
    public static void reset(){
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("Resetting TutorActionsWidget");
        }
        
        instance = null;
        
        //necessary so that the next course object's widget that needs an avatar will
        //load a new instance of the avatar.  This will cause the avatar idle notification
        //to be sent to the server.
        AvatarContainer.setAvatarData(null);  
    }
    
    /**
     * Clears all of the received feedback messages from this widget's feedback panel
     */
    public void clearFeedback(){
    	feedbackWidget.clearFeedback();
    }
    
}
