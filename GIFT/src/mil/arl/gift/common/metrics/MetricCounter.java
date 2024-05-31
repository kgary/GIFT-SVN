/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metrics;


import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MetricCounter class is used to keep a running total for any given metric.  This could be used
 * for things like cpu usage, memory used, heap usage, etc. where some metric has a single value that
 * has a count associated with it.
 * 
 * @author nblomberg
 *
 */
public class MetricCounter extends AbstractMetric {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MetricCounter.class);

    /* The value of the counter */
    private long counterTotal = 0;

    /**
     * Constructor
     */
    public MetricCounter() {
        setType(MetricType.COUNTER);

    }

    /** Accessor to retrieve the counter value (count) 
     * 
     * @return - Current value of the counter. 
     */
    public long getCount() {
        return counterTotal;
    }
    
    /**
     * Accessor to set the value of the counter (count)
     * 
     * @param count - The new value for the counter.  This will override any previous value.
     */
    public void setCount(long count) {
        counterTotal = count;
    }
    
    /**
     * Increase the value of the counter by a new count value.  This is equivalent of
     *   counterVal = counterVal + count
     *   
     * @param count - The amount to increase the counter value by.  
     */
    public void increaseCount(long count) {
        counterTotal += count;
    }

    @Override
    public void encodeJSON(JSONObject obj) {
        try {
            obj.put("Type", MetricType.COUNTER.toString());
            obj.put("Count", getCount());
            obj.put("CounterName", getName());
            
        } catch (JSONException e) {

            logger.error("Error encoding MetricCounter to JSON.", e);
        }
        
    }

    @Override
    public void decodeJSON(JSONObject obj) {
        try {
            String name = obj.getString("CounterName");
            long totalCount = obj.getLong("Count");
            setName(name);
            setCount(totalCount);
   
        } catch (JSONException e) {
            logger.error("Error decoding MetricCounter to JSON.", e);
        }
        
    }

}
