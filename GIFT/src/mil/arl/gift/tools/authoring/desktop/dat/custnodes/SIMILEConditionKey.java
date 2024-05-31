/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.FToggleNode;

import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This is the custom dialog for the DAT that provides a list of SIMILE condition keys from the selected
 * SIMILE configuration file. 
 * 
 * @author mhoffman
 *
 */
public class SIMILEConditionKey extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SIMILEConditionKey.class);
    
    /** dialog components */
    private static final String TITLE = "SIMILE Condition Key";
    private static final String LABEL = "Please select a rule from the SIMILE configuratin file that corresponds to this DKF condition.\n\n"
            + "Note: The SIMILE configuration file must reside in the course folder in order to be used.";
    
    /** schema elements */
    private static final String CONDITION_KEY = "conditionKey";
    private static final String SIMILE_CONDITION_INPUT = "SIMILEConditionInput";
    private static final String CONFIGURATION_FILE = "configurationFile";
    
    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /**
     * Class constructor
     */
    public SIMILEConditionKey(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }

    @Override
    public Object[] getCustomValues() {

        List<String> items = new ArrayList<>();        
        
        try {
            //get the SIMILE configuration file
            FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
            if(selectedNode != null){
                
                if(selectedNode.getLabelText().equals(CONDITION_KEY)){
                    
                    FToggleNode simileConditionNode = (FToggleNode)selectedNode.getParent();
                    if(simileConditionNode != null && simileConditionNode.getLabelText().equals(SIMILE_CONDITION_INPUT)){
                        //found SIMILE condition input node, get child configuration file node
                        
                        File courseFolder = DAT.getInstance().getDATForm().updateCourseFolder();
                        if(courseFolder != null){
                        
                            //iterate over all child elements 
                            for(int i = 0; i < simileConditionNode.getChildCount(); i++){
                                
                                FToggleNode childNode = (FToggleNode) simileConditionNode.getChildAt(i);
                                if(childNode.getLabelText().equals(CONFIGURATION_FILE)){
                                    //found configuration file element
                                    
                                    String configValue = (String)childNode.getValue();
                                    if(configValue != null && configValue.length() > 0){
                                        //have some value for the configuration file input element
                                        
                                        File configFile = new File(courseFolder + File.separator + configValue);
                                        items.addAll(DomainKnowledgeUtil.getSIMILEConcepts(configFile.getAbsolutePath(), new FileInputStream(configFile)));
    
                                    }else{
                                        
                                        JOptionPane.showMessageDialog(this,
                                                "Please provide a value for the SIMILE Configuration file element\n before attempting to select a SIMILE Condition key.",
                                                "Missing SIMILE Configuration File",
                                                JOptionPane.WARNING_MESSAGE);
                                    }
                                    
                                    //done with for loop
                                    break;
                                }
                                
                            }//end for
                        }
                    }else{
                        logger.error("Found the wrong selected node label - wanted "+SIMILE_CONDITION_INPUT+", found "+selectedNode.getLabelText());
                        return null;
                    }
                }
                
            }else{
                logger.error("The selected node is null, therefore can't determine which SIMILE configuration file to parse to populate the list of condition keys");
                return null;
            }

            
        } catch (Throwable e) {
            
            logger.error("Caught exception while gathering SIMILE Condition keys", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather SIMILE Condition keys, check the DAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        Collections.sort(items);
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        // no user history for this dialog
    }

}
