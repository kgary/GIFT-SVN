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

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This is the XML custom dialog for selecting a learner state attribute type.
 * 
 * @author mhoffman
 *
 */
public class LearnerAttributeTypeDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** title of the dialog */
    private static final String TITLE = "Attribute Type";
    
    private static final String LABEL = "Please select a learner attribute type.\n";
    
    private static final Object[] NO_VALUES = new Object[0];
    
    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    static{
        
        for(LearnerStateAttributeNameEnum name : LearnerStateAttributeNameEnum.VALUES()){
            SelectionItem item = new SelectionItem(name.getName(), null);
            DEFAULT_VALUES.add(item);
        }
        
        Collections.sort(DEFAULT_VALUES);
    }
    
    public LearnerAttributeTypeDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }

    @Override
    public Object[] getCustomValues() {
        return NO_VALUES;
    }

    @Override
    public void addUserEntry(String value) {
        //not supporting user history for this dialog
    }

}
