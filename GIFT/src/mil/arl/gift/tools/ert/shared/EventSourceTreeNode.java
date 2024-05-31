/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A Event source tree node is a node object which belongs to a tree.  
 * This class contains the event source information for a node in the tree.  
 * 
 * @author mhoffman
 *
 */
public class EventSourceTreeNode implements IsSerializable {

    /** unique node id w/in a tree */
    private int nodeId;
    
    /** whether this node's event source is a folder */
    private boolean isFolder;
    
    /** the name of the node */
    private String name;
    
    /** the child nodes of this node */
    private List<EventSourceTreeNode> children = new ArrayList<EventSourceTreeNode>();
    
    
    /**
     * Default Constructor
     * 
     * Required by IsSerializable to exist and be public
     */
    public EventSourceTreeNode() {
        
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param nodeId unique node id w/in a tree
     * @param name the name of the node
     * @param isFolder whether this node's event source is a folder
     */
    public EventSourceTreeNode(int nodeId, String name, boolean isFolder){
        this.nodeId = nodeId;
        this.name = name;
        this.isFolder = isFolder;
    }
    
    /**
     * Return the unique node id w/in a tree
     * 
     * @return int
     */
    public int getNodeId(){
        return nodeId;
    }
    
    /**
     * Return the name of the node
     * 
     * @return String
     */
    public String getName(){
        return name;
    }
    
    /**
     * Return whether this node's event source is a folder
     * 
     * @return boolean
     */
    public boolean isFolder(){
        return isFolder;
    }
    
    /**
     * Add a child node to this node.
     * 
     * @param child - child of this node
     */
    public void addChild(EventSourceTreeNode child){
        children.add(child);
    }
    
    /**
     * Return the child nodes of this node
     * 
     * @return List<EventSourceTreeNode>
     */
    public List<EventSourceTreeNode> getChildren(){
        return children;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EventSourceTreeNode: ");
        sb.append("id = ").append(getNodeId());
        sb.append(", name = ").append(getName());
        sb.append(", isFolder = ").append(isFolder());
        
        sb.append("]");
        return sb.toString();
    }
}
