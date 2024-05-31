/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Objects;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.VolumeSetting.VolumeChangeHandler;

/**
 * A slider that modifies and displayes the current state of a specific {@link VolumeSetting}
 * 
 * @author nroberts
 */
public class VolumeSettingSlider extends Composite implements VolumeChangeHandler{
    
    /** The multiplier to apply to slider values when they are displayed back to the user */
    private static final int SLIDER_SCALE_FACTOR = 100;

    private static VolumeSettingSliderUiBinder uiBinder = GWT.create(VolumeSettingSliderUiBinder.class);
    
    interface VolumeSettingSliderUiBinder extends UiBinder<Widget, VolumeSettingSlider> {
    }
    
    /** The volume setting that this slider modifies */
    private VolumeSetting volumeSetting = null;
    
    /** The slider scale that displays the volume */
    @UiField(provided=true)
    protected Slider volumeSlider = new Slider(
            SLIDER_SCALE_FACTOR * VolumeSetting.MIN_VOLUME, 
            SLIDER_SCALE_FACTOR * VolumeSetting.MAX_VOLUME, 
            SLIDER_SCALE_FACTOR * VolumeSetting.MAX_VOLUME
    );
    
    /** A button used to minimize the volume */
    @UiField
    protected Button minButton;
    
    /** A button used to maximize the volume */
    @UiField
    protected Button maxButton;

    /**
     * Creates a new slider that displays and modifies the given volume setting
     * 
     * @param setting a setting representing a sound whose volume can be adjusted
     */
    public VolumeSettingSlider(VolumeSetting setting) {
        initWidget(uiBinder.createAndBindUi(this));
        
        //update the setting when the user moves the slider
        volumeSlider.addValueChangeHandler(new ValueChangeHandler<Double>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                if(volumeSetting != null) {
                    
                    double value = event.getValue() != null 
                            ? event.getValue()/SLIDER_SCALE_FACTOR 
                            : VolumeSetting.MIN_VOLUME/SLIDER_SCALE_FACTOR;
                            
                    volumeSetting.setVolume(value);
                    volumeSetting.setMuted(value <= 0);
                }
            }
        });
        
        //move the slider when the user clicks the min button
        minButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(volumeSetting != null) {
                    volumeSlider.setValue(VolumeSetting.MIN_VOLUME * SLIDER_SCALE_FACTOR, true);
                }
            }
        });
        
        //move the slider when the user clicks the max button
        maxButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(volumeSetting != null) {
                    volumeSlider.setValue(VolumeSetting.MAX_VOLUME * SLIDER_SCALE_FACTOR, true);
                }
            }
        });
        
        if(setting == null) {
            throw new IllegalArgumentException("A volume slider must have a volume setting to adjust");
        }
        
        this.volumeSetting = setting;
        
        refresh();
        
        //subscribe to listen for changes to the setting outside of this widget
        setting.addManagedHandler(this);
    }

    @Override
    public void onVolumeChange(VolumeSetting setting) {
        if(Objects.equals(setting, volumeSetting)) {
            refresh();
        }
    }
    
    /**
     * Refreshes the UI components to match the internal state of the setting
     */
    private void refresh() {
        
        int muteFactor = volumeSetting.isMuted() ? 0 : 1;
        volumeSlider.setValue(volumeSetting.getVolume() * SLIDER_SCALE_FACTOR * muteFactor);
    }

}
