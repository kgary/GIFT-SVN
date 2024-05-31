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
import mil.arl.gift.sensor.tools.SensorController;
import mil.arl.gift.sensor.tools.SensorControllerInterface;
import mil.arl.gift.sensor.tools.SensorControllerJPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sensor produces data that changes by a specific rate every tick. It will
 * show a dialog which allows the user to change the rate thereby affecting the
 * values which are produced by the sensor.
 * 
 * @author mhoffman
 *
 */
public class SelfAssessmentSensor extends AbstractSensor{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SelfAssessmentSensor.class);
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ENGAGEMENT, DoubleValue.class));
    }
    
    /** change commands */
    private static final int INCREASE_RATE = 1;
    private static final int DECREASE_RATE = -1;
    
    /** the thread that determines when a new value is calculated */
    private SensorThread sThread = null;
    
    /** the amount to change the rate by */
    private double rateChangeAmt = 0.01;
    
    private static final double MIN_VALUE = 0.0;
    private static final double MAX_VALUE = 1.0;
    
    /** the sensors current change in value per tick */
    private Double currentRate = 0.0;
    
    /** the sensors current value */
    private double currentValue = Math.random();
    
    private SensorController sensorController;
    
    /**
     * Configure using the default configuration.
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public SelfAssessmentSensor(String sensorName){
        super(sensorName, SensorTypeEnum.SELF_ASSESSMENT);
        setEventProducerInformation(eventProducerInformation);
    }
    
    /**
     * Class constructor - configure using the sensor configuration input for this sensor
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration parameters to configure this sensor
     */
    public SelfAssessmentSensor(String sensorName, generated.sensor.SelfAssessmentSensor configuration){
        this(sensorName);
        
        setRateChangeAmt(configuration.getRateChangeAmount().doubleValue());
    }
    
    /**
     * Set the rate change amount value
     * 
     * @param value
     */
    private void setRateChangeAmt(double value){
        
        if(value <= 0){
            throw new IllegalArgumentException("The rate change must be greater than zero");
        }
        
        rateChangeAmt = value;
    }
    
    @Override
    public boolean test(){
        openDialog();
        
        return true;
    }
    
    /**
     * Show the sensor controller dialog
     */
    private void openDialog(){
        
        SensorControllerInterface sController = new SensorControllerInterface(){
            
            @Override
            public void modifySensorChangeRate(int rate){
                
                synchronized(currentRate){
                    
                    if(rate == INCREASE_RATE){
                        currentRate += rateChangeAmt;
                    }else if(rate == DECREASE_RATE){
                        currentRate -= rateChangeAmt;
                    }else{
                        currentRate = 0.0;
                    }
                }
            }
        };
        
        sensorController = SensorControllerJPanel.showPanel(getSensorName(), sController, MIN_VALUE, MAX_VALUE);
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
    
    @Override
    public void dispose(){
        stop();        
        
        if(sensorController != null){
            sensorController.kill();
        }
    }
    
    /**
     * Calculate the next value of the sensor
     * 
     * @param time - current elapsed time (ms)
     * @return double - the next sensor value
     */
    private double nextValue(double time){
        
        double value = -1.0;
        
        synchronized(currentRate){
            
            value = currentValue + currentRate;
            
            //bounds check...
            if(value > MAX_VALUE){
                value = MAX_VALUE;
                currentRate = 0.0; //reset
            }else if(value < MIN_VALUE){
                value = MIN_VALUE;
                currentRate = 0.0; //reset
            }
            
            if(logger.isInfoEnabled()){
                logger.info("the next calculated sensor value is "+ value + " based on current rate = "+currentRate);
            }
        }
        
        return value;
    }
    
    /**
     * Send the sensor data event
     * 
     * @param data contains sensor data to send
     */
    protected void sendDataEvent(SensorData data){
        
        super.sendDataEvent(data);
        
        //update dialog's current value
        sensorController.updateSensorValue(data.getSensorAttributeToValue().get(SensorAttributeNameEnum.ENGAGEMENT).getNumber().doubleValue());
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SelfAssessmentSensor: ");
        sb.append(super.toString());
        sb.append(", value = ").append(currentValue);
        sb.append(", rate = ").append(currentRate);
        sb.append(", rate change amount = ").append(rateChangeAmt);
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
                currentValue = nextValue(elapsedTime/1000.0);
                
                Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>();
                sensorAttributeToValue.put(SensorAttributeNameEnum.ENGAGEMENT, new DoubleValue(SensorAttributeNameEnum.ENGAGEMENT, currentValue));
                SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
                sendDataEvent(data);
                //System.out.println("sine: t= "+time/1000.0+ " v= "+value);
                
                try {
                    sleep((int)(interval * 1000));
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    logger.info("SelfAssessmentSensor interrupted");
                }

            }
        }
        
    }
}
