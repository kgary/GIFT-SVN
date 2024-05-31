/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.survey.SurveyItemProperties;

/**
 * The ScaleAppearancePropertySetWidget class is responsible for displaying the controls that
 * allow the author to customize the layout of the rating scale question.
 * 
 * @author nblomberg
 *
 */
public class ScaleAppearancePropertySetWidget extends AbstractPropertySetWidget  {

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(ScaleAppearancePropertySetWidget.class.getName());
    
    /** UiBinder used to combine the ui.xml file with this java class */
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    /** Defines the UiBinder interface for binding to the ui.xml file */
	interface WidgetUiBinder extends
			UiBinder<Widget, ScaleAppearancePropertySetWidget> {
	}
	
	@UiField
	protected CheckBox hideAnswerLabelsBox;
	
	@UiField
	protected CheckBox displayScaleLabelsBox;
	
	@UiField
	protected Collapse displayScaleLabelsCollapse;
	
    /** TextBox containing the left extreme label's text */
	@UiField
	protected TextBox leftExtremeLabelBox;
	
    /** TextBox containing the right extreme label's text */
	@UiField
	protected TextBox rightExtremeLabelBox;
	
    /** Container for each of the mid scale label TextBoxes */
	@UiField
	protected FlowPanel midScaleLabelContainer;

	/**
	 * Constructor (default)
	 * 
	 * @param propertySet - The property set for the widget.
	 * @param listener - The listener that is responsible for handling changes to the properties.
	 */
    public ScaleAppearancePropertySetWidget(ScaleAppearancePropertySet propertySet, PropertySetListener listener) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    hideAnswerLabelsBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
					
				ScaleAppearancePropertySet props = (ScaleAppearancePropertySet) propSet;
				props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY, event.getValue());
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    displayScaleLabelsBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                ScaleAppearancePropertySet props = (ScaleAppearancePropertySet) propSet;
                props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY, event.getValue());

                if (event.getValue()) {
                    displayScaleLabelsCollapse.show();
                } else {
                    displayScaleLabelsCollapse.hide();
                }

                propListener.onPropertySetChange(propSet);
            }
		});

	    leftExtremeLabelBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
					
				ScaleAppearancePropertySet props = (ScaleAppearancePropertySet) propSet;
				props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, event.getValue());
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    rightExtremeLabelBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
					
				ScaleAppearancePropertySet props = (ScaleAppearancePropertySet) propSet;
				props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY, event.getValue());
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    if(propertySet != null){
	    	
	    	Serializable hideAnswerLabels = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_REPLY_OPTION_LABELS_KEY);
	    	
	    	if(hideAnswerLabels != null && hideAnswerLabels instanceof Boolean){	    		
	    		hideAnswerLabelsBox.setValue((Boolean) hideAnswerLabels);
	    		
	    	} else {
	    		hideAnswerLabelsBox.setValue(null);
	    	}
	    	
	    	Serializable displayScaleLabels = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_LABELS_KEY);
	    	
	    	if(displayScaleLabels != null && displayScaleLabels instanceof Boolean){
	    		
	    		displayScaleLabelsBox.setValue((Boolean) displayScaleLabels);
	    		
	    		if((Boolean) displayScaleLabels){
	    			displayScaleLabelsCollapse.show();
					
				} else {
					displayScaleLabelsCollapse.hide();
				}
	    		
	    	} else {
	    		hideAnswerLabelsBox.setValue(null);
	    		displayScaleLabelsCollapse.hide();
	    	}
	    	
	    	Serializable leftExtremeLabel = propertySet.getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
	    	
	    	if(leftExtremeLabel != null && leftExtremeLabel instanceof String){
	    		leftExtremeLabelBox.setValue((String) leftExtremeLabel);
	    		
	    	} else {
	    		leftExtremeLabelBox.setValue(null);
	    	}
	    	
	    	Serializable midScaleLabels = propertySet.getProperties().getPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY);
	    	
	    	midScaleLabelContainer.clear();
	    	
	    	if(midScaleLabels != null && midScaleLabels instanceof String){
	    		
	    		List<String> midScaleLabelsString = SurveyItemProperties.decodeListString((String) midScaleLabels);
	    		
	    		for(String label : midScaleLabelsString){
	    			
	    			if(label != null && !label.isEmpty()){
	    				addMidScaleLabel(label);
	    			}
	    		}
	    	}
	    	
	    	addMidScaleLabel(null);
	    	
	    	Serializable rightExtremeLabel = propertySet.getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY);
	    	
	    	if(rightExtremeLabel != null && rightExtremeLabel instanceof String){
	    		rightExtremeLabelBox.setValue((String) rightExtremeLabel);
	    		
	    	} else {
	    		rightExtremeLabelBox.setValue(null);
	    	}
	    	
	    }

	}
    
    /**
     * Adds a new mid scale label with the given text
     * 
     * @param labelText the text to use for the label
     * @return the text box used to edit the label
     */
    private TextBox addMidScaleLabel(String labelText){
    	
    	final TextBox labelBox = new TextBox();
    	labelBox.setPlaceholder("New Label");
    	labelBox.getElement().getStyle().setMarginBottom(4, Unit.PX);
    	
    	labelBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(midScaleLabelContainer.getWidgetIndex(labelBox) == midScaleLabelContainer.getWidgetCount() - 1){
					
					if(event.getValue() != null && !event.getValue().isEmpty()){
						
						//if the user enters text into the last column choice, add another column choice after it
						TextBox newResponse = addMidScaleLabel(null);
						newResponse.setFocus(true);
					}
					
				} else {
					
					//if the user removes all the text in any other column choice, remove that choice
					if(event.getValue() == null || event.getValue().isEmpty()){	
						
						midScaleLabelContainer.remove(midScaleLabelContainer.getWidgetIndex(labelBox));
					}
										
				}
				
				List<String> labels = new ArrayList<String>();
				
				for(int i = 0;i < midScaleLabelContainer.getWidgetCount() - 1; i++){
					
					Widget widget = midScaleLabelContainer.getWidget(i);
					
					if(widget != null && widget instanceof TextBox){
						
						String value = ((TextBox) widget).getValue();
						
						if(value != null){
							labels.add(value);
						}
					}
				}
				
				ScaleAppearancePropertySet props = (ScaleAppearancePropertySet) propSet;
				
				if(!labels.isEmpty()){
					props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY, SurveyItemProperties.encodeListString(labels));
				
				} else {
					props.getProperties().removeProperty(SurveyPropertyKeyEnum.MID_POINT_LABEL_KEY);
				}
				
				
				propListener.onPropertySetChange(propSet);
			}
		});
    	
    	labelBox.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				
				boolean enterPressed = KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode();
				
				if(enterPressed){
					labelBox.setFocus(false);
				}
			}
		});
    	
    	labelBox.setValue(labelText);
    	
    	midScaleLabelContainer.add(labelBox);
    	
    	return labelBox;
    }

}
