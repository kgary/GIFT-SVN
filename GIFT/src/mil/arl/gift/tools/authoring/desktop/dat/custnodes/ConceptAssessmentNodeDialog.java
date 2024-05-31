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

import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This is the XML Editor custom node dialog for the Concept Assessment class element 
 * in the DKF schema file. The dialog allows the user to select all concept nodes in the DKF.
 * 
 * @author bzahid
 *
 */
public class ConceptAssessmentNodeDialog extends PerformanceNodeDialog{
    
    private static final long serialVersionUID = 1L;    
    
    private static Logger logger = LoggerFactory.getLogger(ConceptEndedNodeDialog.class);
          
    /**
     * Class constructor - creates dialog
     */
    public ConceptAssessmentNodeDialog(){
        super();
        this.setTitle("Concept Assessment Node Dialog");
    }
    
    /**
     * Returns the node id of the last selected item in the combobox 
     */
    @Override
    public Object getData(){
        
        if(selectedValue != null && selectedValue instanceof PerformanceNodeItem){
            int perfNodeId = ((PerformanceNodeItem)selectedValue).getId();
            
            return perfNodeId;
        }
        
        return selectedValue;
    }
    
    /** Returns an array of all concepts authored in the DFK */
    @Override
    public Object[] getCustomValues() {

    	List<PerformanceNodeItem> items = new ArrayList<>();

    	try {

    		Element rootNode = DAT.getInstance().getDATForm().getRootNode();
    		NodeList nodeList = rootNode.getElementsByTagName(CONCEPT);

    		if (nodeList != null) {
    			int nodeId;

    			for (int index = 0; index < nodeList.getLength(); index++) {

    				Element concept = (Element) nodeList.item(index);
    				String conceptName = concept.getAttribute(NAME);
    				String nodeIdStr = concept.getAttribute(NODE_ID);

    				if (nodeIdStr == null || nodeIdStr.length() == 0) {

    					logger.warn("While searching for concept node ids, "
    							+ "skipping a task in the task hierarchy w/ name = "
    							+ conceptName + " because its node id is not set yet");    					    					
    					nodeId = -1;
    				} else {
    					nodeId = Integer.valueOf(nodeIdStr);
    				}

    				items.add(new PerformanceNodeItem(nodeId, conceptName,
    						CONCEPT));
    			}// end for
    		}
    	} catch (Throwable e) {
    		logger.error("Caught exception while gathering task node ids", e);

    		/* show error message dialog */
    		JOptionPane.showMessageDialog(this, "There was an error caught while"
    				+ " trying to gather concept node ids, check the DAT log for"
    				+ " more details", "Error", JOptionPane.ERROR_MESSAGE);
    	}

    	return items.toArray();
    }
}
