/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.sensor.MalformedDataException;
import mil.arl.gift.sensor.SensorData;

/**
 * This sensor class interfaces with the Microsoft Band 2 Broadcaster application created for GIFT.  The broadcaster application
 * sends Microsoft Band 2 data streams via UDP network packets.  The band sends (among other things) temperature, accelerometer and 
 * Galvanic skin response (GSR) activity readings.
 * 
 * https://developer.microsoftband.com/content/docs/microsoft%20band%20sdk.pdf
 * 
 * @author mhoffman
 *
 */
public class MicrosoftBandSensor extends AbstractSensor {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MicrosoftBandSensor.class);
    private static boolean isDebug = logger.isDebugEnabled();
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.TEMPERATURE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.GSR_RESISTANCE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.GSR_CONDUCTANCE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ACCELERATION3D, Tuple3dValue.class));
    }
    
    /** how long to wait for a packet to be received during the test sensor connection step */
    private static final int TEST_TIMEOUT_MS = 5000;
    
    /** 
     * how long to wait for a packet during course execution 
     * Note: smaller values are great for checking if the sensor should stop but at the same
     *       time will cause more timeout errors to be reported.
     */
    private static final int READ_WAIT_TIMEOUT_MS = 500;
    
    /**
     * JSON message keys
     */
    private static final String DATA_TYPE_KEY = "type";
    private static final String TIME_KEY = "time";
    private static final String DATA_KEY = "data";
    private static final String ACCELEROMETER_X_KEY = "X";
    private static final String ACCELEROMETER_Y_KEY = "Y";
    private static final String ACCELEROMETER_Z_KEY = "Z";
    
    /**
     * sensor data stream types
     */
    private static final String ACCELEROMETER_DATA_TYPE = "Accelerometer";
    private static final String SKIN_TEMP_DATA_TYPE = "SkinTemperature";
    private static final String GSR_DATA_TYPE = "Resistance";
    private static final String CONTACT_DATA_TYPE = "Contact";
    
    private static final String WORN_CONTACT_VALUE = "Worn";
    @SuppressWarnings("unused")
    private static final String NOT_WORN_CONTACT_VALUE = "NotWorn";
    
    /** holds current sensor values */
    private long dataTime;
    private double temp;  //in Fahrenheit
    private double gsrResistance;
    private boolean isWorn;
    private Vector3d accel = new Vector3d();
    
    /** contains the sensor configuration params */
    private generated.sensor.MicrosoftBandSensor config;
    
    /** used to prevent interruption of sensor thread while it is reading data */
    private Object sensorMutex = new Object();
    
    /** the socket connection */
    private DatagramSocket socket;
    
    /** used for parsing the JSON encoded sensor data */
    private JSONParser parser = new JSONParser();
    
    /** the thread that determines when a new value is calculated */
    private SensorThread sThread = null;
    
    private byte[] buffer = new byte[256];
    
    /**
     * Configure using the default configuration.
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public MicrosoftBandSensor(String sensorName){
        super(sensorName, SensorTypeEnum.MICROSOFT_BAND_2);
        setEventProducerInformation(eventProducerInformation);
    }
    
    /**
     * Class constructor - configure using the sensor configuration input for this sensor
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration parameters to configure this sensor
     */
    public MicrosoftBandSensor(String sensorName, generated.sensor.MicrosoftBandSensor configuration){
        this(sensorName);
        
        this.config = configuration;
    }
    
    private void clearBuffer(){
        Arrays.fill(buffer,(byte)0);
    }

    @Override
    public boolean test() {

        int port = config.getNetworkPort();
        
        try {

            socket = new DatagramSocket(port);
            
            try {
                clearBuffer();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                socket.setSoTimeout(TEST_TIMEOUT_MS);
                
                boolean keepChecking = true;
                do{
                    try{
                        socket.receive(packet);
                        keepChecking = false;
                    }catch(@SuppressWarnings("unused") SocketTimeoutException timeout){
                        createSensorError("Unable to find Microsoft Band Sensor data stream (connection test)");

                        //
                        // Display dialog to get user's attention
                        //
                        int choice = JOptionPaneUtil.showOptionDialog("GIFT is trying to check for the Microsoft Band data stream connection to the Microsoft Band Broadcaster application.\n\n"+
                        "Is the Microsoft Band Broadcaster application running and configured to the network port "+port+"?.\nIf not, please start it.\n\nWhen ready select 'Try Again'.  Selecting 'Terminate' will cause the sensor module to not start.", 
                                "Microsoft Band Sensor Timeout", 
                                JOptionPane.ERROR_MESSAGE, "Try Again", "Terminate", null);

                        if(choice != JOptionPane.OK_OPTION){
                            throw new RuntimeException("Failed to find the Microsoft Band Broadcaster application data stream.");
                        }
                    }
                }while(keepChecking);
                
                //read the data received on the port
                readData(new String(packet.getData()));

            } catch (MalformedDataException e) {
                //the data received is malformed
                createSensorError("Microsoft Band Sensor data is malformed (connection test)");

                throw new DetailedException("Failed to successfully check if the Microsoft Band sensor is running.", 
                        "The data received was malformed.  Are you sure GIFT and the Microsoft Band Broadcaster application are running on the same network port? GIFT is using network port "+port+".", e);
            } catch (RuntimeException e){
                throw e;
                
            } catch (Exception e){
                //the data received contains unhandled values
                createSensorError("Microsoft Band Sensor data contains unhandled data (connection test)");

                throw new DetailedException("Failed to successfully check if the Microsoft Band sensor is running.", 
                        "The data received contained unhandled value(s).", e);
            } finally{
                
                //clean up connection
                socket.close();
                socket.disconnect();
            }
            
        } catch (IOException e) {
            createSensorError("Unable to connect to the network port "+port+" when testing the Microsoft Band Sensor connection (connection test)");
            
            throw new DetailedException("Failed to successfully check if the Microsoft Band sensor is running.", 
                    "There was an exception thrown when trying to connect to the socket on port "+port+
                    ".  Is that network port being used by another process?  Try changing the network port value in the GIFT sensor configuration properties file being used as well as the Microsoft Band Broadcaster application.", e);
        }
        
        createSensorStatus("Microsoft Band Sensor found (connection test)");

        return true;
    }
    
    @Override
    public void start(long domainSessionStartTime) throws Exception{
        
        if(sThread == null || sensorState == SensorStateEnum.STOPPED){
            super.start(domainSessionStartTime);
            
            int port = config.getNetworkPort();
            
            socket = new DatagramSocket(port);
            socket.setSoTimeout(READ_WAIT_TIMEOUT_MS);            
            
            sThread = new SensorThread(getSensorName());
            sThread.start();
            logger.info("Sensor Thread started for "+this);
        }
    }
    
    @Override
    public void stop(){
        
        logger.info("Stopping "+this);
        
        if(sThread != null){
            super.stop();
            sensorState = SensorStateEnum.STOPPED;
            
            synchronized(sensorMutex){
                //stop the thread
                sThread.interrupt();
            }
            
            socket.close();
            socket.disconnect();
            
            logger.info("Sensor Thread stopped for "+this);
        }
    }
    
    /**
     * Parse the raw data string received from the Microsoft Band Broadcaster and set
     * class variables.
     * 
     * @param data the data received from the broadcaster application<p>
     * Format: <br>
     *  {{@link #TIME_KEY} : long, <br>
     *   {@link #DATA_TYPE_KEY} : [{@link #ACCELEROMETER_DATA_TYPE},{@link #SKIN_TEMP_DATA_TYPE},{@link #GSR_DATA_TYPE},{@link #CONTACT_DATA_TYPE}], <br>
     *   {@link #DATA_KEY} : <br>
     *     if {@link #DATA_TYPE_KEY} = {@link #ACCELEROMETER_DATA_TYPE} <br>
     *        then {{@link #ACCELEROMETER_X_KEY} : double, {@link #ACCELEROMETER_Y_KEY} : double, {@link #ACCELEROMETER_Z_KEY} : double} <br>
     *     if {@link #DATA_TYPE_KEY} = {@link #SKIN_TEMP_DATA_TYPE} <br>
     *        then double <br>
     *     if {@link #DATA_TYPE_KEY} = {@link #GSR_DATA_TYPE} <br>
     *        then long <br>
     *     if {@link #DATA_TYPE_KEY} = {@link #CONTACT_DATA_TYPE} <br>
     *        then {{@link #WORN_CONTACT_VALUE}, {@link #NOT_WORN_CONTACT_VALUE}} <br>
     * @throws MalformedDataException if there was a problem converting the data into a JSON object
     * @throws Exception if an unexpected data type or value was found anywhere in the JSON object
     */
    private void readData(String data) throws MalformedDataException, Exception{        
        
        JSONObject jsonObj;
        try{
            jsonObj = (JSONObject) parser.parse(data.trim());
        }catch(ParseException e){
            throw new MalformedDataException("Found malformed Microsoft Band data.",
                    "The band data of '"+data.trim()+"' is not parsable.  The error reads:\n"+e.getMessage(), e);
        }
        
        //epoch
        dataTime = (long)jsonObj.get(TIME_KEY);
        
        String dataType = (String) jsonObj.get(DATA_TYPE_KEY);
        switch(dataType){
        
        case ACCELEROMETER_DATA_TYPE:
            JSONObject dataObj = (JSONObject) jsonObj.get(DATA_KEY);
            accel.x = (double) dataObj.get(ACCELEROMETER_X_KEY);
            accel.y = (double) dataObj.get(ACCELEROMETER_Y_KEY);
            accel.z = (double) dataObj.get(ACCELEROMETER_Z_KEY);
            break;
            
        case SKIN_TEMP_DATA_TYPE:
            //convert Celsius to Fahrenheit
            temp = ((double)jsonObj.get(DATA_KEY) * 9 / 5.0) + 32;
            break;
            
        case GSR_DATA_TYPE:
            //kohms
            gsrResistance = ((Long)jsonObj.get(DATA_KEY)).intValue();
            break;
            
        case CONTACT_DATA_TYPE:
            String contactValue = (String) jsonObj.get(DATA_KEY);
            isWorn = contactValue != null && contactValue.equals(WORN_CONTACT_VALUE);
            break;
            
        default:
            throw new Exception("Found unhandled data type of '"+dataType+"' in band data packet of:\n"+data);

        }
    }
    
    /**
     * Send the sensor data event
     * 
     * @param data contains sensor data to send
     */
    protected void sendDataEvent(SensorData data){
        
        super.sendDataEvent(data);
    }
    
    /**
     * Converts the kilo-ohms resistance value coming from the Microsoft Band 2 into
     * microsiemens.
     * 
     * @param resistanceKOHMS the value to convert
     * @return the microsiemens equivalent, 0.0 if resistance value is 0.0
     */
    private double convertToMicrosiemens(double resistanceKOHMS){
        
        if(resistanceKOHMS == 0.0){
            return 0.0;
        }
        
        double ohms = resistanceKOHMS * 1000.0;
        double siemens = 1.0 / ohms;
        double microsiemens = siemens * 1000000.0;
        return microsiemens;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[MicrosoftBandSensor: ");
        sb.append(super.toString());
        
        if(config != null){
            sb.append("networkPort = ").append(config.getNetworkPort());
        }
        
        sb.append(", isWorn = ").append(isWorn);
        sb.append(", temperature = ").append(temp);
        sb.append(", GSR-Resistance = ").append(gsrResistance);
        sb.append(", Accel = ").append(accel);
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * This thread is responsible for ticking the sensor which retrieves
     * the sensors next value.  The sensor value is then sent via sensor data event.
     * 
     * @author mhoffman
     *
     */
    private class SensorThread extends Thread{
        
        public SensorThread(String threadName){
            super(threadName);
            
        }
        
        @Override
        public void run(){
            
            sensorState = SensorStateEnum.RUNNING;
            
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            long elapsedTime;
            String data = null;
            int sequentialTimeouts = 0, skippedErrorsReportMod = 10;
            while(sensorState == SensorStateEnum.RUNNING){
                
                synchronized(sensorMutex){
                    try {
                        clearBuffer();
                        socket.receive(packet);
                        sequentialTimeouts = 0; //reset
                        data = new String(packet.getData());
                        readData(data);
                        
                        elapsedTime = dataTime - getDomainSessionStartTime();
                        
                        if(isDebug){
                            logger.debug("Read data from Microsoft Band sensor -> temp = "+temp+", resistance = "+gsrResistance);
                        }
                    
                        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();
                        sensorAttributeToValue.put(SensorAttributeNameEnum.ACCELERATION3D, new Tuple3dValue(SensorAttributeNameEnum.ACCELERATION3D, new Point3d(accel.x, accel.y, accel.z)));                    
                        sensorAttributeToValue.put(SensorAttributeNameEnum.TEMPERATURE, new DoubleValue(SensorAttributeNameEnum.TEMPERATURE, temp));
                        sensorAttributeToValue.put(SensorAttributeNameEnum.GSR_RESISTANCE, new DoubleValue(SensorAttributeNameEnum.GSR_RESISTANCE, gsrResistance));
                        sensorAttributeToValue.put(SensorAttributeNameEnum.GSR_CONDUCTANCE, new DoubleValue(SensorAttributeNameEnum.GSR_CONDUCTANCE, convertToMicrosiemens(gsrResistance)));


                        SensorData sensorData = new SensorData(sensorAttributeToValue, elapsedTime);
                        sendDataEvent(sensorData);
    
                    } catch (MalformedDataException e) {
                        // malformed data on this port
                        createSensorError("Microsoft Band Sensor data is malformed (during course)");
                        e.printStackTrace();
                        logger.error("Malformed data", e);
                        
                    } catch(@SuppressWarnings("unused") SocketTimeoutException timeout){
                        //don't care about 1 timeout, maybe external Microsoft Band Broadcaster is down
                        
                        sequentialTimeouts++;
                    } catch (IOException e) {
                        //there was a problem receiving data
                        createSensorError("Microsoft Band Sensor data can't be received on the current port (during course)");
                        e.printStackTrace();
                    } catch (Throwable t){
                        //catch all
                        t.printStackTrace();
                        logger.error("Caught exception while retrieving and handled Microsoft Band data", t);
                    }

                }
                
                if(sequentialTimeouts > 0 && sequentialTimeouts % skippedErrorsReportMod == 0){
                    //increase visibility of error to user
                    logger.warn("Its been a while since the last successful data packet from the Microsoft Band Broadcaster.");
                }
            }
        }
    }

}
