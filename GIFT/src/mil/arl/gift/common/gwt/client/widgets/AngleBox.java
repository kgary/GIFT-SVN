/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author nroberts
 */
public class AngleBox extends Composite implements HasValue<Double>{

	private static AngleBoxUiBinder uiBinder = GWT
			.create(AngleBoxUiBinder.class);

	interface AngleBoxUiBinder extends UiBinder<Widget, AngleBox> {
	}
	
	static{
		Image.prefetch("images/angle_select.png");
	}

	@UiField
	protected DoubleBox angleBox;
	
	@UiField
	protected Image angleImage;
	
	public AngleBox() {
		initWidget(uiBinder.createAndBindUi(this));
		
		angleBox.addValueChangeHandler(new ValueChangeHandler<Double>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				
				if(event.getValue() != null && !angleBox.getText().isEmpty()){
					updateGraphicAngle(event.getValue());
					
				} else {
					updateGraphicAngle(0);
				}
			}
		});
		
		angleBox.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				
				if(angleBox.getText() != null && !angleBox.getText().isEmpty()){
					
					try{
						updateGraphicAngle(Double.valueOf(angleBox.getText()));
					
					} catch(@SuppressWarnings("unused") NumberFormatException e){
						updateGraphicAngle(0);
					}
					
				} else {
					updateGraphicAngle(0);
				}
			}
		});
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Double> handler) {
		
		return angleBox.addValueChangeHandler(handler);
	}

	@Override
	public Double getValue() {
		return angleBox.getValue();
	}

	@Override
	public void setValue(Double value) {
		angleBox.setValue(value);
		
		updateGraphicAngle(value);
	}

	@Override
	public void setValue(Double value, boolean fireEvents) {
		angleBox.setValue(value, fireEvents);
	}

	private void updateGraphicAngle(double degrees){
		
		//Note: Need to use the reverse angle here since CSS rotation rotates in the opposite direction of what we want
		angleImage.getElement().getStyle().setProperty("MsTransform", "rotate(" + Double.toString(-degrees) + "deg)");
		angleImage.getElement().getStyle().setProperty("WebkitTransform", "rotate(" + Double.toString(-degrees) + "deg)");
		angleImage.getElement().getStyle().setProperty("transform", "rotate(" + Double.toString(-degrees) + "deg)");
	}
	
	/**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        angleBox.setEnabled(!isReadonly);
    }
}
