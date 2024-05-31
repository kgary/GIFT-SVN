/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import generated.dkf.Concept;
import generated.dkf.Condition;

/**
 * A wrapper for a condition that references a place of interest
 * @author mhoffman
 *
 */
public class PlaceOfInterestConditionReference extends PlaceOfInterestReference {

    /** The condition's nearest parent concept. Used to get the parent concept name for display purposes. */
    private Concept parent;
    
    /** The condition containing the reference */
    private Condition condition;
    
    /**
     * Creates a wrapper around the given condition reference
     * 
     * @param parent the nearest parent concept of the condition containing the reference. Can't be null.
     * @param condition the condition containing the reference. Can't be null.
     */
    public PlaceOfInterestConditionReference(Concept parent, Condition condition) {
        super();
        
        if(parent == null){
            throw new IllegalArgumentException("The parent conept can't be null");
        }else if(condition == null){
            throw new IllegalArgumentException("The condition can't be null");
        }
        this.parent = parent;
        this.condition = condition;
    }
    
    /**
     * Gets the condition containing the reference
     * 
     * @return the condition that contains the place of interest reference.  Won't be null.
     */
    public Condition getCondition() {
        return condition;
    }
    
    /**
     * Gets the nearest parent concept of the condition containing the reference
     * 
     * @return the parent concept to the condition that has the place of interest reference.  Won't be null.
     */
    public Concept getParent() {
        return parent;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[PlaceOfInterestConditionReference: ");
        builder.append("count = ").append(getReferenceCount());
        builder.append(",m parent = ");
        builder.append(parent);
        builder.append(", condition = ");
        builder.append(condition);
        builder.append("]");
        return builder.toString();
    }
    
    
}
