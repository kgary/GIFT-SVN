/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;
import java.util.List;

import mil.arl.gift.common.enums.TrainingApplicationEnum;

/**
 * Used to set the different gateway connections that are requested by the game master user.
 * 
 * @author mhoffman
 *
 */
public class GatewayConnection implements Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /** the training applications types the gateway should connect too */
    private List<TrainingApplicationEnum> taTypes;
    
    /** whether the gateway connection for DIS should be established */
    private boolean useDIS = false;
    
    /**
     * Used for NO connections and GWT serialization.
     */
    public GatewayConnection(){}
    
    /**
     * Used for connecting to zero or more training applications and whether to use DIS connection.
     * 
     * @param taTypes the training applications types the gateway should connect too.  Can be empty but not null.
     * @param useDIS whether the gateway connection for DIS should be established
     */
    public GatewayConnection(List<TrainingApplicationEnum> taTypes, boolean useDIS){

        if(taTypes == null){
            throw new IllegalArgumentException("The Training app type enum can't be null.  If you want to turn off connections than use the no arg constructor");
        }
        
        this.taTypes = taTypes;
        
        this.useDIS = useDIS;
    }

    /**
     * Return the training applications types the gateway should connect too.  
     * @return Can be empty but not null.
     */
    public List<TrainingApplicationEnum> getTaTypes() {
        return taTypes;
    }

    /**
     * Return whether the gateway connection for DIS should be established
     * @return default is false
     */
    public boolean shouldUseDIS() {
        return useDIS;
    }
    
    /**
     * Return whether there is at least one connection mentioned in this instance.
     * 
     * @return checkes all attributes to see if at least one connection has been set.
     */
    public boolean hasConnection(){
        return useDIS || !taTypes.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[GatewayConnection: taTypes = ");
        builder.append(taTypes);
        builder.append(", useDIS = ");
        builder.append(useDIS);
        builder.append("]");
        return builder.toString();
    }


}
