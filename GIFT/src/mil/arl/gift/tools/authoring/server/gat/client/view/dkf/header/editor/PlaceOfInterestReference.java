/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

/**
 * A wrapper for an object that references a place of interest
 * 
 * @author nroberts
 */
public abstract class PlaceOfInterestReference {   

    
    /** The number of times this condition references a particular place of interest */
    private int refCount = 1;


    /**
     * Gets the number of times this condition references a particular place of interest
     * 
     * @return the number of references
     */
    public int getReferenceCount() {
        return refCount;
    }
    
    /**
     * Increments the number of references by one
     */
    public void incrementReferences() {
        refCount++;
    }


}