/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import java.io.Serializable;

/**
 * This class contains the simulation address information
 * 
 * @author mhoffman
 *
 */
public class SimulationAddress implements TrainingAppState, Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** the unique site identifier for this address */
    private int siteID;
    
    /** the unique id for this address */
    private int appID;
    
    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private SimulationAddress(){}

    /**
     * Class constructor
     * 
     * @param siteID the unique site identifier for this address
     * @param appID the unique id for this address
     */
    public SimulationAddress(int siteID, int appID){
        this.siteID = siteID;
        this.appID = appID;
    }

    public int getSiteID(){
        return siteID;
    }

    public int getApplicationID(){
        return appID;
    }
    
    @Override
    public boolean equals(Object otherSimulationAddressData){
    	
        if(otherSimulationAddressData == null){
            return false;
        }else if(otherSimulationAddressData instanceof SimulationAddress){        
            return this.getApplicationID() == ((SimulationAddress)otherSimulationAddressData).getApplicationID() && 
                this.getSiteID() == ((SimulationAddress)otherSimulationAddressData).getSiteID();
        }else{
            return false;
        }
    }
    
    @Override
    public int hashCode(){
    	
    	// Start with prime number
    	int hash = 1153;
    	int mult = 47;
    	
    	if(this != null) {
	    	// Take another prime as multiplier, add members used in equals
    		
	    	hash = mult * hash + this.getApplicationID();
	    	hash = mult * hash + this.getSiteID();
    	}
    	
    	return hash;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[SimulationAddress: ");
        sb.append("Site ID = ").append(getSiteID());
        sb.append(", Application ID = ").append(getApplicationID());
        sb.append("]");

        return sb.toString();
    }

}
