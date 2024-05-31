/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fg.ftreenodes.Params;

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This is the XML Editor custom node dialog for the Performance Node id reference element in the DKF schema.
 * The dialog allows the user to specify which Performance node Id from the Task/Concept hierarchy to use in a DKF file being developed using the DAT.
 * 
 * @author mhoffman
 *
 */
public class PerformanceNodeDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(PerformanceNodeDialog.class);

    /** title of the dialog */
    private static final String TITLE = "Performance Node Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing Performance Node entry from the Task/Concept hirerachy.\n"+
                "Note: if there are no node present, either you have not assigned a node id to the existing tasks and/or \nconcepts yet or you have not created any.";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    protected static final String TASKS = "tasks";
    protected static final String TASK = "task";
    protected static final String CONCEPT = "concept";
    protected static final String NODE_ID = "nodeId";
    protected static final String NAME = "name";
    
    /** parameter to not include tasks in the list of performance nodes */
    private static final String NO_TASKS_PARAM = "noTasks";
    private boolean noTasks = false;
    
    private static final String NO_NAME = "(No-Name-Provided)";
    private static final String NO_ID   = "(No-Node-Id)";
    
    //the following block checks to make sure the class structure of Tasks->Task, Tasks and Concept from the xsd hasn't changed
    //as this class uses that structure to to find node elements in the tree.
    //If changes are made to this structure or the names of class fields, then the code that walks/searches nodes in this class
    //will need to be changed accordingly.
    static{         
        generated.dkf.Tasks tasks = new generated.dkf.Tasks();
        tasks.getTask();
        generated.dkf.Task task = new generated.dkf.Task();
        task.getConcepts();
        task.getName();
        task.getNodeId();
        generated.dkf.Concept concept = new generated.dkf.Concept();
        concept.getName();
        concept.getNodeId();
    }
    
    /**
     * Class constructor - create dialog
     */
    public PerformanceNodeDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object getData(){
        
        if(selectedValue != null && selectedValue instanceof PerformanceNodeItem){
            int perfNodeId = ((PerformanceNodeItem)selectedValue).getId();
            return perfNodeId;
        }
        
        return selectedValue;
    }
    
    @Override
    public Object[] getCustomValues() {
        
        List<PerformanceNodeItem> items = new ArrayList<>();        
            
        try{
            //
            // gather all performance node Ids from Task/Concept hierarchy
            //
            Element rootNode = DAT.getInstance().getDATForm().getRootNode();
            
            NodeList nl = rootNode.getElementsByTagName(TASKS);
            if(nl != null && nl.getLength() == 1){
                //found tasks element, the root of the Task/Concept hierarchy
                
                Element tasks = (Element)nl.item(0);
                
                nl = tasks.getElementsByTagName(TASK);
                if(nl != null){
                    
                    //get node info for each task and it's concepts
                    int nodeId;
                    for(int taskIndex = 0; taskIndex < nl.getLength(); taskIndex++){
                        
                        Element task = (Element)nl.item(taskIndex);
                        String taskName = task.getAttribute(NAME);
                        String nodeIdStr = task.getAttribute(NODE_ID);
                        
                        if(nodeIdStr == null || nodeIdStr.length() == 0){
                            logger.warn("While searching for performance node ids, skipping a task in the task hierarchy w/ name = "+taskName+" because it's node id is not set yet");
                            
                            nodeId = -1;
                            
                        }else{
                            
                            nodeId = Integer.valueOf(nodeIdStr);                           
                        }
                        
                        if(!noTasks){
                            items.add(new PerformanceNodeItem(nodeId, taskName, TASK));
                        }

                        
                        NodeList conceptsNL = task.getElementsByTagName(CONCEPT);
                        if(conceptsNL != null){
                            
                            //get node info for each task's concept
                            for(int conceptIndex = 0; conceptIndex < conceptsNL.getLength(); conceptIndex++){
                                
                                Element concept = (Element)conceptsNL.item(conceptIndex);
                                String conceptName = concept.getAttribute(NAME);
                                nodeIdStr = concept.getAttribute(NODE_ID);
                                
                                if(nodeIdStr == null || nodeIdStr.length() == 0){
                                    logger.warn("While searching for performance node ids, skipping a concept in the hierarchy w/ name = "+conceptName+" (child of task named = "+taskName+") because it's node id is not set yet");
                                    nodeId = -1;
                                
                                }else{
                                
                                    nodeId = Integer.valueOf(nodeIdStr);
                                }
                                
                                items.add(new PerformanceNodeItem(nodeId, conceptName, CONCEPT));
                                
                            }
                        }
                    }//end for
                }
                
            }else{
                
                JOptionPane.showMessageDialog(this,
                        "Please provide some performance nodes by first creating a task node.  \nUntil at least one performance node exists this dialog will have no choices to select from.",
                        "Performance Node Needed",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            
            
        
        }catch(Throwable e){
            logger.error("Caught exception while gathering performance node ids", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather performance node ids, check the DAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        return items.toArray();
    }
    
    @Override
    protected void useParameters(Params params){
        
        if(params.getMap() != null){
            
            try{
                //determine if the list of performance nodes should exclude tasks
                noTasks = Boolean.valueOf((String)params.getMap().get(NO_TASKS_PARAM));
            }catch(@SuppressWarnings("unused") Exception e){
                //dont care
                noTasks = false;
            }
            
            return;
            
        }
        
        //reset to default
        noTasks = false;
    }

    @Override
    public void addUserEntry(String value) {
        //no custom entries allowed for user history for this dialog implementation
    }
    
    /**
     * Wrapper class for Performance Node information.  This class is responsible for 
     * handling the toString implementation used to display a string representing an instance of this class on the dialog.
     * 
     * @author mhoffman
     *
     */
    public class PerformanceNodeItem{
        
        int id;
        String name;
        String nodeTypeLabel;
        
        public PerformanceNodeItem(int id, String name, String nodeTypeLabel){
            this.id = id;
            this.name = name;
            this.nodeTypeLabel = nodeTypeLabel;
        }
        
        public int getId(){
            return id;
        }
        
        @Override
        public String toString(){
            return (getId() > 0 ? getId() : NO_ID) + "  '" + (name == null || name.length() == 0 ? NO_NAME : name) + "' - [Node Type: "+nodeTypeLabel+"]";
        }
    }    
}
