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
import mil.arl.gift.common.sensor.IntegerValue;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.tools.SensorController;
import mil.arl.gift.sensor.tools.SensorControllerInterface;
import mil.arl.gift.sensor.tools.SensorControllerJPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sensor produces temperature and humidity data that changes by a specific rate every tick. It will
 * show two dialogs which allows the user to change the rate thereby affecting the values which are produced by the sensor.
 * 
 * @author mhoffman
 *
 */
public class MouseTempHumiditySurrogateSensor extends AbstractSensor{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MouseTempHumiditySurrogateSensor.class);
    private static boolean isDebugEnabled = logger.isDebugEnabled();
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();        
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.TEMPERATURE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.HUMIDITY, DoubleValue.class));
    }
    
    /** change commands */
    private static final int INCREASE_RATE = 1;
    private static final int DECREASE_RATE = -1;
    
    /** the thread that determines when a new value is calculated */
    private SensorThread sThread = null;
    
    /** the amount to change the rate by */
    private double tempRateChangeAmt = 0.01;
    private double humidityRateChangeAmt = 0.01;
    
    private static final double MIN_VALUE = 0.0;
    private static final double MAX_VALUE = 100.0;
    
    /** the sensors current change in value per tick */
    private Double currentTempRate = 0.0;
    private Double currentHumidityRate = 0.0;
    
    /** the sensors current value */
    private double currentTempValue = Math.random() * 100.0;
    private double currentHumidityValue = Math.random() * 100.0;
    
    private SensorController tempSensorController;
    private SensorController humiditySensorController;
    
    /**
     * Configure using the default configuration.
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public MouseTempHumiditySurrogateSensor(String sensorName){
        super(sensorName, SensorTypeEnum.MOUSE_TH_SURROGATE);
        setEventProducerInformation(eventProducerInformation);
        
        openTemperatureDialog();
        openHumidityDialog();
    }

    /**
     * Class constructor - configure using the sensor configuration input for this sensor
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration parameters to configure this sensor
     */
    public MouseTempHumiditySurrogateSensor(String sensorName, generated.sensor.MouseTempHumiditySurrogateSensor configuration){
        this(sensorName);
        
        setTempRateChangeAmt(configuration.getTemperatureRateChangeAmount().doubleValue());
        setHumidityRateChangeAmt(configuration.getHumidityRateChangeAmount().doubleValue());        
    }
    
    /**
     * Set the humidity rate change amount value
     * 
     * @param value
     */
    private void setHumidityRateChangeAmt(double value){
        
        if(value <= 0){
            throw new IllegalArgumentException("The humidity rate change must be greater than zero");
        }
        
        humidityRateChangeAmt = value;
    }
    
    /**
     * Set the temperature rate change amount value
     * 
     * @param value
     */
    private void setTempRateChangeAmt(double value){
        
        if(value <= 0){
            throw new IllegalArgumentException("The temperature rate change must be greater than zero");
        }
        
        tempRateChangeAmt = value;
    }
    
//    /**
//     * Class constructor 
//     * 
//     * @param sensorName - display name for the sensor
//     * @param sensorInterval - interval in ms to tick the sensor
//     */
//    public MouseTempHumiditySurrogateSensor(String sensorName, int sensorInterval){
//        super(sensorName, sensorInterval, SensorTypeEnum.MOUSE_TH_SURROGATE);
//        
//        setAttributeNames(ATTR_NAMES);
//        openTemperatureDialog();
//        openHumidityDialog();
//    }
    
    /**
     * Show a sensor controller dialog for the temperature attribute
     */
    private void openTemperatureDialog(){
        
        SensorControllerInterface sController = new SensorControllerInterface(){
            
            @Override
            public void modifySensorChangeRate(int rate){
                
                synchronized(currentTempRate){
                    
                    if(rate == INCREASE_RATE){
                        currentTempRate += tempRateChangeAmt;
                    }else if(rate == DECREASE_RATE){
                        currentTempRate -= tempRateChangeAmt;
                    }else{
                        currentTempRate = 0.0;
                    }
                }
            }
        };
        
        tempSensorController = SensorControllerJPanel.showPanel(getSensorName() + ":"+SensorAttributeNameEnum.TEMPERATURE, sController, MIN_VALUE, MAX_VALUE);
    }
    
    /**
     * Show a sensor controller dialog for the humidity attribute
     */
    private void openHumidityDialog(){
        
        SensorControllerInterface sController = new SensorControllerInterface(){
            
            @Override
            public void modifySensorChangeRate(int rate){
                
                synchronized(currentHumidityRate){
                    
                    if(rate == INCREASE_RATE){
                        currentHumidityRate += humidityRateChangeAmt;
                    }else if(rate == DECREASE_RATE){
                        currentHumidityRate -= humidityRateChangeAmt;
                    }else{
                        currentHumidityRate = 0.0;
                    }
                }
            }
        };
        
        humiditySensorController = SensorControllerJPanel.showPanel(getSensorName() + ":"+SensorAttributeNameEnum.HUMIDITY, sController, MIN_VALUE, MAX_VALUE);
    }
    
    @Override
    public boolean test(){
        //nothing to test, after all its a surrogate sensor
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
    
    @Override
    public void dispose(){
        stop();        
        
        if(tempSensorController != null){
            tempSensorController.kill();
        }
        
        if(humiditySensorController != null){
            humiditySensorController.kill();
        }
    }
    
    /**
     * Calculate the next temperature value of the sensor
     * 
     * @param time - current elapsed time (ms)
     * @return double - the next temperature sensor value
     */
    private double nextTemperatureValue(double time){
        
        double value = -1.0;
        
        synchronized(currentTempRate){
            
            value = currentTempValue + currentTempRate;
            
            //bounds check...
            if(value > MAX_VALUE){
                value = MAX_VALUE;
                currentTempRate = 0.0; //reset
            }else if(value < MIN_VALUE){
                value = MIN_VALUE;
                currentTempRate = 0.0; //reset
            }
            
            if(isDebugEnabled){
                logger.info("the next calculated sensor temperature value is "+ value + " based on current rate = "+currentTempRate);
            }
        }
        
        return value;
    }
    
    /**
     * Calculate the next humidity value of the sensor
     * 
     * @param time - current elapsed time (ms)
     * @return double - the next humidity sensor value
     */
    private double nextHumidityValue(double time){
        
        double value = -1.0;
        
        synchronized(currentHumidityRate){
            
            value = currentHumidityValue + currentHumidityRate;
            
            //bounds check...
            if(value > MAX_VALUE){
                value = MAX_VALUE;
                currentHumidityRate = 0.0; //reset
            }else if(value < MIN_VALUE){
                value = MIN_VALUE;
                currentHumidityRate = 0.0; //reset
            }
            
            if(isDebugEnabled){
                logger.info("the next calculated sensor humidity value is "+ value + " based on current rate = "+currentHumidityRate);
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
        tempSensorController.updateSensorValue(data.getSensorAttributeToValue().get(SensorAttributeNameEnum.TEMPERATURE).getNumber().doubleValue());
        humiditySensorController.updateSensorValue(data.getSensorAttributeToValue().get(SensorAttributeNameEnum.HUMIDITY).getNumber().doubleValue());
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[MouseTempHumiditySurrogateSensor: ");
        sb.append(super.toString());
        sb.append(", {temperature: value = ").append(currentTempValue);
        sb.append(", rate = ").append(currentTempRate);
        sb.append(", rate change amount = ").append(tempRateChangeAmt).append("} ");
        sb.append(", {humidity: value = ").append(currentHumidityValue);
        sb.append(", rate = ").append(currentHumidityRate);
        sb.append(", rate change amount = ").append(humidityRateChangeAmt).append("} ");
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
                
                currentTempValue = nextTemperatureValue(elapsedTime/1000.0);
                currentHumidityValue = nextHumidityValue(elapsedTime/1000.0);
                
                Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>();
                //Note: convert to int to mimic actual mouse sensor implementation
                sensorAttributeToValue.put(SensorAttributeNameEnum.TEMPERATURE, new IntegerValue(SensorAttributeNameEnum.TEMPERATURE, (int)currentTempValue));
                sensorAttributeToValue.put(SensorAttributeNameEnum.HUMIDITY, new IntegerValue(SensorAttributeNameEnum.HUMIDITY, (int)currentHumidityValue));
                SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
                sendDataEvent(data);
                //System.out.println("sine: t= "+time/1000.0+ " v= "+value);
                
                try {
                    sleep((int)(interval * 1000));
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    logger.info("Sensor thread interrupted");
                }

            }
        }
        
    }
}
