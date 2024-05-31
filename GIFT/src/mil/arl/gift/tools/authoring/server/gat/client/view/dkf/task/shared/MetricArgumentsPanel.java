/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared;

import java.math.BigDecimal;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.PerformanceMetricArguments;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.AssessmentRollupWidget.MetricArgumentsWrapper;

/**
 * An editor used to modify arguments that will be passed into the performance assessment algorithm that
 * calculates the performance metrics
 * 
 * @author nroberts
 */
public class MetricArgumentsPanel extends Composite {

    /** 
     * The factor by which weights should be visually multiplied. Used to minimize how much 
     * the author needs to adjust decimal values
     */
    private static final int WEIGHT_VISUAL_FACTOR = 100; //display percentage values out of 100%

    private static MetricArgumentsPanelUiBinder uiBinder = GWT.create(MetricArgumentsPanelUiBinder.class);

    interface MetricArgumentsPanelUiBinder extends UiBinder<Widget, MetricArgumentsPanel> {
    }
    
    /** The box used to enter the performance weight argument */
    @UiField(provided=true)
    protected DecimalNumberSpinner weightBox = 
        new DecimalNumberSpinner(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(WEIGHT_VISUAL_FACTOR));
    
    /** A wrapper around the arguments for the performance metrics algorithm */
    private MetricArgumentsWrapper<?> wrapper;
    
    /** a listener to be notified when the weight value changes */
    private ValueChangeHandler<BigDecimal> weightBoxValueChangeListener = null;
    
    /**
     * Creates a new editor for modifying arguments for the performance metrics algorithm
     */
    public MetricArgumentsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        weightBox.setEnabled(!GatClientUtility.isReadOnly());
        weightBox.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                
                if(wrapper.getArguments() == null) {
                    wrapper.setArguments(new PerformanceMetricArguments());
                }
                
                wrapper.getArguments().setWeight(event.getValue().doubleValue()/WEIGHT_VISUAL_FACTOR);
                
                if(weightBoxValueChangeListener != null){
                    weightBoxValueChangeListener.onValueChange(event);
                }
            }
        });
    }

    /**
     * Loads the given wrapper object into this editor and begins editing it
     * 
     * @param obj a wrapper around a set of arguments for the performance metrics algorithm
     */
    protected void edit(MetricArgumentsWrapper<?> obj) {
        
        wrapper = obj;
        
        PerformanceMetricArguments metricArgs = obj.getArguments();
        
        BigDecimal weight = metricArgs != null
                ? BigDecimal.valueOf(metricArgs.getWeight() * WEIGHT_VISUAL_FACTOR) 
                : BigDecimal.ZERO;
                
        weightBox.setValue(weight);
    }

    /**
     * Sets whether this editor's interactive fields are read-only
     * 
     * @param isReadonly whether the fields should be read-only
     */
    protected void setReadonly(boolean isReadonly) {
        weightBox.setEnabled(!isReadonly);
    }
    
    /**
     * Set a listener to be notified when the weight value changes.
     * 
     * @param weightBoxValueChangeListener the listener to notify.  Can be null.
     */
    public void setWeightChangedListener(ValueChangeHandler<BigDecimal> weightBoxValueChangeListener){
        this.weightBoxValueChangeListener = weightBoxValueChangeListener;
    }
}
