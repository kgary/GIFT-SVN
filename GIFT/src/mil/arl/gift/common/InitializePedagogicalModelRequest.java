/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class contains information needed to initialize a Pedagogical Model
 *
 * @author jleonard
 *
 */
public class InitializePedagogicalModelRequest {
    
    /** the XML Schema actions element populated with Pedagogical related information. */
    private String actions;
    
    /** the XML Pedagogical configuration information. */
    private String configuration;
    
    /** whether or not the actions are from a course (versus a DKF). */
    private boolean courseActions;

    /**
     * Class constructor
     *
     * @param actions - the XML Schema actions element populated with Pedagogical related information.
     * @param courseActions - whether or not the actions are from a course (versus a DKF).
     */
    public InitializePedagogicalModelRequest(String actions, boolean courseActions) {
        this.actions = actions;
        this.courseActions = courseActions;
    } 
    
    /**
     * Whether or not the actions are from a course (versus a DKF).
     * 
     * @return whether or not the actions are from a course (versus a DKF). Default is false.
     */
    public boolean isCourseActions(){
        return courseActions;
    }

    /**
     * Return the XML Schema actions element populated with Pedagogical related information.
     * 
     * @return the XML Schema actions element populated with Pedagogical related information.  See GIFT/config/ped/eMAP.xsd.
     */
    public String getActions(){
        return actions;
    }
    
    /**
     * Sets the Pedagogical configuration.
     * 
     * @param config - the XML Pedagogical configuration. Can be null.  See GIFT/config/ped/eMAP.xsd.
     */
    public void setPedModelConfig(String config) {
    	this.configuration = config;
    }
    
    /**
     * Returns the Pedagogical configuration as a String.
     * 
     * @return the XML Pedagogical configuration, or null if no configuration was set.  See GIFT/config/ped/eMAP.xsd.
     */
    public String getPedModelConfig(){
    	return configuration;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[InitializePedagogicalModelRequest: ");
        sb.append("configuration = ").append(getPedModelConfig());
        sb.append(", actions = ").append(getActions()); 
        sb.append(", isCourseActions = ").append(isCourseActions());
        sb.append("]");

        return sb.toString();
    }
}
