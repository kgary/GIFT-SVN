/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.IntegerValue;
import mil.arl.gift.sensor.SensorData;

/**
 * This sensor class interfaces with the mouse hardware that contains the temperature and humidity sensors.
 * The sensors are attached to an Arduino board which translates the data and writes it to a serial port.
 * 
 * @author mhoffman
 *
 */
public class MouseTempHumiditySensor extends AbstractSensor{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MouseTempHumiditySensor.class);
    private static boolean isDebug = logger.isDebugEnabled();
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();        
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.TEMPERATURE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.HUMIDITY, DoubleValue.class));
    }
    
    /** amount of time to wait for a connection to open */
    private static final int TIMEOUTSECONDS = 2;
    
    /** the arduino data transfer rate */
    private static final int BAUD = 9600;
    
    /** the data request command that is sent to the arduino */
    private static final String DATA_REQUEST = "r";
    
    /** the delimiter used between data elements in a single set of data from the arduino */
    private static final String DELIM = ",";
    
    /** indexes of sensor attributes in a string split by the delimiter */
    private static final int TEMP_INDEX = 0;
    private static final int HUMIDITY_INDEX = 1;
    
    /** the thread that determines when a new value is calculated */
    private SensorThread sThread = null;
    
    /** the serial port the arduino board is attached too */
    private SerialPort mouseSerialPort;
    
    /** holds current sensor values */
    private Integer temp;
    private Integer humidity;
    
    /** used to prevent interruption of sensor thread while it is reading data */
    private Object sensorMutex = new Object();
    
    /**
     * Configure using the default configuration.
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public MouseTempHumiditySensor(String sensorName){
        super(sensorName, SensorTypeEnum.MOUSE_TH);
        setEventProducerInformation(eventProducerInformation);
    }

    /**
     * Class constructor - configure using the sensor configuration input for this sensor
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration parameters to configure this sensor
     */
    public MouseTempHumiditySensor(String sensorName, generated.sensor.MouseTempHumiditySensor configuration){
        this(sensorName);
    }
    
    @Override
    public boolean test(){
        
        if(!findMouse()){
            createSensorStatus("Unable to find mouse");
            throw new RuntimeException("Unable to find mouse.  Make sure you are not plugging the mouse into a USB hub but rather directly into the computer's USB ports.");
        }
        
        closeSerialPort();
        
        return true;
    }
    
    
    /**
     * Read the temp and humidity data coming from the mouse
     * 
     * @return boolean - whether the mouse data was successfully read
     */
    private boolean readData(){
    	
    	boolean success = false;
    	
        try {
        	if(mouseSerialPort != null){
        	    
        	    int read = 0;
                byte[] buffer = new byte[9];
        	    synchronized (mouseSerialPort){
        		
                    //request mouse data
                    mouseSerialPort.getOutputStream().write(DATA_REQUEST.getBytes());

                    //NOTE: not sure why but (sometimes) the read returns with 0 bytes read, maybe there is a terminator character in the buffer?
                    read = mouseSerialPort.getInputStream().read(buffer);
                    
                    //if read was empty, get the real data
                    read = read <= 0 ? mouseSerialPort.getInputStream().read(buffer) : read;
                    
        	    }
                
                if(read > 0){
                    String data = new String(buffer);
                    //System.out.println("BUFF=>"+data+"->EOL");
                    data = data.trim();
                    data = data.replace("\n", "");
                    data = data.replace("\r", "");
                    String[] tokens = data.split(DELIM);
                    
                    if(tokens.length == 2){

                    	temp = Integer.parseInt(tokens[TEMP_INDEX]);
                    	humidity = Integer.parseInt(tokens[HUMIDITY_INDEX]);
                    	success = true;                    	
                    	
                    	//DEBUG
                    	//System.out.println(this);
                    	
                    }else{
                    	logger.error("Read malformed data from the mouse: "+data);
                    }
                }else{
                	logger.error("The mouse data stream exists but nothing was read from the mouse");
                }
        	}else{
        		logger.error("The mouse data stream has not been created, therefore no data will be read from the mouse");
        	}
        	
        } catch (IOException e) {
            logger.error("Caught exception while trying to read input stream", e);
        }
        
        return success;
    }
    
    /**
     * Search the available communication ports (i.e. Serial Ports) for the mouse sensor
     * 
     * @return boolean - whether the mouse was found
     */
    private boolean findMouse(){
        
        boolean found = false;
        
        StringBuffer searchResults = new StringBuffer();
        
        try{
            
            logger.info("Searching for mouse...");
        	
            // get list of ports available on this particular computer,
            Enumeration<?> pList = CommPortIdentifier.getPortIdentifiers(); 
            
            searchResults.append("Are there any open comm ports? ").append((pList.hasMoreElements() ? "yes":"no"));

            // Process the list, putting serial and parallel into ComboBoxes
            while (pList.hasMoreElements()) {

                CommPortIdentifier cpi = (CommPortIdentifier)pList.nextElement();

                SerialPort serialPort = null;
                try{
                	serialPort = (SerialPort)cpi.open(this.getClass().getName(), TIMEOUTSECONDS * 1000);
                }catch(PortInUseException e){
                	logger.warn("Port "+cpi+" is in use.  Try the next port.", e);
                	continue;
                }
                
                // set up the serial port
                serialPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                
                //NOTE: Originally I had a timeout of 1000 (probably from someone's source code), then I experimented and use 800
                //Update: 800 works fine on first connect, but found that using the mouse more than once in a single user session
                //        caused the reads below to not read anything unless this value was 1000.
                serialPort.enableReceiveTimeout(1000);
                serialPort.enableReceiveThreshold(9);

                //NOTE: after connecting to the serial port, we need to wait 1-1.5 seconds before writing
                //http://www.arduino.cc/playground/Interfacing/Java
                Thread.sleep(1500);
                
                //request mouse data
                serialPort.getOutputStream().write(DATA_REQUEST.getBytes());
                
                try {
                    byte[] byteBuffer = new byte[9];
                    
                    searchResults.append("\nchecking serial port ").append(cpi.getName());
                    logger.info("checking serial port "+cpi.getName());
                    
                    //NOTE: not sure why but (sometimes) the first read always returns with 0 bytes read, maybe there is a terminator character in the buffer?
                    int read = serialPort.getInputStream().read(byteBuffer);
                    
                    //if read was empty, get the real data
                    read = read <= 0 ? serialPort.getInputStream().read(byteBuffer) : read;
                    
                    if(read > 0){
                        String data = new String(byteBuffer);
                        data.trim();
                        data = data.replace("\n", "");
                        data = data.replace("\r", "");
                        String[] tokens = data.split(DELIM);
                        
                        if(tokens.length == 2){
                        	found = true;
                        	logger.info("Found mouse on port: "+cpi.getName());
                            //System.out.println("found mouse on port: "+cpi.getName());
                            mouseSerialPort = serialPort;
                            break;
                        }                        
                        
                        searchResults.append("\nincorrect data = ").append(data).append(" read from serial port ").append(cpi.getName());
                        logger.info("incorrect data = "+data+" read from serial port "+cpi.getName());
                        
                    }else{
                        searchResults.append("\nnothing found on serial port ").append(serialPort.getName());
                        logger.info("nothing found on serial port "+serialPort.getName());
                    }
                } catch (IOException e) {
                    logger.error("Can't open input stream for communication port named "+cpi.getName(), e);
                }
            }
            
        }catch(Exception e){
            logger.error("Caught exception while trying to find mouse", e);
        }catch (UnsatisfiedLinkError e){
            //this is known to be thrown when getting port identifiers
            logger.error("Caught UnsatisfiedLinkError while trying to find mouse", e);
        }
        
        if(!found){
            logger.error("Mouse not found - Search results are: "+searchResults);
            
            //TODO: send sensor status like the QSensor does at this point
            
            //TODO: present dialog to ask if user wants to check again, giving them time to connect the mouse sensor
        }

        return found;
    }

    
    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms.
     */
    private synchronized void closeSerialPort() {
    	
        if (mouseSerialPort != null) {
            
            synchronized(mouseSerialPort){
                mouseSerialPort.removeEventListener();
                mouseSerialPort.close();
            }
        	
        	logger.info("Mouse serial port has been closed");
        }
    }
    
    @Override
    public void start(long domainSessionStartTime) throws Exception{
        
        if(sThread == null || sensorState == SensorStateEnum.STOPPED){
            super.start(domainSessionStartTime);
            
            if(findMouse()){
            	sThread = new SensorThread(getSensorName(), getSensorInterval());
            	sThread.start();
            	logger.info("Sensor Thread started for "+this);
            }else{
                createSensorStatus("Unable to find mouse");
            	logger.error("Unable to open the mouse serial port, therefore the sensor thread will not be started");
            }
        }
    }
    
    @Override
    public void stop(){
        
        if(sThread != null){
            super.stop();
            sensorState = SensorStateEnum.STOPPED;
            
            synchronized(sensorMutex){
                //stop the thread
                sThread.interrupt();
            }
            
            closeSerialPort();
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
        sb.append("[MouseTempHumiditySensor: ");
        sb.append(super.toString());
        sb.append(", temperature = ").append(temp);
        sb.append(", humidity = ").append(humidity);
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
            
            long elapsedTime;
            while(sensorState == SensorStateEnum.RUNNING){
                            
                synchronized(sensorMutex){
                    try{
                        
                        if(readData()){  
                            
                            // NOTE: decided to capture the time after the data has been collected from the hardware
                            elapsedTime = System.currentTimeMillis() - getDomainSessionStartTime();
                            
                            if(isDebug){
                                logger.debug("Read data from mouse -> temp = "+temp+", humidity = "+humidity);
                            }
                        
                            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>();
                            sensorAttributeToValue.put(SensorAttributeNameEnum.TEMPERATURE, new IntegerValue(SensorAttributeNameEnum.TEMPERATURE, temp));
                            sensorAttributeToValue.put(SensorAttributeNameEnum.HUMIDITY, new IntegerValue(SensorAttributeNameEnum.HUMIDITY, humidity));
                            SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
                            sendDataEvent(data);
                        }else{
                            createSensorStatus("Had an issue reading data from mouse");
                        }
                        
                    }catch(Exception e){
                        logger.error("Caught exception while trying to read and send sensor data", e);
                    }                
                }
                
                try {
                    sleep((int)(interval * 1000));
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    logger.info("Sensor thread interrupted");
                }

            }
        }
        
    }
}
