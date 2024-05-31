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

import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This is the XML custom dialog for selecting a sensor type.
 * 
 * @author mhoffman
 *
 */
public class SensorTypeDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** title of the dialog */
    private static final String TITLE = "Sensor Type";
    
    private static final String LABEL = "Please select a sensor type.\n";
    
    private static final Object[] NO_VALUES = new Object[0];
    
    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    static{
        
        for(SensorTypeEnum type : SensorTypeEnum.VALUES()){
            SelectionItem item = new SelectionItem(type.getName(), null);
            DEFAULT_VALUES.add(item);
        }
        
        Collections.sort(DEFAULT_VALUES);
    }
    
    public SensorTypeDialog(){
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
