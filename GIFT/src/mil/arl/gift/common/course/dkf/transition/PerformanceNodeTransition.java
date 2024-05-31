/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.transition;

import mil.arl.gift.common.course.transition.AbstractTransition;
import mil.arl.gift.common.enums.AbstractEnum;

/**
 * This transition class if for performance nodes (task and concepts) which can have a state (e.g. "at expectation").
 * 
 * @author mhoffman
 *
 */
public class PerformanceNodeTransition extends AbstractTransition {

    /** the performance node name (e.g. "perimeter sweep | corridor check") that has an associated state which can change */
    private String name;
    
    /** the unique performance node id */
    private int nodeId;
    
    /**
     * Class constructor - set attributes
     * 
     * @param nodeId unique performance node id for this transition 
     * @param name - the performance node name that has an associated state which can change 
     * @param previous - the previous enumerated value for the transition 
     * @param current - the current enumerated value for this transition
     */
    public PerformanceNodeTransition(int nodeId, String name, AbstractEnum previous, AbstractEnum current){
        super(previous, current, name, null);
        
        if(name == null){
            throw new IllegalArgumentException("The transition name can't be null");
        }else if(nodeId < 0){
            throw new IllegalArgumentException("The node id must be a positive number");
        }        
        
        this.name = name;
        this.nodeId = nodeId;
    }
    
    /**
     * Return the unique performance node id
     * 
     * @return int
     */
    public int getNodeId(){
        return nodeId;
    }
    
    /**
     * Return the performance node name
     * 
     * @return String
     */
    public String getName(){
        return name;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PerformanceNodeTransition: ");
        sb.append(super.toString());
        sb.append(", id = ").append(getNodeId());
        sb.append(", name = ").append(getName());
        sb.append("]");
     
        return sb.toString();
    }
    
}
