/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.segmentgenerator;

import mil.arl.gift.net.api.message.Message;

/**
 * Notifies the listener that DIS packets have been sent over the GIFT network
 *
 * @author jleonard
 */
public interface DISListener {
    
    /**
     * A entity state packet has been received
     * 
     * @param msg An entity state packet
     */
    void entityStateReceived(Message msg);
    
    /**
     * A detonation packet has been received
     * 
     * @param msg A detonation packet
     */    
    void detonationReceived(Message msg);
    
    /**
     * A weapon fire packet has been received
     * 
     * @param msg A weapon fire packet
     */
    void weaponFireReceived(Message msg);
    
}
