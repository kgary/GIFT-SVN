/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.constants.Styles;

import mil.arl.gift.common.gwt.client.survey.SurveyWidget;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog used to provide users with a preview of what a survey will look like when it is eventually shown in the TUI
 * 
 * @author nroberts
 */
public class PreviewSurveyDialog extends PopupPanel {

	private static PreviewSurveyDialogUiBinder uiBinder = GWT
			.create(PreviewSurveyDialogUiBinder.class);

	interface PreviewSurveyDialogUiBinder extends
			UiBinder<Widget, PreviewSurveyDialog> {
	}
	
	@UiField CheckBox testCheck;
	
	@UiField CheckBox coloredScoringCheck;
	
	@UiField
	protected Button closeButton;
	
	@UiField
	protected FlowPanel bodyPanel;
	
	@UiField (provided = true)
	protected PreviewSurveyPanel previewPanel;

	/**
	 * where the header image will be placed
	 */
    @UiField
    protected NavbarBrand navBarHeader;
	
	/**
	 * Creates a new preview dialog for the given survey
	 * 
	 * @param survey the survey to preview
	 * @param fullScreen whether or not the survey should be previewed in full screen mode
	 * @param isDebug true if the GAT should render in debug mode (e.g. color code scored answers in surveys)
	 */
	public PreviewSurveyDialog(Survey survey, boolean fullScreen, boolean isDebug) {
	    
	    /* Let the survey widget access media in the course folder */
	    survey.applySurveyMediaHost(GatClientUtility.getBaseCourseFolderUrl());
	    
	    final SurveyWidget surveyPreviewWidget = new SurveyWidget(survey, true, isDebug);
        surveyPreviewWidget.addCloseHandler(new CloseHandler<SurveyResponse>() {
            
            @Override
            public void onClose(CloseEvent<SurveyResponse> event) {
                hide();
            }
        });  
        
        previewPanel = new PreviewSurveyPanel(surveyPreviewWidget, fullScreen, isDebug);
	    
		setWidget(uiBinder.createAndBindUi(this));
		
		addStyleName("previewSurveyDialog");
		addStyleName(Styles.MODAL_CONTENT);
    	setGlassEnabled(true); 
    	
        // set background
        String backgroundUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
        bodyPanel.getElement().getStyle().setBackgroundImage("url('"+backgroundUrl+"')");
        
        // set system icon
        Image headerImage = new Image();
        headerImage.setUrl(GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.SYSTEM_ICON_SMALL));
        headerImage.addStyleName("headerIconAdjustment");
        navBarHeader.add(headerImage);
        
        coloredScoringCheck.setValue(isDebug);
        
        testCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				surveyPreviewWidget.setPreviewModeEnabled(!event.getValue());
			}
		});
        
        coloredScoringCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                surveyPreviewWidget.loadSurvey(event.getValue(), null);
            }
        });
        
        closeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}

}
