/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

/**
 * A node in a hierarchy of team roles
 * 
 * @author nroberts
 */
public interface TeamRoleNode {

    /**
     * Gets the name of this role node
     * 
     * @return the role name
     */
    public String getName();
    
    /**
     * Gets the parent of this role node in the team role hierarchy
     * 
     * @return the parent node. Can be null if this role node is a root node.
     */
    public TeamRoleNode getParentRole();
}
