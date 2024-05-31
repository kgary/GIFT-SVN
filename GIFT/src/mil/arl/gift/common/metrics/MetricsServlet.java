/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metrics;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mil.arl.gift.common.metrics.AbstractMetric.MetricType;
import mil.arl.gift.common.io.CommonProperties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet that can be run on a Jetty server that tracks and aggregates metrics across various services.  
 * 
 * The MetricsServlet is responsible for maintaining the running total of metrics for the server / services.  The metrics
 * are stored in-memory only and an external application such as Graphite, etc must be used if the user needs to store the metrics.
 * 
 * Services can POST metrics to the metrics servlet.  The servlet listens for POST requests.  The post requests contain JSON
 * encoded metrics that the servlet than aggregates into it's running totals.
 * 
 * The servlet also can display a JSON encoded webpage containing the metrics in real-time via a GET request.  So if a caller
 * or external program like Graphite, the metrics url can be put into a web browser to query the current metrics for the server.
 * 
 * The method of storing / aggregating the stats currnetly is:
 *    Metric GroupName - A group name for a set of related metrics.
 *        - Metric Names - Within a group, there is an array of metrics by metric name.  Metric Names must be unique within the group.
 *        
 * @author nblomberg
 *
 */
public class MetricsServlet extends HttpServlet {

    /**
     * Default version id
     */
    private static final long serialVersionUID = 1L;

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MetricsServlet.class);

    public static final String CONTENT_TYPE = "application/json";
    public static final String TAG_METRICS_GROUP = "MetricsGroups";
    public static final String TAG_METRIC_GROUPNAME = "GroupName";
    public static final String TAG_METRIC_LIST = "MetricList";
    
    private HashMap<String, HashMap<String, AbstractMetric>> metricsMap = new HashMap<String, HashMap<String, AbstractMetric>>();
    
    
    /** Flag to control if the servlet is enabled or not. */
    boolean isEnabled = false;
    
    
    @Override
    public void init() throws ServletException {
    	
    	logger.info("init() called");
    	
    	
    	isEnabled = CommonProperties.getInstance().isServerMetricsEnabled();
    	
    	logger.info("Metrics servlet is started.  Enabled flag = " + isEnabled);
    	
    	metricsMap.clear();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // If the servlet is not enabled, then early return.
        if (!isEnabled) {
            super.doGet(request,  response);
            return;
        }
        
        logger.info("doGet() called");
        
        
        // Actual logic goes here.
        PrintWriter out = response.getWriter();
        // Set response content type
        response.setContentType(CONTENT_TYPE);

        try {
            
        
            JSONObject rootObj = new JSONObject();

            synchronized (metricsMap) {
                
                JSONArray groupArr = new JSONArray();
                for (Entry<String, HashMap<String, AbstractMetric>> groupEntry: metricsMap.entrySet()) {
                    
                    JSONObject groupObj = new JSONObject();
                    
                    
                    HashMap<String, AbstractMetric> metricMap = groupEntry.getValue();
                    String groupName = groupEntry.getKey();
                    groupObj.put(TAG_METRIC_GROUPNAME,  groupName);
                    
                    JSONArray metricArr = new JSONArray();
                    for (Entry<String, AbstractMetric> entry: metricMap.entrySet()) {
                        
                        JSONObject metricObj = new JSONObject();
                        
                        AbstractMetric absMetric = entry.getValue();
                        
                        absMetric.encodeJSON(metricObj);

                        metricArr.put(metricObj);
                    }
                    
                    groupObj.put(TAG_METRIC_LIST, metricArr);
                    groupArr.put(groupObj);
                }
                rootObj.put(TAG_METRICS_GROUP, groupArr);
                
                
                out.print(rootObj.toString());
            }
        }
        catch (Exception e) {
            logger.error("Error generating json: ", e);
        }
        
        
        
       
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // If the servlet is not enabled, then early return.
        if (!isEnabled) {
            super.doPost(req,  resp);
            return;
        }
        
        logger.info("doPost called()");
        try {
            BufferedReader reader = req.getReader();
            StringBuffer reqBody = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                reqBody.append(line);
            }
            
            logger.trace("requestBody = " + reqBody.toString());
            try {
                JSONObject rootObj = new JSONObject(reqBody.toString());
                logger.trace("requestJSON = " + rootObj);
                if (rootObj != null) {
                    
                    String groupName = rootObj.getString(TAG_METRIC_GROUPNAME);
                    
                    synchronized (metricsMap) {
                        if (!metricsMap.containsKey(groupName)) {
                            HashMap<String, AbstractMetric> newMetricMap = new HashMap<String, AbstractMetric>();
                            metricsMap.put(groupName,  newMetricMap);
                            
                        }

                        HashMap<String, AbstractMetric> metricMap = metricsMap.get(groupName);
                        
                        JSONArray metricsList = rootObj.getJSONArray("MetricList");
                        
                        for (int x = 0; x < metricsList.length(); x++) {
                            
                            JSONObject jsonObj = metricsList.getJSONObject(x);
                            
                            String metricTypeStr = jsonObj.getString("Type");
                            
                            MetricType metricType = MetricType.valueOf(metricTypeStr);
                            
                            switch(metricType) {
                                case COUNTER: {
                                    MetricCounter decodedMetric = new MetricCounter();
                                    decodedMetric.decodeJSON(jsonObj); 
                                    String name = decodedMetric.getName();

                                    if (!metricMap.containsKey(name)) {
                                        // If this is the first entry, add it to the map with the decoded values only.
                                        metricMap.put(name,  decodedMetric);
                                        
                                    } else {
                                        AbstractMetric absMetric = metricMap.get(name);
                                            
                                        if (absMetric instanceof MetricCounter) {
                                            MetricCounter metric = (MetricCounter) absMetric;
                                            metric.setCount(decodedMetric.getCount());
                                            
                                            metricMap.put(name,  metric);
                                        } else {
                                            logger.error("Trying to decode MetricRpc class, but encountered class of: " + absMetric.getClass().getName());
                                        }
                                        
                                    }
                                    
                                    break;
                                }
                                case RPC: {
                                    MetricRpc decodedMetric = new MetricRpc();
                                    decodedMetric.decodeJSON(jsonObj);
                                    String name = decodedMetric.getName();

                                    if (!metricMap.containsKey(name)) {
                                        // If this is the first entry, add it to the map with the decoded values only.
                                        metricMap.put(name,  decodedMetric);
                                        
                                    } else {
                                        AbstractMetric absMetric = metricMap.get(name);
                                            
                                        if (absMetric instanceof MetricRpc) {
                                            MetricRpc metric = (MetricRpc) absMetric;
                                            metric.increaseRpcTotalCount(decodedMetric.getRpcTotalCount());
                                            metric.increaseRpcTotalTime(decodedMetric.getRpcTotalTime()); 
                                            metricMap.put(name,  metric);
                                        } else {
                                            logger.error("Trying to decode MetricRpc class, but encountered class of: " + absMetric.getClass().getName());
                                        }
                                        
                                    }
                                    break;
                                }
                                case INVALID:
                                    //intentional fall through
                                default:
                                    logger.error("Unable to decode AbstractMetric of type: " + metricType);
                                    break;
                            
                            }
                            
                            
                        }
                    }
                }
              } catch (Exception e) {
                logger.error("Exception reading request: ", e);
              }

        } catch (Exception e) {
            logger.error("Exception reading request: ", e);
        }
        
    }
    
    @Override
    public void destroy() {
        
        metricsMap.clear();
        
        super.destroy();
        
        
    }
    

}
