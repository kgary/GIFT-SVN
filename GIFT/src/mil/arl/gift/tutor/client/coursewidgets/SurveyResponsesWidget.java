/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.coursewidgets;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import mil.arl.gift.common.gwt.client.survey.SurveyWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.CourseEventScoreWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyResponseMetadata;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that displays the user's after action review for surveys or training sessions.  
 */
public class SurveyResponsesWidget extends Composite {

    private static SurveyResponsesWidgetUiBinder uiBinder = GWT
            .create(SurveyResponsesWidgetUiBinder.class);

    interface SurveyResponsesWidgetUiBinder extends
            UiBinder<Widget, SurveyResponsesWidget> {

    }
    
    /** Allows access to styles in the ui.xml file */
    interface Style extends CssResource {
        String aarConceptMapPanel();
        String aarSurveyPanel();
        String continueButton();
    }

    private static Logger logger = Logger.getLogger(SurveyResponsesWidget.class.getName());

    @UiField
    StackPanel aarStackPanel;

    @UiField
    Style style;
    
    protected VerticalPanel surveyResultsPanel = new VerticalPanel();
    protected VerticalPanel verticalDetailsPanel = new VerticalPanel();

    /**
     * Constructor - creates an empty survey responses panel
     */
    public SurveyResponsesWidget() {

        initWidget(uiBinder.createAndBindUi(this));

        CourseHeaderWidget.getInstance().setHeaderTitle("Structured Review");
        CourseHeaderWidget.getInstance().setContinueButtonEnabled(true);
    }

    /**
     * Adds the details and survey response to the widget under the 'Structured Review' panel
     * 
     * @param detailsHtml The details of this survey response. If null, the string "Details unavailable in preview mode." will be used
     * @param surveyResponseMetadata The survey response metadata to add
     * @param survey The survey that the response is responding to
     */
    public void addSurveyResponse(String detailsHtml, final SurveyResponseMetadata surveyResponseMetadata, final Survey survey) {

        logger.info("Building survey response panel for Structured Review.");

        if (surveyResultsPanel.getWidgetCount() == 0) {

            // Add this widget to the DOM if it hasn't been populated yet

            aarStackPanel.add(surveyResultsPanel, "Tests & Quizzes", false);
            surveyResultsPanel.setSize("100%", "100%");
        }

        String html;
        FlowPanel surveyResponsePanel = new FlowPanel();
        surveyResponsePanel.setStyleName(style.aarSurveyPanel());

        if (detailsHtml == null) {
            html = "Details unavailable in preview mode";
        } else {
            html = detailsHtml;
        }
        surveyResponsePanel.add(new HTML(html));

        if (surveyResponseMetadata != null) {
            Button viewSurveyResponsePanel = new Button("View '" + surveyResponseMetadata.getSurveyName() + "' Results");

            viewSurveyResponsePanel.setType(ButtonType.PRIMARY);
            viewSurveyResponsePanel.addStyleName(style.continueButton());

            viewSurveyResponsePanel.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    displaySurveyResults(surveyResponseMetadata, survey);
                }
            });

            surveyResponsePanel.add(viewSurveyResponsePanel);
        }

        surveyResultsPanel.add(surveyResponsePanel);
    }

    /**
     * Adds the details for a graded score node to the widget under the 'Tutor Assessments' panel
     * 
     * @param detailsHtml The details to be displayed
     */
    public void addDetails(String detailsHtml) {

        logger.info("Building score node course event panel for Structured Review.");

        if (verticalDetailsPanel.getWidgetCount() == 0) {

            // Add this widget to the DOM if it hasn't been populated yet

            aarStackPanel.add(new ScrollPanel(verticalDetailsPanel), "Tutor Assessments", false);
            verticalDetailsPanel.setSize("100%", "100%");
        }

        FlowPanel conceptMapPanel = new FlowPanel();
        conceptMapPanel.setStyleName(style.aarConceptMapPanel());

        conceptMapPanel.add(new HTML(detailsHtml));

        verticalDetailsPanel.add(conceptMapPanel);
    }

    /**
     * Adds the score node to the widget under the 'Tutor Assessments' panel
     * 
     * @param scoreNode The score node to add
     */
    public void addGradedScoreNode(GradedScoreNode scoreNode) {

        logger.info("Building score node course event panel for Structured Review.");

        if (verticalDetailsPanel.getWidgetCount() == 0) {

            // Add this widget to the DOM if it hasn't been populated yet

            aarStackPanel.add(new ScrollPanel(verticalDetailsPanel), "Tutor Assessments", false);
            verticalDetailsPanel.setSize("100%", "100%");
        }

        verticalDetailsPanel.add(new CourseEventScoreWidget(scoreNode));
    }

    /**
     * Displays a response to a survey in a dialog
     *
     * @param responseMetadata The survey response metadata to display
     * @param survey the survey that the response is responding to
     */
    public static void displaySurveyResults(SurveyResponseMetadata responseMetadata, Survey survey) {
        
        final ModalDialogBox dialogBox = new ModalDialogBox();
        
        FlowPanel panel = new SurveyWidget(survey, true, false, responseMetadata).displaySurveyResponseWidget(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                dialogBox.hide();
            }
            
        });
        
        dialogBox.setWidget(panel);
        
        dialogBox.center();
    }
}
