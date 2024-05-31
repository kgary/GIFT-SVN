/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.lcat.custnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This class presents a custom dialog for selecting Training Application Game State message types.
 * 
 * @author mhoffman
 *
 */
public class TrainingAppStateMessageEnumDialog extends
        XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** title of the dialog */
    private static final String DEFAULT_TITLE = "Training Application State Message Type";
    
    /** information text to show in the dialog */
    private static final String DEFAULT_LABEL = "Please select a Training Application State Message Type.\n";
    
    private static Object[] EMPTY = new Object[0];
    
    private static List<SelectionItem> DEFAULT_ENTRIES = new ArrayList<>();   
    static{
        
        for(MessageTypeEnum mType : MessageTypeEnum.TRAINING_APP_STATE_MESSAGE_TYPES){
            SelectionItem item = new SelectionItem(mType.getName(), mType.getDescription());
            DEFAULT_ENTRIES.add(item);
        }
        
        Collections.sort(DEFAULT_ENTRIES);
    }
    
    public TrainingAppStateMessageEnumDialog(){
        super(DEFAULT_TITLE, DEFAULT_LABEL, DEFAULT_ENTRIES, false);
    }

    @Override
    public Object[] getCustomValues() {
        return EMPTY;
    }

    @Override
    public void addUserEntry(String value) {
        //not supported
    }

}
