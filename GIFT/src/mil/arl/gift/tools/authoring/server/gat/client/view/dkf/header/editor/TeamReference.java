/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;

/**
 * A wrapper for a schema object that references a team or team member
 * 
 * @author nroberts
 */
public class TeamReference {
    
    /** The parent of the schema object containing the reference. Used to get an object name for display purposes. */
    private Serializable parent;
    
    /** The schema object containing the reference */
    private Serializable referenceObject;
    
    /** The number of times the schema object references a particular team or team member */
    private int refCount = 1;
    
    /**
     * Creates a wrapper around the given schema reference
     * 
     * @param parent the parent of the schema object containing the reference
     * @param referenceObject the schema object containing the reference
     */
    public TeamReference(Serializable parent, Serializable referenceObject) {
        this.parent = parent;
        this.referenceObject = referenceObject;
    }

    /**
     * Gets the schema object containing the reference
     * 
     * @return the schema object containing the reference
     */
    public Serializable getReferenceObject() {
        return referenceObject;
    }

    /**
     * Gets the number of times the schema object references a particular team or team member
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

    /**
     * Gets the parent schema object containing a display name for the reference
     * 
     * @return the parent schema object
     */
    public Serializable getParent() {
        return parent;
    }
}