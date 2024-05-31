/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.test;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.learner.common.rapidminer.KinectSensorDataModel;
import mil.arl.gift.learner.common.rapidminer.RapidMinerInterface;
import mil.arl.gift.learner.common.rapidminer.RapidMinerInterface.RapidMinerProcess;

/**
 * Tests the RapidMiner interface by running a process with made up with sample kinect sensor input data.
 * 
 * @author nblomberg
 *
 */
public class RapidMinerInterfaceKinectSensorTest {

    
    // This outputkey must match the rapidminer model.
    private static final String OUTPUT_KEY = "confidence(True)";
    private static final String SENSOR_FILTERNAME = "KenectSensorFilter";
    private static final String SENSOR_NAME = "KenectSensor";
    private static final SensorTypeEnum SENSOR_TYPE = SensorTypeEnum.KINECT;
    
    
    /**
     * Runs a test of the RapidMiner API by creating input that is given to a
     * RapidMiner process.  That process outputs a result that is then displayed
     * through system.out.
     * 
     * @param args not used
     */
    public static void main(String[] args){
        
        System.out.println("Running RapidMiner API test...\n");
        
        try{
            //
            // Build a data model - we build 2 states of kinect sensor data.  In this case, we are sending only 
            // the head, left hand, right hand, and top skull positions over two samples/states.
            //
            KinectSensorDataModel dataModel = new KinectSensorDataModel();
            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributesA = new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();
            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributesB = new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();
            
            
            Point3d headPosA = new Point3d(0.0012940169544890523, 0.3112492859363556, 1.3098469972610474);
            Point3d ltHandPosA = new Point3d(-0.20270100235939026, -0.18375800549983978, 0.9185894131660461);
            Point3d rtHandPosA = new Point3d(0.2586025893688202, -0.24581649899482727, 1.0696860551834106);
            Point3d topSkullPosA = new Point3d(0.02102920040488243, 0.37593820691108704, 1.2407180070877075);
                     
            
            sensorAttributesA.put(SensorAttributeNameEnum.HEAD, new Tuple3dValue(SensorAttributeNameEnum.HEAD, headPosA));
            sensorAttributesA.put(SensorAttributeNameEnum.LEFT_HAND,  new Tuple3dValue(SensorAttributeNameEnum.LEFT_HAND, ltHandPosA));
            sensorAttributesA.put(SensorAttributeNameEnum.RIGHT_HAND,  new Tuple3dValue(SensorAttributeNameEnum.RIGHT_HAND, rtHandPosA));
            sensorAttributesA.put(SensorAttributeNameEnum.TOP_SKULL,  new Tuple3dValue(SensorAttributeNameEnum.TOP_SKULL, topSkullPosA));
            
            // Create the filtered sensor data object for our 1st state.            
            FilteredSensorData stateA = new FilteredSensorData(SENSOR_FILTERNAME, SENSOR_NAME, SENSOR_TYPE, 0, sensorAttributesA);
            dataModel.addState(stateA);
            
            Point3d headPosB = new Point3d(0.02021039091050625, 0.29525020718574524, 1.247689962387085);
            Point3d ltHandPosB = new Point3d(-0.15324179828166962, -0.23272240161895752, 0.9249749183654785);
            Point3d rtHandPosB = new Point3d(0.2419515997171402, -0.26706650853157043, 1.0284940004348755);
            Point3d topSkullPosB = new Point3d(0.055026501417160034, 0.37294670939445496, 1.2055319547653198);
          
            sensorAttributesB.put(SensorAttributeNameEnum.HEAD, new Tuple3dValue(SensorAttributeNameEnum.HEAD, headPosB));
            sensorAttributesB.put(SensorAttributeNameEnum.LEFT_HAND,  new Tuple3dValue(SensorAttributeNameEnum.LEFT_HAND, ltHandPosB));
            sensorAttributesB.put(SensorAttributeNameEnum.RIGHT_HAND,  new Tuple3dValue(SensorAttributeNameEnum.RIGHT_HAND, rtHandPosB));
            sensorAttributesB.put(SensorAttributeNameEnum.TOP_SKULL,  new Tuple3dValue(SensorAttributeNameEnum.TOP_SKULL, topSkullPosB));
          
            // Create the filtered sensor data object for the 2nd state.
            FilteredSensorData stateB = new FilteredSensorData(SENSOR_FILTERNAME, SENSOR_NAME, SENSOR_TYPE, 0, sensorAttributesB);
            dataModel.addState(stateB);
            
            RapidMinerProcess ENGAGED_PROCESS = new RapidMinerProcess(RapidMinerInterface.ENG_CONCENTRATION_PROCESS_FILE, OUTPUT_KEY);
                 
            //
            // Apply the RapidMiner process to the data
            //
            double anxiousConfidence = ENGAGED_PROCESS.runProcess(dataModel.getIOContainer());
            
            if(anxiousConfidence > 0.5){
                System.out.println("ENGAGED = HIGH, "+anxiousConfidence);
            }else{
                System.out.println("ENGAGED = LOW, "+anxiousConfidence);
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println("\nFinished.");
    }
    
}
