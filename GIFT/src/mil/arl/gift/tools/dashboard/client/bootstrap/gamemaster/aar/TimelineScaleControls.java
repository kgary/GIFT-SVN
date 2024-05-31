/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import org.gwtbootstrap3.extras.slider.client.ui.Slider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget containing UI controls to change the visual scale of a timeline
 * 
 * @author nroberts
 */
public class TimelineScaleControls extends Composite {

    private static TimelineScaleControlsUiBinder uiBinder = GWT.create(TimelineScaleControlsUiBinder.class);

    interface TimelineScaleControlsUiBinder extends UiBinder<Widget, TimelineScaleControls> {
    }
    
    /** A slider that lets the user control the scaling factor for the time scale (i.e. the horizontal zoom level) */
    @UiField(provided=true)
    protected Slider timescaleSlider = new Slider(1, 10, 1);
    
    /** A slider that lets the user control the scaling factor for the y scale (i.e. the vertical zoom level) */
    @UiField(provided=true)
    protected Slider zoomSlider = new Slider(0.5, 2, 1);

    /** 
     * The timeline's implementation of the zoom (i.e. y-scale) control. This should be invoked
     * when the user interacts with the proper UI component 
     */
    private Command zoomImpl;

    /** 
     * The timeline's implementation of the timescale (i.e. x-scale) control. This should be invoked
     * when the user interacts with the proper UI component 
     */
    private Command timescaleImpl;

    /**
     * Creates a new widget containing scale controls for a timeline chart
     */
    public TimelineScaleControls() {
        initWidget(uiBinder.createAndBindUi(this));
        
        zoomSlider.addValueChangeHandler(new ValueChangeHandler<Double>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                if(zoomImpl != null) {
                    zoomImpl.execute();
                }
            }
        });
        
        timescaleSlider.addValueChangeHandler(new ValueChangeHandler<Double>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                if(timescaleImpl != null) {
                    timescaleImpl.execute();
                }
            }
        });
    }
    
    /**
     * Gets the zoom level that should be used based on the controls
     *  
     * @return the zoom level. Cannot be null.
     */
    public Double getZoomLevel() {
        return zoomSlider.getValue();
    }
    
    /**
     * Gets the timescale level that should be used based on the controls
     *  
     * @return the timescale level. Cannot be null.
     */
    public Double getTimescaleLevel() {
        return timescaleSlider.getValue();
    }
    
    /** 
     * Sets the timeline's implementation of the zoom (i.e. y-scale) control. This will be invoked
     * when the user interacts with the proper UI component 
     * 
     * @param impl a command implementing the logic for the control. Can be null, if the 
     * control should do nothing
     */
    public void setZoomImpl(Command impl) {
        this.zoomImpl = impl;
    }

    /** 
     * Sets the timeline's implementation of the timescale (i.e. x-scale) control. This will be invoked
     * when the user interacts with the proper UI component 
     * 
     * @param impl a command implementing the logic for the control. Can be null, if the 
     * control should do nothing
     */
    public void setTimescaleImpl(Command impl) {
        this.timescaleImpl = impl;
    }
}
