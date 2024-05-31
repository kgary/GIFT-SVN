/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import java.util.Set;

/**
 * This class represents a gateway module's status.  In addition the gateway's topic is provided in this class.
 * 
 * @author mhoffman
 *
 */
public class GatewayModuleStatus extends ModuleStatus {	

    /** a module's status information */
    private String topicName;
    
    /** collection of IP addresses for the machine the gateway module is running on. */
    private Set<String> ipAddresses;

    /**
     * Class constructor - populate module status data.  Used when the Gateway module is deployed
     * over Java Web Start (JWS) via downloaded JNLP file.
     * 
     * @param topicName - the topic name the gateway sends simulation message too.  Can't be null or empty.
     * @param status - a module's status information. Can't be null.
     */
    public GatewayModuleStatus(String topicName, ModuleStatus status){
        super(status.getModuleName(), status.getQueueName(), status.getModuleType());

        this.topicName = topicName;
    }
    
    /**
     * Class constructor - populate module status data.  Used when running Gateway module not over Server
     * deployment mode and all IP addresses will 
     * 
     * @param topicName - the topic name the gateway sends simulation message too
     * @param status - a module's status information 
     * @param ipAddresses - one or more IP addresses of the machine running the Gateway module.  Can't be null or empty.
     * Can contain localhost equivalents.
     */
    public GatewayModuleStatus(String topicName, ModuleStatus status, Set<String> ipAddresses){
        this(topicName, status);
        
        if(ipAddresses == null || ipAddresses.isEmpty()){
            throw new IllegalArgumentException("The ipaddresses is null or empty");
        }
        
        this.ipAddresses = ipAddresses;
    }
    
    /**
     * Return the collection of IP addresses for the machine the gateway module is running on.
     * 
     * @return Can be null if the Gateway module is running over Java Web Start (JNLP file download).  If
     * not null than it will contain one or more IP addresses and won't be empty. Can contain localhost equivalents.
     */
    public Set<String> getIPAddresses(){
        return ipAddresses;
    }

    /**
     * Return the address to which gateway message are sent too.
     * 
     * @return String
     */
    public String getTopicName(){
        return topicName;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GatewayModuleStatus: ");
        sb.append("topicName = ").append(getTopicName()).append(", ");
        if(getIPAddresses() != null){
            sb.append(", ipAddressess =\n").append(getIPAddresses()).append(",\n");
        }
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
