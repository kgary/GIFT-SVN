/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metrics;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import mil.arl.gift.common.io.CommonProperties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MetricSender class is used to send metrics to a common metrics server.  This class is more preferable to 
 * be used rather than the MetricsSenderSingleton class since the MetricsSender class can be embedded uniquely in
 * any object.  
 * 
 * The primary use case is for the MetricsSender class to be embedded on a JettyServer to be able to gather metrics
 * about rpcs that are used on the Jetty server and forward those metrics to the metrics server.  In the GWT framework, this
 * is intended to be made a private class that is part of the RemoteServiceServlet.  On the init() method of the RemoteServiceServlet
 * the metricsSender can be set to 'startTracking'.  On the destroy method of the servlet, the stopTracking method should be called.
 * 
 * Then in each rpc, the MetricsSender can be used to track metrics as needed for profiling to the metric server.  See the 
 * DashboardServiceImpl.java file for an example of how this is setup.
 * 
 * The primary use case of the MetricsSender is in a server/cloud environment for GIFT.  The metricsSender can be disabled
 * via a configuration setting in the common.properties file. By default metrics sending is disabled.  
 * 
 * GIFT can have any number of MetricsSender classes embedded for profiling.  The user must consider the "GroupName" that is used
 * to group the metrics.  The "GroupName" should normally be unique across various MetricsSender classes, however it is possible to have
 * multiple MetricsSender classes sending to the same "GroupName" if needed.  In most cases the user should make the "GroupName" unique
 * to ensure that metrics are contained uniquely in the group that is specified.  
 * 
 * Within a Group, the MetricName MUST be unique for each different metric that should be tracked.  The caller is responsible for
 * identifying the names of the metrics and groups to ensure they are unique and track properly.
 * 
 * @author nblomberg
 *
 */
public class MetricsSender {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MetricsSender.class);
    
    private static final String POST = "POST";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final int HTTP_OK = 200;
    private static final int WAIT_TIMEOUT_MS = 5000;
    
    
    /** A map of the metric name to the Metric classes.  Duplicate metric names are not allowed within the same metric group */
    private HashMap<String, AbstractMetric> metricsMap = new HashMap<String, AbstractMetric>();
    
    /* The metrics group name.  Each MetricsSender sends metrics to the same group */
    private String metricsGroupName = "";
    
    /* The timer used to send the metrics to the server */
    private Timer sendTimer = null;
    
    /** Controls the time interval (ms) between sending metrics to the metrics server.  Currently we set to 
     * send gathered metrics every 5 seconds to the metrics server.
     */
    private static final int METRICS_TIMER_MS = 5000;
    /**
     * Controls the delay (ms) before the metrics timer starts.  
     */
    private static final int METRICS_TIMER_START_DELAY_MS = 200;
    
    /** Flag if the metrics sender is enabled or not.  If it is disabled, the metrics are not gathered or sent to the server */
    private boolean metricsEnabled = false;
    
    /** The url of the metrics server */
    private String metricsServerUrl = "";


    

    /** 
     * Constructor
     * 
     * Each metrics sender class is set to send metrics within a single group name.  The GroupName controls what group
     * the metrics are bundled under for this sender.
     * 
     * @param groupName - The name of the metrics group (eg. dashboard, gat, tutor)
     */
    public MetricsSender(String groupName) {

        metricsGroupName = groupName;
        metricsMap.clear();
        
        metricsEnabled = CommonProperties.getInstance().isServerMetricsEnabled();

        metricsServerUrl = CommonProperties.getInstance().getMetricsServerUrl();
        
        if (metricsServerUrl.isEmpty() && metricsEnabled) {
            logger.error("Metrics are enabled, but the url is empty.  Please check the common.properties config file to ensure the MetricsUrl is set properly.");
            // Disable metrics reporting if we don't have a url specified.
            metricsEnabled = false;
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Metrics Sender for group " + groupName + " initialized.  Metrics enabled = " + metricsEnabled);
        }
         
    }
    
    /**
     * Return whether metrics sending has been enabled.
     * 
     * @return the flag indicating whether metrics sending is enabled
     */
    public boolean isMetricsEnabled(){
        return metricsEnabled;
    }
    
    /**
     * Starts sending the metrics to the server (if the metricsEnabled flag is set).
     * 
     */
    public void startSending() {
    
        if (!metricsEnabled) {
            return;
        }
        
        if (sendTimer == null) {
            String timerName = "MetricsSender_" + metricsGroupName;
            sendTimer = new Timer(timerName);
            
            
            sendTimer.scheduleAtFixedRate(new TimerTask() {
    
                @Override
                public void run() {
                    sendMetrics();
                    
                }
                
            }, METRICS_TIMER_START_DELAY_MS, METRICS_TIMER_MS);
        
        }  
    }
    
    /**
     * Sends metrics to the metrics server.  If metrics are not enabled this will do nothing. 
     * The metrics are encoded & sent to the metrics
     * server via a POST call and the metrics are embedded in JSON format.
     */
    private void sendMetrics() {
        // Early return if metrics are not enabled.
        if (!metricsEnabled) {
            return;
        }
        
        if(logger.isTraceEnabled()){
            logger.trace("sending current metrics");
        }
        
        HttpURLConnection connection = null;
        try {
            JSONObject jsonRootObj = new JSONObject();
            
            jsonRootObj.put(MetricsServlet.TAG_METRIC_GROUPNAME, metricsGroupName);

            JSONArray jsonMetrics = new JSONArray();
            
            // Convert the metrics to json.
            synchronized(metricsMap) {
                
                for (Entry<String, AbstractMetric> entry: metricsMap.entrySet()) {
                    
                    AbstractMetric absMetric = entry.getValue();
                    
                    if (absMetric instanceof MetricRpc) {
                        MetricRpc metric = (MetricRpc)absMetric;
                        
                        JSONObject jsonObj = new JSONObject();
                        
                        metric.encodeJSON(jsonObj);
                        jsonMetrics.put(jsonObj);
                       
                        // Reset the time/count since it has been posted now.
                        metric.setRpcTotalCount(0);
                        metric.setRpcTotalTime(0);
                    } else if (absMetric instanceof MetricCounter) {
                        MetricCounter metric = (MetricCounter)absMetric;
                        
                        JSONObject jsonObj = new JSONObject();
                        metric.encodeJSON(jsonObj);
                        jsonMetrics.put(jsonObj);
                        
                    } else {
                        logger.error("Cannot encode metric to JSON format.  Unsupported metric type of : " + absMetric.getClass());
                    }
                }
            }

            // Add the array to the list of metrics.
            jsonRootObj.put(MetricsServlet.TAG_METRIC_LIST, jsonMetrics);

            String metricsData = jsonRootObj.toString();
            
            URL fullUrl = new URL(metricsServerUrl);
            connection = (HttpURLConnection) fullUrl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(POST);
            connection.setConnectTimeout(WAIT_TIMEOUT_MS);  // wait 5 seconds
            connection.setRequestProperty( CONTENT_TYPE, MetricsServlet.CONTENT_TYPE );
            connection.setRequestProperty( CONTENT_LENGTH, String.valueOf(metricsData.length()));
            try(OutputStream os = connection.getOutputStream()){
                os.write(metricsData.getBytes());
            }
                
            int responseCode = connection.getResponseCode();

            if ( responseCode == HTTP_OK) {
                // This is a post request, so we don't need to do anything with the response.
                if(logger.isTraceEnabled()){
                    logger.trace("Url is reachable, server returned response of 200: " + metricsServerUrl);
                }
            } else {                
                logger.error("Retrieved response code of: " + connection.getResponseCode() + ", message of: " + connection.getResponseMessage());                
            }
            
        } catch (Exception e) {
            logger.error("Exception caught: ", e);
        } finally{
            
            if(connection != null){
                connection.disconnect();
            }
        }
        
    }
    
    /**
     * Stops tracking an Rpc on the server. If metrics are not enabled this will do nothing.  
     * 
     * @param name - The name of the metric.  This MUST be unique within the group that the metric is being tracked for.
     * @param timeStarted - The start time when the rpc was first called on the server.
     */
    public void endTrackingRpc(String name, long timeStarted) {
        
        if (!metricsEnabled) {
            return;
        }

        // Compute the elapsed time that was spent in the rpc.
        long end = System.currentTimeMillis();
        long elapsedTime = end - timeStarted;

        if (!metricsMap.containsKey(name)) {
            MetricRpc newMetric = new MetricRpc();
            newMetric.setName(name);
            metricsMap.put(name,  newMetric);
        }
        
        AbstractMetric absMetric = metricsMap.get(name);
        
        if (absMetric instanceof MetricRpc) {
            MetricRpc metric = (MetricRpc)absMetric;
            
            
            synchronized(metricsMap) {
                metric.increaseRpcTotalTime(elapsedTime);
                metric.increaseRpcTotalCount();
                
                metricsMap.put(name,  metric);
            }
        } else {
            
            if (absMetric != null) {
                logger.error("Error occurred trying to end the metric time for an rpc.  Expected a MetricRpc class for name: " + name + ".  Instead a metric of type: " + absMetric.getClass() + " was found.");
            } else {
                logger.error("Error occurred trying to end the metric time for an rpc.  The metric for name: " + name + ", could not be found.");
            }
            
        }
    }
    
    /**
     * Updates the metric counter by name with a specified count value. If metrics are not enabled this will do nothing.  
     * 
     * @param name - the name of the counter (must be unique within a group)
     * @param counterValue - the value that the counter will be set to
     */
    public void updateMetricCounter(String name, long counterValue) {
        
        if (!metricsEnabled) {
            return;
        }

        if (!metricsMap.containsKey(name)) {
            MetricCounter newCounter = new MetricCounter();
            newCounter.setName(name);
            metricsMap.put(name,  newCounter);
        }
        
        AbstractMetric absMetric = metricsMap.get(name);

        if (absMetric instanceof MetricCounter) {
            MetricCounter metric = (MetricCounter)absMetric;

            synchronized(metricsMap) {
                metric.setCount(counterValue);
                
                metricsMap.put(name,  metric);
            }

        } else {
         
            logger.error("Error occurred update the metric counter.  Expected a MetricCounter class for name: " + name + ".  Instead a metric of type: " + absMetric.getClass() + " was found.");

        }
        
    }
    
    /**
     * Add one to the current metric count value for the metric mapped to the name provided.</br>
     * Note: this will create a MetricCounter for the named metric if it doesn't already exist.
     * 
     * @param name the unique name of the metric to increment its count value by 1 for.
     * @return true iff the metric counter was incremented successfully.  False will be returned if metrics is NOT enabled
     * or there exist another metric type besides MetricCounter for the metric name provided.
     */
    public boolean incrementMetricCounter(String name){        
        return addToMetricCounter(name, 1);
    }
    
    /**
     * Subtract one to the current metric count value for the metric mapped to the name provided.</br>
     * Note: this will create a MetricCounter for the named metric if it doesn't already exist.
     * 
     * @param name the unique name of the metric to decrement its count value by 1 for.
     * @return true iff the metric counter was decremented successfully.  False will be returned if metrics is NOT enabled
     * or there exist another metric type besides MetricCounter for the metric name provided.
     */
    public boolean decrementMetricCounter(String name){        
        return addToMetricCounter(name, -1);
    }
    
    /**
     * Add the specified value to the current metric count value for the metric mapped to the name provided.</br>
     * Note: this will create a MetricCounter for the named metric if it doesn't already exist.
     * 
     * @param name the unique name of the metric to change its count value for.
     * @return true iff the metric counter was changed successfully.  False will be returned if metrics is NOT enabled
     * or there exist another metric type besides MetricCounter for the metric name provided.
     */
    private boolean addToMetricCounter(String name, int value){
        
        if (!metricsEnabled) {
            return false;
        }
        
        if (!metricsMap.containsKey(name)) {
            MetricCounter newCounter = new MetricCounter();
            newCounter.setName(name);
            metricsMap.put(name,  newCounter);
        }
     
        AbstractMetric absMetric = metricsMap.get(name);

        if (absMetric instanceof MetricCounter) {
            MetricCounter metric = (MetricCounter)absMetric;

            synchronized(metricsMap) {
                metric.setCount(metric.getCount()+value);
                
                metricsMap.put(name,  metric);
            }
            
            return true;

        } else {
         
            logger.error("Error occurred adding "+value+" to the metric counter.  Expected a MetricCounter class for name: " + name + ".  Instead a metric of type: " + absMetric.getClass() + " was found.");
            return false;
        }
    }
    
    /*
     * Stop sending metrics to the metrics server.
     */
    public void stopSending() {
        
        if(logger.isInfoEnabled()){
            logger.info("stoping metrics sender");
        }
        
        if (sendTimer != null) {
            sendTimer.cancel();
            sendTimer = null;
        }
    }
    
    
    /** 
     * Main function for the MetricsSender class.  This can be used as a test function to test sending metrics to the
     * server without having to load all of GIFT.
     * 
     * @param args arguments passed from the command line
     */
    public static void main(String[] args) {
        
        System.out.println("Starting Metrics Test");
        
        MetricsSender metrics = new MetricsSender("dashboard");
        
        metrics.startSending();
        while(true) {
            try {
                long startTime = System.currentTimeMillis() - (int)(Math.random() * 500);
                metrics.endTrackingRpc("loginUser", startTime);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
    }

}
