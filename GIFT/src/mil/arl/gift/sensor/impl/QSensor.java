/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.swing.JOptionPane;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.tools.ModuleUserInterfaceEventListener;

/**
 * This sensor class interfaces with the Q sensor hardware that contains temperature, accelerometer and 
 * electrodermal activity readings.
 * 
 * @author mhoffman
 *
 */
public class QSensor extends AbstractSensor{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(QSensor.class);
    private static boolean isDebug = logger.isDebugEnabled();
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.TEMPERATURE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EDA, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ACCELERATION3D, Tuple3dValue.class));
    }
    
    /** used to find the Affectiva Q sensor bluetooth remote device by name comparison */
    private static final String SEARCH_NAME = "AffectivaQ";
 
    /** the delimiter used between data elements in a single set of data from the sensor */
    private static final String DELIM = ",";
    private static final String CARRIAGE_RETURN = "\r";
    private static final String NEWLINE = "\n";
    private static final String BLANK = "";
    
    /** indexes of sensor attributes in a string split by the delimiter */
    private static final int ACCEL_Z_INDEX = 1;
    private static final int ACCEL_Y_INDEX = 2;
    private static final int ACCEL_X_INDEX = 3;
    private static final int TEMP_INDEX = 5;
    private static final int EDA_INDEX = 6;
    //PACKET DATA STREAM
    //0-9*          Gs          Volts     C      uS
    //Packet #,  Z,   Y,   X,   Battery, Temp,    EDA
    //EXAMPLE: 0,0.39,0.12,0.86,3.55,32.9,0.068\r\n
    
    /** the thread that determines when a new value is calculated */
    private SensorThread sThread = null;
    
    /** holds current sensor values */
    private double temp;
    private double eda;
    private Vector3d accel = new Vector3d();
    
    /** used to prevent interruption of sensor thread while it is reading data */
    private Object sensorMutex = new Object();
    
    /** contains all the bluetooth devices discovered */
    private Vector<RemoteDevice> devicesDiscovered = new Vector<RemoteDevice>();
    
    /** contains all the services found for the Q Sensor device (if found) */
    private Vector<ServiceRecord> services = new Vector<ServiceRecord>();
    
    //http://www.bluecove.org/bluecove/apidocs/javax/bluetooth/UUID.html
    //Serial Port   0x1101
    private static final UUID[] UUIDS = new UUID[] {new UUID(0x1101)};
    
    /** the input stream of the Q Sensor bluetooth connection */
    private DataInputStream dis;

    /**
     * Configure using the default configuration.
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public QSensor(String sensorName){
        super(sensorName, SensorTypeEnum.Q);
        setEventProducerInformation(eventProducerInformation);
    }
    
    /**
     *  Class constructor - configure using the sensor configuration input for this sensor
     *  
     *  @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration parameters to configure this sensor
     */
    public QSensor(String sensorName, generated.sensor.QSensor configuration){
        this(sensorName);
    }
    
    @Override
    public boolean test(){
        
        if(!findQSensor()){
            createSensorStatus("Unable to find Q Sensor");
            throw new RuntimeException("Unable to find Q Sensor");
        }
        
        return true;
    }    
    
    /**
     * Read the data coming from the Q Sensor
     * 
     * @return string - error message if the Q Sensor data was not successfully read, otherwise null
     */
    private String readData(BufferedReader br) throws Exception{

    	String msg = null;

    	try {
    		if(br != null){

    			//check sensorState so I don't get caught in this loop after stopped
    			while( !br.ready() && sensorState == SensorStateEnum.RUNNING) {
    				try {
    					Thread.sleep(1);
    				}
    				catch(@SuppressWarnings("unused") InterruptedException ex) {    					
    					 logger.info("Sensor thread interrupted");
    				}
    			}
    			
    			String data = br.readLine();
    			if(data != null && data.length() > 0){
    				//System.out.println("BUFF=>"+data+"->EOL");
    				data = data.trim();
    				data = data.replace(NEWLINE, BLANK);
    				data = data.replace(CARRIAGE_RETURN, BLANK);
    				String[] tokens = data.split(DELIM);

    				if(tokens.length == 7){

    					accel.x = Double.parseDouble(tokens[ACCEL_X_INDEX]);
    					accel.y = Double.parseDouble(tokens[ACCEL_Y_INDEX]);
    					accel.z = Double.parseDouble(tokens[ACCEL_Z_INDEX]);
    					temp = Double.parseDouble(tokens[TEMP_INDEX]);
    					eda = Double.parseDouble(tokens[EDA_INDEX]);                 	

    					//DEBUG
    					//System.out.println(this);

    				}else{
    					msg ="Read malformed data from the Q Sensor: "+data; 
    				}
    			}else{
    				msg = "There was a problem reading Q Sensor data - the data stream exists but nothing was read from it";
    			}		

    		}else{
    			msg = "The Q Sensor data stream has not been created, therefore no data will be read from the Q Sensor";
    		}

    	} catch (IOException e) {

    		logger.error("Caught exception while trying to read input stream", e);
    		msg = "There was a problem reading Q Sensor data because an expection was detected";
    	}

    	return msg;
    }

    /**
     * Search for the Q Sensor connection
     * 
     * @return boolean - true iff the connection was successfully opened to the Q Sensor
     */
    private boolean findQSensor(){
        
        boolean found = false;
//        System.out.println("\nbluecove stack = "+LocalDevice.getProperty("bluecove.stack"));
        
        final Object inquiryCompletedEvent = new Object();
        
        //check if the data stream is already initialized
        if(dis != null){
            try {
                if(dis.available() > 0){
                    return true;
                }
            } catch (IOException e) {
                logger.error("An exception occurred while trying to open the Q sensor connection after already configured.", e);

                String message = "Failed to display message to user using the sensor module's method of delivery.\n\n" +
                        "The message is:\n\n" +
                        "'An exception occurred while trying to open the Q sensor connection after already configured.'\n\n" +
                        "Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.";
                displayMessageToUser(message, null);
            }            
        }
        
        //
        // Display dialog to get user's attention
        //
        JOptionPane.showMessageDialog(null, 
                "The Q Sensor needs your attention.\n" +
                "\nIf your running GIFT in power user mode, please open the command prompt window, otherwise\n" +
                "select OK on this dialog and interact with the sensor module user interface (when presented).", 
                "Q Sensor", 
                JOptionPane.INFORMATION_MESSAGE);
        

        //
        // Start new search
        //
        devicesDiscovered.clear();
        services.clear();

        //setup the device listener used for finding devices and their services
        DiscoveryListener listener = new DiscoveryListener() {

            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                displayMessageToUser("Device " + btDevice.getBluetoothAddress() + " found", null);
                devicesDiscovered.addElement(btDevice);
                try {
                    displayMessageToUser("     name " + btDevice.getFriendlyName(false), null);
                } catch (IOException exception) {
                    logger.error("Caught exception while trying to retrieve the friendly name of a device that was discovered.", exception);
                    displayMessageToUser("Caught exception while trying to retrieve the friendly name of a device that was discovered.. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
                }
            }

            @Override
            public void inquiryCompleted(int discType) {
                displayMessageToUser("Device Inquiry completed!", null);

                //Release the wait on waiting for devices
                synchronized(inquiryCompletedEvent){
                    inquiryCompletedEvent.notifyAll();
                }
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                displayMessageToUser("Device Service Search completed!", null);
                
                switch(respCode) {
                    case DiscoveryListener.SERVICE_SEARCH_COMPLETED:

                        if(services.isEmpty()){
                            displayMessageToUser("The service was not found", null);
                        }

                    break;
                    case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
                        displayMessageToUser("Device not Reachable", null);
                    break;
                    case DiscoveryListener.SERVICE_SEARCH_ERROR:
                        displayMessageToUser("Service serch error", null);
                    break;
                    case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
                        displayMessageToUser("No records returned", null);
                    break;
                    case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
                        displayMessageToUser("Inqury Cancled", null);
                    break;
                    default:
                        displayMessageToUser("Received unhandled response code of "+respCode, null);
                }

                //Release the wait on searching for services
                synchronized(inquiryCompletedEvent){
                    inquiryCompletedEvent.notifyAll();
                }
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                
                for (int x = 0; x < servRecord.length; x++ ){
//                    System.out.println("Found service: "+servRecord[x]);
                    services.addElement(servRecord[x]);
                }

            }
        };

        synchronized(inquiryCompletedEvent) {
            boolean started = false;
            try {
                LocalDevice localDevice = LocalDevice.getLocalDevice();
//                boolean ispoweron = LocalDevice.isPowerOn();
                started = localDevice.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
            } catch (BluetoothStateException e) {
                logger.error("Caught exception while starting to search for devices", e);
                displayMessageToUser("Exception caught while starting to search for devices. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
            }
            if (started) {
                displayMessageToUser("\n *** Please press the sync button on the Q Sensor *** ", null);
                displayMessageToUser(" (HINT: the Q Sensor is ready to be synced when the blue light\n\tflashes several times followed by a single green flash of light)", null);
                displayMessageToUser("searching for Q Sensor, please wait ...\n", null);
                try {
                    inquiryCompletedEvent.wait();
                } catch (InterruptedException e) {
                    logger.error("Caught exception while searching for Q sensor.", e);
                    displayMessageToUser("Exception while searching for Q Sensor. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
                }
                displayMessageToUser("\n" + devicesDiscovered.size() +  " device(s) found", null);
            }
        }
        
        String name;
        for(RemoteDevice device : devicesDiscovered){
                        
            try {
                name = device.getFriendlyName(false);
                displayMessageToUser("Looking at "+name, null);
                
                if(name != null && name.contains(SEARCH_NAME)){
                    
                    displayMessageToUser("Found Q Sensor, please wait while searching for services...", null);
                    
                    synchronized(inquiryCompletedEvent) {
                        LocalDevice localDevice = LocalDevice.getLocalDevice();
                        localDevice.getDiscoveryAgent().searchServices(null, UUIDS, device, listener);
                        try {
                            inquiryCompletedEvent.wait();
                        } catch (InterruptedException e) {
                            logger.error("Caught exception caught while waiting for Q Sensor services to be found", e);
                            displayMessageToUser("Exception caught while waiting for Q sensor services to be found. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
                        }
                    }
                    
                    displayMessageToUser("Found "+services.size()+" services", null);
                    if(!services.isEmpty()){
                        StreamConnection conn = null;
                        ServiceRecord service = services.get(0);
                        String url = service.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                        
                        try {
                            //establish the connection
                            conn = (StreamConnection) Connector.open(url);       
                            dis = conn.openDataInputStream();
                            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
                            String errorMsg = readData(br);
                            found = errorMsg == null;
                            
                            if(errorMsg == null){                                
                                displayMessageToUser("Q Sensor connection opened\n", null);
                            }else{
                                createSensorStatus("While searching for an active Q Sensor, there was an error when reading from device named "+name+": "+errorMsg);
                            }
                            
//                            conn.close();
                        } catch (Exception e) {
                            logger.error("Caught exception caught while trying to open the Q Sensor connection", e);
                            displayMessageToUser("Exception caught while trying to open the Q sensor connection. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
                        }
                    }

                    if(found){
                        //found active Q sensor, don't need to look at any other device
                        break;
                    }
                    
                }else{
                    displayMessageToUser(name + " is not a Q Sensor.", null);
                }
                
            } catch (IOException e) {
                logger.error("Caught exception caught while looking at the found device(s)", e);
                displayMessageToUser("Exception caught while looking at the found device(s). Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
            }

        }//end for
        
        //
        //Allow the user to search again before the sensor module fails to initialize
        //
        if(!found){
            
            createSensorStatus("Unable to find Q Sensor.  Are you properly synching the device when the sensor module requests?");
            
            displayMessageToUser("\nTry again? (y): ", new ModuleUserInterfaceEventListener() {
                
                @Override
                public void textEntered(String text) {
                    setUserInput(text);                    
                }

                @Override
                public void errorOccurred(String message) {
                    displayMessageToUser("There was a problem reading your response: "+message, null); 
                    
                    //make sure the thread-wait continues...
                    setUserInput(null);
                }
            });
            
            //wait for the user's response (from either command prompt window or module user interface frame)
            try {
                
                synchronized(userInputMonitor){
                    logger.info("Waiting for user input...");
                    userInputMonitor.wait();
                }
                logger.info("User input is now '"+userInput+"'.");
                
            } catch (InterruptedException e) {
                logger.error("Caught exception while waiting for your response.", e);
                displayMessageToUser("Exception caught while waiting for your response. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
            }
            
            if("y".equals(userInput)){                
                found = findQSensor();
            }

        }
        
        logger.info("Returning found value of "+found+".");
        
        return found;
    }
    
    @Override
    public void start(long domainSessionStartTime) throws Exception{
        
        if(sThread == null || sensorState == SensorStateEnum.STOPPED){
            super.start(domainSessionStartTime);
            
            if(findQSensor()){
            	sThread = new SensorThread(getSensorName(), getSensorInterval());
            	sThread.start();
            	logger.info("Sensor Thread started for "+this);
            }else{
                createSensorStatus("Unable to find Q Sensor");
            	logger.error("Unable to open the Q Sensor serial port, therefore the sensor thread will not be started");
            }
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
            
            logger.info("Sensor Thread stopped for "+this);
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
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[QSensor: ");
        sb.append(super.toString());
        sb.append(", temperature = ").append(temp);
        sb.append(", EDA = ").append(eda);
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
        
        private double interval;
        
        public SensorThread(String threadName, double interval){
            super(threadName);
            
            this.interval = interval;
        }
        
        @Override
        public void run(){
            
            sensorState = SensorStateEnum.RUNNING;
            
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            
            String errorMsg, prevErrorMsg = null;
            long elapsedTime = 0, lastErrorTime = Long.MIN_VALUE, waitBtwSameErrors = 1000;
            int skippedSameErrorsCnt = 0, skippedErrorsReportMod = 100;
            while(sensorState == SensorStateEnum.RUNNING){
                
                synchronized(sensorMutex){
                    try{
                        
                        errorMsg = readData(br);
                        if(errorMsg == null){  
                            
                            //reset
                            prevErrorMsg = null;
                            skippedSameErrorsCnt = 0;
                            
                            // NOTE: decided to capture the time after the data has been collected from the hardware
                            elapsedTime = System.currentTimeMillis() - getDomainSessionStartTime();
                            
                            if(isDebug){
                                logger.debug("Read data from Q sensor -> temp = "+temp+", EDA = "+eda);
                            }
                        
                            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();
                            sensorAttributeToValue.put(SensorAttributeNameEnum.TEMPERATURE, new DoubleValue(SensorAttributeNameEnum.TEMPERATURE, temp));
                            sensorAttributeToValue.put(SensorAttributeNameEnum.EDA, new DoubleValue(SensorAttributeNameEnum.EDA, eda));
                            sensorAttributeToValue.put(SensorAttributeNameEnum.ACCELERATION3D, new Tuple3dValue(SensorAttributeNameEnum.ACCELERATION3D, new Point3d(accel.x, accel.y, accel.z)));

                            SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
                            sendDataEvent(data);
                            
                        }else{
                            //handle error message
                            
                            if(!errorMsg.equals(prevErrorMsg) || (elapsedTime - lastErrorTime) > waitBtwSameErrors){
                                logger.error("Received error message while trying to read data: "+errorMsg);
                                
                                prevErrorMsg = errorMsg;
                                lastErrorTime = elapsedTime;
                                
                                //send error over network...
                                createSensorStatus(errorMsg);     
                                
                            } else if(errorMsg.equals(prevErrorMsg)){
                                //increment skip count
                                
                                skippedSameErrorsCnt++;
                                if(skippedSameErrorsCnt % skippedErrorsReportMod == 0){
                                    logger.error("So far there have been "+skippedSameErrorsCnt+" error messages skipped stating: "+prevErrorMsg);
                                }
                            }
                            
                        }
                        
                    }catch(Exception e){
                        logger.error("Caught exception while trying to read and send sensor data", e);
                        createSensorStatus("There was a problem reading/sending Q Sensor data");
                    }                
                }
                
                try {
                    sleep((int)(interval * SECONDS_TO_MILLISECONDS));
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    logger.info("Sensor thread interrupted");
                }

            }
        }
        
    }
}
