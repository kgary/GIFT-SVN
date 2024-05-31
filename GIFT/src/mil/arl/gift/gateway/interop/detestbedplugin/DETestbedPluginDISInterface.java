/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.detestbedplugin;
import mil.arl.gift.gateway.interop.dis.DISInterface;

/**
 * This interop plugin interface is responsible for handling in-bound communications with the DE Testbed training application via DIS.
 * 
 * @author dscrane
 *
 */
public class DETestbedPluginDISInterface extends DISInterface {
  
    /** Instance of the logger */
    //private static Logger logger = LoggerFactory.getLogger(DETestbedPluginDISInterface.class);
    
    /**
     * Class constructor
     * 
     * @param name - display name for this plugin
     */
    public DETestbedPluginDISInterface(String name){
        super(name, true);
        
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DETestbedPluginDISInterface ");
        sb.append(super.toString());
        sb.append("]");
        return sb.toString();
    }
}
