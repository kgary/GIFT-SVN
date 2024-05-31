/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Objects;

/**
 * A path of performance nodes IDs leading to a target performance node ID. This is used
 * to avoid unnecessarily traversing an entire tree of performance nodes in order to locate a
 * specific node within that tree.
 * 
 * @author nroberts
 */
public class PerformanceNodePath {

    /** The path of the parent node above this node. If null, then this node is the start of the path. */
    private PerformanceNodePath parent;
    
    /** The path of the child node below this node. If null, then this node is the end of the path and,
     * therefore, the target node that is being looked for. */
    private PerformanceNodePath child;
    
    /** The ID of the performance node that this path represents */
    private int nodeId;
    
    /**
     * Creates a new path representing the performance node with the given ID, with no
     * parent or child nodes.
     * 
     * @param nodeId the ID of the performance node to represent
     */
    public PerformanceNodePath(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Gets the path of the parent node above this node.
     * 
     * @return the parent node. If null, then this node is the start of the path.
     */
    public PerformanceNodePath getParent() {
        return parent;
    }

    /**
     * Sets the path of the parent node above this node. If the given node already has a child,
     * then the existing child will be replaced with this node and will be orphaned.
     * 
     * @param parent the parent node. Can be null, if this node is the start of the path.
     * CANNOT be this node's existing child node, since the performance node hierarchy 
     * doesn't allow loops.
     */
    public void setParent(PerformanceNodePath parent) {
        
        if(parent != null && Objects.equals(child, parent)) {
            throw new IllegalArgumentException("The provided parent is already a child of this node");
        }
        
        if(parent != null) {
            
            if(parent.getChild() != null) {
                parent.getChild().setParent(null);
            }
            
            parent.setChild(this);
        }
        
        this.parent = parent;
    }

    public int getNodeId() {
        return nodeId;
    }

    /**
     * Gets the path of the child node below this node.
     * 
     * @return the child node. If null, then this node is the end of the path and,
     * therefore, the target node that is being looked for.
     */
    public PerformanceNodePath getChild() {
        return child;
    }

    /**
     * Sets the path of the child node below this node. If the given node already has a parent,
     * then the existing parent will be replaced with this node and will be childless.
     * 
     * @param child the child node. Can be null, if this node is the end of the path and,
     * therefore, the target node that is being looked for. CANNOT be this node's existing parent node, 
     * since the performance node hierarchy doesn't allow loops.
     */
    public void setChild(PerformanceNodePath child) {
        
        if(child != null && Objects.equals(child, parent)) {
            throw new IllegalArgumentException("The provided child is already a parent of this node");
        }
        
        if(child != null) {
            
            if(child.getParent() != null) {
                child.getParent().setChild(null);
            }
            
            child.setParent(null);
        }
        
        this.child = child;
    }
    
    /**
     * Gets the path of the topmost parent node that has no parent. This node is where the
     * path begins.
     * 
     * @return the path start. Will not be null.
     */
    public PerformanceNodePath getPathStart() {
        
        PerformanceNodePath currPath = this;
        while(currPath.getParent() != null) {
            currPath = currPath.getParent();
        }
        
        return currPath;
    }
    
    /**
     * Gets the path of the bottommost child node that has no child.  This node is where the
     * path end and, therefore, is the target node of the path.
     * 
     * @return the path end node. Will not be null.
     */
    public PerformanceNodePath getPathEnd() {
        
        PerformanceNodePath currPath = this;
        while(currPath.getChild() != null) {
            currPath = currPath.getChild();
        }
        
        return currPath;
    }
}
