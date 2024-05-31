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

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fg.ftreenodes.FToggleNode;

/**
 * This is the XML Editor custom node dialog for the Child Concept Ended class element in the DKF schema file.
 * The dialog allows the user to select a concept node that is a child to the current task node.  The selected
 * concept node will be used to populate an element in a DKF file being developed using the DAT.
 * 
 * @author mhoffman
 *
 */
public class ConceptEndedNodeDialog extends PerformanceNodeDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ConceptEndedNodeDialog.class);
    
    private static final String CONCEPT_ENDED = "childConceptEnded";
    private static final String CHOICE = "Choice:";

    @Override
    public Object[] getCustomValues() {
        
        List<PerformanceNodeItem> items = new ArrayList<>();        
            
        try{
            //
            // gather all concept performance node Ids that are a child to the selected Task
            //            
            Element rootNode = DAT.getInstance().getDATForm().getRootNode();            
            
            FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
            if(selectedNode != null){
                
                if(selectedNode.getLabelText().equals(NODE_ID)){
                    //found node id attribute
                    
                    //filter out selected node id from being added to the combobox
                    Integer selectedTaskNodeId = getSelectedTaskNodeId(selectedNode);
                    
                    if(selectedTaskNodeId != null){
                        
                        String nodeIdStr;                                    
                        
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
                                    nodeIdStr = task.getAttribute(NODE_ID);
                                    if(nodeIdStr == null || nodeIdStr.length() == 0){
                                        //task id not populated yet, can't be the selected task
                                        continue;
                                    }
                                      
                                    nodeId = Integer.valueOf(nodeIdStr);
                                    
                                    if(nodeId != selectedTaskNodeId){
                                        //not the selected task
                                        continue;
                                    }

                                    NodeList conceptsNL = task.getElementsByTagName(CONCEPT);
                                    if(conceptsNL != null){
                                        
                                        //get node info for each task's concept
                                        for(int conceptIndex = 0; conceptIndex < conceptsNL.getLength(); conceptIndex++){
                                            
                                            Element concept = (Element)conceptsNL.item(conceptIndex);
                                            String conceptName = concept.getAttribute(NAME);
                                            nodeIdStr = concept.getAttribute(NODE_ID);
                                            if(nodeIdStr == null || nodeIdStr.length() == 0){
                                                logger.warn("While searching for performance node ids, found a concept in the hierarchy w/ name = "+conceptName+" (child of task named = "+taskName+") with no node id");
                                                items.add(new PerformanceNodeItem(-1, conceptName, CONCEPT));
                                            }else{
                                                nodeId = Integer.valueOf(nodeIdStr);
                                                items.add(new PerformanceNodeItem(nodeId, conceptName, CONCEPT));
                                            }
                                        }
                                    }
                                }//end for
                            }
                            
                        }
                    }else{
                        JOptionPane.showMessageDialog(this, "Please populate the Task node id before attempting to select it's concepts.",
                                "Task Node Id Required", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                }//end if on found node id element

            }else{
                logger.error("The selected node is null, therefore can't determine which task node to find concept ids for");
                return null;
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
    
    /**
     * Get the selected task node id by finding its element value by starting at the selected concept ended
     * element in the XML tree.
     * 
     * @param selectedNode - the currently selected node
     * @return Integer - the task node id (if available), otherwise null
     */
    private Integer getSelectedTaskNodeId(FToggleNode selectedNode){
        
      //check if this is for a concept ended trigger
        FToggleNode parentNode = (FToggleNode) selectedNode.getParent();
        if(parentNode != null && parentNode.getLabelText().equals(CONCEPT_ENDED)){
            
            FToggleNode taskNode = (FToggleNode) selectedNode.getParent().getParent().getParent().getParent().getParent();
            String nodeIdStr;
            //iterate over all child elements to this task to find the task node id
            for(int i = 0; i < taskNode.getChildCount(); i++){
                
                //reset
                nodeIdStr = null;
                
                FToggleNode childNode = (FToggleNode) taskNode.getChildAt(i);
                if(childNode.getLabelText().equals(NODE_ID)){
                    
                    Object nodeIdObj = childNode.getValue();
                    if(nodeIdObj != null){
                        
                        if(nodeIdObj instanceof String){
                            //if the id is not provided it will be an empty string
                            nodeIdStr = (String) nodeIdObj;
                            
                            if(nodeIdStr.length() > 0){
                                return Integer.valueOf(nodeIdStr);
                            }
                        }else{
                            //has a value and that value is an integer
                            return Integer.valueOf(nodeIdObj.toString());
                        }
                    }
                }
            }
        }else if(selectedNode.getLabelText().equals(CHOICE)){
            logger.info("Waiting for selected node to change from "+selectedNode+" to "+NODE_ID+" before attempting to gather concepts for the list");
        }else{
            logger.error("Found the wrong selected node label - wanted "+NODE_ID+", found "+selectedNode.getLabelText());
            return null;
        }
        
        return null;
        
    }

}
