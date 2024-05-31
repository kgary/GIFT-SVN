/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.enums.MetadataAttributeEnum;
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
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    private static final Object[] EMPTY = new Object[0]; 
    
    static{
        
        //gather list of metadata attribute choices
        List<MetadataAttributeEnum> tempList = new ArrayList<>();
        for(MetadataAttributeEnum attribute : MetadataAttributeEnum.VALUES()){
            tempList.add(attribute);
        }
        
        Collections.sort(tempList);
        
        for(MetadataAttributeEnum attribute : tempList){
            SelectionItem item = new SelectionItem(attribute.getName(), null);
            DEFAULT_VALUES.add(item);
        }
        
//        Collections.sort(DEFAULT_VALUES);
    }

    /**
     * Default constructor
     */
    public MetadataAttributeSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
 
    }

    @Override
    public Object[] getCustomValues() {
        // nothing else to add
        return EMPTY;
    }

    @Override
    public void addUserEntry(String value) {
        // not supported for this dialog        
    }       

}
