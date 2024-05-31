/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.lti;

import java.util.ArrayList;

import com.github.gwtd3.api.core.Value;
import com.google.gwt.core.client.JsonUtils;

import generated.course.LtiProvider;


/**
 * Contains common gwt client utility methods that may be needed for lti. This class is intended
 * to be accessed by static methods only.
 * 
 * @author nblomberg
 *
 */
public class LtiClientUtility  {
    
    /**
     * Constructor - private.
     */
    private LtiClientUtility() {
        
    }

    /**
     * Converts an LTI Consumer List JSON string into a list of LtiConsumer objects.
     * 
     * @param consumersJson The json string to convert.
     * @return List of LtiConsumers that was converted.  The list is empty if there is an error or if the string is null.
     */
    public static ArrayList<LtiConsumer> getLtiConsumerList(String consumersJson) {
        
        ArrayList<LtiConsumer> consumerList = new ArrayList<LtiConsumer>();
        
        if(consumersJson != null){
            LtiConsumerList consumers = JsonUtils.safeEval(consumersJson);
            for(Value consumerValue : consumers.getConsumers().asIterable()){
                
                LtiConsumer consumer = consumerValue.as();
                consumerList.add(consumer);
            }
        }
        
        return consumerList;
    }
    
    /**
     * Converts an LTI Provider List JSON string into a list of LtiProvider objects.
     * 
     * @param providersJson The json string to convert.
     * @return List of LtiProviders that was converted.  The list is empty if there is an error or if the string is null.
     */
    public static ArrayList<LtiProvider> getLtiProviderList(String providersJson) {
        
        ArrayList<LtiProvider> providerList = new ArrayList<LtiProvider>();
        
        if(providersJson != null){
            LtiProviderList providers = JsonUtils.safeEval(providersJson);
            for(Value providerValue : providers.getProviders().asIterable()){
                
                LtiProviderJSO providerJso = providerValue.as();
                LtiProvider provider = new LtiProvider();
                provider.setIdentifier(providerJso.getName());
                provider.setKey(providerJso.getKey());
                provider.setSharedSecret(providerJso.getSharedSecret());
                providerList.add(provider);
            }
        }
        
        return providerList;
    }

}
