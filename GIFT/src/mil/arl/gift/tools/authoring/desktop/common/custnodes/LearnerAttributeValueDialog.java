/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.FToggleNode;
import com.fg.ftreenodes.Params;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;

/**
 * This is the XML custom dialog for selecting a learner state attribute value for a particular learner state attribute type.
 * 
 * @author mhoffman
 *
 */
public class LearnerAttributeValueDialog extends AbstractLearnerAttributeValueDialog{

    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LearnerAttributeValueDialog.class);
    
    /** parameter keys in the schema */
    private static final String ATTRIBUTE_TYPE = "attributeTypeNodeName";
    private static final String ATTRIBUTE_VALUE = "attributeValueNodeName";
    private static final String ALLOW_ANY_VALUE = "allowAnyValue";
    
    /** parameter values */
    private String attributeValueNodeName = null;
    private String DEFAULT_ATTRIBUTE_VALUE = "attribute";
    private String attributeTypeNodeName = null;
    private String DEFAULT_ATTRIBUTE_TYPE = "type";
    private boolean allowAnyValue; 
    private String ANY_VALUE = "Any";
    
    private static final Object[] NO_VALUES = new Object[0];
    
    public LearnerAttributeValueDialog(){
        super();
    }
    
    @Override
    protected void useParameters(Params params){
        
        if(params.getMap() != null){
            //use parameters to customize dialog
            
            Map<?,?> paramsMap = params.getMap();
            
            if(paramsMap.containsKey(ATTRIBUTE_TYPE)){
                attributeTypeNodeName = (String)paramsMap.get(ATTRIBUTE_TYPE);
            }else{
                attributeTypeNodeName = DEFAULT_ATTRIBUTE_TYPE;
            }
            
            if(paramsMap.containsKey(ATTRIBUTE_VALUE)){
                attributeValueNodeName = (String)paramsMap.get(ATTRIBUTE_VALUE);
            }else{
                attributeValueNodeName = DEFAULT_ATTRIBUTE_VALUE;
            }
            
            if(paramsMap.containsKey(ALLOW_ANY_VALUE)){
                
                allowAnyValue = Boolean.parseBoolean((String)paramsMap.get(ALLOW_ANY_VALUE));
            }else{
                allowAnyValue = false;
            }
            
        }else{
            attributeTypeNodeName = DEFAULT_ATTRIBUTE_TYPE;
            attributeValueNodeName = DEFAULT_ATTRIBUTE_VALUE;
            allowAnyValue = false;
        }
    }

    @Override
    public Object[] getCustomValues() {
        
        try{
            //
            // determine the possible values of the attribute type provided
            //                  
            
            FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
            if(selectedNode != null){
                
                if(selectedNode.getLabelText().equals(attributeValueNodeName)){
                    //found attribute value xml attribute
                    
                    //find sibling attribute type node
                    for(int i = 0; i < selectedNode.getParent().getChildCount(); i++){
                        
                        FToggleNode typeNode = (FToggleNode)selectedNode.getParent().getChildAt(i);
                        if(typeNode.getLabelText().equals(attributeTypeNodeName)){
                            
                            //read attribute type element value
                            String value = (String)typeNode.getValue();
                            
                            if(value != null && value.length() > 0){       
                                //get attribute type values choices
                                
                                LearnerStateAttributeNameEnum nameEnum = LearnerStateAttributeNameEnum.valueOf(value);
                                List<String> values = super.getCustomValues(nameEnum);
                                
                                Collections.sort(values);
                                
                                if(allowAnyValue){
                                    values.add(0, ANY_VALUE);
                                }
                                return values.toArray();
                                
                            }else{
                                //type is not provided yet
                                JOptionPane.showMessageDialog(this, "Please populate the attribute type before attempting to select an attribute value.",
                                        "Attribute Type Required", JOptionPane.ERROR_MESSAGE);
                                return null;
                            }
                        }
                    }
                    
                    JOptionPane.showMessageDialog(this, "Unable to find the learner attribute type node named "+attributeTypeNodeName+".  Has the schema changed?",
                            "Attribute Type Node Not Found", JOptionPane.ERROR_MESSAGE);
                    return null;

                    
                }
                
            }
            
        }catch(Exception e){
            logger.error("Caught exception while gathering attribute type values", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather attribute type values, check the MAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        return NO_VALUES;
    }

    @Override
    public void addUserEntry(String value) {
        // not supporting user history for this dialog
    }
}
