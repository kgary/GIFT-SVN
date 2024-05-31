/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor.LearnerStateOutline;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * Serves as a node within the tree that represents a learner's current state.
 * 
 * @param <T> the bounded type parameter that should restrict this node type to a String.
 *
 * @author mzellars
 */
public class LearnerStateNode<T> {

    /** The unique name of the node that can be used for matching purposes within a single hierarchy of nodes.  */
    private String name;
    
    /** the value that will be displayed in the tree. */
    private String displayName;
    
    /** the icon to use next to the display name */
    private Icon icon = null;
    
    /** (only for performance state attributes) the runtime state of the task/concept */
    private PerformanceNodeStateEnum nodeStateEnum = PerformanceNodeStateEnum.UNACTIVATED;
    
    private String shortTermVal = "", longTermVal = "", predictedVal = "";
    private long sTTimestamp, lTTimestamp, pTimestamp; 
    
    private boolean shortTemValHold;
    
    /** the parent to this node (optional) */
    private LearnerStateNode<T> parent;
    
    /** the children to this null. */
    private List<LearnerStateNode<String>> children;
    
    /**
     * Constructor for this class
     * 
     * @param name The unique name of the node that can be used for matching purposes within a single hierarchy of nodes.  Can only
     * be empty if the parent is null (indicates this is a hidden root node).
     * @param displayName the value that will be displayed in the tree.  Can only
     * be empty if the parent is null (indicates this is a hidden root node).
     * @param parent The parent node of this node.  Can be null.
     */
    public LearnerStateNode(String name, String displayName, LearnerStateNode<T> parent) {
        
        if(parent != null){
            if(StringUtils.isBlank(name)){
                throw new IllegalArgumentException("The name can't be null or empty.");
            }else if(StringUtils.isBlank(displayName)){
                throw new IllegalArgumentException("The display name can't be null or empty.");
            }
        }
        
    	this.name = name;
    	this.displayName = displayName;
    	this.parent = parent;
    	this.children = new ArrayList<LearnerStateNode<String>>();
    }
    
    public void addChild(LearnerStateNode<String> child) {
    	this.children.add(child);
    }
    
    public void removeChild(LearnerStateNode<String> child) {
    	this.children.remove(child);
    }
    
    /**
     * The unique name of the node that can be used for matching purposes within a single hierarchy of nodes.
     * 
     * @return  wont be null but can only be empty if the parent is null (indicates this is a hidden root node).
     */
    public String getName() {
    	return name;
    }
    
    /**
     * the value that will be displayed in the tree.
     * 
     * @return wont be null but can only be empty if the parent is null (indicates this is a hidden root node).
     */
    public String getDisplayName(){
        return displayName;
    }
    
    public Icon getIcon() {
    	return icon;
    }
    
    public LearnerStateNode<T> getParent() {
    	return parent;
    }
    
    public List<LearnerStateNode<String>> getChildren() {
    	return children;
    }
    
    /**
     * Return the task/concept state enum.
     * 
     * @return won't be null.
     */
    public PerformanceNodeStateEnum getNodeState(){
        return nodeStateEnum;
    }
    
    public String getShortTermVal() {
    	return shortTermVal;
    }
    
    public String getLongTermVal() {
    	return longTermVal;
    }
    
    public String getPredictedVal() {
    	return predictedVal;
    }
    
    public long getSTTimestamp() {
    	return sTTimestamp;
    }
    
    public long getLTTimestamp() {
    	return lTTimestamp;
    }
    
    public long getPTimestamp() {
    	return pTimestamp;
    }
    
    /**
     * Set the task/concept enumerated state.
     * 
     * @param nodeStateEnum can't be null.
     */
    public void setNodeState(PerformanceNodeStateEnum nodeStateEnum){
        
        if(nodeStateEnum == null){
            throw new IllegalArgumentException("The node state can't be null.");
        }
        this.nodeStateEnum = nodeStateEnum;
    }

    public void setShortTermVal(String val) {
    	this.shortTermVal = val;
    }
    
    public void setLongTermVal(String val) {
    	this.longTermVal = val;
    }
    
    public void setPredictedVal(String val) {
    	this.predictedVal = val;
    }
    
    public void setSTTimestamp(long timestamp) {
    	this.sTTimestamp = timestamp;
    }
    
    public void setLTTimestamp(long timestamp) {
    	this.lTTimestamp = timestamp;
    }
    
    public void setPTimestamp(long timestamp) {
    	this.pTimestamp = timestamp;
    }
    
    public void setIcon(Icon icon) {  	
    	this.icon = icon;
    }
    
    public boolean isShortTemValHold() {
        return shortTemValHold;
    }

    public void setShortTemValHold(boolean shortTemValHold) {
        this.shortTemValHold = shortTemValHold;
    }

    @Override
	public String toString() {
    	
    	StringBuffer sb = new StringBuffer();
    	sb.append("[LearnerStateNode: ");
    	sb.append("name = ").append(name);
    	sb.append("displayName = ").append(displayName);
    	sb.append(", state = ").append(nodeStateEnum.getDisplayName());
    	sb.append(", (short-term = ").append(shortTermVal);
    	sb.append("|timestamp = ").append(sTTimestamp);
    	sb.append("|hold =").append(shortTemValHold);
    	sb.append(") , (long-term = ").append(longTermVal);
    	sb.append("|timestamp = ").append(lTTimestamp);
    	sb.append("), predicted = ").append(predictedVal);
    	sb.append("|timestamp = ").append(pTimestamp);
    	sb.append(")]");
    	
    	return sb.toString();   	
    }
}
