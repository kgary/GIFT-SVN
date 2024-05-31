/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This is the base class for learner attribute value dialogs.  It provides the common functionality
 * for selecting a learner attribute value.
 * 
 * @author mhoffman
 */
public abstract class AbstractLearnerAttributeValueDialog extends XMLAuthoringToolSelectionDialog {

    private static final long serialVersionUID = 1L;
    
    /** title of the dialog */
    private static final String TITLE = "Attribute Value";
    
    private static final String LABEL = "Please select the attribute value for the metadata relationship.\n";
    
    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /**
     * Constructor
     */
    public AbstractLearnerAttributeValueDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }

    /**
     * Find the possible values for the learner state attribute provided.
     * 
     * @param nameEnum the learner state attribute to retrive possible values for
     * @return List<String> - possible values for the specific learner state
     * @throws Exception if there was a problem retriveing the custom values
     */
    public List<String> getCustomValues(LearnerStateAttributeNameEnum nameEnum) throws Exception {
        
        //
        // determine the possible values of the attribute type provided
        //                 

        List<? extends AbstractEnum> vals = nameEnum.getAttributeValues();
        
        List<String> customVals = new ArrayList<>(vals.size());
        for(Object val : vals){
            customVals.add(((AbstractEnum)val).getName());
        }
        
        return customVals;

    }
    
    /**
     * Return the enumerated learner state attribute value object for the given learner state attribute and value's name specified.
     * 
     * @param nameEnum - the learner state attribute name whose enum value object is of interest
     * @param valueName - the enum name of the enum value object to return
     * @return AbstractEnum - the enum value object.  Can be null if no enum object was found.
     * @throws Exception - if there was an issue looking up the object for the value
     */
    public static AbstractEnum getValueEnum(LearnerStateAttributeNameEnum nameEnum, String valueName) throws Exception{
        
        AbstractEnum valueEnum = nameEnum.getAttributeValue(valueName);         
        return valueEnum;
    }

    @Override
    public void addUserEntry(String value) {
        // not supporting user history for this dialog
    }

}
