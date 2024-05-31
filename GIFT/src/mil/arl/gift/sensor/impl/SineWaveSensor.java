/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.sensor.SensorData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Sine Wave Sensor is a sensor which produces data according to
 * the sine wave formula.
 * 
 * 		y(t) = A * sin (wt)
 * 
 * 		t = time (seconds)
 * 		A = amplitude, peak deviation of the function from its center position
 * 		w = angular frequency, specifies how many oscillations occur in a unit time interval, in radians per second
 * 
 * @author mhoffman
 *
 */
public class SineWaveSensor extends AbstractSensor {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SineWaveSensor.class);

	/** the peak deviation of the function from its center position*/
	private Double amplitude;
	
	/** time it takes for a duration (seconds) */
	private Double periodDuration;
	
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ENGAGEMENT, DoubleValue.class));
    }
	
	private SensorThread sThread = null;

	/**
	 * Class constructor - configure using the sensor configuration input for this sensor
	 * 
	 * @param sensorName an authored name for this sensor.  Can't be null or empty.
	 * @param configuration parameters to configure this sensor
	 */
	public SineWaveSensor(String sensorName, generated.sensor.SineWaveSensor configuration){
		super(sensorName, SensorTypeEnum.SINE_WAVE);
        setEventProducerInformation(eventProducerInformation);
        
        setAmplitude(configuration.getAmplitude().doubleValue());
        setPeriodDuration(configuration.getPeriod().doubleValue());
	}
	
    @Override
    public boolean test(){
        //nothing to test, after all its a simple sensor
        return true;
    }
	
	@Override
	public void start(long domainSessionStartTime) throws Exception{
		
		if(sThread == null || sensorState == SensorStateEnum.STOPPED){
		    super.start(domainSessionStartTime);
		    
			sThread = new SensorThread(getSensorName(), getSensorInterval());
			sThread.start();
			logger.info("Sensor Thread started for "+this);
		}
	}
	
	@Override
	public void stop(){
		
		if(sThread != null){
		    super.stop();
		    
			sensorState = SensorStateEnum.STOPPED;
			sThread.interrupt();
			logger.info("Sensor Thread stopped for "+this);
		}
	}
	
	/**
     * Set the sine wave period duration (seconds)
     * 
     * @param value
     */
    private void setPeriodDuration(double value){
        
        if(value <= 0){
            throw new IllegalArgumentException("The period duration must be greater than zero");
        }
        
        periodDuration = value;
    }
	
	/**
	 * Set the sine wave amplitude
	 * 
	 * @param value
	 */
	private void setAmplitude(double value){
	    
	    if(value <= 0){
	        throw new IllegalArgumentException("The amplitude must be greater than zero");
	    }
	    
	    amplitude = value;
	}
	
	/**
	 * Calculate the next value of the sensor
	 * 
	 * @param time - current elapsed time (ms)
	 * @return double - the next sensor value
	 */
	private double nextValue(double time){
		
		double value = amplitude * Math.sin((1/periodDuration) * 2 * Math.PI * time);
		
		if(logger.isInfoEnabled()){
			logger.info("the next calculated sensor value is "+ value + " based on amplitude = "+amplitude+", periodDuration = "+periodDuration+" and time = "+time);
		}
		
		return value;
	}
	
	/**
	 * Return the amplitude
	 * 
	 * @return double
	 */
	public double getAmplitude() {
		return amplitude;
	}

	/**
	 * Return the period duration
	 * 
	 * @return double
	 */
	public double getPeriodDuration() {
		return periodDuration;
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[SineWaveSensor: ");
	    sb.append(super.toString());
	    sb.append(", period duration = ").append(getPeriodDuration());
	    sb.append(", amplitude = ").append(getAmplitude());
	    sb.append("]");
		return sb.toString();
	}
	
	/**
	 * This thread is responsible for ticking the sensor which calculates
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
			
                elapsedTime = System.currentTimeMillis() - getDomainSessionStartTime();
				double value = nextValue(elapsedTime/1000.0);
				
				Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>();
				sensorAttributeToValue.put(SensorAttributeNameEnum.ENGAGEMENT, new DoubleValue(SensorAttributeNameEnum.ENGAGEMENT, value));
				SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
				sendDataEvent(data);
				//System.out.println("sine: t= "+time/1000.0+ " v= "+value);
				
	            try {
	        		sleep((int)(interval*1000));
	        	} catch (@SuppressWarnings("unused") InterruptedException e) {
	        		logger.info("SineWaveSensor interrupted");
	        	}

			}
		}
		
	}
}
