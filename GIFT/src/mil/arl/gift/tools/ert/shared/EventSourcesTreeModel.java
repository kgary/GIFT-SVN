/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.shared;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.TreeViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the tree model containing nodes which represent event source(s).  The tree model contents
 * are shown to the user of the ERT.
 * 
 * @author mhoffman
 *
 */
public class EventSourcesTreeModel implements TreeViewModel, IsSerializable {
    
    /** used to create unique tree node ids */
    private static int currentNodeId = 0;
    
    /** contains all the root nodes of the tree */
    private List<EventSourceTreeNode> rootNodes = new ArrayList<EventSourceTreeNode>();
    
    /** map of all nodes in the tree by their unique node id */
    private Map<Integer, EventSourceTreeNode> nodeMap = new HashMap<Integer, EventSourceTreeNode>();
    
    /** the selection model used by the tree model */
    private final MultiSelectionModel<EventSourceTreeNode> selectionModel = new MultiSelectionModel<EventSourceTreeNode>();
    
    /**
     * Default Constructor
     * 
     * Required by IsSerializable to exist and be public
     */
    public EventSourcesTreeModel() {
        
    }
    
    /**
     * Return the next unique tree node id.
     * 
     * @return int - the next unique tree node id
     */
    public static synchronized int getNextNodeId(){
        return currentNodeId++;
    }
    
    /**
     * Add a new node to the tree
     * 
     * @param node - node to add to the tree
     * @param isRoot - whether this node is a root node in the tree
     */
    public void addNode(EventSourceTreeNode node, boolean isRoot){
        nodeMap.put(node.getNodeId(), node);
        
        if(isRoot){
            rootNodes.add(node);
        }
    }
    
    /**
     * Reset the tree model by clearing out its knowledge
     */
    public void reset(){
        
        nodeMap.clear();
        rootNodes.clear();
    }
    
    /**
     * Return the root nodes of the tree
     * 
     * @return List<EventSourceTreeNode>
     */
    public List<EventSourceTreeNode> getRootNodes(){
        return rootNodes;
    }
    
    /**
     * Return the node with the given unique node id.
     * 
     * @param nodeId - unique id of the node to find
     * @return EventSourceTreeNode
     */
    public EventSourceTreeNode getNode(int nodeId){
        return nodeMap.get(nodeId);
    }
    
    /**
     * Return the selected node.
     * 
     * @return EventSourceTreeNode
     */
    public Set<EventSourceTreeNode> getSelectedNodes() {
        return selectionModel.getSelectedSet();
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        
        if (value instanceof EventSourceTreeNode) {
            EventSourceTreeNode node = (EventSourceTreeNode) value;

            // Create a cell to display the node name.
            Cell<EventSourceTreeNode> cell = new AbstractCell<EventSourceTreeNode>() {

                @Override
                public void render(Context context, EventSourceTreeNode value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        sb.appendEscaped(value.getName());
                    }
                }
            };
            
            ListDataProvider<EventSourceTreeNode> dataProvider = new ListDataProvider<EventSourceTreeNode>(node.getChildren());
            
            if(isLeaf(node)){
                //found a leaf node              

                return new DefaultNodeInfo<EventSourceTreeNode>(dataProvider, cell, selectionModel, null);
                
            }else{
                //found a non-leaf node
             
                return new DefaultNodeInfo<EventSourceTreeNode>(dataProvider, cell, selectionModel, null);
            }
        }
        
        return null;
    }

    @Override
    public boolean isLeaf(Object value) {
        
        if(value instanceof EventSourceTreeNode){
            return ((EventSourceTreeNode)value).getChildren().isEmpty();
        }
        return false;
    }
    
    /**
     * Selects the given node within this tree model
     *
     * @param node The node to select. Cannot be null.
     */
    public void setSelectedNode(EventSourceTreeNode node) {
        selectionModel.setSelected(node, true);
    }
    
    /**
     * Adds the given selection handler to this tree model. Said handler will be notified when nodes within this tree model are selected/deselected
     *
     * @param the handler to add. Cannot be null.
     * @return the handler registration of the added handler.
     */
    public HandlerRegistration addSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
        return selectionModel.addSelectionChangeHandler(handler);
    }
}
