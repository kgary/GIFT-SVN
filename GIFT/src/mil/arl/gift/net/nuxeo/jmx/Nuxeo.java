/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.nuxeo.jmx;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MXBean implementation to monitor Nuxeo statistics
 * 
 */
public class Nuxeo implements NuxeoMXBean {
    
    /** 
     * how many connections have been made
     * Note: should not be decremented because we have a derivate graph
     */
    private final AtomicLong accumulatedHttpUrlConnectionCount = new AtomicLong();
    
    /** 
     * how many active user sessions 
     */
    private final AtomicInteger activeClientSessionCount = new AtomicInteger();
    
    /** 
     * how many times a session has been requested for users 
     */
    private final AtomicInteger clientSessionUseTotal = new AtomicInteger();
    
    @Override
    public long getAccumulatedHttpUrlConnectionCount() {
        return accumulatedHttpUrlConnectionCount.get();
    }
    
    public long incrementAccumulatedHttpUrlConnectionCount() {
        return accumulatedHttpUrlConnectionCount.incrementAndGet();
    }
    
    @Override
    public int getActiveClientSessionCount() {
        return activeClientSessionCount.get();
    }
    
    public int incrementActiveClientSessionCount() {
        return activeClientSessionCount.incrementAndGet();
    }
    
    public int decrementActiveClientSessionCount() {
        return activeClientSessionCount.decrementAndGet();
    }
    
    public int incrementClientSessionUseTotal() {
        return clientSessionUseTotal.incrementAndGet();
    }
    
    @Override
    public int getClientSessionUseTotal() {
        return clientSessionUseTotal.get();
    }
}
