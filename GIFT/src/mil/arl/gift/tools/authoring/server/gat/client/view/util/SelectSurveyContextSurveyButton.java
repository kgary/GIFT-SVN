/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import java.math.BigInteger;

import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.SelectSurveyContextSurveyDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * A button used to select a survey context survey
 * 
 * @author nroberts
 */
public class SelectSurveyContextSurveyButton extends Composite implements HasValue<String>, HasText{

	private static SelectSurveyContextSurveyButtonUiBinder uiBinder = GWT
			.create(SelectSurveyContextSurveyButtonUiBinder.class);

	interface SelectSurveyContextSurveyButtonUiBinder extends
			UiBinder<Widget, SelectSurveyContextSurveyButton> {
	}
	
	private final static String NO_VALUE_STRING = "<span style='color: red;'>No survey selected</span>";
	
	@UiField
	protected HasClickHandlers selectButton;
	
	@UiField
	protected HasHTML valueLabel;
	
	/** The dialog used to select the survey context survey*/
	private SelectSurveyContextSurveyDialog dialog = new SelectSurveyContextSurveyDialog();
	
	/** The survey context survey selected*/
	private String value = null;
	
	@UiField(provided=true)
	protected Image clearButton = new Image(GatClientBundle.INSTANCE.cancel_image());

	/**
	 * Creates a new button used to select a survey context survey
	 */
	public SelectSurveyContextSurveyButton() {
		initWidget(uiBinder.createAndBindUi(this));
		
		selectButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				dialog.center();
			}
		});
		
		dialog.addValueChangeHandler(new ValueChangeHandler<SurveyContextSurvey>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<SurveyContextSurvey> event) {
				
				if(event.getValue() != null){
					value = event.getValue().getKey();
					
					if(value != null){
					    // Show the name of the survey to the user instead of the name of the survey context key.
						valueLabel.setText(event.getValue().getSurvey().getName());
						clearButton.setVisible(true);
						
					} else {
						valueLabel.setHTML(NO_VALUE_STRING);
						clearButton.setVisible(false);
					}
					
					ValueChangeEvent.fire(SelectSurveyContextSurveyButton.this, value);
				}
				dialog.hide();
			}
		});
		
		clearButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				OkayCancelDialog.show(
						"Deselect Survey?", 
						"Are you sure you want to deselect " 
								+ (valueLabel.getText() != null ? ("<b>" + valueLabel.getText() + "</b>") : "this survey") 
								+ "?"
								+ "<br/><br/><b>Note</b>: This will not remove the survey from the database or alter its data in any way.", 
						"Yes, Deselect the Survey", 
						new OkayCancelCallback() {
					
					@Override
					public void okay() {
						setValue(null, true);
					}
					
					@Override
					public void cancel() {
						//Nothing to do
					}
				});
			}
		});
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {

		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		
		setValue(value, false);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		
		if(value != null){
			valueLabel.setText(value);
			clearButton.setVisible(true);
			
		} else {
			valueLabel.setHTML(NO_VALUE_STRING);
			clearButton.setVisible(false);
		}
		
		//create a dummy survey that is used to select the survey with the appropriate key once the list loads
		SurveyContextSurvey dummySurvey = new SurveyContextSurvey();
		dummySurvey.setKey(value);
		
		dialog.setValue(dummySurvey, fireEvents);
	}

	@Override
	public String getText() {
		return valueLabel.getText();
	}
	
	@Override
	public void setText(String text) {
		valueLabel.setText(text);
	}

	/**
	 * Sets the survey context ID to use to get the list of surveys. If no survey context ID is set, then no surveys will be loaded.
	 * 
	 * @param id the ID to use
	 */
	public void setSurveyContextId(BigInteger id){
		dialog.setSurveyContextId(id);
	}
}
