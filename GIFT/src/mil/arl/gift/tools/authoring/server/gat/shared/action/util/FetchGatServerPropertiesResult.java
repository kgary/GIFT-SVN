/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;


import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The class containing the result from the server. This class returns
 * the server properties which are a list of server-defined properites
 * that can be used to help configure the GAT client at runtime. 
 * 
 * @author nblomberg
 *
 */
public class FetchGatServerPropertiesResult extends GatServiceResult {
	
	private ServerProperties properties = new ServerProperties();
	
    /**
     * Default public constructor.
     */
    public FetchGatServerPropertiesResult() {
        super();

    }
    

    /**
     * Constructor
     * 
     * @param props - The server properties that can be used to configure the gat client at runtime.
     */
    public FetchGatServerPropertiesResult(ServerProperties props) {
    	properties = props;
	}
    
    /**
     * Accessor to retrieve the server properties.
     * 
     * @return ServerProperties - list of properties that are server-driven that are used to configure the gat client at runtime.
     */
	public ServerProperties getServerProperties() {
		return properties;
	}
}
