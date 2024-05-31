/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.common.rapidminer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import mil.arl.gift.common.python.XMLRPCPythonServerManager;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.GenericJSONState.CommonStateJSON;
import mil.arl.gift.learner.LearnerModuleProperties;
import mil.arl.gift.net.xmlrpc.XMLRPCClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DoubleArrayDataRow;


/**
 * The ProcessedTC3DataModel implements the ability for the raw tc3 data to be 
 * processed via a python script before the data is fed into RapidMiner.  The pipeline is
 * 1) raw tc3 data is received 
 * 2) the data is processed via a configurable python script and
 * 3) the processed results are fed into rapid miner.
 * 
 * This data model distinguishes itself from the normal TC3DataModel in that it adds this extra
 * ability to process the data via a configurable python script.  
 * 
 * @author nblomberg
 *
 */
public class ProcessedTC3DataModel extends TC3DataModel{
        
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ProcessedTC3DataModel.class);
    
    private String SERVICE_METHOD_NAME = "ProcessTC3Data";
    
    private XMLRPCClient client = null;
    
    private ArrayList<ArrayList<Double>> inputTable = new ArrayList<ArrayList<Double>>();
       
    /** This is a magic number that signifies that we have 'no value' for the current column.
     *  The python script must match this value.  The max double size in Java may not match
     *  the max double/float size in Python, so we're going to hardcode a value to signify
     *  that this column has no data.
     */
    private static final double PYTHON_NO_VALUE = -9999.99;
    
    public ProcessedTC3DataModel(){        
        super();
        
        try {
            
            // Start the python client if it is configured to be started.
            if (LearnerModuleProperties.getInstance().getStartXMLRpcPythonServer()) {
                
                // Connect to a service that is already running (create a client connection only by passing in the URL for the server).
                client = new XMLRPCClient(XMLRPCPythonServerManager.DEFAULT_SERVER_BASE_URL, LearnerModuleProperties.getInstance().getXMLRpcPythonServerPort()); 
            } else {
                // Log an error and do not start the service.
                logger.error("Unable to start the required xmlrpc service.  The StartXMLRpcPythonServer option must be set to true in the learner.properties file.  Python server is not started so we cannot create the client interface.");
            }
            
        }
        catch (Exception e) {
            logger.error("Unable to start the xmlrpcservice.", e);
        }
    }
    
    /**
     * Adds the TC3 state content to the data model table being built by this class.
     * If the content has no values for the attributes represented by this data model than
     * no row will be added to the table.  Otherwise, the value for each attribute for this data
     * model found in the state object will be placed in the appropriate column of a new row.
     * 
     * @param state contains state information encoded in JSON format.
     * @throws Exception if there was a problem adding the state;s attributes to the data model.
     */
    @Override
    protected void addTableRow(GenericJSONState state) throws Exception{
        
        // Early return if the python service was not started.  
        if (client == null) {
            return;
        }
        
        //flag used to indicate if at least one column had data place in it (below)
        boolean updated = false;
        
        if(logger.isTraceEnabled()){
            logger.trace("Received state: " + state);
        }        
       
        final long NANOSECS_PER_MS = 1000000;
        double msTimeStamp = Math.floor(System.nanoTime() / NANOSECS_PER_MS);
        
        // Unlike Kinect sensor, data, we want to send only a single row of data to the python script.
        // In this way the managing of 'how much data to keep' is done in the python server rather than GIFT.
        inputTable.clear();
        
        // Create a new row of data to populate with the state data.
        final int MAX_COLUMNS = attributes.size() + 1; // add one here for timestamp column.
        ArrayList<Double> dataRow = new ArrayList<Double>(MAX_COLUMNS);
        
        // Add the current time (ms)
        if(logger.isTraceEnabled()){
            logger.trace("Timestamp (as DoubleValue) = " + msTimeStamp);
        }
        
        dataRow.add(msTimeStamp);
        
        // Default the number of expected columns to an invalid (default) value.
        // This will be used so that the script can determine if we received a valid value for a specific column for this timestamp.
        // Since we receive TC3 gamestate messages in any order, one state message may contain only the bloodvolume data, but another state
        // message may only contain the has heart rate data.  For any data we don't receive, we populate those values with NaN to signal
        // that there is "no data" for this timestamp for those specific columns.
        for (int x=0; x < attributes.size(); x++) {
            dataRow.add(PYTHON_NO_VALUE);
        }
        
        try {
            populateInputRowWithStateData(state, dataRow);
        } catch (Exception e) {
            // Abort out if we hit an error here.
            logger.trace("Error trying to process the inputRow with state data.", e);
            return;
        }
        
        // Add the row to the table (in this case, we only send a single row to the python script).  The python script
        // manages holding onto however many rows it may need to maintain state of variables.
        inputTable.add(dataRow);   
        
        Long domainSessionId = (Long)state.getJSONObject().get(CommonStateJSON.TC3_SessionId_key);
        int sessionid = domainSessionId.intValue();
        
        if(logger.isTraceEnabled()){
            logger.trace("Domain session: " + sessionid);
        }
        
        
        Vector<Object> params = new Vector<Object>();
        params.addElement(sessionid);
        params.addElement(inputTable);
        
        Object[] dataRows = null;
        
        try {        	
            dataRows = (Object[]) client.callMethod(SERVICE_METHOD_NAME, params, null);        
            
            if (dataRows != null) {
            
                if(logger.isTraceEnabled()){
                    logger.trace("Result from Python script is: " + dataRows);
                }
                
                // The result should be an array of an array of objects.                 
                if(logger.isTraceEnabled()){
                    logger.trace("Number of Rows = " + dataRows.length);
                }
                for (int x=0; x < dataRows.length; x++) {
                    
                    String columnTrace = "";
                    
                    
                    Object[] dataCols = (Object[])(dataRows[x]);
                    
                    // We need to ensure that the expected columns match, otherwise, we don't process it.
                    if (data.length == dataCols.length) {
                        
                        
                        // First index is the timestamp, so ignore that for now. 
                        for (int y=0; y < dataCols.length; y++) {
                            
                            
                            // timestamp is the first value, so we index the next element
                            Object col = dataCols[y];
                            Double dataValue = Double.parseDouble(col.toString());
                           
                            // Insert each column 1:1 as it comes in from Python into our expected RapidMiner model.
                            // We subtract 1 here because the timestamp is included in the return parameters.
                            data[y] = dataValue;
                            updated = true;
                            
                            if (logger.isTraceEnabled()) {                                
                                columnTrace = columnTrace.concat("[" + col.toString() + "]");
                            }                           
                            
                        }
                        
                        // Log the entire row 
                        if (logger.isTraceEnabled()) {
                            logger.trace("Row(" + x + "): = " + columnTrace);
                        }                        
                        
                    } else {
                        
                        // If the number of columns don't match up, then log an error.
                        logger.error("Mismatch in the number of columns from the python script (" + dataCols.length   
                                + ") to what RapidMiner expects (" + data.length + ")");                                
                    }                    
                }
            }
            
            if (updated) {
                table.addDataRow(new DoubleArrayDataRow(data));
                
                // Log debug information about the rapidminer table along with the raw double values that were inserted into the rapidminer table.
                if(logger.isTraceEnabled()){                    
                    logger.trace("RapidMiner MemoryExampleTable updated: " + table);
                    logger.trace("  Data (as doubles): " + Arrays.toString(data));
                }
            }           
            
        } catch (Exception e) {
            logger.error("Error executing the python script. ", e);
        }               

        //only add a row to the table if at least 1 attribute had data
        if(updated){
            
            table.addDataRow(new DoubleArrayDataRow(data));
            
            // Log debug information about the rapidminer table along with the raw double values that were inserted into the rapidminer table.
            if(logger.isTraceEnabled()){
                logger.trace("MemoryExampleTable updated: " + table);
                logger.trace("  Data (as Double): " + Arrays.toString(data));
            }            
        }       
    }
    
    
    /**
     * Populates the input row (row) with the state data that is being handled.  It will parse the state message
     * (which is in JSON format) and look for any attributes that are configued to be processed.  For any attribute
     * found, the value is placed in the input row.
     * 
     * @param state - contains state information encoded in JSON format.
     * @param row - Contains the row of data that will be fed into RapidMiner.  The state data should get added to this row.
     * @throws Exception if there was a problem adding the state;s attributes to the data model.
     */
    void populateInputRowWithStateData(GenericJSONState state, ArrayList<Double> row) throws Exception {

        JSONObject obj = state.getJSONObject();        
        
        // $TODO$ nblomberg
        // For now we don't know what the contents of the genericjsonstate message are, so for each message,
        // we need to do an O(n^2) loop to find if the data has any matching attributes.  This may be an area
        // to optimizae if needed.
        JSONArray msgData = (JSONArray)obj.get(CommonStateJSON.DATA);
        for (int x=0; x < msgData.size(); x++) {
        
            
            JSONObject msgObj = (JSONObject)msgData.get(x);
            
            // Iterate over each attribute we're interested in and get the value for it.
            for (String key : labelToAttribute.keySet()) {
                
                if (msgObj.containsKey(key)) {
      
                    Attribute attr = labelToAttribute.get(key);
                    
                    //get column index in table for this attribute
                    int index = attributes.indexOf(attr);
                    
                    if(index == -1){
                        throw new Exception("unable to find the attribute index for "+attr.getName()+".");
                    }
                    
                    double doubleValue;
                    Object value = msgObj.get(key);
                    if(value == null){
                        throw new Exception("The value for "+attr.getName()+" was null.");
                    }
                    
                    //process strings into doubles
                    if(value instanceof String){
                        String boolStr = (String)value;
                        // Convert booleans to 1 or 0s here.
                        if (boolStr.contentEquals(Boolean.TRUE.toString())) {
                            value = "1";
                        } else if (boolStr.contentEquals(Boolean.FALSE.toString())) {
                            value = "0";
                        }
                        
                        doubleValue = Double.parseDouble((String) value);
                    }else{
                        throw new Exception("Found unhandled value of "+value+" for attribute named "+attr.getName()+".");
                    }
                        
                    // place data in column at index
                    // We add at index of 1, because the first column is a timestamp which we send to the python script.
                    row.set(index + 1, doubleValue);

                    if(logger.isTraceEnabled()){
                        logger.trace("Table updated with key: " + key + "  -- value is: " + value);
                    }
                }
                else {
                    // Uncomment to see ignored keys, however, leaving this commented out, because it really spams the logs. 
                    // logger.trace("Key with name of " + key + " is not found in the attribute list.  This key will not be added to the data.");                    
                }
            }
        }
    }
    
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ProcessedTC3DataModel: ");
        sb.append("number-of-rows = ").append(table.size());
        sb.append("]");
        return sb.toString();
    }

}
