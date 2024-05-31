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

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This is the XML Editor custom node dialog for the Task Ended Node id reference element in the DKF schema.
 * It will present the authored tasks to choose from.
 * 
 * @author mzellars
 *
 */
public class TaskEndedNodeDialog extends XMLAuthoringToolSelectionDialog {

	 private static final long serialVersionUID = 1L;
	    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TaskEndedNodeDialog.class);

    /** title of the dialog */
    private static final String TITLE = "Task Ended Node Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing Task entry from the Task/Concept hierarchy.\n"+
                "Note: if there are no nodes present, either you have not assigned a node id to the existing tasks \n yet or you have not created any.";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);  
    
    protected static final String TASKS = "tasks";
    protected static final String TASK = "task";
    protected static final String NODE_ID = "nodeId";
    protected static final String NAME = "name";
 
    private static final String NO_NAME = "(No-Name-Provided)";
    private static final String NO_ID   = "(No-Node-Id)";
    
    //the following block checks to make sure the class structure of Tasks->Task and Tasks from the xsd hasn't changed
    //as this class uses that structure to find node elements in the tree.
    //If changes are made to this structure or the names of class fields, then the code that walks/searches nodes in this class
    //will need to be changed accordingly.
    static{         
        generated.dkf.Tasks tasks = new generated.dkf.Tasks();
        tasks.getTask();
        generated.dkf.Task task = new generated.dkf.Task();
        task.getName();
        task.getNodeId();
    }
    
    /**
     * Class constructor - create dialog
     */
    public TaskEndedNodeDialog(){
    	super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object getData(){
        
        if(selectedValue != null && selectedValue instanceof TaskEndedNodeItem){
            int perfNodeId = ((TaskEndedNodeItem)selectedValue).getId();
            return perfNodeId;
        }
        
        return selectedValue;
    }
    
	@Override
	public Object[] getCustomValues() {
		 List<TaskEndedNodeItem> items = new ArrayList<>();        
         
	        try{
	            //
	            // gather all task ended node Ids from Task/Concept hierarchy
	            //
	            Element rootNode = DAT.getInstance().getDATForm().getRootNode();
	            
	            NodeList nl = rootNode.getElementsByTagName(TASKS);
	            if(nl != null && nl.getLength() == 1){
	                //found tasks element, the root of the Task/Concept hierarchy
	                
	                Element tasks = (Element)nl.item(0);
	                
	                nl = tasks.getElementsByTagName(TASK);
	                if(nl != null){
	                    
	                    //get node info for each task
	                    int nodeId;
	                    for(int taskIndex = 0; taskIndex < nl.getLength(); taskIndex++){
	                        
	                        Element task = (Element)nl.item(taskIndex);
	                        String taskName = task.getAttribute(NAME);
	                        String nodeIdStr = task.getAttribute(NODE_ID);
	                        
	                        if(nodeIdStr == null || nodeIdStr.length() == 0){
	                            logger.warn("While searching for task ended node ids, skipping a task in the task hierarchy w/ name = "+taskName+" because its node id is not set yet");
	                            
	                            nodeId = -1;
	                            
	                        }else{
	                            
	                            nodeId = Integer.valueOf(nodeIdStr);                           
	                        }
	                        
	                        items.add(new TaskEndedNodeItem(nodeId, taskName));

	                    }//end for
	                }
	                
	            }else{
	                
	                JOptionPane.showMessageDialog(this,
	                        "Please provide some task ended nodes by first creating a task node.  \nUntil at least one task ended node exists this dialog will have no choices to select from.",
	                        "Task Ended Node Needed",
	                        JOptionPane.INFORMATION_MESSAGE);
	            }          
	        
	        }catch(Throwable e){
	            logger.error("Caught exception while gathering task node ids", e);
	            
	            //show error message dialog
	            JOptionPane.showMessageDialog(this,
	                    "There was an error caught while trying to gather task node ids, check the DAT log for more details",
	                    "Error",
	                    JOptionPane.ERROR_MESSAGE);
	        }
	        
	        return items.toArray();
	}

	@Override
	public void addUserEntry(String value) {
		//no custom entries allowed for user history for this dialog implementation
	}
	
	/**
     * Wrapper class for Task Node information.  This class is responsible for 
     * handling the toString implementation used to display a string representing an instance of this class on the dialog.
     * 
     * @author mhoffman
     *
     */
    public class TaskEndedNodeItem{
        
        int id;
        String name;
        
        public TaskEndedNodeItem(int id, String name){
            this.id = id;
            this.name = name;
        }
        
        public int getId(){
            return id;
        }
        
        @Override
        public String toString(){
            return (getId() > 0 ? getId() : NO_ID) + "  '" + (name == null || name.length() == 0 ? NO_NAME : name);
        }
    }    
}
