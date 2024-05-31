/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import java.util.logging.Logger;

import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tutor.client.conversations.ChatWidget;
import mil.arl.gift.tutor.client.widgets.AfterActionReviewWidget;
import mil.arl.gift.tutor.client.widgets.AvatarContainer;
import mil.arl.gift.tutor.client.widgets.BlankWidget;
import mil.arl.gift.tutor.client.widgets.CourseInitInstructionsWidget;
import mil.arl.gift.tutor.client.widgets.DisplayFooterWidget;
import mil.arl.gift.tutor.client.widgets.DisplayMediaCollectionWidget;
import mil.arl.gift.tutor.client.widgets.DisplayMediaWidget;
import mil.arl.gift.tutor.client.widgets.DisplayMessageWidget;
import mil.arl.gift.tutor.client.widgets.DisplaySurveyWidget;
import mil.arl.gift.tutor.client.widgets.ExperimentCompleteWidget;
import mil.arl.gift.tutor.client.widgets.ExperimentWelcomeWidget;
import mil.arl.gift.tutor.client.widgets.FeedbackWidget;
import mil.arl.gift.tutor.client.widgets.LogoWidget;
import mil.arl.gift.tutor.client.widgets.ResumeSessionWidget;
import mil.arl.gift.tutor.client.widgets.SelectDomainWidget;
import mil.arl.gift.tutor.client.widgets.SimpleLoginWidget;
import mil.arl.gift.tutor.client.widgets.TeamSessionWidget;
import mil.arl.gift.tutor.client.widgets.TutorActionsWidget;
import mil.arl.gift.tutor.client.widgets.UserActionAvatarContainer;
import mil.arl.gift.tutor.client.widgets.UserActionWidget;
import mil.arl.gift.tutor.shared.LessonStatusAction.LessonStatus;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.UserActionWidgetProperties;

/**
 * Factory for constructing GIFT widgets of various types
 *
 * @author jleonard
 */
public class WidgetFactory {
    
    private static Logger logger = Logger.getLogger(WidgetFactory.class.getName());

    /**
     * Constructs GIFT widgets with an instance
     *
     * @param instance The instance of the widget in GIFT
     * @return Widget The newly created widget
     * @throws Exception If the widget cannot be created
     */
    public static Widget createWidgetInstance(WidgetInstance instance) throws Exception {

        Widget widget = null;
        if (instance != null) {

        	if (instance.getWidgetType() == WidgetTypeEnum.BLANK_WIDGET) {

        	    widget = new BlankWidget();
            } else if (instance.getWidgetType() == WidgetTypeEnum.SIMPLE_LOGIN_WIDGET) {
                // Don't display this widget if the tutor is embedded
                
                boolean showLogo = TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment();
                widget = showLogo ? new LogoWidget() : new SimpleLoginWidget();
            } else if (instance.getWidgetType() == WidgetTypeEnum.SELECT_DOMAIN_WIDGET) {
                // Don't display this widget if the tutor is embedded
                
                boolean showLogo = TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment();
                widget = showLogo ? new LogoWidget() : new SelectDomainWidget(instance);	
            } else if (instance.getWidgetType() == WidgetTypeEnum.RESUME_SESSION_WIDGET) {
            	// Don't display this widget if the tutor is embedded
            	
                boolean showLogo = TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment();
                widget = showLogo ? new LogoWidget() : new ResumeSessionWidget(instance);
            	
            } else if (instance.getWidgetType() == WidgetTypeEnum.MESSAGE_WIDGET) {

                logger.info("Creating message widget");
                widget = new DisplayMessageWidget(instance);

            } else if (instance.getWidgetType() == WidgetTypeEnum.FOOTER_TEXT_WIDGET) {

                widget = new DisplayFooterWidget(instance);

            } else if (instance.getWidgetType() == WidgetTypeEnum.SURVEY_WIDGET) {

                logger.info("Creating display survey widget");
                widget = new DisplaySurveyWidget(instance);

            } else if (instance.getWidgetType() == WidgetTypeEnum.MEDIA_COLLECTION_WIDGET) {

                logger.info("Creating display media collection widget");
                widget = new DisplayMediaCollectionWidget(instance);

            } else if (instance.getWidgetType() == WidgetTypeEnum.MEDIA_WIDGET) {

                logger.info("Creating display media widget");
                widget = new DisplayMediaWidget(instance);

            } else if (instance.getWidgetType() == WidgetTypeEnum.LOGO_WIDGET) {

                widget = new LogoWidget();

            } else if (instance.getWidgetType() == WidgetTypeEnum.AAR_WIDGET) {

                logger.info("Creating structured review widget");
                widget = new AfterActionReviewWidget(instance);

            } else if (instance.getWidgetType() == WidgetTypeEnum.USER_ACTION_WIDGET) {
            	
                UserActionAvatarContainer avatarContainer = UserActionAvatarContainer.getInstance();
            	TutorActionsWidget actionsWidget = TutorActionsWidget.getInstance();
            	
            	if(actionsWidget == null) {
            		actionsWidget = new TutorActionsWidget(instance);
            		
            	} else {
            		actionsWidget.refreshServerStatusOnLoad();
            	}
            	
            	if(!UserActionWidgetProperties.shouldUsePreviousActions(instance.getWidgetProperties())) {
            		actionsWidget.setUserActions(new UserActionWidget(instance));         
            	}
            	
            	avatarContainer.setContainerWidget(actionsWidget);  
            	
            	widget = avatarContainer;
            	
            } else if (instance.getWidgetType() == WidgetTypeEnum.FEEDBACK_WIDGET) {
            	
                logger.info("Creating feedback widget");
                widget = new FeedbackWidget(instance, false);

            } else if (instance.getWidgetType() == WidgetTypeEnum.CHAT_WINDOW_WIDGET) {
            	
                widget = new ChatWidget(instance);            		
            	
            } else if (instance.getWidgetType() == WidgetTypeEnum.COURSE_INIT_INSTRUCTIONS_WIDGET) {

                widget = new CourseInitInstructionsWidget(instance);
                
            } else if (instance.getWidgetType() == WidgetTypeEnum.EXPERIMENT_WELCOME_WIDGET) {

                widget = new ExperimentWelcomeWidget(instance);
                
            } else if (instance.getWidgetType() == WidgetTypeEnum.EXPERIMENT_COMPLETE_WIDGET) {

                widget = new ExperimentCompleteWidget(instance);

            } else if (instance.getWidgetType() == WidgetTypeEnum.AVATAR_CONTAINER_WIDGET) {
            	
            	if(!LessonStatus.INACTIVE.equals(UserActionAvatarContainer.getInstance().getLessonStatus())
                		&& !(Document.getInstance().getArticleWidget() instanceof UserActionAvatarContainer)){
            		
            		instance.getWidgetProperties().setIsFullscreen(true);
                		
            		//if a training application lesson is currently active, use the appropriate avatar
            		widget = UserActionAvatarContainer.getInstance();
            		
            	} else {

            	    widget = new AvatarContainer(instance);
            	}

            } else if (instance.getWidgetType() == WidgetTypeEnum.TEAM_SESSION_WIDGET) {
                widget = new TeamSessionWidget(instance);
                
            } else {

                throw new Exception("Unable to create a widget of instance type - " + instance.getWidgetType());
            }

        } else {

            throw new IllegalArgumentException("The widget instance can't be null");
        }
        
        // stop the animated loading indicator that is statically placed in the TUI html file and started by
        // the handleContinue method in Document.java.
        Document.stopLoadingProgress();
        return widget;
    }

    /**
     * Constructs GIFT Widgets of a type
     *
     * @param widgetType The widget to create
     * @return Widget The newly created widget
     * @throws Exception If the widget cannot be created
     */
    public static Widget createWidgetType(WidgetTypeEnum widgetType) throws Exception {

        if (widgetType != null) {
        	
            if (widgetType == WidgetTypeEnum.SIMPLE_LOGIN_WIDGET) {

                //make sure the login page is full screen
                Document.getInstance().fullScreenMode();
                
                boolean embedded = TutorUserWebInterface.isEmbedded() && !TutorUserWebInterface.isExperiment();
                return embedded ? null : new SimpleLoginWidget();

            } else if (widgetType == WidgetTypeEnum.LOGO_WIDGET) {

                return new LogoWidget();
                
            } else if (widgetType == WidgetTypeEnum.BLANK_WIDGET) {

                return new BlankWidget();
                
            } else {
                throw new Exception("Unable to create a widget of widget type: " + widgetType);
            }
        }

        throw new Exception("Unable to create a widget of widget type - " + widgetType);
    }

    private WidgetFactory() {
    }
}
