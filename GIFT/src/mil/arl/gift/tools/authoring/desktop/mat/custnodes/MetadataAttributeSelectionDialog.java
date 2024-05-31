/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.mat.custnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.FToggleNode;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.tools.authoring.common.util.MetadataUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This custom dialog provides the list of metadata attributes available to select.
 * 
 * @author mhoffman
 *
 */
public class MetadataAttributeSelectionDialog extends
        XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MetadataAttributeSelectionDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Metadata Attribute Selection Dialog";
    
    private static final String LABEL = "Please select from one of the enumerated Metadata Attributes.\n";
    
    private static final String VALUE = "value";
    private static final String CONCEPTS = "Concepts";
    private static final String MERRILL_QUADRANT = "MerrillQuadrant";
    
    //zero based index
    private static final int MERRILL_QUAD_CHILD_INDEX = 2;
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /** the entire enumerated list which will be filtered based on quadrant being authored */
    static List<MetadataAttributeEnum> completeEnumList = new ArrayList<>();
    
    static{
        
        //gather list of metadata attribute choices
        for(MetadataAttributeEnum attribute : MetadataAttributeEnum.VALUES()){
            completeEnumList.add(attribute);
        }
        
        Collections.sort(completeEnumList);
    }

    /**
     * Default constructor
     */
    public MetadataAttributeSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
 
    }

    @Override
    public Object[] getCustomValues() {

        List<SelectionItem> items = new ArrayList<>();   
        
        // Determine the quadrant
        // value ^ attribute ^ array ^ attributes ^ concept ^ array ^ concepts -> (sibling) MerrillQuadrant
        FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
        if(selectedNode != null){
            
            if(selectedNode.getLabelText().equals(VALUE)){
                //found value element
                
                FToggleNode node = (FToggleNode)selectedNode.getParent().getParent().getParent().getParent().getParent().getParent();
                if(node != null && node.getLabelText().equals(CONCEPTS)){
                    
                    //the 3rd sibling of the parent 
                    node = (FToggleNode) ((FToggleNode)node.getParent()).getRealChildAt(MERRILL_QUAD_CHILD_INDEX);
                    
                    if(MERRILL_QUADRANT.equals(node.getLabelText())){
                        //get quadrant selection
                        
                        Object valueObj = node.getValue();
                        if(valueObj != null && valueObj instanceof String && !((String)valueObj).isEmpty()){
                            //determine the quadrant
                            
                            MerrillQuadrantEnum quadrant = MerrillQuadrantEnum.valueOf((String) valueObj);
                            if(quadrant != null){
                                //gather metadata attribute for that quadrant
                                
                                List<MetadataAttributeEnum> attributes = MetadataUtil.getMetadataAttributeByQuadrant(quadrant);
                                for(MetadataAttributeEnum attribute : attributes){
                                    SelectionItem item = new SelectionItem(attribute.getName(), null);
                                    items.add(item);
                                }
                
                            }else{
                                logger.error("Found unhandled null quadrant from quadrant choice of "+(String) valueObj+".");
                                return null;
                            }
                        }else{
                            //present dialog
                            JOptionPane.showMessageDialog(this,
                                    "Please provide a value for the Merrill Quadrant element\n before attempting to select a metadata attribute.",
                                    "Missing Merrill Quadrant",
                                    JOptionPane.WARNING_MESSAGE);
                        }

                        
                    }else{
                        //ERROR - did the schema change?
                        logger.error("The child at index "+MERRILL_QUAD_CHILD_INDEX+" of the parent to "+CONCEPTS+" element in the schema is called "+node.getLabelText()+" when it is expected to be "+MERRILL_QUADRANT+
                                ".  If the meatadata schema has changed at this level then the logic in this class needs to change as well.");
                        return null;
                    }
                }
            }
            
        }
        
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        // not supported for this dialog        
    }       

}
