/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.pcat.custnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fg.ftreenodes.FToggleNode;

import mil.arl.gift.common.enums.MetadataAttributeEnum;
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
    
    /** title of the dialog */
    private static final String TITLE = "Metadata Attribute Selection Dialog";
    
    private static final String LABEL = "Please select from one of the enumerated Metadata Attributes.\n";
    
    private static final String VALUE = "value";
    private static final String PRACTICE = "Practice";
    
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
        // value ^ metadata attribute ^ array ^ metadata attributes ^ attribute ^ array ^ attributes ^ quadrant!
        FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
        if(selectedNode != null){
            
            if(selectedNode.getLabelText().equals(VALUE)){
                //found value element
                
                FToggleNode node = (FToggleNode)selectedNode.getParent().getParent().getParent().getParent().getParent().getParent().getParent();
                if(node != null){
                    
                    if(PRACTICE.equals(node.getLabelText())){
                        //gather enumerations that are for practice quadrant
                        
                        for(MetadataAttributeEnum attribute : completeEnumList){
                            
                            if(attribute.isPracticeAttribute()){
                                SelectionItem item = new SelectionItem(attribute.getName(), null);
                                items.add(item);
                            }
                        }
                        
                    }else{
                        
                        //gather enumerations that are NOT for practice quadrant
                        
                        for(MetadataAttributeEnum attribute : completeEnumList){
                            
                            if(attribute.isContentAttribute()){
                                SelectionItem item = new SelectionItem(attribute.getName(), null);
                                items.add(item);
                            }
                        }
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
