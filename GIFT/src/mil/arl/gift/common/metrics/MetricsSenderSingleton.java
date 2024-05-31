/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metrics;

/**
 * The MetricsSenderSingleton class is a wrapper for holding the metricssender object across a JVM where all metrics objects
 * may not have access to a common servlet.
 * 
 * This class currently should ONLY be used in an environment such as GWT-dispatch where the rpcs are created via an AbstractHandler
 * structure that is different than the RemoteServiceServlet structure in other places.  
 * In this case, the MetricsSenderSingleton is created during the DispatchServletModule upon intialization such that all Handlers
 * in the Gat can access the same MetricSender object.  
 * 
 * Please use the MetricsSender class directly for most cases, and only use the singleton in an environment such as gwt-dispatch.  
 * Having a singleton means only one instance of this sender can be used at a time (which currently reports all metrics to the same group).
 * 
 * @author nblomberg
 *
 */
public class MetricsSenderSingleton {

    /**  singleton instance of this class. */
    private static MetricsSenderSingleton instance = null;
     
    /** The MetricsSender object */
    MetricsSender sender = null;

    /** Constructor
     * 
     * @param groupName - The group name that all metrics will be sent to the metrics server with.
     */
    private MetricsSenderSingleton(String groupName) {
        sender = new MetricsSender(groupName);
    }
    
    
    /**
     * Create the instance of the singleton with a specified group name.  This MUST be called
     * prior to using getInstance() so that the group name can be setup properly.
     * 
     * @param groupName - The metrics group name that all metrics will be sent to the metrics server with.
     */
    static synchronized public void createInstance(String groupName) {
        
        if (instance == null) {
            instance = new MetricsSenderSingleton(groupName);
        }
    }
    
    /**
     * Get the instance of the singleton object.
     * @return MetricsSenderSingleton - An instance of the singleton object.  Can return null if 'create instance' was not called.
     */
    static synchronized public MetricsSenderSingleton getInstance() {
        return instance;
    }
    
    /**
     * Destroy the singleton instance.  Primarily this used to stop tracking of the metrics.
     * 
     */
    static synchronized public void destroyInstance() {
        instance.stopSending();
        instance = null;
    }
    
    /**
     * Start sending metrics to the metrics server.
     */
    public void startSending() {
        sender.startSending();
    }
    
    /**
     * Stops tracking an Rpc on the server.
     * 
     * @param metricName - The name of the metric.  This MUST be unique within the group that the metric is being tracked for.
     * @param timeStarted - The start time when the rpc was first called on the server.
     */
    public void endTrackingRpc(String metricName, long timeStarted) {
        
        sender.endTrackingRpc(metricName, timeStarted);
        
    }
    
    /**
     * Stops sending metrics to the metrics server.
     */
    private void stopSending() {
        sender.stopSending();
    }

}
