/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl.os3d;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.impl.os3d.InertialLabsInterface.ReceiveDataCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;

/**
 * This sensor class interfaces with the Inertial Labs OS3D-GS orientation
 * sensor hardware.
 * 
 * @author ohasan
 */
public class OS3DSensor extends AbstractSensor {

    /**
     * Reference to the Logger object.
     */
    private static Logger logger = LoggerFactory.getLogger(OS3DSensor.class);
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
//        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OS3D_FRAME_COUNTER, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OS3D_RAW_ACC, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OS3D_RAW_GYR, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OS3D_RAW_MAG, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OS3D_CAL_ACC, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OS3D_CAL_GYR, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OS3D_CAL_MAG, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.QUAT_X, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.QUAT_Y, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.QUAT_Z, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.QUAT_W, DoubleValue.class));
    }

    /**
     * The ID of the OS3D sensor device currently in use.
     */
    int deviceID;

    /**
     * Receives the data callback from the sensor and creates and sends an
     * internal sensor data event containing the received data.
     */
    ReceiveDataCallback userCallback = new ReceiveDataCallback() {

        short counter;
        double batteryCharge;
        FullDataEntry[] dataEntryArray;
        long elapsedTime;
        double[] rawAccArray, rawGyrArray, rawMagArray;
        double[] calAccArray, calGyrArray, calMagArray;
        double quatX, quatY, quatZ, quatW;

        @Override
        public void apply(DataFrame pData, Pointer pUserData) {

            counter = pData.wCounter;
            batteryCharge = pData.dBatteryCharge;
            dataEntryArray = pData.DataEntry;

            logger.info("Received callback: counter: " + counter
                    + " batteryCharge: " + batteryCharge);

            // only use the first element of the DataEntryArrry since we are
            // using sensor 0
            // Get the Raw data
            DataRawEntry dataRawEntry = dataEntryArray[0].Raw;

            // read the raw data from memory into the DataRawEntry structure
            dataRawEntry.read();

            rawAccArray = dataRawEntry.field1.dAcc;
            rawGyrArray = dataRawEntry.field1.dGyr;
            rawMagArray = dataRawEntry.field1.dMag;

            DataCalibratedEntry dataCalibratedEntry = dataEntryArray[0].Calibrated;

            // read the calibrated data from memory into the DataRawEntry
            // structure
            dataCalibratedEntry.read();

            calAccArray = dataCalibratedEntry.field1.dCalibratedAcc;
            calGyrArray = dataCalibratedEntry.field1.dCalibratedGyr;
            calMagArray = dataCalibratedEntry.field1.dCalibratedMag;

            DataQuatEntry dataQuatEntry = dataEntryArray[0].Quat;

            // read the quat data from memory into the DataRawEntry structure
            dataQuatEntry.read();

            quatX = dataQuatEntry.field1.dX;
            quatY = dataQuatEntry.field1.dY;
            quatZ = dataQuatEntry.field1.dZ;
            quatW = dataQuatEntry.field1.dW;

            elapsedTime = System.currentTimeMillis()
                    - getDomainSessionStartTime();

            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();
            // sensorAttributeToValue.put(
            // SensorAttributeNameEnum.OS3D_FRAME_COUNTER,
            // new DoubleValue(SensorAttributeNameEnum.OS3D_FRAME_COUNTER,
            // pData.wCounter));

            // Raw
            sensorAttributeToValue.put(SensorAttributeNameEnum.OS3D_RAW_ACC,
                    new Tuple3dValue(SensorAttributeNameEnum.OS3D_RAW_ACC, new Point3d(rawAccArray[0], rawAccArray[1], rawAccArray[2])));
            
            sensorAttributeToValue.put(SensorAttributeNameEnum.OS3D_RAW_GYR,
                    new Tuple3dValue(SensorAttributeNameEnum.OS3D_RAW_GYR, new Point3d(rawGyrArray[0], rawGyrArray[1], rawGyrArray[2])));
            
            sensorAttributeToValue.put(SensorAttributeNameEnum.OS3D_RAW_MAG,
                    new Tuple3dValue(SensorAttributeNameEnum.OS3D_RAW_MAG, new Point3d(rawMagArray[0], rawMagArray[1], rawMagArray[2])));

            // Calibrated
            sensorAttributeToValue.put(SensorAttributeNameEnum.OS3D_CAL_ACC,
                    new Tuple3dValue(SensorAttributeNameEnum.OS3D_CAL_ACC, new Point3d(calAccArray[0], calAccArray[1], calAccArray[2])));
            
            sensorAttributeToValue.put(SensorAttributeNameEnum.OS3D_CAL_GYR,
                    new Tuple3dValue(SensorAttributeNameEnum.OS3D_CAL_GYR, new Point3d(calGyrArray[0], calGyrArray[1], calGyrArray[2])));
            
            sensorAttributeToValue.put(SensorAttributeNameEnum.OS3D_CAL_MAG,
                    new Tuple3dValue(SensorAttributeNameEnum.OS3D_CAL_MAG, new Point3d(calMagArray[0], calMagArray[1], calMagArray[2])));

            // Quat
            sensorAttributeToValue
                    .put(SensorAttributeNameEnum.QUAT_X, new DoubleValue(
                            SensorAttributeNameEnum.QUAT_X, quatX));
            sensorAttributeToValue
                    .put(SensorAttributeNameEnum.QUAT_Y, new DoubleValue(
                            SensorAttributeNameEnum.QUAT_Y, quatY));
            sensorAttributeToValue
                    .put(SensorAttributeNameEnum.QUAT_Z, new DoubleValue(
                            SensorAttributeNameEnum.QUAT_Z, quatZ));
            sensorAttributeToValue
                    .put(SensorAttributeNameEnum.QUAT_W, new DoubleValue(
                            SensorAttributeNameEnum.QUAT_W, quatW));

            // create and send a new SensorData event
            SensorData data = new SensorData(sensorAttributeToValue,
                    elapsedTime);
            sendDataEvent(data);
        }
    };

    /**
     * Configure using the default configuration.
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public OS3DSensor(String sensorName) {
        super(sensorName, SensorTypeEnum.OS3D);
        setEventProducerInformation(eventProducerInformation);

        logger.info("Created OS3DSensor");

        int version = InertialLabsInterface.INSTANCE
                .iInertialLabs_SDK_Lite_GetVersion();

        logger.info("Version: " + version);
    }

    /**
     * Class constructor - configure using the sensor configuration input for
     * this sensor
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration parameters to configure this sensor
     */
    public OS3DSensor(String sensorName, generated.sensor.OS3DSensor configuration) {

        this(sensorName);
    }

    @Override
    public boolean test() {

        logger.info("Testing creation of the OS3D device");

        // create a new device - use WorksetNode 2 as defined in the
        // Inertial_Labs_SDK_Lite.ini configuration file since it is
        // set up for a wireless gun - make sure the COM port defined
        // in that workset matches what is being used on the local machine
        deviceID = InertialLabsInterface.INSTANCE
                .iInertialLabs_SDK_Lite_CreateDevice(2, null, 0, 0);

        logger.info("Device ID: " + deviceID);
        
        if (deviceID < 0) {

            createSensorError("Unable to connect to OS3D sensor");
            throw new RuntimeException("Unable to connect to OS3D ensor");
        }

        logger.info("Closing device: " + deviceID);

        InertialLabsInterface.INSTANCE
                .iInertialLabs_SDK_Lite_CloseDevice(deviceID);
        
        return true;
    }

    @Override
    public void start(long domainSessionStartTime) throws Exception {

        super.start(domainSessionStartTime);

        logger.info("Creating device");

        // create a new device - use WorksetNode 2 as defined in the
        // Inertial_Labs_SDK_Lite.ini configuration file since it is
        // set up for a wireless gun - make sure the COM port defined
        // in that workset matches what is being used on the local machine
        deviceID = InertialLabsInterface.INSTANCE
                .iInertialLabs_SDK_Lite_CreateDevice(2, null, 0, 0);

        logger.info("Device ID: " + deviceID);

        logger.info("Setting up listener to receive device callback");

        InertialLabsInterface.INSTANCE
                .iInertialLabs_SDK_Lite_SetReceiveDataCallback(deviceID,
                        userCallback, null);

        // wrap this in a property - we probably don't want to generate the
        // sensor's normal output file unless we want to verify the data sent to
        // the GIFT sensor log

        // InertialLabsInterface.INSTANCE.iInertialLabs_SDK_Lite_EnableDump(
        // deviceID,
        // (byte) 1,
        // "OS3D_InData_"
        // + TimeUtil.formatTimeLogFilename(System
        // .currentTimeMillis()) + ".txt",
        // "OS3D_OutData_"
        // + TimeUtil.formatTimeLogFilename(System
        // .currentTimeMillis()) + ".txt");
    }

    @Override
    public void stop() {

        super.stop();
        sensorState = SensorStateEnum.STOPPED;

        logger.info("Closing device: " + deviceID);

        InertialLabsInterface.INSTANCE
                .iInertialLabs_SDK_Lite_CloseDevice(deviceID);
    }

    @Override
    public String toString() {

        return "OS3DSensor: deviceID: " + deviceID;
    }
}
