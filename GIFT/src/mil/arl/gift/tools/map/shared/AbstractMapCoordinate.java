/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An abstract representation of a coordinate on a map
 * 
 * @author nroberts
 */
public class AbstractMapCoordinate implements IsSerializable{

    /**
     * Default, no-arg constructor required for GWT RPC serialization
     */
    protected AbstractMapCoordinate() { }
}
