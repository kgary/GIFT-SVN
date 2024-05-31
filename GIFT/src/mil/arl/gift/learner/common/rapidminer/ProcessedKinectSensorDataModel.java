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
import java.util.Map;
import java.util.Vector;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.python.XMLRPCPythonServerManager;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.learner.LearnerModuleProperties;
import mil.arl.gift.net.xmlrpc.XMLRPCClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapidminer.example.table.DoubleArrayDataRow;

/** 
 * The ProcessedKinectSensorDataModel adds the ability for the raw Kinect sensor data to be processed via
 * a configurable python script before the results are fed into RapidMiner.  The pipeline is such that
 * 1)  Raw kinect data (vertex data) is received
 * 2)  The raw data is processed via a configurable python function call.
 * 3)  The output of the python data is then saved and eventually fed into RapidMiner.
 * 
 * The ProcessedKinectSensorDataModle distinguishes itself from the KinectSensorData model in that it adds
 * this extra ability to preprocess the raw data before it gets fed into RapidMiner.
 * 
 * $TODO$ nblomberg 
 * Currently the raw kinect vertex data is processed with a rigid expectation of the 'output' from python.  This 'output' contains
 * about 40 columns of data (as doubles) which matches the expectations of the RapidMiner model.
 * This should be made more configurable in the future such that any user could customize the input, and output in coordination with
 * the rapidminer model.
 * 
 * @author nblomberg
 *
 */
public class ProcessedKinectSensorDataModel extends KinectSensorDataModel{
        
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ProcessedKinectSensorDataModel.class);
    
    
    /** The expected python function call that will be made to process the raw data */
    private String SERVICE_METHOD_NAME = "ProcessRawVerts";
    
    /** The instance of the xmlrpc client */
    private XMLRPCClient client = null;
    
    /** The column index in the data table where the 'timestamp' value is held. */
    private static final int INDEX_TIMESTAMP = 0;
    
    /** Keep track of 40 seconds of raw data) */
    private static final int MAX_MS_RAW_DATA = 40000;
    
    private ArrayList<ArrayList<Double>> inputTable = new ArrayList<ArrayList<Double>>();
    
    
    public ProcessedKinectSensorDataModel(){        
        super();
        
        try {         
            
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
     * Helper function to add the vertex data to the row of values that will get sent to the python script.  The values are appended to the row (as columns).  
     * The values saved are the X,Y, and Z coordinates of the 3d vector.
     * 
     * @param dataRow - The current row of values that the vertex data will be appended to (vertex here is a tuple3d of X,Y,Z).  Cannot be null. 
     * @param verts - The Tuple3d (3d vector) whose points (X,Y,Z) will get added to the row.  Cannot be null.
     * 
     */
    private void addVertexData(ArrayList<Double> dataRow, Tuple3dValue verts) {
    
        dataRow.add(verts.getX());
        dataRow.add(verts.getY());
        dataRow.add(verts.getZ());
        
    }
    
    
    /**
     * Helper function to add the vertex data based on an attribute name that is passed in.  If the attributeName is found in the
     * map of attributes, then we attempt to append the vertex data to the current row.  
     * 
     * @param attributes - The map of attributes contained in the filteredsensordata message.
     * @param attributteName - the name of the attribute to look for in the attributes map.
     * @param dataRow - The current row of values that the vertex data will be appended to (vertex here is a tuple3d of X,Y,Z). 
     * 
     */
    private void addAttributeVertexData(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> attributes, SensorAttributeNameEnum attributeName, ArrayList<Double> dataRow) {
        
        Tuple3dValue vertexData = (Tuple3dValue) attributes.get(attributeName);
        if (vertexData != null) {
            addVertexData(dataRow, vertexData);
        } else {
            
            // We didn't get any data for this vertex...so we need to keep something in the table.
            dataRow.add(0.0);
            dataRow.add(0.0);
            dataRow.add(0.0);
        }
        
        
    }
    
    
    /**
     * Prunes the input table (before sending it to the python server).  We keep a history of at most
     * a 'configurable' amount of time.  Currently 40 seconds is the most data we keep in the table.
     * We want to purge out any entries that are older than 40 seconds.
     * 
     */
    private void pruneInputTable() {
    
        // We keep the last X seconds in the input table.  Any data that is older should get purged.
        while (inputTable.size() > 1) {
            
            ArrayList<Double> dataRowOldest = inputTable.get(0);
            ArrayList<Double> dataRowNewest = inputTable.get(inputTable.size()-1);
            
            if (!dataRowOldest.isEmpty() &&
                !dataRowNewest.isEmpty()) {
                
                Double tsOldest = dataRowOldest.get(INDEX_TIMESTAMP);
                Double tsNewest = dataRowNewest.get(INDEX_TIMESTAMP);
                
                if (tsNewest - tsOldest > MAX_MS_RAW_DATA) {
                    // Remove the oldest data point.
                    inputTable.remove(0);
                } else {
                    // The oldest record is not yet over the 40 second period, so let's keep it and stop pruning.
                    break;
                }
                
            }
            
        }
    }
    
    /**
     * Adds the filteredsensordata state content to the data model table being built by this class.
     * If the content has no values for the attributes represented by this data model than
     * no row will be added to the table.  Otherwise, the value for each attribute for this data
     * model found in the state object will be placed in the appropriate column of a new row.
     * 
     * @param state contains state information encoded in JSON format.
     * @throws Exception if there was a problem adding the state;s attributes to the data model.
     */
    @Override
    protected void addTableRow(FilteredSensorData state) throws Exception{
        
        
        // Early return if the python service was not started.  
        if (client == null) {
            return;
        }
        
        //flag used to indicate if at least one column had data place in it (below)
        boolean updated = false;
        
        logger.trace("Received sensorState: " + state);

        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> attributes = state.getAttributeValues();
        
        if (!attributes.isEmpty()) {
            
        
            states.add(state);
            
            // We are building a table with multiple rows of vertex data.  The columns look like this:
            // timestamp | headX | headY | headZ | leftHandX| leftHandY | leftHandZ | etc
            
            
            ArrayList<Double> vertRow = new ArrayList<Double>();
            
            // Add a timestamp as the first column.
            Number elapsedTime = state.getElapsedTime();
            vertRow.add(elapsedTime.doubleValue());
            
            // Each of the following attributes gets 3 columns (for X, Y, Z)
            // The python script must match the order of the columns being sent in here.
            // head 
            addAttributeVertexData(attributes, SensorAttributeNameEnum.HEAD, vertRow);
            
            //top skull
            addAttributeVertexData(attributes, SensorAttributeNameEnum.TOP_SKULL, vertRow);
            
            // lefthand
            addAttributeVertexData(attributes, SensorAttributeNameEnum.LEFT_HAND, vertRow);
            
            // righthand
            addAttributeVertexData(attributes, SensorAttributeNameEnum.RIGHT_HAND, vertRow);
            
            // leftshoulder
            addAttributeVertexData(attributes, SensorAttributeNameEnum.LEFT_SHOULDER, vertRow);
            
            // rightshoulder
            addAttributeVertexData(attributes, SensorAttributeNameEnum.RIGHT_SHOULDER, vertRow);
            
            // centershoulder
            addAttributeVertexData(attributes, SensorAttributeNameEnum.CENTER_SHOULDER, vertRow);

            // Add the row of data.
            inputTable.add(vertRow);
            
            // Prune the table to keep only the last 40 seconds of data.
            pruneInputTable();
            
            
            Vector<Object> params = new Vector<Object>();
            params.addElement(inputTable);
            
            Object[] dataRows = null;
            

            try {
            	
            	StringBuilder errorMsg = new StringBuilder();
            	
                dataRows = (Object[]) client.callMethod(SERVICE_METHOD_NAME, params, errorMsg);
                
                if(!errorMsg.toString().isEmpty()){
                	logger.error(errorMsg.toString());
                }
                
                
                if (dataRows != null) {
                
                    logger.trace("Result from Python script is: " + dataRows);
                    // The result should be an array of an array of objects.  
                    logger.trace("Number of Rows = " + dataRows.length);
                    for (int x=0; x < dataRows.length; x++) {
                        
                    
                        Object[] dataCols = (Object[])(dataRows[x]);
                        
                        if (data.length == dataCols.length) {
                            logger.trace("Number of Cols = " + dataCols.length);
                            // Check the length of the datarow to ensure it matches the expected number of columns.
                            for (int y=0; y < dataCols.length; y++) {
                                
                                
                                Object col = dataCols[y];
                                Double dataValue = Double.parseDouble(col.toString());
                               
                                // Insert each column 1:1 as it comes in from Python into our expected RapidMiner model.
                                data[y] = dataValue;
                                updated = true;
                                
                                logger.trace("Column class is " + col.getClass().getName());
                                logger.trace("Column as string = " + col.toString());
                                
                                
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
                    logger.trace("RapidMiner MemoryExampleTable updated: " + table);
                    logger.trace("  Data (as doubles): " + Arrays.toString(data));
                }
                
                
                
            } catch (Exception e) {
                logger.error("Error executing the python script. ", e);
            }
            
        }     

        //only add a row to the table if at least 1 attribute had data
        if(updated){
            
            table.addDataRow(new DoubleArrayDataRow(data));
            
            // Log debug information about the rapidminer table along with the raw double values that were inserted into the rapidminer table.
            logger.trace("MemoryExampleTable updated: " + table);
            logger.trace("  Data (as Double): " + Arrays.toString(data));
            
        }
        
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("ProcessedKinectSensorDataModel: ");
        sb.append("number-of-rows = ").append(table.size());
        sb.append("]");
        return sb.toString();
    }

}
