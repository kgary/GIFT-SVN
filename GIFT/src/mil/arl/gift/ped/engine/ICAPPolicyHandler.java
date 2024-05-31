/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.ped.ActionEnum;
import generated.ped.ICAPPolicy;
import generated.ped.Policies.Policy;
import generated.ped.StateAttributes;
import mil.arl.gift.common.course.PedagogyFileValidationException;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.ped.PedagogicalModuleProperties;

/**
 * Responsible for parsing the ICAP Policy file into a generated class object.
 * 
 * @author mhoffman
 *
 */
public class ICAPPolicyHandler extends AbstractSchemaHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ICAPPolicyHandler.class);
    
    /** the generated object containing the XML file contents of the ICAP Policy to use */
    private ICAPPolicy icapPolicy;
    
    /** the source ICAP policy file */
    private FileProxy icapPolicyFile;
    
    /** the number of state attributes defined in the policy */
    private int stateAttributeSize = 0;
    
    /** the ICAP policy file */
    private static final File icap_policy_file = new File(PedagogicalModuleProperties.getInstance().getICAPPolicyFileName());
    
    /** singleton instance of this handler, so the policy isn't read in multiple times un-necessarily */
    private static ICAPPolicyHandler instance = null;
    
    /** mapping of state attribute policy rule to the ICAP action to take when that rule matches the state of the learner. */
    private Map<String, generated.ped.ActionEnum> policyMap = new HashMap<>();
    
    /**
     * Return the singleton instance
     * @return the singleton instance of this handler
     * @throws PedagogyFileValidationException if there was a problem configuration the pedagogical model using the configuration file.
     * @throws FileNotFoundException if the ICAP Policy file specified in the ped.properties could not be found.
     * @throws IllegalArgumentException if the ICAP policy file specified in the ped.properties is null or is a directory
     */
    public static synchronized ICAPPolicyHandler getInstance() throws PedagogyFileValidationException, IllegalArgumentException, FileNotFoundException{
        
        if(instance == null){
            instance = new ICAPPolicyHandler();
        }
        
        return instance;
    }
    
    /**
     * Class constructor - parses and validates the ICAP policy contents and sets the ICAP Policy object
     * 
     * @throws PedagogyFileValidationException if there was a problem configuration the pedagogical model using the configuration file.
     * @throws FileNotFoundException if the ICAP Policy file specified in the ped.properties could not be found.
     * @throws IllegalArgumentException if the ICAP policy file specified in the ped.properties is null or is a directory
     */
    public ICAPPolicyHandler() throws PedagogyFileValidationException, IllegalArgumentException, FileNotFoundException {
        super(ICAP_POLICY_SCHEMA_FILE);
        
        FileProxy file = new FileProxy(icap_policy_file);

        try{
            icapPolicyFile = file;
            UnmarshalledFile uFile = parseAndValidate(ICAP_POLICY_ROOT, file.getInputStream(), true);
            icapPolicy = (ICAPPolicy) uFile.getUnmarshalled();
            checkICAPPolicy();
        }catch(Exception e){
            throw new PedagogyFileValidationException("Failed to parse and validate the ICAP policy file against the schema.",
                    e.getMessage(),
                    file.getFileId(),
                    e);
        }
    }
    
    /**
     * Check the ICAP policy object read in from the XML file for issues.
     * This doesn't check for schema violations which is already done in the constructor.
     * 
     * @throws PedagogyFileValidationException if there was an issue found in the contents of the ICAP policy.
     */
    private void checkICAPPolicy() throws PedagogyFileValidationException{
        
        if(logger.isInfoEnabled()){
            logger.info("Checking ICAP Policy contents for issues...");
        }
        
        stateAttributeSize = 0;
        
        //
        // check that the number of state attribute values in each policy matches the number of state attributes defined
        //
        StateAttributes stateAttributes = icapPolicy.getStateAttributes();
        
        // use reflection to get the number of state attributes possible in the xsd
        Method[] stateAttributesMethods = StateAttributes.class.getDeclaredMethods();
        for(Method m : stateAttributesMethods){
            
            if(m.getName().startsWith("get")){
                
                try{
                    Object returnedObj = m.invoke(stateAttributes);
                    if(returnedObj != null){
                        stateAttributeSize++;
                    }
                }catch(@SuppressWarnings("unused") Exception e){
                    // don't care
                }
            }
        }
        
        List<Policy> policies = icapPolicy.getPolicies().getPolicy();
        for(int index = 0; index < policies.size(); index++){
            
            Policy policy = policies.get(index);
            List<String> stateAttributeValues = policy.getStateAttributeValue().getValue();
            if(stateAttributeSize != stateAttributeValues.size()){
                throw new PedagogyFileValidationException("Incorrect number of state attributes in the policy at position "+index+1, 
                        "The number of state attributes in every policy must be equal to the number of defined state attributes, "+
                                stateAttributeSize+" in this case.  Please correct this policy in '"+icapPolicyFile.getFileId()+"'.", 
                                icapPolicyFile.getFileId(),
                                null);
            }
            
            //
            // check that there are no duplicate policies by looking at state attribute values for repeat sequences
            //
            String mapKey = getPolicyKeyFromStateAttributeValue(stateAttributeValues);
            ActionEnum existingValue = policyMap.put(mapKey, policy.getActionChoice());
            if(existingValue != null){
                throw new PedagogyFileValidationException("Duplicate set of state attribute values in the policy at position "+index+1, 
                        "Each policy must have a unique set of state attribute values, otherwise which would be selected.  The duplicate values have a sequence of "+mapKey, 
                        icapPolicyFile.getFileId(),
                        null);
            }
        }
        

        
        if(logger.isInfoEnabled()){
            logger.info("Finished checking ICAP Policy contents for issues.");
        }
                
    }
    
    public ActionEnum getActionForStateAttributeValues(List<String> stateAttributeValues){
        return policyMap.get(getPolicyKeyFromStateAttributeValue(stateAttributeValues));
    }
    
    /**
     * Returns state attribute policy keys used for lookups in the policy map.
     * 
     * @param stateAttributeValues the state attribute true/false values for each state attribute defined
     * in the policy (e.g. concept remediation count)
     * @return a comma delimited string that contains the state attribute values from the list of strings
     * provided.  E.g. "1,0" for true on the first state attribute and false on the second state attribute.
     */
    public String getPolicyKeyFromStateAttributeValue(List<String> stateAttributeValues){
        return StringUtils.join(",", stateAttributeValues);
    }
    
    /**
     * Return the number of state attributes defined in the ICAP policy.
     * @return the number of defined state attributes (e.g. concept remediation count)
     */
    public int getStateAttributeSize(){
        return stateAttributeSize;
    }
    
    /**
     * Return the ICAP policy generated object instance from the ICAP policy file.
     * @return the ICAP Policy object populated from the policy file
     */
    public ICAPPolicy getICAPPolicy(){
        return icapPolicy;
    }
}
