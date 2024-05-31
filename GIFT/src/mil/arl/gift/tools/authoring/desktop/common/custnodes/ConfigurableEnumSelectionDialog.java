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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.Params;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This is the XML Editor custom node dialog for an enumeration selection element in a GIFT xml file.
 * The dialog allows the user to select from a list of enumerated values whose name values will appear as the value of an element.
 * Furthermore this dialog has customizable elements such as the dialog's title, information text and the enumerated Java class to use.
 * 
 * @author mhoffman
 *
 */
public class ConfigurableEnumSelectionDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ConfigurableEnumSelectionDialog.class);
    
    /** default title of the dialog if a custom one is not provided */
    private static final String DEFAULT_TITLE = "Enumeration Selection";
    
    /** default information text if custom content is not provided */
    private static final String DEFAULT_LABEL = "Please select the appropriate enumerated value.\n";
    
    /** parameter keys in the schema */
    private static final String TITLE = "title";
    private static final String ENUM_CLASS = "enumClass";
    private static final String INFO = "information";

    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /**
     * Class constructor - create dialog
     */
    public ConfigurableEnumSelectionDialog(){
        super(DEFAULT_TITLE, DEFAULT_LABEL, DEFAULT_VALUES, false);

    }
    
    @Override
    protected void useParameters(Params params){
        
        if(params.getMap() != null){
            //use parameters to customize dialog
            
            Map<?,?> paramsMap = params.getMap();
            
            if(paramsMap.containsKey(TITLE)){
                this.setTitle((String)paramsMap.get(TITLE));
            }
            
            if(paramsMap.containsKey(ENUM_CLASS)){
                
                try{
                    //get the enumerations values
                    @SuppressWarnings("unchecked")
                    Class<? extends AbstractEnum> enumClass = (Class<? extends AbstractEnum>) Class.forName((String) paramsMap.get(ENUM_CLASS));
                    List<? extends AbstractEnum> enumValues = AbstractEnum.VALUES(enumClass);
                    
                    if(!enumValues.isEmpty()){
                        //re-populate the combobox with the enumeration values found
                        
                        List<SelectionItem> values = new ArrayList<>(enumValues.size());
                        for(AbstractEnum value : enumValues){
                            SelectionItem item = new SelectionItem(value.getName(), null);
                            values.add(item);
                        }
                        
                        Collections.sort(values);
                        
                        setDefaultValues(values);
                        refreshEntries();
                    }
                }catch(Exception e){
                    logger.error("Unable to retrieve enumeration values for class named "+(String) paramsMap.get(ENUM_CLASS)+" because an exception was thrown.", e);
                }
            }
            
            if(paramsMap.containsKey(INFO)){
                this.setHelpfulInformation((String)paramsMap.get(INFO));
            }
        }
    }

    @Override
    public Object[] getCustomValues() {
        return new Object[0];
    }

    @Override
    public void addUserEntry(String value) {
        // currently don't support user history for this dialog, therefore nothing to do        
    }

}
