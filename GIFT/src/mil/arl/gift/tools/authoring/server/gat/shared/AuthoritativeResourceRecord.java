/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import java.io.Serializable;
import java.util.List;

/**
 * A record 
 * 
 * @author nroberts
 */
public class AuthoritativeResourceRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** The ID used to uniquely identify and locate the authoritative resource */
    private String id;
    
    /** The name of the authoritative resource */
    private String name;
    
    /** A description of the authoritative resource */
    private String description;
    
    /** The IDs of child resources related to the authoritative resource */
    private List<String> childrenIDs = null;
    
    /** The authoritative resource's type */
    private String type;
    
    /** The parent resource that this resource was obtained from */
    private AuthoritativeResourceRecord parent = null;

    /**
     * A default no-arg constructor required for serialization
     */
    private AuthoritativeResourceRecord() {}
    
    /**
     * Creates a record for the authoritative resource with the given ID
     * 
     * @param id the ID of the authoritative resource. Cannot be null.
     */
    public AuthoritativeResourceRecord(String id) {
        this();
        
        if(id == null) {
            throw new IllegalArgumentException("The ID of the authoritative resource to represent cannot be null");
        }
        
        this.id = id;
    }
    
    /**
     * Creates a record for the authoritative resource with the given ID, name, and description
     * 
     * @param id the ID of the authoritative resource. Cannot be null.
     * @param name the name of the authoritative resource. Can be null.
     * @param description a description of the authoritative resource. Can be null.
     */
    public AuthoritativeResourceRecord(String id, String name, String description) {
        this(id);
        
        this.name = name;
        this.description = description;
    }
    
    /**
     * Creates a record for the authoritative resource with the given ID, name, description, type, and children
     * 
     * @param id the ID of the authoritative resource. Cannot be null.
     * @param name the name of the authoritative resource. Can be null.
     * @param description description a description of the authoritative resource. Can be null.
     * @param type the authoritative resource's type. Can be null.
     * @param childrenIDs the IDs of this resource's children. Can be null.
     */
    public AuthoritativeResourceRecord(String id, String name, String description, String type, List<String> childrenIDs) {
        this(id, name, description);
        
        this.type = type;
        this.childrenIDs = childrenIDs;
    }

    /**
     * Gets the ID used to uniquely identify and locate the authoritative resource
     * 
     * @return the ID. Will not be null.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name of the authoritative resource
     * 
     * @return the name. Can be null.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a description of the authoritative resource
     * 
     * @return the description. Can be null.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the IDs of this authoritative resource's child resources
     * 
     * @return the IDs of the child resources. Can be null, if this resource has no children.
     */
    public List<String> getChildrenIDs() {
        return childrenIDs;
    }
    
    /**
     * Gets the authoritative resource's type
     * 
     * @return the authoritative resource's type. Can be null.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the parent resource that this resource was obtained from
     * 
     * @return the parent resource. Can be null.
     */
    public AuthoritativeResourceRecord getParent() {
        return parent;
    }

    /**
     * Sets the parent resource that this resource was obtained from
     * 
     * @param parent the parent resource. Can be null.
     */
    public void setParent(AuthoritativeResourceRecord parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return new StringBuilder("[AuthoritativeResourceRecord id='")
            .append(id)
            .append("', name='")
            .append(name)
            .append("', description='")
            .append(description)
            .append(", childrenIDs='")
            .append(childrenIDs)
            .append("]")
            .toString();
    }
    
    
}
