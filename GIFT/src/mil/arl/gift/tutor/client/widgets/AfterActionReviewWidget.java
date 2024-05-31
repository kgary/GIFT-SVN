/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import mil.arl.gift.common.gwt.client.survey.SurveyWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.CourseEventScoreWidget;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyGiftData;
import mil.arl.gift.common.survey.SurveyResponseMetadata;
import mil.arl.gift.tutor.shared.AfterActionReviewDetailsNode;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.AfterActionReviewWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget;

/**
 * The widget displays the learner's after action review for a training session.
 * 
 * @author jleonard
 */
public class AfterActionReviewWidget extends Composite {
    
    private static Logger logger = Logger.getLogger(AfterActionReviewWidget.class.getName());
    
    private static final String DEFAULT_TITLE = "Structured Review";
    
    /** Instance of the deck panel widget that contains the summary and the survey results. */
    private DeckPanel deckPanel;
    
    /** The following are the widget indices of the summary widget and the survey result widget in the deck panel. */
    private int SUMMARY_INDEX = 0;
    private int SURVEY_RESULT_INDEX = 1;

    /**
     * Constructor
     * 
     * @param instance The instance of the after action review widget
     */
    public AfterActionReviewWidget(WidgetInstance instance) {
        WidgetProperties properties = instance.getWidgetProperties();

        deckPanel = new DeckPanel();
        
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSpacing(5);
        deckPanel.add(verticalPanel);
        
        // Default to the summary widget.
        deckPanel.showWidget(SUMMARY_INDEX);
        
        ScrollPanel scrollPanel = new ScrollPanel(deckPanel);
        scrollPanel.addStyleName("AAR-container");
        this.initWidget(scrollPanel);
        verticalPanel.addStyleName("AAR-table");

        StackPanel aarStackPanel = new StackPanel();
        verticalPanel.add(aarStackPanel);
        aarStackPanel.setWidth("100%");

        VerticalPanel verticalDetailsPanel = new VerticalPanel();
        VerticalPanel surveyResultsPanel = new VerticalPanel();
        
        final AfterActionReviewWidget thisWidget = this;
        
        if (properties != null) {

            List<AfterActionReviewDetailsNode> detailsList = AfterActionReviewWidgetProperties.getDetails(properties);
            
            if(detailsList == null || detailsList.isEmpty()){
                throw new RuntimeException("There are no events to show in the Structured Review.");
            }

            for (AfterActionReviewDetailsNode details : detailsList) {
                
                if(details.getSurveyResponse() != null) {
                    
                    logger.info("Building survey response panel for Structured Review.");
                    
                    final SurveyResponseMetadata surveyResponseMetadata = details.getSurveyResponse();
                    
                    FlowPanel surveyResponsePanel = new FlowPanel();
                    surveyResponsePanel.setStyleName("AAR-surveyPanel");
                    
                    Button viewSurveyResponsePanel = new Button("View '"+surveyResponseMetadata.getSurveyName()+"' Results");
                    
                    viewSurveyResponsePanel.setType(ButtonType.PRIMARY);
                    viewSurveyResponsePanel.addStyleName("continueButton");
                    
                    viewSurveyResponsePanel.addClickHandler(new ClickHandler(){

                        @Override
                        public void onClick(ClickEvent event) {
                            thisWidget.displaySurveyResults(surveyResponseMetadata);
                        }
                    });
                    
                    surveyResponsePanel.add(new HTML(details.getHtml()));
                    
                    surveyResponsePanel.add(viewSurveyResponsePanel);                    
                    
                    surveyResultsPanel.add(surveyResponsePanel);
                    
                } else if(details.getHtml() != null){

                    logger.info("Building score node course event panel for Structured Review.");

                    FlowPanel conceptMapPanel = new FlowPanel();
                    conceptMapPanel.setStyleName("AAR-conceptMapPanel");
                    
                    conceptMapPanel.add(new HTML(details.getHtml()));
                    
                    verticalDetailsPanel.add(conceptMapPanel);
                }else if(details.getScoreNode() != null){
                    logger.info("Building score node course event panel for Structured Review.");
                    verticalDetailsPanel.add(new CourseEventScoreWidget(details.getScoreNode()));
                }else{
                    //ERROR
                    throw new RuntimeException("Received unhandled structured review details of :\n"+details);
                }
            }
        }else{
            throw new RuntimeException("Failed to retrieve the widget properties needed to create the Structured Review widget.");
        }
        
        if(surveyResultsPanel.getWidgetCount() > 0){
            aarStackPanel.add(surveyResultsPanel, "Tests & Quizzes", false);
            surveyResultsPanel.setSize("100%", "100%");
        }

        if(verticalDetailsPanel.getWidgetCount() > 0){
            aarStackPanel.add(new ScrollPanel(verticalDetailsPanel), "Tutor Assessments", false);
            verticalDetailsPanel.setSize("100%", "100%");
        }

        
        String title = AfterActionReviewWidgetProperties.getTitle(properties);
        if(title != null) {
            CourseHeaderWidget.getInstance().setHeaderTitle(title);
        }else{
            CourseHeaderWidget.getInstance().setHeaderTitle(DEFAULT_TITLE);
        }
        
        CourseHeaderWidget.getInstance().setContinueButtonEnabled(true);
        CourseHeaderWidget.getInstance().setContinuePageId(instance.getWidgetId());
    }

    /**
     * Displays a response to a survey in the deck panel.
     *
     * @param responseMetadata The metadata of the survey response to display
     */
    public void displaySurveyResults(final SurveyResponseMetadata responseMetadata) {
        
        BrowserSession.getInstance().getSurvey(responseMetadata.getSurveyContextId(), responseMetadata.getGiftKey(), new AsyncCallback<SurveyGiftData>(){

            @Override
            public void onFailure(Throwable t) {
                logger.severe("There was a problem fetching the survey\n" + t.toString());
            }

            @Override
            public void onSuccess(SurveyGiftData surveyGiftData) {
                Survey survey = surveyGiftData.getSurvey();
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("The survey with id '" 
                            + survey.getId() 
                            + "' and name '" 
                            + survey.getName() 
                            + "' and key '" 
                            + surveyGiftData.getGiftKey() 
                            + "' was successfully fetched from the UMS");
                }
                
                final SurveyWidget surveyWidget = new SurveyWidget(survey, true, false, responseMetadata);
                FlowPanel panel = surveyWidget.displaySurveyResponseWidget(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        logger.info("AfterActionReviewWidget::onClose()");
                        deckPanel.showWidget(SUMMARY_INDEX);
                    }
                });
                
                if (deckPanel.getWidgetCount() > 1) {
                    deckPanel.remove(SURVEY_RESULT_INDEX);
                }
                
                deckPanel.add(panel);
                deckPanel.showWidget(SURVEY_RESULT_INDEX);
            }
        });
    }
    
}