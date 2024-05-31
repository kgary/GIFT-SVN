/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metrics;


import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AbstractMetric class provides the base implementation for all metrics that can be tracked on the
 * metrics server.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractMetric {

    /** instance of the logger */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(AbstractMetric.class);


    /** The available types of metrics that can be tracked by the metric server. 
     *  INVALID - an invalid metric type (default value if type is not specified).
     *  COUNTER - A simple metric that can be used to track a count (examples are heap usage,  memory usage, cpu usage)
     *  RPC - A metric that can be used to track a request to the server (total time spent in the rpc, total count of the rpc, success count, fail count)
     * 
     */
    public enum MetricType {
        INVALID,
        COUNTER,
        RPC
    }

    /** The name of the metric */
    private String metricName = "";
    
    /** The type of the metric */
    private MetricType metricType = MetricType.INVALID;
    
    /**
     * Default constructor
     */
    public AbstractMetric() {
    

    }
    
    /** 
     * Returns the name of the metric.  Metric names should be unique within the group they are tracked in.
     * 
     * @return String - the name of the metric.  
     */
    public String getName() {
        return metricName;
    }
    
    /**
     * Sets the name of the metric to be tracked.  The metric name should be unique for the group it is tracked in.
     * 
     * @param name - The name of the metric (cannot be null)
     */
    public void setName(String name) {
        metricName = name;
    }
    
    /** 
     * Accessor to get the type of the metric.
     * 
     * @return MetricTye - The type of the metric.
     */
    public MetricType getType() {
        return metricType;
    }
    
    /** 
     * Accessor to set the type of metric.
     * 
     * @param type - The type of metric (eg. COUNTER, RPC)
     */
    public void setType(MetricType type) {
        metricType = type;
    }
    
    /**
     * 
     * Each metric class is responsible for encoding the parameters into JSON format.
     * A JSONObject is passed in (not null) and the metric should fill in the name/value pairs that 
     * encapsulate the metric as a JSON object.
     * 
     * @param obj - The JSONObject to encode the metric data into.  Should never be null.
     */
    abstract public void encodeJSON(JSONObject obj);
    
    /**
     * Each metric class is responsible for decoding the parameters from a JSON object into
     * the metric class data types.  
     * 
     * @param obj - the JSONObject to decode the metric data from.
     */
    abstract public void decodeJSON(JSONObject obj);
    

}
