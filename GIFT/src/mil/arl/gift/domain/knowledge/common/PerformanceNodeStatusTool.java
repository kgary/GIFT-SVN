/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.io.LearnerStateIconMap;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Scenario;
import mil.arl.gift.domain.knowledge.Task;

/**
 * This class creates a window that display the status of the task and concepts from a
 * scenario object.  The tool shows the latest values of:
 * 1) status - {not started, running, finished}
 * 2) assessment - {Above, At, Below, Unknown}
 * 
 * 
 * @author mhoffman
 */
public class PerformanceNodeStatusTool extends javax.swing.JFrame {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(PerformanceNodeStatusTool.class);
        
    /** the data model for the table */
    TreeModel treeModel = null;
    OutlineModel outlineModel = null;
    DataRenderer dataRenderer = new DataRenderer();
    CellRenderer cellRenderer = new CellRenderer();
    PerformanceNodeRowModel rowModel = new PerformanceNodeRowModel();

    /** the root of the tree */
    PerformanceNode<String> performanceRootNode = new PerformanceNode<String>("", null);
    
    /** used to obtain the current node assessment */
    AssessmentProxyManager proxyMgr = AssessmentProxyManager.getInstance();

    /** used to store images for specific outline nodes */
    private HashMap<String, Icon> iconMap = new HashMap<String, Icon>();
    
    /** used to store paths to nodes so their properties can be updated when necessary. */
    private HashMap<Integer, NodeEntry> nodeMap = new HashMap<Integer, NodeEntry>();
    
    private static final String ACTIVE = "running";
    private static final String FINISHED = "finished";
    private static final String NOT_STARTED = "not started";
    
    /**
     * Creates new form PerformanceNodeStatusTool
     * Note: the caller must still call setVisible(true) in order to show the tool's
     * window when deemed appropriate.
     * 
     * @param scenario contains information about the tasks and concepts to represent in the table.
     * @param currentAssessment the current assessment of the scenario.  Can be null.
     */
    public PerformanceNodeStatusTool(Scenario scenario, ProxyPerformanceAssessment currentAssessment) {
                
        if(scenario == null){
            throw new IllegalArgumentException("The scenario can't be null");
        }
        
        initComponents();
        
        this.setTitle("Performance Node Status Tool - " + scenario.getName());
        this.setIconImage(ImageUtil.getInstance().getSystemIcon());  
        
        /* Prepare the outline: */
        performanceOutline.setDefaultRenderer(Object.class, cellRenderer);
        performanceOutline.setRenderDataProvider(dataRenderer);        
        performanceOutline.setRootVisible(false);
        performanceOutline.setRowSorter(null);
        performanceOutline.getTableHeader().setReorderingAllowed(false);  
//        performanceOutline.setFont(new Font("Times New Roman", Font.TRUETYPE_FONT, 12));

        //populate table
        updateTable(scenario, currentAssessment);
    }

    /**
     * Update the table entries with the latest information provided by the scenario.
     * 
     * @param scenario contains references to the tasks and concepts
     * @param currentAssessment the current scenario performance assessment proxy information to use to retrieve the current
     * assessment values for each performance node.
     */
    public synchronized void updateTable(Scenario scenario, ProxyPerformanceAssessment currentAssessment){
        
        if(scenario == null){
            throw new IllegalArgumentException("The scenario can't be null");
        }else if(currentAssessment == null){
            return;
        }
                
        String status;
        NodeEntry entry;
        AssessmentProxy proxy;
        AssessmentLevelEnum assessmentLevel;
        double confidence, competence, trend;
        
        /** only for tasks */
        Double difficulty = null, stress = null;
        int priority;
        boolean assessmentHold, confidenceHold, competenceHold, trendHold, priorityHold;        
                            
        if(treeModel == null) {
            // if the table wasn't initialized, make the table

            createTable(scenario, currentAssessment);
            return;
        }
        
        for(AbstractPerformanceAssessmentNode node : scenario.getPerformanceNodes().values()){
            // Check for changes to any nodes. If a change was made, update the tree.
            
            // Get the assessment
            AbstractAssessment assessment = null;
            proxy = proxyMgr.getAssessmentProxy(node);
            
            if(node instanceof Concept) {
                assessment = proxy.get(((Concept)node).getAssessment().getCourseNodeId());
            } else if(node instanceof Task) {
                assessment = proxy.get(((Task)node).getAssessment().getCourseNodeId());
            }else{
                continue;
            }
            
            assessmentLevel = (assessment == null) ? 
                    AssessmentLevelEnum.UNKNOWN : assessment.getAssessmentLevel();
            
            confidence = (assessment == null) ?
                    1.0 : assessment.getConfidence();
            
            competence = (assessment == null) ?
                    1.0 : assessment.getCompetence();
            
            trend = (assessment == null) ?
                    1.0 : assessment.getTrend();
            
            priority = (assessment == null || assessment.getPriority() == null) ?
                    0 : assessment.getPriority();
            
            if(assessment instanceof ProxyTaskAssessment) {
                ProxyTaskAssessment taskAssessment = (ProxyTaskAssessment)assessment;
                difficulty = taskAssessment.getDifficulty();
                stress = taskAssessment.getStress();                
            }else {
                difficulty = null;
                stress = null;
            }
            
            assessmentHold = assessment != null ? assessment.isAssessmentHold() : false;
            confidenceHold = assessment != null ? assessment.isConfidenceHold() : false;
            competenceHold = assessment != null ? assessment.isCompetenceHold() : false;
            trendHold = assessment != null ? assessment.isTrendHold() : false;
            priorityHold = assessment != null ? assessment.isPriorityHold() : false;
            
            // Get the status
            if(node.isActive()) {
                status = ACTIVE;
            } else if (node.isFinished()) {
                status = FINISHED;              
            } else { 
                status = NOT_STARTED;
            }
                            
            entry = nodeMap.get(node.getNodeId());
                            
            if( (assessmentLevel != entry.getAssessment()) || 
                    !(status.equals(entry.getStatus())) ||
                    confidence != entry.getConfidence() ||
                    competence != entry.getCompetence() ||
                    trend != entry.getTrend() ||
                    priority != entry.getPriority() ||
                    assessmentHold != entry.isAssessmentHold() ||
                    confidenceHold != entry.isConfidenceHold() ||
                    competenceHold != entry.isCompetenceHold() ||
                    trendHold != entry.isTrendHold() ||
                    priorityHold != entry.isPriorityHold() ||
                    difficulty != entry.getDifficulty() ||
                    stress != entry.getStress()){
                // If the a value changes, update the tree 
                
                entry.setAssessment(assessmentLevel);
                entry.setConfidence(confidence);
                entry.setCompetence(competence);
                entry.setTrend(trend);
                entry.setPriority(priority);
                entry.setStatus(status); 
                entry.setAssessmentHold(assessmentHold);
                entry.setConfidenceHold(confidenceHold);
                entry.setCompetenceHold(competenceHold);
                entry.setTrendHold(trendHold);
                entry.setPriorityHold(priorityHold);
                
                /** only for tasks, can be null */
                entry.setDifficulty(difficulty);
                entry.setStress(stress);
                
                treeModel.valueForPathChanged(entry.getPath(), entry);
            }   
        }
        
        // Update the outline
        performanceOutline.repaint();                   
    }
    
    @SuppressWarnings("unchecked")
    public void createTable(Scenario scenario, ProxyPerformanceAssessment currentAssessment){
        
        if(scenario == null){
            throw new IllegalArgumentException("The scenario can't be null");
        }

        AssessmentProxy proxy;
        AssessmentLevelEnum assessmentLevel;
        double confidence, competence, trend;
        Double difficulty = null, stress = null;
        int priority;
        PerformanceNode<String> taskNode;

        if(currentAssessment != null) {      

            for(Task task : scenario.getTasks()){

                // Get the assessment
                AbstractAssessment assessment;
                proxy = proxyMgr.getAssessmentProxy(task);
                assessment = proxy.get(task.getAssessment().getCourseNodeId());
                assessmentLevel = (assessment == null) ? 
                        AssessmentLevelEnum.UNKNOWN : assessment.getAssessmentLevel();
                
                confidence = (assessment == null) ?
                        1.0 : assessment.getConfidence();
                
                competence = (assessment == null) ?
                        1.0 : assessment.getCompetence();
                
                trend = (assessment == null) ?
                        1.0 : assessment.getTrend();
                
                priority = (assessment == null || assessment.getPriority() == null) ?
                        0 : assessment.getPriority();
                
                difficulty = (assessment == null || ((ProxyTaskAssessment)assessment).getDifficulty() == null) ? 
                        null : ((ProxyTaskAssessment)assessment).getDifficulty();
                stress = (assessment == null || ((ProxyTaskAssessment)assessment).getStress() == null) ?
                        null : ((ProxyTaskAssessment)assessment).getStress();

                // Create a new node
                taskNode = new PerformanceNode<String>(task.getName(), performanceRootNode);

                // Set the node's data to be represented in the table
                taskNode.setId(task.getNodeId()); 
                taskNode.setStatus(task.isActive(), task.isFinished());
                taskNode.setTimestamp(TimeUtil.timeFirstFormat.format(new Date())); 
                taskNode.setAssessmentLevel(assessmentLevel);   
                taskNode.setConfidence(confidence);
                taskNode.setCompetence(competence);
                taskNode.setTrend(trend);
                taskNode.setPriority(priority);
                taskNode.setAssessmentHold(assessment != null ? assessment.isAssessmentHold() : false);
                taskNode.setConfidenceHold(assessment != null ? assessment.isConfidenceHold() : false);
                taskNode.setCompetenceHold(assessment != null ? assessment.isCompetenceHold() : false);
                taskNode.setTrendHold(assessment != null ? assessment.isTrendHold() : false);
                taskNode.setPriorityHold(assessment != null ? assessment.isPriorityHold() : false);
                taskNode.setIcon(getIcon(taskNode.getName(), LearnerStateIconMap.getTaskIconPath())); 
                
                taskNode.setDifficulty(difficulty);
                taskNode.setStress(stress);

                performanceRootNode.addChild(taskNode);

                for(AbstractPerformanceAssessmentNode concept : task.getConcepts()) {
                    // Get children 
                    
                    taskNode.addChild(createConceptNode(concept, taskNode));
                }       
            }

            // create the table
            treeModel = new PerformanceNodeTreeModel(performanceRootNode);                  
            outlineModel = DefaultOutlineModel.createOutlineModel(treeModel, rowModel, true, "Name");
            performanceOutline.setModel(outlineModel);

            expandAll(performanceOutline, new TreePath(treeModel.getRoot()), (PerformanceNode<String>)treeModel.getRoot(), true);

        } else {
            logger.error("currentAssessment is null");
        }
    }
    
    /**
     * Builds a node that represents a performance assessment concept
     * 
     * @param concept - the concept node to add to the parent
     * @param parent - the parent of the concept node being created
     * @return PerformanceNode - the created concept node
     */
    private PerformanceNode<String> createConceptNode(AbstractPerformanceAssessmentNode concept, PerformanceNode<String> parent) {
        
        // Create the new concept node as a child of the given parent
        PerformanceNode<String> conceptNode = new PerformanceNode<String>(concept.getName(), parent);
        
        // Get the assessment
        AssessmentProxy proxy = proxyMgr.getAssessmentProxy(concept);
        AbstractAssessment assessment = proxy.get(((Concept)concept).getAssessment().getCourseNodeId());
        AssessmentLevelEnum assessmentLevel = (assessment == null) ? 
                AssessmentLevelEnum.UNKNOWN : assessment.getAssessmentLevel();
        
        double confidence, competence, trend;
        int priority;
        
        confidence = (assessment == null) ?
                1.0 : assessment.getConfidence();
        
        competence = (assessment == null) ?
                1.0 : assessment.getCompetence();
        
        trend = (assessment == null) ?
                1.0 : assessment.getTrend();
        
        priority = (assessment == null || assessment.getPriority() == null) ?
                0 : assessment.getPriority();
        
        // Fill in data
        conceptNode.setId(concept.getNodeId());
        conceptNode.setAssessmentLevel(assessmentLevel);
        conceptNode.setConfidence(confidence);
        conceptNode.setCompetence(competence);
        conceptNode.setTrend(trend);
        conceptNode.setPriority(priority);
        conceptNode.setStatus(concept.isActive(), concept.isFinished());
        conceptNode.setTimestamp(TimeUtil.timeFirstFormat.format(new Date()));
        conceptNode.setAssessmentHold(assessment != null ? assessment.isAssessmentHold() : false);
        conceptNode.setConfidenceHold(assessment != null ? assessment.isConfidenceHold() : false);
        conceptNode.setCompetenceHold(assessment != null ? assessment.isCompetenceHold() : false);
        conceptNode.setTrendHold(assessment != null ? assessment.isTrendHold() : false);
        conceptNode.setPriorityHold(assessment != null ? assessment.isPriorityHold() : false);
        conceptNode.setIcon(getIcon(conceptNode.getName(), LearnerStateIconMap.getConceptIconPath()));
        
        if(concept instanceof IntermediateConcept) {
            // Check if this node has children
            for(Concept subConcept : ((IntermediateConcept) concept).getConcepts()){
                // Add children to this node
                PerformanceNode<String> subConceptNode = createConceptNode(subConcept, conceptNode);
                conceptNode.addChild(subConceptNode);
            }
        }
        
        return conceptNode;     
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        performanceOutline = new org.netbeans.swing.outline.Outline();        
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        
        performanceOutline.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        performanceOutline.setPreferredScrollableViewportSize(new java.awt.Dimension(780, 200));
        jScrollPane1.setViewportView(performanceOutline);
        
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private org.netbeans.swing.outline.Outline performanceOutline;
    // End of variables declaration//GEN-END:variables        
    
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
                Image newImg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);           
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
    private void expandAll(Outline outline, TreePath parentPath, PerformanceNode<String> parent, boolean expand) {
                        
            for (PerformanceNode<String> node : parent.getChildren()) { 
                // Traverse children to expand while adding to the nodeMap
                
                TreePath path = parentPath.pathByAddingChild(node);             
                NodeEntry entry = new NodeEntry(new TreePath(node), node.getAssessmentLevel(), node.getStatus());
                entry.setCompetence(node.getCompetence());
                entry.setConfidence(node.getConfidence());
                entry.setTrend(node.getTrend());
                entry.setPriority(node.getPriority());
                entry.setAssessmentHold(node.isAssessmentHold());
                entry.setCompetenceHold(node.isCompetenceHold());
                entry.setConfidenceHold(node.isConfidenceHold());
                entry.setTrendHold(node.isTrendHold());
                entry.setPriorityHold(node.isPriorityHold());
                entry.setDifficulty(node.getDifficulty());
                entry.setStress(node.getStress());
                nodeMap.put(node.getId(), entry);
                
                expandAll(outline, path, node, expand);
            }
           
            // Expansion or collapse must be done bottom-up
            if (expand) {
                outline.expandPath(parentPath);
            }else {
                outline.collapsePath(parentPath);
            }
        }  
    
    
    /** 
     * Serves as a node within the tree that represents a learner's current performance.
     * @param <T> the bounded type parameter that should restrict this node type to a String.
     */
    public class PerformanceNode<T> {
        
        private int id;          
        private String name;
        private String status;
        private String timestamp; 
        private Icon icon = null;
        private AssessmentLevelEnum assessmentLevel;
        private double confidence;
        private double competence;
        private double trend;
        private int priority;
        private PerformanceNode<T> parentNode;
        private List<PerformanceNode<String>> childNodes;
        
        /** only for tasks */
        private Double difficulty;
        
        /** only for tasks */
        private Double stress;
        
        private boolean assessmentHold;
        private boolean confidenceHold;
        private boolean competenceHold;
        private boolean trendHold;
        private boolean priorityHold;  
        
        /**
         * Constructor for this class
         * 
         * @param name The name of the node that will be displayed in the tree
         * @param parent The parent node of this node
         */
        public PerformanceNode(String name, PerformanceNode<T> parent) {
            
            this.name = name;
            this.parentNode = parent;
            this.childNodes = new ArrayList<PerformanceNode<String>>();
        }
        
        public void addChild(PerformanceNode<String> child) {
            this.childNodes.add(child);
        }
        
        public void removeChild(PerformanceNode<String> child) {
            this.childNodes.remove(child);
        }
        
        public String getName() {
            return name;
        }
        
        public Icon getIcon() {
            return icon;
        }
        
        public int getId() {
            return id;
        }
        
        public PerformanceNode<T> getParent() {
            return parentNode;
        }
        
        public List<PerformanceNode<String>> getChildren() {
            return childNodes;
        }
        
        public AssessmentLevelEnum getAssessmentLevel() {
            return assessmentLevel;
        }
        
        public double getConfidence(){
            return confidence;
        }
        
        public double getCompetence(){
            return competence;
        }
        
        public double getTrend(){
            return trend;
        }
        
        public int getPriority(){
            return priority;
        }
               
        public String getStatus() {
            return status;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
       
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        public void setIcon(Icon icon) {    
            this.icon = icon;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public void setAssessmentLevel(AssessmentLevelEnum assessmentLevel) {
            this.assessmentLevel = assessmentLevel;
        }
        
        public void setConfidence(double confidence){
            this.confidence = confidence;
        }
        
        public void setCompetence(double competence){
            this.competence = competence;
        }
        
        public void setTrend(double trend){
            this.trend = trend;
        }
        
        public void setPriority(int priority){
            this.priority = priority;
        }
        
        public void setStatus(boolean isActive, boolean isFinished) {
            
            if(isActive) {
                status = ACTIVE;
            } else if (isFinished) {
                status = FINISHED;              
            } else { 
                status = NOT_STARTED;
            }
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public boolean isAssessmentHold() {
            return assessmentHold;
        }

        public void setAssessmentHold(boolean assessmentHold) {
            this.assessmentHold = assessmentHold;
        }

        public boolean isConfidenceHold() {
            return confidenceHold;
        }

        public void setConfidenceHold(boolean confidenceHold) {
            this.confidenceHold = confidenceHold;
        }

        public boolean isCompetenceHold() {
            return competenceHold;
        }

        public void setCompetenceHold(boolean competenceHold) {
            this.competenceHold = competenceHold;
        }

        public boolean isTrendHold() {
            return trendHold;
        }

        public void setTrendHold(boolean trendHold) {
            this.trendHold = trendHold;
        }

        public boolean isPriorityHold() {
            return priorityHold;
        }

        public void setPriorityHold(boolean priorityHold) {
            this.priorityHold = priorityHold;
        }
        
        public Double getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(Double difficulty) {
            this.difficulty = difficulty;
        }

        public Double getStress() {
            return stress;
        }

        public void setStress(Double stress) {
            this.stress = stress;
        }

        @Override
        public String toString() {
            
            StringBuffer sb = new StringBuffer();
            
            sb.append("[PerformanceNode: ");
            sb.append("id = ").append(id);
            sb.append(", (name = ").append(name);
            sb.append(", status = ").append(status);
            sb.append(", assessment = ").append(assessmentLevel);
            sb.append(", confidence = ").append(confidence);
            sb.append(", competence = ").append(competence);
            sb.append(", trend = ").append(trend);
            sb.append(", priority = ").append(priority);
            sb.append(", difficulty = ").append(difficulty);
            sb.append(", stress = ").append(stress);
            sb.append(", timestamp = ").append(timestamp);
            sb.append(", assessmentHold = ");
            sb.append(assessmentHold);
            sb.append(", confidenceHold = ");
            sb.append(confidenceHold);
            sb.append(", competenceHold = ");
            sb.append(competenceHold);
            sb.append(", trendHold = ");
            sb.append(trendHold);
            sb.append(", priorityHold = ");
            sb.append(priorityHold);
            sb.append(")]");
            return sb.toString();       
        }
    }
    
    
    /**
     * This class is used to render a Performance Node Outline with custom style
     */
    public class DataRenderer implements RenderDataProvider {
        
        @Override
        public Color getBackground(Object node) {
            
            return null;
        }

        @Override
        public String getDisplayName(Object node) {
            
            @SuppressWarnings("unchecked")
            PerformanceNode<String> n = (PerformanceNode<String>)node;
            
            return n.getName();
        }

        @Override
        public Color getForeground(Object node) {

            return null;
        }

        @Override
        public Icon getIcon(Object node) {
            
            @SuppressWarnings("unchecked")
            PerformanceNode<String> n = (PerformanceNode<String>)node;
            
            return n.getIcon();
        }

        @Override
        public String getTooltipText(Object node) {
            
            return null;
        }

        @Override
        public boolean isHtmlDisplayName(Object arg0) {

            return false;
        }
    }
    

    /**
     * Used to render a performance node outline with custom style
     */
    public class PerformanceNodeRowModel implements RowModel {
        
        private static final int ID_COL_INDEX = 0,
                                 STATUS_COL_INDEX = 1,
                                 ASSESSMENT_COL_INDEX = 2,
                                 DIFFICULTY_COL_INDEX = 3,
                                 STRESS_COL_INDEX = 4,
                                 CONFIDENCE_COL_INDEX = 5,
                                 COMPETENCE_COL_INDEX = 6,
                                 TREND_COL_INDEX = 7,
                                 PRIORITY_COL_INDEX = 8,
                                 TIMESTAMP_COL_INDEX = 9;
        
        /** Column indices of id, status, assessment, and timestamp. */
        private final String[] columns = new String[]{"Id", "Status", "Assessment", "Difficulty", "Stress", "Confidence", "Competence", "Trend", "Priority", "Time"};
        
        @Override
        public int getColumnCount() {
            return columns.length;
        }

        /**
         * Displays data in the appropriate column.
         */
        @Override
        public Object getValueFor(Object node, int column) {
            @SuppressWarnings("unchecked")
            PerformanceNode<String> n = (PerformanceNode<String>)node;
            
            switch (column) {
            
            case ID_COL_INDEX:
                return n.getId(); // id column
            case STATUS_COL_INDEX:
                return n.getStatus(); // status column
            case ASSESSMENT_COL_INDEX:
                return n.getAssessmentLevel().getDisplayName() + (n.isAssessmentHold() ? " (HOLD)" : Constants.EMPTY); // assessment column
            case DIFFICULTY_COL_INDEX:
                return n.getDifficulty() != null ? String.format("%.1f",n.getDifficulty()) : "";  // difficulty column
            case STRESS_COL_INDEX:
                return n.getStress() != null ? String.format("%.1f", n.getStress()) : ""; // stress column
            case CONFIDENCE_COL_INDEX:
                return String.format("%.2f",n.getConfidence()) + (n.isConfidenceHold() ? " (HOLD)" : Constants.EMPTY);  //confidence column
            case COMPETENCE_COL_INDEX:
                return String.format("%.2f",n.getCompetence()) + (n.isCompetenceHold() ? " (HOLD)" : Constants.EMPTY); //competence column
            case TREND_COL_INDEX:
                return String.format("%.2f",n.getTrend()) + (n.isTrendHold() ? " (HOLD)" : Constants.EMPTY);  //trend column
            case PRIORITY_COL_INDEX:
                return n.getPriority() + (n.isPriorityHold() ? " (HOLD)" : Constants.EMPTY);  //priority column
            case TIMESTAMP_COL_INDEX:
                return n.getTimestamp(); // timestamp column
            default:
                assert false;
            }
            
            return null;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(Object o, int i) {
            return false;
        }

        @Override
        public void setValueFor(Object o, int i, Object o1) {
            // do nothing for now
        }

        @Override
        public String getColumnName(int i) {
            return columns[i];
        }
    }
    
    
    /**
     * Displays data for a performance node outline in the form of a tree.
     */
    public class PerformanceNodeTreeModel implements TreeModel {
        
     private PerformanceNode<String> root;

       public PerformanceNodeTreeModel(PerformanceNode<String> root) {
           this.root = root;
       }
       
       @Override
       public Object getRoot() {
           return root;
       }

       @Override
       public Object getChild(Object parent, int index) {
           @SuppressWarnings("unchecked")
            PerformanceNode<String> n = (PerformanceNode<String>)parent;
           
           if(index >= 0 && index < n.getChildren().size())
               return n.getChildren().get(index);
          
           return null;
       }

       @Override
       public int getChildCount(Object parent) {
           @SuppressWarnings("unchecked")
            PerformanceNode<String> n = (PerformanceNode<String>)parent;
           
           return n.getChildren().size();
       }

       @Override
       public boolean isLeaf(Object node) {
        @SuppressWarnings("unchecked")
            PerformanceNode<String> n = (PerformanceNode<String>)node;
        
        return n.getChildren().isEmpty();
       }

       /* This should only be used to update status or assessment */
       @Override
       public void valueForPathChanged(TreePath path, Object newValue) {
           
           @SuppressWarnings("unchecked")
           PerformanceNode<String> node = (PerformanceNode<String>) path.getLastPathComponent();
           NodeEntry entry = (NodeEntry) newValue;
           
           node.setTimestamp(TimeUtil.timeFirstFormat.format(new Date()));  
           node.setAssessmentLevel(entry.getAssessment());
           node.setConfidence(entry.getConfidence());
           node.setCompetence(entry.getCompetence());
           node.setTrend(entry.getTrend());
           node.setPriority(entry.getPriority());
           node.setStatus(entry.getStatus());
           node.setAssessmentHold(entry.isAssessmentHold());
           node.setConfidenceHold(entry.isConfidenceHold());
           node.setCompetenceHold(entry.isCompetenceHold());
           node.setTrendHold(entry.isTrendHold());
           node.setPriorityHold(entry.isPriorityHold());
           node.setDifficulty(entry.getDifficulty());
           node.setStress(entry.getStress());
       }

       @Override
       public int getIndexOfChild(Object parent, Object child) {

           @SuppressWarnings("unchecked")
           PerformanceNode<String> p = (PerformanceNode<String>)parent;
           @SuppressWarnings("unchecked")
           PerformanceNode<String> c = (PerformanceNode<String>)child;

           for (int i=0; i<p.getChildren().size(); i++) {
               if (p.getChildren().get(i) == c) {
                   return i;
               }
           }

           return -1;
       }

       @Override
       public void addTreeModelListener(TreeModelListener l) {
           // do nothing
       }

       @Override
       public void removeTreeModelListener(TreeModelListener l) {
           // do nothing
       }       
    }
    
    /**
     * Used to customize cells in the a performance node outline
     */
    public class CellRenderer extends DefaultOutlineCellRenderer {
        
        /** serial version uid */
        private static final long serialVersionUID = 1L;
        
        // The indices of each table column
        @SuppressWarnings("unused")
        private static final int ID_COL_INDEX = 1,
                                 STATUS_COL_INDEX = 2,
                                 ASSESSMENT_COL_INDEX = 3,
                                 DIFFICULTY_COL_INDEX = 4,
                                 STRESS_COL_INDEX = 5,
                                 CONFIDENCE_COL_INDEX = 6,
                                 COMPETENCE_COL_INDEX = 7,
                                 TREND_COL_INDEX = 8,
                                 PRIORITY_COL_INDEX = 9,
                                 TIMESTAMP_COL_INDEX = 10;
        
        // The column colors that are used
        private final Color LIGHT_COL_COLOR = Color.WHITE;
        private final Color MEDIUM_COL_COLOR = new Color(0xE9EDF4);
        private final Color DARK_COL_COLOR = new Color(0xCED8E8);
        
        @Override
        public Component getTableCellRendererComponent(final JTable table, 
                                                       final Object value, 
                                                       final boolean isSelected, 
                                                       final boolean hasFocus, 
                                                       final int rowIndex, 
                                                       final int columnIndex) {
            
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, columnIndex);

            // Set an appropriate background color for this cell.       
            if (columnIndex == ID_COL_INDEX || columnIndex == ASSESSMENT_COL_INDEX) {
                // The id column and assessment column
                cell.setBackground(MEDIUM_COL_COLOR);
                
            }else if (columnIndex == STATUS_COL_INDEX || columnIndex == TIMESTAMP_COL_INDEX) {
                // The status column and timestamp column
                cell.setBackground(DARK_COL_COLOR);         
            }else {
                cell.setBackground(LIGHT_COL_COLOR);
            }

            return cell;
        }
    }
    
    /** 
     * Used to keep track of changing values in the tree. 
     */
    public class NodeEntry {
        
        private TreePath path;
        private AssessmentLevelEnum assessment;
        private double confidence;
        private double competence;
        private double trend;
        private int priority;
        
        /** only for tasks */
        private Double difficulty;
        
        /** only for tasks */
        private Double stress;
        
        private String status;
        
        private boolean assessmentHold;
        private boolean confidenceHold;
        private boolean competenceHold;
        private boolean trendHold;
        private boolean priorityHold;        
        
        /**
         * Constructor for this class.
         * 
         * @param path - the path to this node.
         * @param assessment - the node's current assessment
         * @param status - the node's current status
         */
        public NodeEntry(TreePath path, AssessmentLevelEnum assessment, String status) {
            
            this.path = path;
            this.assessment = assessment;
            this.status = status;
        }
        
        public TreePath getPath() {
            return path;
        }
        
        public void setPath(TreePath path) {
            this.path = path;
        }
        
        public AssessmentLevelEnum getAssessment() {
            return assessment;
        }
        
        public void setAssessment(AssessmentLevelEnum assessment) {
            this.assessment = assessment;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public double getCompetence() {
            return competence;
        }

        public void setCompetence(double competence) {
            this.competence = competence;
        }

        public double getTrend() {
            return trend;
        }

        public void setTrend(double trend) {
            this.trend = trend;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public boolean isAssessmentHold() {
            return assessmentHold;
        }

        public void setAssessmentHold(boolean assessmentHold) {
            this.assessmentHold = assessmentHold;
        }

        public boolean isConfidenceHold() {
            return confidenceHold;
        }

        public void setConfidenceHold(boolean confidenceHold) {
            this.confidenceHold = confidenceHold;
        }

        public boolean isCompetenceHold() {
            return competenceHold;
        }

        public void setCompetenceHold(boolean competenceHold) {
            this.competenceHold = competenceHold;
        }

        public boolean isTrendHold() {
            return trendHold;
        }

        public void setTrendHold(boolean trendHold) {
            this.trendHold = trendHold;
        }

        public boolean isPriorityHold() {
            return priorityHold;
        }

        public void setPriorityHold(boolean priorityHold) {
            this.priorityHold = priorityHold;
        }      

        public Double getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(Double difficulty) {
            this.difficulty = difficulty;
        }

        public Double getStress() {
            return stress;
        }

        public void setStress(Double stress) {
            this.stress = stress;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[NodeEntry: path=");
            builder.append(path);
            builder.append(", assessment=");
            builder.append(assessment);
            builder.append(", confidence=");
            builder.append(confidence);
            builder.append(", competence=");
            builder.append(competence);
            builder.append(", trend=");
            builder.append(trend);
            builder.append(", priority=");
            builder.append(priority);
            builder.append(", difficulty=");
            builder.append(difficulty);
            builder.append(", stress=");
            builder.append(stress);
            builder.append(", status=");
            builder.append(status);
            builder.append(", assessmentHold=");
            builder.append(assessmentHold);
            builder.append(", confidenceHold=");
            builder.append(confidenceHold);
            builder.append(", competenceHold=");
            builder.append(competenceHold);
            builder.append(", trendHold=");
            builder.append(trendHold);
            builder.append(", priorityHold=");
            builder.append(priorityHold);
            builder.append("]");
            return builder.toString();
        }
    }
}
