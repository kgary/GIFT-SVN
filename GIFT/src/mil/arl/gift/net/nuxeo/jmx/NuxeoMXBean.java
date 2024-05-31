/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.nuxeo.jmx;

/**
 * MXBean interface to monitor Nuxeo connection information
 * 
 */
public interface NuxeoMXBean {
    /**
     * Get the running count of HttpURLConnection's opened since startup
     * @return Number of connections opened since application startup
     */
    public long getAccumulatedHttpUrlConnectionCount();
    
    /**
     * Get the current active Nuxeo client session count.
     * @return Number of active client sessions
     */
    public int getActiveClientSessionCount();
    
    /**
     * Get the number of times Nuxeo client sessions were used for transactions.
     * @return Number of times client sessions were used.
     */
    public int getClientSessionUseTotal();
}
