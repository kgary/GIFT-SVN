/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.LearnerStateIconMap;
import mil.arl.gift.common.state.AbstractLearnerState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.tools.monitor.LearnerStateOutline.CellRenderer;
import mil.arl.gift.tools.monitor.LearnerStateOutline.LearnerStateNode;
import mil.arl.gift.tools.monitor.LearnerStateOutline.LearnerStateRowModel;
import mil.arl.gift.tools.monitor.LearnerStateOutline.LearnerStateTreeModel;
import mil.arl.gift.tools.monitor.LearnerStateOutline.PerformanceLearnerStateRowModel;
import mil.arl.gift.tools.monitor.LearnerStateOutline.RenderData; 

/**
 * Receives data about a learner's performance, cognitive and affective state 
 * during an active domain session and represents each one using an Outline.
 * 
 * @author mhoffman
 * @author mzellars
 */
public class LearnerStatePanel extends javax.swing.JPanel implements MonitorMessageListener, DomainSessionMonitorListener, DomainSessionStatusListener {

    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LearnerStatePanel.class);
    
    protected final JList<Message> messageListView = new JList<>();
    protected final JList<MessageTypeEnum> filterListView  = new JList<>();
    
    private Integer domainSessionId = null;
    private final int ICON_WIDTH = 20, ICON_HEIGHT = 20;
    
    /** Used to store images for specific outline nodes */
    private HashMap<String, Icon> iconMap = new HashMap<String, Icon>();
    
    /** 
     * used to keep the last learner state for all users, this way when a user is selected to
     * be monitored, that user's last learner state (if received) will be used to populate this panel
     * 
     *  key: domain session id
     *  value: last learner state received in that domain session
     */
    private HashMap<Integer, LearnerState> dsIdToLearnerState = new HashMap<>();
    
    private static final String DEFAULT_ICON_PATH = "/mil/arl/gift/common/images/icons/placeholder.png";
    
    //icons for expand/collapse all buttons
    private final URL expand_all_iconURL = getClass().getResource("/mil/arl/gift/common/images/expand_all.png");
    private final URL collapse_all_iconURL = getClass().getResource("/mil/arl/gift/common/images/collapse_all.png");
    private final ImageIcon EXPAND_ALL_ICON = new ImageIcon(expand_all_iconURL);
    private final ImageIcon COLLAPSE_ALL_ICON = new ImageIcon(collapse_all_iconURL);
    
    /** Gives the outline custom style. */
    RenderData dataRenderer = new RenderData();
    
    /** Gives the cells custom style. */
    CellRenderer cellRenderer = new CellRenderer();
    
    /** Defines the table structure of the learner state outline */
    LearnerStateRowModel defaultRowModel = new LearnerStateRowModel();
    LearnerStateRowModel performanceRowModel = new PerformanceLearnerStateRowModel();
    
    // Root nodes for each learner state outline
    LearnerStateNode<String> performanceRootNode = new LearnerStateNode<String>(Constants.EMPTY, Constants.EMPTY, null);
    LearnerStateNode<String> cognitiveRootNode = new LearnerStateNode<String>(Constants.EMPTY, Constants.EMPTY, null);
    LearnerStateNode<String> affectiveRootNode = new LearnerStateNode<String>(Constants.EMPTY, Constants.EMPTY, null);

    /** An empty outline model used to clear the learner state panel outlines */
    OutlineModel emptyOutlineModel = DefaultOutlineModel.createOutlineModel(
    		new LearnerStateTreeModel(
    				new LearnerStateNode<String>(Constants.EMPTY, Constants.EMPTY, null)), 
    		defaultRowModel, true, "");
    
    /** Creates new form LearnerStatePanel */
    public LearnerStatePanel() {
        initComponents();
        
        // Initialize the three learner state outlines
        initOutline(performanceOutline);
        initOutline(affectiveOutline);
        initOutline(cognitiveOutline);
        
        // Add a default node icon
        getIcon(null, DEFAULT_ICON_PATH);
    }
    
    private void initOutline(Outline outline) {
    	
    	// Add the custom renderers to the outline
        outline.setDefaultRenderer(Object.class, cellRenderer);
        outline.setRenderDataProvider(dataRenderer);
        outline.setSelectionForeground(Color.BLUE);

        // By default, the root is shown, while here that isn't necessary.
        outline.setRootVisible(false);
        
        // Disable sorting and reordering
        outline.setRowSorter(null);
        outline.getTableHeader().setReorderingAllowed(false);   
    }
    
    /**
     * The callback for when a message is received by the Monitor module
     * 
     * @param msg The message received by the Monitor module
     */
    @Override
    public void handleMessage(Message msg){
        
        if(domainSessionId == null){
            return;
        }
        
        if(msg.getMessageType() == MessageTypeEnum.LEARNER_STATE){
            handleLearnerStateMessage((DomainSessionMessage) msg);
        }
    }
    
    /**
     * Populate the learner state table with the contents of the learner state message
     * 
     * @param msg
     */
	private void handleLearnerStateMessage(DomainSessionMessage msg) {
        
        if(msg.getDomainSessionId() == domainSessionId){

        	// Found appropriate domain session.
        	// Build three outlines to represent the performance, affective, and cognitive states that were just reported.          
        	LearnerState state = (LearnerState)msg.getPayload();
        	handleLearnerState(state);
        	
        }else{
        	dsIdToLearnerState.put(msg.getDomainSessionId(), (LearnerState) msg.getPayload());
        }
    }
	
	private void handleLearnerState(LearnerState state){		
    	
        // Performance State
    	buildLearnerStateOutline(state.getPerformance(), performanceOutline, performanceRootNode, performanceAutoExpandCheckbox.isSelected());
            
    	// Cognitive State
        buildLearnerStateOutline(state.getCognitive(), cognitiveOutline, cognitiveRootNode, cognitiveAutoExpandCheckbox.isSelected());
        
        // Affective State
        buildLearnerStateOutline(state.getAffective(), affectiveOutline, affectiveRootNode, affectiveAutoExpandCheckbox.isSelected());
        
        warningLabel.setVisible(false);
	}
  
	/**
	 * Builds an outline to represent a learner's current performance, cognitive, or affective state.
	 * 
	 * @param learnerState the learner's performance, cognitive, or affective state
	 * @param outline the outline that is being built
	 * @param rootNode
	 * @param expandAll whether all the nodes should be expanded
	 */
    @SuppressWarnings("unchecked")
	private void buildLearnerStateOutline(Object learnerState, Outline outline, LearnerStateNode<String> rootNode, boolean expandAll) {
    	
        LearnerStateRowModel rowModelToUse = defaultRowModel;
    	if (learnerState instanceof PerformanceState) {
    		// Building a performance state tree
    		rootNode = buildPerformanceTree(rootNode, (PerformanceState)learnerState);
            rowModelToUse = performanceRowModel;
    		
    	}else if (learnerState instanceof AbstractLearnerState) {
    		// Building a cognitive or affective state tree
    		rootNode = buildAbstractStateTree(rootNode, (AbstractLearnerState)learnerState);
    	}   
    	
    	//save the current selection for this outline to reselect that row after the outline has been rebuilt
    	int currentSelectedIndex = outline.getSelectedRow();

    	//
    	// Rebuild the tree/outline with the new node data model
    	// This is required because it was discovered that the tree would not redraw itself entirely after the first
    	// time the outline model was created.  Visually this would prevent new nodes from being shown (unless the node 
    	// had descendants).  Several attempts where made at refreshing using TableModelEvent, repaint, invalidate, SwingUtilities,
    	// add/remove artificial descendants to a leaf node, etc.
    	//
    	//
            
        // The outline will use our custom tree model for accessing data
        TreeModel learnerStateTreeModel = new LearnerStateTreeModel(rootNode);
        
        // Create an outline model with the tree model and our custom row model.
        OutlineModel outlineModel = DefaultOutlineModel.createOutlineModel(learnerStateTreeModel, rowModelToUse, true, "Name");     
        
        /* IssueID: #5439 Need to update the model in the UI event thread, otherwise, an ArrayIndexOutOfBounds exception might get thrown
         * if the thread surrounding this call runs too quickly. */
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                
                outline.setModel(outlineModel);
                
                // Auto expand the outline
                if (expandAll) {
                    expandAll(outline, new TreePath(((TreeModel) outline.getModel()).getRoot()),
                            (LearnerStateNode<String>) ((TreeModel) outline.getModel()).getRoot(), true,
                            currentSelectedIndex);
                }
            }
        });
        
        
        

    }    
    
    /**
     * Builds a tree of nodes to represent the learner's current state of cognition or affectiveness
     * 
     * @param state the learner's current cognitive or affective state
     * @param rootName the name of the tree
     * @return the root node in the tree
     */
    private LearnerStateNode<String> buildAbstractStateTree(LearnerStateNode<String> root, AbstractLearnerState state) {
    	
    	// Add each attribute as a child of the root.
    	for (Map.Entry<LearnerStateAttributeNameEnum, LearnerStateAttribute> attribute : state.getAttributes().entrySet()) {
    	    
    	    LearnerStateNode<String> aNode = null;
    	    for(LearnerStateNode<String> existingNode : root.getChildren()){
    	        
    	        if(existingNode.getName().equalsIgnoreCase(attribute.getValue().getName().getName())){
    	            aNode = existingNode;
    	            break;
    	        }
    	    }
    	    
    	    if(aNode == null){

        		// Create the attribute node
    	        logger.info("Created new learner state node for '"+attribute.getKey().getDisplayName()+"'.");
        		aNode = new LearnerStateNode<String>(attribute.getKey().getName(), attribute.getKey().getDisplayName(), root);
        		root.addChild(aNode);
    	    }
    	    
    	    updateAbstractStateNode(aNode, root, attribute.getKey().getDisplayName(), attribute.getValue());
    	}
    	
    	return root;
    }

    /**
     * Builds a tree of nodes to represent the learner's current state of performance
     * 
     * @param state the learner's current performance state
     * @param rootName the name of the tree
     * @return the root node in the tree
     */
    private LearnerStateNode<String> buildPerformanceTree(LearnerStateNode<String> root, PerformanceState state) {
    	
    	// Add each task as a child of the root of the tree
    	for (Map.Entry<Integer, TaskPerformanceState> task : state.getTasks().entrySet()) {
    	    
    	    //search for existing task node
    	    LearnerStateNode<String> taskNode = null;
    	    for(LearnerStateNode<String> existingTask : root.getChildren()){
    	        
    	        if(existingTask.getName().equalsIgnoreCase(task.getValue().getState().getName())){
    	            taskNode = existingTask;
    	            break;
    	        }
    	    }

    	    if(taskNode == null){
    	        //create new node for this previously unknown task
    	        logger.info("Created new learner state node for task '"+task.getValue().getState().getName()+"'.");
    	        taskNode = new LearnerStateNode<String>(task.getValue().getState().getName(), task.getValue().getState().getName(), root);
    	           
                root.addChild(taskNode);
    	    }
    		
    		// Set the task's data. This will be represented in a table row
    	    updateTaskNode(taskNode, task.getValue());

    		
    		// Add each concept as a child of this task
    		TaskPerformanceState concepts = task.getValue();
    		for (ConceptPerformanceState concept : concepts.getConcepts()) {
    			
    		    //search for existing concept node
    		    LearnerStateNode<String> conceptNode = null;
    		    for(LearnerStateNode<String> existingConcept : taskNode.getChildren()){
    		        
    		        if(existingConcept.getName().equalsIgnoreCase(concept.getState().getName())){
    		            conceptNode = existingConcept;
    		            break;
    		        }
    		    }
    		    
    		    if(conceptNode == null){
    		        //create new node for this previously unknown concept
    		        logger.info("Created new learner state node for concept '"+concept.getState().getName()+"' (a child of task '"+taskNode.getName()+"').");
    		        conceptNode = new LearnerStateNode<String>(concept.getState().getName(), concept.getState().getName(), taskNode);
    		        
                    taskNode.addChild(conceptNode);
    		    }

                updateConceptNode(conceptNode, concept, taskNode);  
    		}
    	}
    	 
    	return root;
    }
    
    /**
     * Update the data model for the provided task assessment
     * 
     * @param taskNode the data model for the table
     * @param taskPerformanceState the task performance assessment data to put in the data model
     */
    private void updateTaskNode(LearnerStateNode<String> taskNode, TaskPerformanceState taskPerformanceState){
        
        taskNode.setNodeState(taskPerformanceState.getState().getNodeStateEnum());
        
        taskNode.setShortTermVal(taskPerformanceState.getState().getShortTerm().getDisplayName());
        taskNode.setSTTimestamp(taskPerformanceState.getState().getShortTermTimestamp());
        taskNode.setShortTemValHold(taskPerformanceState.getState().isAssessmentHold());
        
        taskNode.setLongTermVal(taskPerformanceState.getState().getLongTerm().getDisplayName());
        taskNode.setLTTimestamp(taskPerformanceState.getState().getLongTermTimestamp());
        
        taskNode.setPredictedVal(taskPerformanceState.getState().getPredicted().getDisplayName());
        taskNode.setPTimestamp(taskPerformanceState.getState().getPredictedTimestamp());
        
        taskNode.setIcon(getIcon(taskNode.getName(), LearnerStateIconMap.getTaskIconPath()));
    }
    
    /**
     * Update the concept node that represents a learner state concept
     * 
     * @param conceptNode the data model for the table
     * @param concept The concept performance assessment data to put in the data model
     * @param parent the parent of the concept node being created (either a task or intermediate concept)
     */
    private void updateConceptNode(LearnerStateNode<String> conceptNode, ConceptPerformanceState concept, LearnerStateNode<String> parent) {
    	    	
    	// Set the concept's data
        conceptNode.setNodeState(concept.getState().getNodeStateEnum());
        
		conceptNode.setShortTermVal(concept.getState().getShortTerm().getDisplayName());
		conceptNode.setSTTimestamp(concept.getState().getShortTermTimestamp());
		conceptNode.setShortTemValHold(concept.getState().isAssessmentHold());
		
		conceptNode.setLongTermVal(concept.getState().getLongTerm().getDisplayName());
		conceptNode.setLTTimestamp(concept.getState().getLongTermTimestamp());
		
		conceptNode.setPredictedVal(concept.getState().getPredicted().getDisplayName());
		conceptNode.setPTimestamp(concept.getState().getPredictedTimestamp());
		
		conceptNode.setIcon(getIcon(conceptNode.getName(), LearnerStateIconMap.getConceptIconPath()));
		
		if (concept instanceof IntermediateConceptPerformanceState) {
			
			// This concept has child concepts
			for (ConceptPerformanceState subConcept : ((IntermediateConceptPerformanceState) concept).getConcepts()) {
				
	             //search for existing concept node
                LearnerStateNode<String> subConceptNode = null;
                for(LearnerStateNode<String> existingConcept : conceptNode.getChildren()){
                    
                    if(existingConcept.getName().equalsIgnoreCase(subConcept.getState().getName())){
                        subConceptNode = existingConcept;
                        break;
                    }
                }
                
                if(subConceptNode == null){
                    //create new node for this previously unknown concept
                    logger.info("Created new learner state node for subconcept '"+subConcept.getState().getName()+"' (a child of concept '"+conceptNode.getName()+"').");
                    subConceptNode = new LearnerStateNode<String>(subConcept.getState().getName(), subConcept.getState().getName(), conceptNode);
                    
                    conceptNode.addChild(subConceptNode);
                }

                updateConceptNode(subConceptNode, subConcept, conceptNode);  
			}
		}
    }
    
    /**
     * Update a node that represents an attribute of the learner's current cognitive or affective state
     * 
     * @param aNode the data model for the table
     * @param parent the parent of the node that will be created
     * @param attributeName the name of the node that will be created
     * @param attributeValue the value for the node
     */
    private void updateAbstractStateNode(LearnerStateNode<String> aNode, LearnerStateNode<String> parent, String attributeName, LearnerStateAttribute attributeValue) {
    	    	
    	// Set the attribute's data. This will be represented in a table row
    	aNode.setShortTermVal(attributeValue.getShortTerm().getDisplayName());
    	aNode.setSTTimestamp(attributeValue.getShortTermTimestamp());
    	
		aNode.setLongTermVal(attributeValue.getLongTerm().getDisplayName());
		aNode.setLTTimestamp(attributeValue.getLongTermTimestamp());
		
		aNode.setPredictedVal(attributeValue.getPredicted().getDisplayName());
		aNode.setPTimestamp(attributeValue.getPredictedTimestamp());
		
		// If this attribute is a LearnerStateAttributeNameEnum, get its icon path from the LearnerStateIconMap.
		String imagePath = LearnerStateIconMap.getEnumerationIconPath(attributeValue.getName());

		aNode.setIcon(getIcon(attributeName, imagePath));
		
		if (attributeValue instanceof LearnerStateAttributeCollection) {
			
			// This attribute has sub-values, represent each one as a child of this attribute node.
			LearnerStateAttributeCollection aCollection = (LearnerStateAttributeCollection)attributeValue;
			
			for (Map.Entry<String, LearnerStateAttribute> subAttribute : aCollection.getAttributes().entrySet()) {
				
                LearnerStateNode<String> aSubNode = null;
                for(LearnerStateNode<String> existingNode : aNode.getChildren()){
                    
                    //Check for an existing node for this attribute under the current node being updated in this method
                    //i.e. checking the children of the the current node being updated
                    if(existingNode.getName().equalsIgnoreCase(subAttribute.getKey())){
                        aSubNode = existingNode;
                        break;
                    }
                }
                
                if(aSubNode == null){
    				// Create the new child node of this current node being updated
                    if(logger.isInfoEnabled()){
                        logger.info("Created new learner state node for '"+subAttribute.getKey()+"' (a child of '"+aNode.getName()+"').");
                    }
    				aSubNode = new LearnerStateNode<String>(subAttribute.getKey(), subAttribute.getKey(), aNode);
    				aNode.addChild(aSubNode);
                }
                
                updateAbstractStateNode(aSubNode, aNode, subAttribute.getKey(), subAttribute.getValue());
			}
		}

    }
    
    /**
     * Obtains the icon for a node that will be shown in the outline.
     * If the icon doesn't exist yet, it is created using the provided image path.
     * 
     * @param nodeName the name of the node that is requesting an icon.
     * @param iconPath the path to the image that should be used for this icon.
     * @return the node's icon
     */
    private Icon getIcon(String nodeName, String imagePath) {
    	    	
    	if (iconMap.containsKey(nodeName)) {
    		
    		// This node's icon has already been created.
    		return iconMap.get(nodeName);
    	}

    	if (imagePath != null) {

    		// Create a new icon for this node.
	    	URL iconURL = getClass().getResource(imagePath);

	    	if (iconURL != null) {

	    		// Create the icon
	    		ImageIcon icon = new ImageIcon(iconURL);
	    		
	    		// Resize icon
	    		Image img = icon.getImage();
	    		Image newImg = img.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH);    		
	        	ImageIcon newIcon = new ImageIcon(newImg); 

	        	// Map this node to the new icon for future reference
	        	iconMap.put(nodeName, newIcon);

	    		return newIcon;
	    	}
    	}
    	
    	// Icon could not be found or created. The outline will use a default icon
    	return iconMap.get(null);
    }
    
    /**
     * Recursively expands or collapses all nodes of an outline.
     * 
     * @param outline the outline whose nodes are being expanded
     * @param parentPath the currently known path of the outline's tree
     * @param parent the node that is being expanded
     * @param expand whether the outline is being expanded or collapsed
     */
    private void expandAll(Outline outline, TreePath parentPath, LearnerStateNode<String> parent, boolean expand) {        
        
        //retrieve the selected row, if any, in order to reset the highlighted row after the tree is expanded
        int selectedRow = outline.getSelectedRow();
        
        expandAll(outline, parentPath, parent, expand, selectedRow);
    }

    /**
     * Recursively expands or collapses all nodes of an outline.
     * 
     * @param outline the outline whose nodes are being expanded
     * @param parentPath the currently known path of the outline's tree
     * @param parent the node that is being expanded
     * @param expand whether the outline is being expanded or collapsed
     * @param selectedRowIndex the current selected row index for this outline.  Can be -1 if a row is not selected.
     */
    private void expandAll(Outline outline, TreePath parentPath, LearnerStateNode<String> parent, boolean expand, int selectedRowIndex) {
        
    	// Traverse children
    	for (LearnerStateNode<String> node : parent.getChildren()) { 
    		
    		TreePath path = parentPath.pathByAddingChild(node);
            expandAll(outline, path, node, expand, selectedRowIndex);
    	}
       
        // Expansion or collapse must be done bottom-up
        if (expand) {
        	outline.expandPath(parentPath);
        }else if(parent.getParent() != null) {
            //if you collapse the root, everything will be removed from the table view
        	outline.collapsePath(parentPath);
        }
        
        if(selectedRowIndex != -1){
            outline.getSelectionModel().setSelectionInterval(selectedRowIndex, selectedRowIndex);
        }
    }  

    private void removeDomainSession(int domainSessionId) {
    	
        if(this.domainSessionId == null || this.domainSessionId == domainSessionId) {
            reset();
        }
        
        dsIdToLearnerState.remove(domainSessionId);
    }
    
    private void reset(){
    	
    	// Clear out the outlines 
    	performanceOutline.setModel(emptyOutlineModel);
    	cognitiveOutline.setModel(emptyOutlineModel);
    	affectiveOutline.setModel(emptyOutlineModel);
    	
    	performanceRootNode.getChildren().clear();
    	cognitiveRootNode.getChildren().clear();
    	affectiveRootNode.getChildren().clear();
    	
        showNotMonitoredLabel();
    }
    
    private void showNotMonitoredLabel(){
        
        warningLabel.setVisible(true);
        warningLabel.setText("<html><font color='red'>You need to monitor an active domain session through the 'Main.Active Sessions' panel to view learner state information for that learner.</font></html>");
    }

    @Override
    public void monitorDomainSession(int domainSessionId) {
        logger.info("Notified that domain session " + domainSessionId + " has been activated");
        setCurrentDomainSession(domainSessionId);
        
        //populate with any cached information
        LearnerState state = dsIdToLearnerState.get(domainSessionId);
        if(state != null){
        	handleLearnerState(state);
        	warningLabel.setVisible(false);
        }else{
            warningLabel.setVisible(true);
            warningLabel.setText("<html><font color='orange'>Waiting for learner state update...</font></html>");
        }
    }

    @Override
    public void ignoreDomainSession(int domainSessionId) {
        logger.info("Notified that domain session " + domainSessionId + " has been deactivated");
    }

    /**
     * Set the current domain session for which learner state should be available for display on the panel.
     * Note: a domain session ID of null will clear the sensor list.
     * 
     * @param domainSessionId unique domain session id associated with the learner state shown on this panel
     */
    public void setCurrentDomainSession(Integer domainSessionId){        
        this.domainSessionId = domainSessionId;
        reset();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        warningLabel = new javax.swing.JLabel();
        performanceLabel = new javax.swing.JLabel();
        performanceScrollPane = new javax.swing.JScrollPane();
        performanceOutline = new org.netbeans.swing.outline.Outline();
        cognitiveLabel = new javax.swing.JLabel();
        affectiveLabel = new javax.swing.JLabel();
        affectiveScrollPane = new javax.swing.JScrollPane();
        affectiveOutline = new org.netbeans.swing.outline.Outline();
        cognitiveScrollPane = new javax.swing.JScrollPane();
        cognitiveOutline = new org.netbeans.swing.outline.Outline();
        performanceExpandAllButton = new javax.swing.JButton();
        performanceCollapseAllButton = new javax.swing.JButton();
        cognitiveExpandAllButton = new javax.swing.JButton();
        cognitiveCollapseAllButton = new javax.swing.JButton();
        affectiveExpandAllButton = new javax.swing.JButton();
        affectiveCollapseAllButton = new javax.swing.JButton();
        performanceAutoExpandCheckbox = new javax.swing.JCheckBox();
        cognitiveAutoExpandCheckbox = new javax.swing.JCheckBox();
        affectiveAutoExpandCheckbox = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(800, 658));
        
        showNotMonitoredLabel();

        performanceLabel.setText("Performance State");
        
        performanceExpandAllButton.setIcon(EXPAND_ALL_ICON);
        performanceExpandAllButton.setToolTipText("Expand All");
        performanceExpandAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                //expand all nodes
                if(!performanceRootNode.getChildren().isEmpty()){
                    expandAll(performanceOutline, new TreePath(performanceRootNode), performanceRootNode, true);
                }
            }
        });
        
        performanceCollapseAllButton.setIcon(COLLAPSE_ALL_ICON);
        performanceCollapseAllButton.setToolTipText("Collapse All");
        performanceCollapseAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                //collapse all nodes
                if(!performanceRootNode.getChildren().isEmpty()){
                    expandAll(performanceOutline, new TreePath(performanceRootNode), performanceRootNode, false);
                }
            }
        });
        
        performanceAutoExpandCheckbox.setText("Auto-Expand");
        performanceAutoExpandCheckbox.setToolTipText("Whether the tree is automatically expanded when an update is received.");
        performanceAutoExpandCheckbox.setSelected(true);

        performanceScrollPane.setPreferredSize(new java.awt.Dimension(0, 0));

        performanceOutline.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        performanceOutline.setPreferredScrollableViewportSize(new java.awt.Dimension(780, 200));
        performanceScrollPane.setViewportView(performanceOutline);

        cognitiveLabel.setText("Cognitive State");
        
        cognitiveExpandAllButton.setIcon(EXPAND_ALL_ICON);
        cognitiveExpandAllButton.setToolTipText("Expand All");
        cognitiveExpandAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                //expand all nodes
                if(!cognitiveRootNode.getChildren().isEmpty()){
                    expandAll(cognitiveOutline, new TreePath(cognitiveRootNode), cognitiveRootNode, true);
                }
            }
        });
        
        cognitiveCollapseAllButton.setIcon(COLLAPSE_ALL_ICON);
        cognitiveCollapseAllButton.setToolTipText("Collapse All");
        cognitiveCollapseAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                //collapse all nodes
                if(!cognitiveRootNode.getChildren().isEmpty()){
                    expandAll(cognitiveOutline, new TreePath(cognitiveRootNode), cognitiveRootNode, false);
                }
            }
        });
        
        cognitiveAutoExpandCheckbox.setText("Auto-Expand");
        cognitiveAutoExpandCheckbox.setToolTipText("Whether the tree is automatically expanded when an update is received.");
        cognitiveAutoExpandCheckbox.setSelected(true);

        affectiveLabel.setText("Affective State");
        
        affectiveExpandAllButton.setIcon(EXPAND_ALL_ICON);
        affectiveExpandAllButton.setToolTipText("Expand All");
        affectiveExpandAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                //expand all nodes
                if(!affectiveRootNode.getChildren().isEmpty()){
                    expandAll(affectiveOutline, new TreePath(affectiveRootNode), affectiveRootNode, true);
                }
            }
        });
        
        affectiveCollapseAllButton.setIcon(COLLAPSE_ALL_ICON);
        affectiveCollapseAllButton.setToolTipText("Collapse All");
        affectiveCollapseAllButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                //collapse all nodes
                if(!affectiveRootNode.getChildren().isEmpty()){
                    expandAll(affectiveOutline, new TreePath(affectiveRootNode), affectiveRootNode, false);
                }
            }
        });
        
        affectiveAutoExpandCheckbox.setText("Auto-Expand");
        affectiveAutoExpandCheckbox.setToolTipText("Whether the tree is automatically expanded when an update is received.");
        affectiveAutoExpandCheckbox.setSelected(true);

        affectiveScrollPane.setPreferredSize(new java.awt.Dimension(0, 0));

        affectiveOutline.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        affectiveOutline.setPreferredScrollableViewportSize(new java.awt.Dimension(780, 200));
        affectiveScrollPane.setViewportView(affectiveOutline);

        cognitiveScrollPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        cognitiveScrollPane.setPreferredSize(new java.awt.Dimension(0, 0));

        cognitiveOutline.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        cognitiveOutline.setPreferredScrollableViewportSize(new java.awt.Dimension(780, 200));
        cognitiveScrollPane.setViewportView(cognitiveOutline);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(warningLabel)
                        .addGroup(layout.createSequentialGroup()
                    .addComponent(performanceLabel)
                    .addGap(10)
                    .addComponent(performanceExpandAllButton, 22, 22, 22)
                    .addGap(5)
                    .addComponent(performanceCollapseAllButton, 22, 22, 22)
                    .addGap(15)
                    .addComponent(performanceAutoExpandCheckbox))

                    .addGroup(layout.createSequentialGroup()
                    .addComponent(cognitiveLabel)
                    .addGap(10)
                    .addComponent(cognitiveExpandAllButton,22, 22, 22)
                    .addGap(5)
                    .addComponent(cognitiveCollapseAllButton, 22, 22, 22)
                    .addGap(15)
                    .addComponent(cognitiveAutoExpandCheckbox))

                    .addGroup(layout.createSequentialGroup()
                    .addComponent(affectiveLabel)
                    .addGap(10)
                    .addComponent(affectiveExpandAllButton, 22, 22, 22)
                    .addGap(5)
                    .addComponent(affectiveCollapseAllButton, 22, 22, 22)
                    .addGap(15)
                    .addComponent(affectiveAutoExpandCheckbox))

                    .addComponent(performanceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cognitiveScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(affectiveScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(warningLabel)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(performanceLabel)
                .addComponent(performanceExpandAllButton)
                .addComponent(performanceCollapseAllButton)
                .addComponent(performanceAutoExpandCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(performanceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(cognitiveLabel)
                .addComponent(cognitiveExpandAllButton)
                .addComponent(cognitiveCollapseAllButton)
                .addComponent(cognitiveAutoExpandCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cognitiveScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(affectiveLabel)
                .addComponent(affectiveExpandAllButton)
                .addComponent(affectiveCollapseAllButton)
                .addComponent(affectiveAutoExpandCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(affectiveScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents


    private javax.swing.JLabel warningLabel;
    private javax.swing.JLabel affectiveLabel;
    private javax.swing.JButton affectiveExpandAllButton;
    private javax.swing.JButton affectiveCollapseAllButton;
    private javax.swing.JCheckBox affectiveAutoExpandCheckbox;
    private org.netbeans.swing.outline.Outline affectiveOutline;
    private javax.swing.JScrollPane affectiveScrollPane;
    private javax.swing.JLabel cognitiveLabel;
    private javax.swing.JButton cognitiveExpandAllButton;
    private javax.swing.JButton cognitiveCollapseAllButton;
    private javax.swing.JCheckBox cognitiveAutoExpandCheckbox;
    private org.netbeans.swing.outline.Outline cognitiveOutline;
    private javax.swing.JScrollPane cognitiveScrollPane;
    private javax.swing.JLabel performanceLabel;
    private javax.swing.JButton performanceExpandAllButton;
    private javax.swing.JButton performanceCollapseAllButton;
    private javax.swing.JCheckBox performanceAutoExpandCheckbox;
    private org.netbeans.swing.outline.Outline performanceOutline;
    private javax.swing.JScrollPane performanceScrollPane;

    @Override
    public void domainSessionActive(DomainSession domainSession) {
        //nothing to do until the domain session is activated for monitoring 
        logger.info("Notified that domain session " + domainSession.getDomainSessionId() + " is active");
    }

    @Override
    public void domainSessionInactive(DomainSession domainSession) {
        //remove the domain sessions's from monitoring
        logger.info("Notified that domain session " + domainSession.getDomainSessionId() + " has been removed");
        removeDomainSession(Integer.valueOf(domainSession.getDomainSessionId()));
    }
}
