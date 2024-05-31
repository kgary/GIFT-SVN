/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.python.XMLRPCPythonServerManager;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.net.xmlrpc.XMLRPCClient;
import mil.arl.gift.sensor.AbstractSensorData;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.impl.AbstractSensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter to determine heart rate from an ECG signal.
 * 
 * @author cragusa
 *
 */
public class QrsFromEcgFilter extends AbstractSensorFilter {
	
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    
    static {
    	
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.HEART_RATE, DoubleValue.class));
    }	
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(QrsFromEcgFilter.class);
	
    /**
     * The classname of the class that contains the service.
     */
	private static final String QRS_DETECT_SERVICE_CLASSNAME = "QRSdetection";   
	
	/**
	 * The method name used on the XML RPC server to reference the service.
	 */
	private static final String QRS_DETECT_SERVICE_METHOD_NAME = "QRSdetect";
    
	/**
	 * The port on which to launch the XML RPC server.
	 */
	private static final int QRS_DETECT_SERVICE_PORT = 8000;
	
	/**
	 * A reference to the XML RPC client.
	 */
    private XMLRPCClient client;
    
    /**
     * Counter of failed XML RPC calls used to avoid repetitive printing in log files.
     */
    private int failedRpcCallCount = 0;
    
	/**
	 * Empty constructor. Launches the XML RPC service using the default configuration.
	 *    
	 */
	public QrsFromEcgFilter() {

		try {
			
			XMLRPCPythonServerManager.launchServer(QRS_DETECT_SERVICE_PORT, QRS_DETECT_SERVICE_CLASSNAME);		
			
			client = new XMLRPCClient(XMLRPCPythonServerManager.DEFAULT_SERVER_BASE_URL, QRS_DETECT_SERVICE_PORT);
			
		} catch (@SuppressWarnings("unused") MalformedURLException ex) {
			
			String msg = "Failed to instantiate QrsFromEcgFilter. Unable to establish xml rpc client connection to localhost on port " + QRS_DETECT_SERVICE_PORT + ".";
			logger.error(msg);
			
			throw new RuntimeException(msg);
			
		} catch (IOException e) {
			
			// It's possible that the user does not have Python installed and properly configured on their computer.
			String msg = "Failed to instantiate QrsFromEcgFilter.\nCaught IOException while launching XML RPC Server on port " + QRS_DETECT_SERVICE_PORT;
			msg += "\nThis filter requires Python.  Please verify that it is installed on your computer and you have configured the PATH environment "
					+ "variable appropriately. For help, refer to the GIFT install instructions for guidance on installation of optional software.";
			logger.error(msg, e);
			
			throw new RuntimeException(msg, e);
			
		}
		
		if (client == null) {
			String msg = "Failed to instantiate QrsFromEcgFilter.";
			throw new RuntimeException(msg);
		}
		
	}
	
	@Override
	public void filterSensorFilterData(SensorFilterEvent sensorFilterEvent){
		//nothing to do yet...
	}
	
	@Override
    public void start(long domainSessionStartTime) throws Exception {
		
	    logger.info("Start called for "+this);
	    super.start(domainSessionStartTime);
	}
	
	/**
	 * Method to handle the specific case of incoming ECG Waveform data.
	 * 
	 * @param sensor the sensor sending the data.
	 * @param ecgValue the ecg signal value.
	 * @param elapsedTime the elapsed time of the domain session.
	 */
	private void handleEcgWaveformData(AbstractSensor sensor, double ecgValue, long elapsedTime) {
		
        Vector<Object> params = new Vector<Object>();
        params.addElement(ecgValue);	
                
		Object[] result = null;
		
		StringBuilder errorMsg = new StringBuilder();
		
		result = (Object[]) client.callMethod(QRS_DETECT_SERVICE_METHOD_NAME, params, errorMsg);
		
		if(!errorMsg.toString().isEmpty()){
			
			//log first 10 occurrences and after that only log every 100 occurences to avoid bloating log files.
			boolean logException = (failedRpcCallCount < 10) || (failedRpcCallCount % 100 == 0);
			
			if(logException) {
				
				StringBuffer msg = new StringBuffer(128);
				msg.append("Caught XmlRpcException while calling remote method '").append(QRS_DETECT_SERVICE_METHOD_NAME).append("'. ");
				msg.append("\n ").append(failedRpcCallCount).append(" occurences so far!");				
				logger.error(msg.toString() + " "+ errorMsg);				
			}
			
			failedRpcCallCount++;
		}
		
		if( result != null && result.length > 1 && result[0] != null && !result[0].equals("null")) {
			
			Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();
			
			double heartRate = Double.parseDouble(result[1].toString());
			
			sensorAttributeToValue.put(SensorAttributeNameEnum.HEART_RATE, new DoubleValue(SensorAttributeNameEnum.HEART_RATE, heartRate) );
			
			SensorData sensorData = new SensorData(sensorAttributeToValue, elapsedTime);
			
			handleSensorDataEvent(sensorData, sensor);
			
			if( logger.isDebugEnabled() ) {
				logger.debug("heartrate: " + heartRate);
			}
			
			//XmlRpcService.printResult(result);
		}			
	}
			
	@Override
	public void filterSensorData(SensorDataEvent sensorDataEvent){
				
		AbstractSensorData absData = sensorDataEvent.getData();
		
		if(absData instanceof SensorData) {
			
			SensorData sData = (SensorData)absData;
			
			long elapsedTime = sData.getElapsedTime();
			
			if( sData.getSensorAttributeToValue().containsKey(SensorAttributeNameEnum.ECG_WAVEFORM_SAMPLE) ) {
				
				DoubleValue value = (DoubleValue)sData.getSensorAttributeToValue().get(SensorAttributeNameEnum.ECG_WAVEFORM_SAMPLE);
								
				handleEcgWaveformData(sensorDataEvent.getSensor(), value.getNumber().doubleValue(), elapsedTime);
			}
		}
	}
	
	
	@Override
	public void stop() {
		super.stop();
		XMLRPCPythonServerManager.destroyServiceProcess();
	}
	
	
    @Override
    protected void writerFileCreated(String fileName){
        //SensorModule sModule = SensorManager.getInstance().getProducerSensorModule(this);        
        //TODO: as of now our UMS db doesn't support filter data file referencing
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[QrsFromEcgFilter:");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }

}
