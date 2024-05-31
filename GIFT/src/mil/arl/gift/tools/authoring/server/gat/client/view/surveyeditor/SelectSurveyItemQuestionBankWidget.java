/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;


/**
 * A widget that allows the user to select a new survey item to be added to a survey page for question bank survey types.
 * 
 * @author nblomberg
 *
 */
public class SelectSurveyItemQuestionBankWidget extends AbstractSelectSurveyItemWidget  {

    private static Logger logger = Logger.getLogger(SelectSurveyItemQuestionBankWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, SelectSurveyItemQuestionBankWidget> {
	}
	
	
	@UiField
	Button mocButton;
	
	@UiField
	Button tfButton;
	
	@UiField
	Button closeButton;

	
	@UiField
	Button sbButton;
	
	@UiField
    Button copyButton;
	
	@UiField
	Button mcButton;
	
	@UiField
	Button rsButton;
	
	@UiField
    Button frButton;
	
	@UiField
	Container mainContainer;

	/**
	 * Converts a source Button widget to the corresponding survey item type.  
	 * 
	 * @param source - The source item that was clicked on (should be a button widget).
	 * @return SurveyItemType - The type of item that the button is mapped to.  
	 */
	@Override
	protected SurveyItemType convertSourceToItemType(Object source) {
        SurveyItemType itemType = SurveyItemType.CLOSE_ITEM;
        
        
     
        if (source.equals(mcButton)) {
            itemType = SurveyItemType.MULTIPLE_CHOICE;
        } else if (source.equals(mocButton)) {
            itemType = SurveyItemType.MATRIX_OF_CHOICES;
        } else if (source.equals(sbButton)) {
            itemType = SurveyItemType.SLIDER_BAR;
        } else if (source.equals(tfButton)) {
            itemType = SurveyItemType.TRUE_FALSE;
        } else if (source.equals(copyButton)) {
            itemType = SurveyItemType.COPY_EXISTING_ITEM;
        } else if (source.equals(rsButton)) {
            itemType = SurveyItemType.RATING_SCALE;
        } else if (source.equals(frButton)) {
            itemType = SurveyItemType.FREE_RESPONSE;
        } 
        
        return itemType;
        
    }

	/**
	 * Constructor (default)
	 */
	public SelectSurveyItemQuestionBankWidget() {
	    
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    closeButton.addMouseDownHandler(closeHandler);
        mocButton.addMouseDownHandler(closeHandler);
        tfButton.addMouseDownHandler(closeHandler);
        sbButton.addMouseDownHandler(closeHandler);
        copyButton.addMouseDownHandler(closeHandler);
        mcButton.addMouseDownHandler(closeHandler);
        rsButton.addMouseDownHandler(closeHandler);
        frButton.addMouseDownHandler(closeHandler);
        
        // Set the html 'id' attribute for the container.
        mainContainer.setId(widgetId.getWidgetId());
	}





}
