/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.io.Serializable;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;

import org.gwtbootstrap3.client.ui.InlineRadio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * The ScaleLayoutPropertySetWidget is responsible for displaying the controls that
 * allow the author to change the style of the rating scale bar.
 * 
 * @author nblomberg
 *
 */
public class ScaleLayoutPropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(ScaleLayoutPropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, ScaleLayoutPropertySetWidget> {
	}
	
	@UiField
	protected InlineRadio radioOption;
	
	@UiField
	protected InlineRadio barOption;

	/**
	 * Constructor (default)
	 * 
	 * @param propertySet - The property set for the class.
	 * @param listener - The listener that will respond to changes to the properties. 
	 */
    public ScaleLayoutPropertySetWidget(ScaleLayoutPropertySet propertySet, PropertySetListener listener) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    radioOption.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(event.getValue()){
					
					ScaleLayoutPropertySet scaleProps = (ScaleLayoutPropertySet) propSet;
					scaleProps.setUseBarLayout(false);
					
					propListener.onPropertySetChange(propSet);
				}
			}
		});
	   
	    barOption.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(event.getValue()){
					
					ScaleLayoutPropertySet scaleProps = (ScaleLayoutPropertySet) propSet;
					scaleProps.setUseBarLayout(true);
					
					propListener.onPropertySetChange(propSet);
				}
			}
		});
	    
	    if(propertySet != null){
	    	
	    	Serializable useBarLayout = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_BAR_LAYOUT);
	    	
	    	if(useBarLayout != null && useBarLayout instanceof Boolean && (Boolean) useBarLayout){	  
	    		barOption.setValue(true);
	    		
	    	} else {
	    		radioOption.setValue(true);
	    	}
	    }
	}

}
