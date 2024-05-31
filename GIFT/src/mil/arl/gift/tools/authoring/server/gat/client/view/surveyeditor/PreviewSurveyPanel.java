/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.survey.SurveyWidget;

/**
 * A widget used to provide users with a preview of what a survey will look like when it is eventually shown in the TUI. This
 * widget basically acts as a wrapper around a survey widget that reflects the TUI's full screen behavior
 * 
 * @author nroberts
 */
public class PreviewSurveyPanel extends Composite {

    private static PreviewSurveyPanelUiBinder uiBinder = GWT.create(PreviewSurveyPanelUiBinder.class);

    interface PreviewSurveyPanelUiBinder extends UiBinder<Widget, PreviewSurveyPanel> {
    }
    
    @UiField
    protected DeckPanel layoutDeck;
    
    @UiField
    protected FlowPanel defaultLayoutPanel;
    
    @UiField
    protected ScrollPanel defaultLayoutSurvey;
    
    @UiField
    protected FlowPanel fullScreenPanel;
    
    @UiField
    protected SimplePanel fullScreenSurvey;

    /**
     * Creates a new panel wrapping the given survey widget so its survey can be previewed.
     * 
     * @param surveyPreviewWidget the survey widget containing the survey being previewed
     * @param fullScreen whether or not to show the survey widget in full screen, similar to how it will be shown in the TUI
     * @param isDebug true if the GAT should render in debug mode (e.g. color code scored answers in surveys)
     */
    public PreviewSurveyPanel(SurveyWidget surveyPreviewWidget, boolean fullScreen, boolean isDebug) {
        initWidget(uiBinder.createAndBindUi(this));
        
        defaultLayoutSurvey.clear();
        fullScreenSurvey.clear();
        
        if(fullScreen){
            
            fullScreenSurvey.setWidget(surveyPreviewWidget);
            
            layoutDeck.showWidget(layoutDeck.getWidgetIndex(fullScreenPanel));
            
        } else {
            
            defaultLayoutSurvey.setWidget(surveyPreviewWidget);
            
            layoutDeck.showWidget(layoutDeck.getWidgetIndex(defaultLayoutPanel));
        }
    }

}
