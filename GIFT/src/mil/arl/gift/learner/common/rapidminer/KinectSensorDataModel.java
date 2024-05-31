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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.sensor.Tuple3dValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.Ontology;

/**
 * This class contains the RapidMiner data model for the kinect sensor.
 * It can be used to convert filtered sensor data kinect state messages into RapidMiner
 * data model table.  That table can then be used as input to a RapidMiner process
 * and model.  Details on the table can be found here:  
 * http://rapid-i.com/wiki/index.php?title=Integrating_RapidMiner_into_your_application
 * 
 * @author nblomberg
 *
 */
public class KinectSensorDataModel implements DataModel{
        
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(KinectSensorDataModel.class);
        
    /**
     * The Sensor FilterData that comes from the Kinect
     */
    public static final String HEAD_LABEL                     = "HEAD";
    public static final String RIGHT_HAND_LABEL               = "RIGHT_HAND";
    public static final String LEFT_HAND_LABEL                = "LEFT_HAND";
    public static final String TOP_SKULL                      = "TOP_SKULL";
   
    // Head value comes in as a 3d value, so we input those as separate values into RapidMiner.
    public static final String HEAD_POS_X_LABEL               = "HEAD_POS_X";
    public static final String HEAD_POS_Y_LABEL               = "HEAD_POS_Y";
    public static final String HEAD_POS_Z_LABEL               = "HEAD_POS_Z";
    
    // 
    public static final String R_HAND_POS_X_LABEL             = "R_HAND_POS_X";
    public static final String R_HAND_POS_Y_LABEL             = "R_HAND_POS_Y";
    public static final String R_HAND_POS_Z_LABEL             = "R_HAND_POS_Z";
    
    //
    public static final String L_HAND_POS_X_LABEL             = "L_HAND_POS_X";
    public static final String L_HAND_POS_Y_LABEL             = "L_HAND_POS_Y";
    public static final String L_HAND_POS_Z_LABEL             = "L_HAND_POS_Z";
    
    //
    public static final String R_SHOULDER_POS_X_LABEL         = "R_SHOULDER_POS_X";
    public static final String R_SHOULDER_POS_Y_LABEL         = "R_SHOULDER_POS_Y";
    public static final String R_SHOULDER_POS_Z_LABEL         = "R_SHOULDER_POS_Z";
    
    //there are processed attributes - hopefully remove them in future
    // $TODO$ These attributes currently seem to be required by the model, but are not used in the WJIP rules.
    public static final String HEAD_POS_CHANGE_LABEL          = "HEAD_pos_change_last_emo";
    public static final String HEAD_DEPTH_CHANGE_3SEC_LABEL = "HEAD_depth_change_3sec";
    public static final String HEAD_POS_CHANGE_3SEC_LABEL = "HEAD_pos_change_3sec";
    public static final String HEAD_DEPTH_CHANGE_6SEC_LABEL = "HEAD_depth_change_6sec";
    public static final String HEAD_POS_CHANGE_6SEC_LABEL     = "HEAD_pos_change_6sec";
    public static final String HEAD_DEPTH_CHANGE_10SEC_LABEL = "HEAD_depth_change_10sec";
    public static final String HEAD_POS_CHANGE_10SEC_LABEL = "HEAD_pos_change_10sec";
    public static final String HEAD_DEPTH_CHANGE_LAST_EMO_LABEL = "HEAD_depth_change_last_emo";
    
    public static final String TOP_SKULL_DEPTH_CHANGE_LABEL   = "TOP_SKULL_depth_change_last_emo";
    public static final String TOP_SKULL_DEPTH_CHANGE_3SEC_LABEL = "TOP_SKULL_depth_change_3sec";
    public static final String TOP_SKULL_POS_CHANGE_3SEC_LABEL = "TOP_SKULL_pos_change_3sec";
    public static final String TOP_SKULL_DEPTH_CHANGE_6SEC_LABEL = "TOP_SKULL_depth_change_6sec";
    public static final String TOP_SKULL_POS_CHANGE_6SEC_LABEL = "TOP_SKULL_pos_change_6sec";
    public static final String TOP_SKULL_DEPTH_CHANGE_10SEC_LABEL = "TOP_SKULL_depth_change_10sec";
    public static final String TOP_SKULL_POS_CHANGE_10SEC_LABEL = "TOP_SKULL_pos_change_10sec";
    public static final String TOP_SKULL_DEPTH_CHANGE_LAST_EMO_LABEL = "TOP_SKULL_pos_change_last_emo";
    
    public static final String L_SHOULDER_DEPTH_CHANGE_3SEC_LABEL = "LEFT_SHOULDER_depth_change_3sec";
    public static final String L_SHOULDER_POS_CHANGE_3SEC_LABEL = "LEFT_SHOULDER_pos_change_3sec";
    public static final String L_SHOULDER_DEPTH_CHANGE_6SEC_LABEL = "LEFT_SHOULDER_depth_change_6sec";
    public static final String L_SHOULDER_POS_CHANGE_6SEC_LABEL = "LEFT_SHOULDER_pos_change_6sec";
    public static final String L_SHOULDER_DEPTH_CHANGE_10SEC_LABEL = "LEFT_SHOULDER_depth_change_10sec";
    public static final String L_SHOULDER_POS_CHANGE_10SEC_LABEL = "LEFT_SHOULDER_pos_change_10sec";
    public static final String L_SHOULDER_DEPTH_CHANGE_LAST_EMOLABEL = "LEFT_SHOULDER_depth_change_last_emo";
    public static final String L_SHOULDER_POS_CHANGE_LAST_EMO_LABEL = "LEFT_SHOULDER_pos_change_last_emo";
    
    public static final String R_SHOULDER_DEPTH_CHANGE_3SEC_LABEL = "RIGHT_SHOULDER_depth_change_3sec";
    public static final String R_SHOULDER_CHANGE_LABEL        = "RIGHT_SHOULDER_pos_change_3sec"; 
    public static final String R_SHOULDER_DEPTH_CHANGE_6SEC_LABEL = "RIGHT_SHOULDER_depth_change_6sec";
    public static final String R_SHOULDER_POS_CHANGE_6SEC_LABEL = "RIGHT_SHOULDER_pos_change_6sec";
    public static final String R_SHOULDER_DEPTH_CHANGE_10SEC_LABEL = "RIGHT_SHOULDER_depth_change_10sec";
    public static final String R_SHOULDER_POS_CHANGE_10SEC_LABEL = "RIGHT_SHOULDER_pos_change_10sec";
    public static final String R_SHOULDER_DEPTH_CHANGE_LAST_EMO_LABEL = "RIGHT_SHOULDER_depth_change_last_emo";
    public static final String R_SHOULDER_POS_CHANGE_LAST_EMO_LABEL = "RIGHT_SHOULDER_pos_change_last_emo";
    
    public static final String C_SHOULDER_DEPTH_CHANGE_3SEC_LABEL = "CENTER_SHOULDER_depth_change_3sec";
    public static final String C_SHOULDER_POS_CHANGE_3SEC_LABEL = "CENTER_SHOULDER_pos_change_3sec";
    public static final String C_SHOULDER_DEPTH_CHANGE_6SEC_LABEL = "CENTER_SHOULDER_depth_change_6sec";
    public static final String C_SHOULDER_POS_CHANGE_6SEC_LABEL = "CENTER_SHOULDER_pos_change_6sec";
    public static final String C_SHOULDER_DEPTH_CHANGE_10SEC_LABEL = "CENTER_SHOULDER_depth_change_10sec";
    public static final String C_SHOULDER_POS_CHANGE_10SEC_LABEL = "CENTER_SHOULDER_pos_change_10sec";
    public static final String C_SHOULDER_DEPTH_CHANGE_LAST_EMO_LABEL = "CENTER_SHOULDER_depth_change_last_emo";
    public static final String C_SHOULDER_POS_CHANGE_LAST_EMO_LABEL = "CENTER_SHOULDER_pos_change_last_emo";
    
 
    /**
     * RapidMiner attributes used a column headers in the data model table.
     */
    
    protected static Attribute headPosAttr          = AttributeFactory.createAttribute(HEAD_POS_CHANGE_LABEL, Ontology.REAL);
    protected static Attribute topSkullAttr         = AttributeFactory.createAttribute(TOP_SKULL_DEPTH_CHANGE_LABEL, Ontology.REAL);
    protected static Attribute headPos6secAttr           = AttributeFactory.createAttribute(HEAD_POS_CHANGE_6SEC_LABEL, Ontology.REAL);
    protected static Attribute rtShoulderAttr         = AttributeFactory.createAttribute(R_SHOULDER_CHANGE_LABEL, Ontology.REAL);
    
    // These values may be model dependent and may need to change based on the model requirements.
    private static Attribute cShoulderDepth3secAttr         = AttributeFactory.createAttribute(C_SHOULDER_DEPTH_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute cShoulderPos3secAttr         = AttributeFactory.createAttribute(C_SHOULDER_POS_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute cShoulderDepth6secAttr         = AttributeFactory.createAttribute(C_SHOULDER_DEPTH_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute cShoulderPos6secAttr         = AttributeFactory.createAttribute(C_SHOULDER_POS_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute cShoulderDepth10secAttr         = AttributeFactory.createAttribute(C_SHOULDER_DEPTH_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute cShoulderPos10secAttr         = AttributeFactory.createAttribute(C_SHOULDER_POS_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute cShoulderDepthLastEmoAttr         = AttributeFactory.createAttribute(C_SHOULDER_DEPTH_CHANGE_LAST_EMO_LABEL, Ontology.REAL);
    private static Attribute cShoulderPosLastEmoAttr         = AttributeFactory.createAttribute(C_SHOULDER_POS_CHANGE_LAST_EMO_LABEL, Ontology.REAL);
    private static Attribute ltShoulderDepth3sAttr         = AttributeFactory.createAttribute(L_SHOULDER_DEPTH_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute ltShoulderPos3secAttr         = AttributeFactory.createAttribute(L_SHOULDER_POS_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute ltShoulderDepth6secAttr         = AttributeFactory.createAttribute(L_SHOULDER_DEPTH_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute ltShoulderPos6secAttr         = AttributeFactory.createAttribute(L_SHOULDER_POS_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute ltShoulderDepth10secAttr         = AttributeFactory.createAttribute(L_SHOULDER_DEPTH_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute ltShoulderPos10secAttr         = AttributeFactory.createAttribute(L_SHOULDER_POS_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute ltShoulderDepthLastEmoAttr         = AttributeFactory.createAttribute(L_SHOULDER_DEPTH_CHANGE_LAST_EMOLABEL, Ontology.REAL);
    private static Attribute ltShoulderPosLastEmoAttr         = AttributeFactory.createAttribute(L_SHOULDER_POS_CHANGE_LAST_EMO_LABEL, Ontology.REAL);
    private static Attribute rtShoulderDepth3secAttr         = AttributeFactory.createAttribute(R_SHOULDER_DEPTH_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute rtShoulderDepth6secAttr         = AttributeFactory.createAttribute(R_SHOULDER_DEPTH_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute rtShoulderPos6secAttr         = AttributeFactory.createAttribute(R_SHOULDER_POS_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute rtShoulderDepth10secAttr         = AttributeFactory.createAttribute(R_SHOULDER_DEPTH_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute rtShoulderPos10secAttr         = AttributeFactory.createAttribute(R_SHOULDER_POS_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute rtShoulderDepthLastEmoAttr         = AttributeFactory.createAttribute(R_SHOULDER_DEPTH_CHANGE_LAST_EMO_LABEL, Ontology.REAL);
    private static Attribute rtShoulderPosLastEmoAttr         = AttributeFactory.createAttribute(R_SHOULDER_POS_CHANGE_LAST_EMO_LABEL, Ontology.REAL);
    private static Attribute topSkullDepth3secAttr         = AttributeFactory.createAttribute(TOP_SKULL_DEPTH_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute topSkullPos3secAttr         = AttributeFactory.createAttribute(TOP_SKULL_POS_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute topSkullDepth6secAttr         = AttributeFactory.createAttribute(TOP_SKULL_DEPTH_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute topSkullPos6secAttr         = AttributeFactory.createAttribute(TOP_SKULL_POS_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute topSkullDepth10secAttr         = AttributeFactory.createAttribute(TOP_SKULL_DEPTH_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute topSkullPos10secAttr         = AttributeFactory.createAttribute(TOP_SKULL_POS_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute topSkullDepthLastEmoAttr         = AttributeFactory.createAttribute(TOP_SKULL_DEPTH_CHANGE_LAST_EMO_LABEL, Ontology.REAL);
    private static Attribute headDepth3secAttr         = AttributeFactory.createAttribute(HEAD_DEPTH_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute headPos3secAttr         = AttributeFactory.createAttribute(HEAD_POS_CHANGE_3SEC_LABEL, Ontology.REAL);
    private static Attribute headDepth6secAttr         = AttributeFactory.createAttribute(HEAD_DEPTH_CHANGE_6SEC_LABEL, Ontology.REAL);
    private static Attribute headDepth10secAttr         = AttributeFactory.createAttribute(HEAD_DEPTH_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute headPos10secAttr         = AttributeFactory.createAttribute(HEAD_POS_CHANGE_10SEC_LABEL, Ontology.REAL);
    private static Attribute headDepthLastEmoAttr         = AttributeFactory.createAttribute(HEAD_DEPTH_CHANGE_LAST_EMO_LABEL, Ontology.REAL);

    /**
     * The exact ordering of the column labels in the data model table
     * Note: the order is important when populating the rows of the table because the value for
     * an attribute needs to appear under the correct column (i.e. index)
     */
    protected static List<Attribute> attributes = new LinkedList<Attribute>();
    
    
    /*
     * This is the list of kinect sensor data attributes that we are looking for (which is a smaller subset
     * of the entire kinect attributes that are written to disk).  
     */
    protected static List<SensorAttributeNameEnum> kinectAttributes = new LinkedList<SensorAttributeNameEnum>();
   
    static{
        
        // $TODO$ nblomberg
        // Make this less hardcoded and configurable for the users.
        // The order of these attributes MUST match the order that the data is coming in from the python script.
        attributes.add(headPosAttr);
        attributes.add(headDepth3secAttr);
        attributes.add(headPos3secAttr);
        attributes.add(headPos6secAttr);
        attributes.add(headDepth6secAttr);
        attributes.add(headDepth10secAttr);
        attributes.add(headPos10secAttr);
        attributes.add(headDepthLastEmoAttr);
        
        attributes.add(topSkullAttr);
        attributes.add(topSkullDepth3secAttr);
        attributes.add(topSkullPos3secAttr);
        attributes.add(topSkullDepth6secAttr);
        attributes.add(topSkullPos6secAttr);
        attributes.add(topSkullDepth10secAttr);
        attributes.add(topSkullPos10secAttr);
        attributes.add(topSkullDepthLastEmoAttr);
        
        attributes.add(ltShoulderDepth3sAttr);
        attributes.add(ltShoulderPos3secAttr);
        attributes.add(ltShoulderDepth6secAttr);
        attributes.add(ltShoulderPos6secAttr);
        attributes.add(ltShoulderDepth10secAttr);
        attributes.add(ltShoulderPos10secAttr);
        attributes.add(ltShoulderDepthLastEmoAttr);
        attributes.add(ltShoulderPosLastEmoAttr);
        
        attributes.add(rtShoulderAttr);
        attributes.add(rtShoulderDepth3secAttr);
        attributes.add(rtShoulderDepth6secAttr);
        attributes.add(rtShoulderPos6secAttr);
        attributes.add(rtShoulderDepth10secAttr);
        attributes.add(rtShoulderPos10secAttr);
        attributes.add(rtShoulderDepthLastEmoAttr);
        attributes.add(rtShoulderPosLastEmoAttr);
        
        attributes.add(cShoulderDepth3secAttr);
        attributes.add(cShoulderPos3secAttr);
        attributes.add(cShoulderDepth6secAttr);
        attributes.add(cShoulderPos6secAttr);
        attributes.add(cShoulderDepth10secAttr);
        attributes.add(cShoulderPos10secAttr);
        attributes.add(cShoulderDepthLastEmoAttr);
        attributes.add(cShoulderPosLastEmoAttr);
  
        // This is the kinect sensor filtered attibutes we are listening for.
        kinectAttributes.add(SensorAttributeNameEnum.HEAD);
        kinectAttributes.add(SensorAttributeNameEnum.RIGHT_HAND);
        kinectAttributes.add(SensorAttributeNameEnum.LEFT_HAND);
        kinectAttributes.add(SensorAttributeNameEnum.RIGHT_SHOULDER);
    }
    
    /**
     * mapping of unique kinect sensor attribute label to the RapidMiner attribute associated with its values
     * This is used when a kinect sensor filteredsensordata is added and needs to be processed (i.e. values added to correct columns
     * of data model table)
     */
    private static Map<String, Attribute> labelToAttribute = new HashMap<>();
    static{
 
        //hopefully these will go away once the RapidMiner process evolves
        labelToAttribute.put(HEAD_POS_CHANGE_LABEL, headPosAttr);
        labelToAttribute.put(TOP_SKULL_DEPTH_CHANGE_LABEL, topSkullAttr);
        labelToAttribute.put(HEAD_POS_CHANGE_6SEC_LABEL, headPos6secAttr);
        labelToAttribute.put(R_SHOULDER_CHANGE_LABEL, rtShoulderAttr);
    }
    
    /** 
     * contains the JSON states received by this data model, in the order received, no matter if it contained
     * information for the data model table or not
     */
    protected List<FilteredSensorData> states = new ArrayList<>();
    
    /**
     * The RapidMiner data model table used as input to the Process in order to apply the model.
     */
    protected MemoryExampleTable table;
    
    /**
     * The exampleSet is a view of the data.  This is created during creation of the data model.
     */
    protected ExampleSet exampleSet;
    
    
    /**
     * The current RapidMiner process is expecting a single
     * data row with all values on the same row.  What we're doing here, is simply overwriting the values
     * of the row as new filtered sensor data messages are received.  The row is never cleared or deleted,
     * only overwritten.  
     */
    protected double[] data = new double[attributes.size()];
    
    public KinectSensorDataModel(){        
        table = new MemoryExampleTable(attributes);
        
        /* The createExampleSet creates the view into the table based on the attributes.
         * Currently this means that the attributes/columns of the table are not expected
         * to change for this instance of the data model. 
        */ 
        exampleSet = table.createExampleSet();
    }
    
    /**
     * Add a new state to this data model.
     * 
     * @param state contains state information encoded in JSON format.
     * @throws Exception if there was a problem adding the state's attributes to the data model
     */
    @Override
    public void addState(Object state) throws Exception{
        
        if (state instanceof FilteredSensorData) {
            
            FilteredSensorData sensorState = (FilteredSensorData) state;
            synchronized (states) {
                states.add(sensorState);
                addTableRow(sensorState);   
            }      
        } else {
            logger.error("Invalid state object added to KinectSensorData model.  Expecting FilteredSensorData, but received: " + state);
        }
       
    }
    
    /**
     * Clear all the states known to this data model.
     */
    @Override
    public void clearStates(){
        
        synchronized (states) {
            states.clear();
            table.clear();
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
    protected void addTableRow(FilteredSensorData state) throws Exception{
        
        
        //flag used to indicate if at least one column had data place in it (below)
        boolean updated = false;
        
        
        state.getAttributeValues();
        
        
        if (!state.getAttributeValues().isEmpty()) {
            // Grab the filtered data that we're listening for (not the full set of kinect sensor data).
            for (int x=0; x < kinectAttributes.size(); x++) {
                SensorAttributeNameEnum key = kinectAttributes.get(x);
                
                
                try {
                    // All kinect sensor data attributes are tuple3dvalues 
                    Tuple3dValue tuple3d = (Tuple3dValue)(state.getAttributeValues().get(key));
                    
                    if (tuple3d != null) {
                     // Since each 'key' is a tuple, we need to add the index into our row of data.
                        int dataIndex = (x * 3);
                        
                        data[dataIndex] = tuple3d.getX();
                        data[dataIndex+1] = tuple3d.getY();
                        data[dataIndex+2] = tuple3d.getZ();
                        
                        
                        updated = true;
                    } else {
                        logger.error("Unable to find a tuple3dvalue for key " + key);
                    }
                    
                    
                } catch (Exception e) {
                    logger.error("Caught exception for key: " + key, e);
                }
            }
            
            
        }
        else {
            logger.info("Ignoring state data due to data having zero attributes.  StateData= " + state);
        }
        
       
        
        // $TODO$ nblomberg
        // This data is model dependent.  Depending on the model, the following values will need to be adjusted.
        // We are providing sample configuration which shows that the head position can be computed from frame
        // to frame and feeding that into a model that is expecting the head position to change.
        // compute the distance that the head has moved.
        data[attributes.indexOf(headPosAttr)] = computeHeadDistance();
        data[attributes.indexOf(topSkullAttr)] = 0.0;
        data[attributes.indexOf(headPos6secAttr)] = 0.0;
        data[attributes.indexOf(rtShoulderAttr)] = 0.0;

        //only add a row to the table if at least 1 attribute had data
        if(updated){
            
            table.addDataRow(new DoubleArrayDataRow(data));
            logger.trace("RapidMiner MemoryExampleTable updated: " + table);
            logger.trace("Double Data: " + Arrays.toString(data));
            
        }
        
    }
    
    /**
     * A sample function used to compute the position that the head has changed from one frame to another.
     * This is model dependent and based on the model that is used, this function could be done in the python
     * implementation.
     * @return the position that the head has moved for this frame.
     */
    private double computeHeadDistance() {
        double distance = 0.0;
        
        int numEvents = states.size();
        
        if (numEvents >= 2) {
            
            
            FilteredSensorData sensorDataA = states.get(states.size()-2);
            FilteredSensorData sensorDataB = states.get(states.size()-1);
            
            Tuple3dValue vec1 = (Tuple3dValue)(sensorDataA.getAttributeValues().get(SensorAttributeNameEnum.HEAD));
            Tuple3dValue vec2 = (Tuple3dValue)(sensorDataB.getAttributeValues().get(SensorAttributeNameEnum.HEAD));
            
            
            if (vec1 != null && vec2 != null) {
                double dx = vec1.getX() - vec2.getX();
                double dy = vec1.getY() - vec2.getY();
                double dz = vec1.getZ() - vec2.getZ();
                
                distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                
                logger.trace("Vec 1: " + vec1);
                logger.trace("Vec 2: " + vec2);
                logger.trace("Distance: " + distance);
            }
       
        } else {
            // do nothing, if there are no events, then just return 0.
        }
            
        
        return distance;
        
    }
           

    
    @Override
    public IOContainer getIOContainer(){
        
        synchronized (states) {
       
            
            // create a wrapper that implements the ExampleSet interface and
            // encapsulates your data
            // ...
            
            // Output the exampleset and exampletable (which gives the number of rows in the table).
            logger.trace("ExampleSet = " + exampleSet);
            logger.trace(" MemoryExampleTable = " + exampleSet.getExampleTable());
            
            // $TODO$ nblomberg 
            // This may be model dependent and based on the model expectations, more or less rows may need to be kept.
            // For now we are keeping the last two rows of data in the table.
            while (table.size() > 2) {
                table.removeDataRow(0);
            }
            
            if (exampleSet.getExampleTable().size() > 0) {
                return new IOContainer(exampleSet);
            }
            
            // Return null if there is nothing in the example table to process.
            return null;
        }
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[KinectSensorDataModel: ");
        sb.append("number-of-rows = ").append(table.size());
        sb.append("]");
        return sb.toString();
    }

}
