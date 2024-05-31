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

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This custom dialog provides the list of Merrill Quadrants available to select.
 * 
 * @author mhoffman
 *
 */
public class MerrillQuadrantSelectionDialog extends
        XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** title of the dialog */
    private static final String TITLE = "Merrill Quadrant Selection Dialog";
    
    private static final String LABEL = "Please select from one of the enumerated Merrill Quadrants.\n";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    private static final Object[] EMPTY = new Object[0]; 
    
    static{
        
        //gather list of quadrant choices
        for(MerrillQuadrantEnum quadrant : MerrillQuadrantEnum.VALUES()){
            SelectionItem item = new SelectionItem(quadrant.getName(), null);
            DEFAULT_VALUES.add(item);
        }
        
        Collections.sort(DEFAULT_VALUES);
    }

    /**
     * Default constructor
     */
    public MerrillQuadrantSelectionDialog(){
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
