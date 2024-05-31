/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.VolumeSetting.VolumeChangeHandler;

/**
 * A setting that adjusts the volume of one or more sounds and notifies any listeners when said
 * volume is changed
 * 
 * @author nroberts
 */
public class VolumeSetting extends AbstractProvider<VolumeChangeHandler>{
    
    /** The maximum volume that can be assigned to a sound */
    public static final double MAX_VOLUME = 1;
    
    /** The minimum volume that can be assigned to a sound */
    public static final double MIN_VOLUME = 0;
    
    /** The current volume of the sounds represented by this setting */
    private double volume = MAX_VOLUME;
    
    /** Whether the sounds represented by this setting are muted */
    private boolean muted = false;
    
    /** 
     * Any additional volume settings representing any sounds encompassed by this setting. 
     * This can be used to allow a single volume setting to modify multiple sounds at once
     */
    private List<VolumeSetting> subVolumeSettings;
    
    /** 
     * The parent volume setting. When calculating the volume of this setting, this will be
     * checked to see if a volume setting encompassing this setting has a lower volume.
     */
    private VolumeSetting parent;
    
    /**
     * Creates a new volume setting representing a sound
     */
    public VolumeSetting() {
        this(MAX_VOLUME);
    }
    
    /**
     * Creates a new volume setting representing a sound
     * 
     * @param defaultVolume the default volume of the sound
     */
    public VolumeSetting(double defaultVolume) {
        setVolume(defaultVolume);
    }
    
    /**
     * Adds the given volume to this one as a sub-volume setting. This essentially allows
     * the volume settings for multiple sounds to be changed by a single parent setting that
     * encompasses them.
     * 
     * @param setting the setting to add. Cannot be null.
     * @return whether the setting was successfully added.
     */
    public boolean addSubVolumeSetting(VolumeSetting setting) {
        
        if(setting == null) {
            throw new IllegalArgumentException("The sub-volume seting to add cannot be null");
        }
        
        if(subVolumeSettings == null) {
            subVolumeSettings = new ArrayList<>();
        }
        
        boolean added = subVolumeSettings.add(setting);
        if(added) {
            setting.parent = this;
        }
        
        return added;
    }

    /**
     * Gets the current volume assigned to the sound represented by this setting. If this setting 
     * is a sub-volume of another setting, then the volume of the parent setting will take precedence
     * if it is lower.
     * 
     * @return the volume to play the sound represented by this setting at.
     */
    public double getVolume() {
        
        double minVolume = volume;
        if(parent != null) {
            minVolume = Math.min(volume, parent.getVolume());
        }
        
        return minVolume;
    }
    
    /**
     * Sets the volume of the sound represented by this setting. If the value is greater than 1 
     * or less than 0, it will be constrained to fit between 0 and 1.
     * 
     * @param volume the volume. Will be constrained to between 0 and 1 if needed.
     */
    public void setVolume(double volume) {
        
        double newVolume = volume;
        if(volume < MIN_VOLUME) {
            newVolume = MIN_VOLUME;
            
        } else if(volume > MAX_VOLUME) {
            newVolume = MAX_VOLUME;
        }
        
        this.volume = newVolume;
        
        onChange();
    }
    
    /**
     * Gets whether the sound represented by this setting is muted. If any parent setting containing
     * this setting is muted, then this setting will also be considered muted.
     * 
     * @return whether the sound is muted.
     */
    public boolean isMuted() {
        
        boolean anyMuted = muted;
        
        if(!anyMuted && parent != null) {
            anyMuted = anyMuted || parent.isMuted();
        }
        
        return anyMuted;
    }
    
    /**
     * Sets whether the sound represented by this setting is muted
     * 
     * @param muted whether the sound is muted
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
        
        onChange();
    }
    
    /**
     * Notifies any listeners that this setting and its parents/children have been changed
     */
    private void onChange() {
        onChange(true);
    }
    
    /**
     * Notifies any listeners that this setting has been changed
     * 
     * @param propagateToRelatives whether listeners for this setting's parents and children
     * should also be notified, if any parents or children are present
     */
    private void onChange(boolean propagateToRelatives) {
        
        executeHandlers(new SafeHandlerExecution<VolumeChangeHandler>() {
            @Override
            public void execute(VolumeChangeHandler handler) {
                handler.onVolumeChange(VolumeSetting.this);
            }
        });
        
        if(propagateToRelatives) {
            
            //notify children of changes first
            if(subVolumeSettings != null) {
                for(VolumeSetting child : subVolumeSettings) {
                    child.onChange(false);
                }
            }
            
            //notify parents next
            VolumeSetting currParent = this;
            while(currParent.parent != null) {
                currParent = currParent.parent;
                currParent.onChange(false);
            }
        }
    }
    
    /**
     * A handler that receives notifications when a volume setting is changed and performs
     * logic to handle the change
     * 
     * @author nroberts
     */
    public interface VolumeChangeHandler {
        
        /**
         * Handles when the given volume setting has changed
         * 
         * @param setting the volume setting that was changed. Will not be null.
         */
        public void onVolumeChange(VolumeSetting setting);
    }
}