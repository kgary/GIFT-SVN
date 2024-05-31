/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

/**
 * Contains dead reckoning algorithm paraters normally used in entity state messages.
 * 
 * @author mhoffman
 *
 */
public class DeadReckoningParameters {
    
    /**  Indicates the type of dead reckoning algorithm used by an entity. */
    private DeadReckoningAlgorithmField deadReckoningAlgorithmField;

    /** enumerated types of dead reckoning algorithms */
    public enum DeadReckoningAlgorithmField{
        
        OTHER,
        STATIC,
        DRM_FPW,
        DRM_RPW,
        DRM_RVW,
        DRM_FVW,
        DRM_FPB,
        DRM_RPB,
        DRM_RVB,
        DRM_FVB
    }
    
    /**
     * Set attributes 
     * @param deadReckoningAlgorithmField Indicates the type of dead reckoning algorithm used by an entity.
     */
    public DeadReckoningParameters(DeadReckoningAlgorithmField deadReckoningAlgorithmField){
        this.deadReckoningAlgorithmField = deadReckoningAlgorithmField;
    }
    
    /**
     * Return the enumerated dead reckoning algorithm field.
     * @return  Indicates the type of dead reckoning algorithm used by an entity.
     */
    public DeadReckoningAlgorithmField getDeadReckoningAlgorithmField(){
        return deadReckoningAlgorithmField;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[DeadReckoningParameters: deadReckoningAlgorithmField = ");
        builder.append(deadReckoningAlgorithmField);
        builder.append("]");
        return builder.toString();
    }
    
    
}
