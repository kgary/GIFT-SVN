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
 * The MetricRpc class is used to track the statistics for an rpc.  An rpc can have a total time,
 * number of times called.  Additionally details such as count for success or failure can be added as well.
 * 
 * @author nblomberg
 *
 */
public class MetricRpc extends AbstractMetric {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MetricRpc.class);
    
    /** The JSON tag for the metric type */
    public static final String TYPE_TAG = "Type";
    /** The JSON tag for the metric name */
    public static final String NAME_TAG = "Name";
    /** The JSON tag for the rpc total count */
    public static final String TOTALCOUNT_TAG = "RpcTotalCount";
    /** The JSON tag for the rpc total time */
    public static final String TOTALTIME_TAG = "RpcTotalTime";
    /** The JSON tag for the rpc success count */
    public static final String SUCCESSCOUNT_TAG = "RpcSuccessCount";
    /** The JSON tag for the rpc fail count */
    public static final String FAILCOUNT_TAG = "RpcFailCount";
    /** The JSON tag for the average time in the rpc */
    public static final String AVERAGETIME_TAG = "RpcAverageTime";
    

    /** The total time spent in the rpc on the server.  Typically this is in milliseconds.  */
    private long rpcTotalTime = 0;
    /** The total count of how many times the rpc was called on the server */
    private long rpcTotalCount = 0;
    
    /** (optional) The number of total 'success' calls of the rpc on the server */
    private long successCount = 0;
    /** (optional) The number of total 'failures' of the rpc on the server */
    private long failCount = 0;


    /**
     * Constructor
     */
    public MetricRpc() {
        setType(MetricType.RPC);
    }
    
    
    /** 
     * Accessor to the the total rpc time.  This value contains the total time spent in the rpc on the server.
     * 
     * @return rpcTotalTime - The total time spent in the rpc.
     */
    public long getRpcTotalTime() {
        return rpcTotalTime;
    }
    
    /**
     * Sets the value of the rpc total time.  This will override any previous value that was set.  
     * 
     * @param time - The new value for the rpc total time.
     */
    public void setRpcTotalTime(long time) {
        rpcTotalTime = time;
    }
    
    /** 
     * Increases the current value of the rpc total time by the new value.  This is equivalent of
     *   rpcTotalTime = rpcTotalTime + time
     *   
     * @param time - The amount to increase the current rpc time by.
     */
    public void increaseRpcTotalTime(long time) {
        rpcTotalTime += time;
    }
    
    /** Increases the current value of the rpc count by 1.  This is eqivalent of
     *   rpcTotalCount = rpcTotalCount + 1
     */
    public void increaseRpcTotalCount() {
        rpcTotalCount++;
    }
    
    /** Increases the current value of the rpc count by the new count value.  This is eqivalent of
     *   rpcTotalCount = rpcTotalCount + count
     * @param count - The amount to increase the current rpc count value by.
     */
    public void increaseRpcTotalCount(long count) {
        rpcTotalCount+=count;
    }
    
    /**
     * Accessor to get the current rpc total count.
     * 
     * @return long - The current amount of times the rpc has been called.
     */
    public long getRpcTotalCount() {
        return rpcTotalCount;
    }
    
    /**
     * Set the rpc total count value by the new count value.  This will override any previous count value.
     * @param count - The value of the new rpc total count for the metric.
     */
    public void setRpcTotalCount(long count) {
        rpcTotalCount = count;
    }
    
    /**
     * (optional) Sets the rpc success count for the metric. This is the number of times the rpc was considered 'successful'.
     * The new count value will override any previous value for the successCount metric.
     * 
     * Not all rpcs need to use this, but can be used to track more finegrain resolution of how the rpc is performing.
     * @param count - The new success count value to be stored.
     */
    public void setRpcSuccessCount(long count) {
        successCount = count;
        
    }
    
    /**
     * (optional) Accessor to get the rpc success count for the metric.  This is the number of times the rpc is considered
     * successful. 
     * 
     * @return long - The total count of successful calls to the rpc.
     */
    public long getRpcSuccessCount() {
        return successCount;
    }
    
    /**
     * (optional) Sets the fail count for the rpc.  The failCount will be overridden with the new count value.
     * 
     * This is the number of times the rpc was considered to be a fail or sends a fail response to the client.  
     * Not all rpcs need to use this, but can be used to track more finegrain resolution of how the rpc is performing.
     * 
     * @param count - The new total failure count for the rpc.
     */
    public void setRpcFailCount(long count) {
        failCount = count;
    }
    
    /** 
     * (optional) Accessor to get the rpc failure count for the metric.  This is the number of times the rpc is considered
     * to have a failure.
     * 
     * @return the count of rpc failures
     */
    public long getRpcFailCount() {
        return failCount;
    }
    
    @Override
    public void encodeJSON(JSONObject obj) {
        
        try {
            obj.put(TYPE_TAG, MetricType.RPC.toString());
            obj.put(NAME_TAG,getName());
            obj.put(TOTALTIME_TAG,  getRpcTotalTime());
            obj.put(TOTALCOUNT_TAG,  getRpcTotalCount());
            obj.put(SUCCESSCOUNT_TAG, getRpcSuccessCount());
            obj.put(FAILCOUNT_TAG, getRpcFailCount());
            long avgTime = 0;
            
            if (getRpcTotalCount() > 0) {
                avgTime = getRpcTotalTime()/getRpcTotalCount();
            }
            obj.put(AVERAGETIME_TAG,  avgTime);
        } catch (JSONException e) {

            logger.error("Error encoding MetricRpc to JSON.", e);
        }
        
    }
    
    @Override
    public void decodeJSON(JSONObject obj) {
        
        try {
            String name = obj.getString(NAME_TAG);
            long totalTime = obj.getLong(TOTALTIME_TAG);
            long totalCount = obj.getLong(TOTALCOUNT_TAG);
            long successCount = obj.getLong(SUCCESSCOUNT_TAG);
            long failCount = obj.getLong(FAILCOUNT_TAG);
            
            // We are not deserializing the average count since it is a computed value that is used for decoding only.
            
            setName(name);
            setRpcTotalTime(totalTime);
            setRpcTotalCount(totalCount);
            setRpcSuccessCount(successCount);
            setRpcFailCount(failCount);
            
            
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            logger.error("Error decoding MetricRpc to JSON.", e);
        }
        
    }

}
