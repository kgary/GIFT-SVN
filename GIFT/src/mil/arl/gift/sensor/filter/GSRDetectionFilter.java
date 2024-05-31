/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.sensor.AbstractSensorData;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataCollection;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.impl.AbstractSensor;

/**
 * Ths GSR Detection filter class was designed to duplicate the process found in "The Design and Analysis of a Real-Time, Continuous
 * Arousal Monitor".  They do this by smoothing, normalizing and feature extracting.
 * 
 * Note: the following algorithm was translated from GSRdetection.py (Author: Keith Brawner)
 * 
 * @author mhoffman
 *
 */
public class GSRDetectionFilter extends AbstractSensorFilter {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GSRDetectionFilter.class);
    
    /** default configuration parameters */
    private static final int DEFAULT_SAMPLING_RATE  = 250;
    private static final float DEFAULT_WINDOW_SIZE_SEC = 0.3f;
    private static final int DEFAULT_USER_REACTION_WINDOW_SIZE = 3000;
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.GSR_MEAN, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.GSR_STD, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.GSR_FEATURE, DoubleValue.class));
    }
    
    /** 
     * the setting for how many points should be taken for smoothing.  This should be about 300 ms 
     * (75 samples @ 250 Hz, 150 samples @ 500 Hz).  The 300 ms time is taken from the research literature, 
     * and should be kept around that value. 
     * 
     * Note: this value is calculated based on the user set sampling rate
     */
    private float smoothingWindowSize;
    
    /**
     * is of 3-10 seconds (data points vary based on sampling rate).  
     * This corresponds to how long it takes a user reaction to be observed in the raw GSR data.  
     * If the experimenter/instructor is looking for short-term responses (strong emotional responses like fear/stress) it 
     * makes more sense for the use of 3 seconds, as it allows for quicker response.  
     * If the experimenter/instructor is doing something like PowerPoint training, it makes more sense to use a longer 
     * sample such as 10 seconds (looking for gradual changes in stress).
     */
    private int windowSize;
    
    /** the sampling rate for the sensor data stream */
    private int samplingRate;
    
    /** collections of values used to calculate the various filter attribute values */
    private List<Double> values;
    private List<Double> norm;
    private List<Double> smoothed;
    private List<Double> firstDifference;
    private List<Double> secondDifference;
    private List<Double> featureSignal;
    
    private Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> filterAttributes;

    /** the last mean value calculated */
    private double lastMean = 0;
    
    /**
     * Empty constructor
     */
    public GSRDetectionFilter(){
    
        setWindowSize(DEFAULT_USER_REACTION_WINDOW_SIZE);     
        setSamplingRate(DEFAULT_SAMPLING_RATE);
        setSmootingWindowSize(samplingRate * DEFAULT_WINDOW_SIZE_SEC);
        
        init();
    }
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param configuration - contains filter configuration values
     */
    public GSRDetectionFilter(generated.sensor.GSRDetectionFilterInput configuration){
        super();
        
        setSamplingRate(configuration.getSamplingRateHz().intValue());
        setWindowSize((int) (samplingRate * configuration.getWindowSize()));
        setSmootingWindowSize(samplingRate * DEFAULT_WINDOW_SIZE_SEC);
        
        init();
    }
    
    private void setSamplingRate(int value){
        
        if(value <= 0){
            throw new IllegalArgumentException("The sampling rate must be greater than zero");
        }
        
        samplingRate = value;
    }
    
    private void setWindowSize(int value){
        
        if(value <= 0){
            throw new IllegalArgumentException("The window size must be greater than zero");
        }
        
        windowSize = value;
    }
    
    private void setSmootingWindowSize(float value){
        
        if(value < 1){
            //this constraint is due too the windowedEnergy method's for loop conditions
            throw new IllegalArgumentException("The smoothing window size must be greater than or equal to one");
        }
        
        smoothingWindowSize = value;
    }
    
    /**
     * Initialize the filter
     */
    private void init(){
        
        values = new ArrayList<>(windowSize);
        smoothed = new ArrayList<>(windowSize);
        norm = new ArrayList<>(windowSize);
        firstDifference = new ArrayList<>(windowSize);
        secondDifference = new ArrayList<>(windowSize);
        featureSignal = new ArrayList<>(windowSize);
        
        filterAttributes = new HashMap<>();
        
        setEventProducerInformation(eventProducerInformation);
    }
    
    /**
     * Calculate the cumulative moving average for the list of values using the provided value as the latest value in
     * the series.
     * 
     * @param currentValue
     * @return double - the new mean
     */
    private double calculateMean(double currentValue){
        
        double mean;
        double diff;  //how much the total of all elements is changing by after adding the incoming value
        
        //cumulative moving average
        
        if(values.size() == (windowSize+1)){
            //list of values is at capacity, shift elements left
            //Note: assumes new value was added before this mean calculation
            
            Double oldValue = values.remove(0);
            diff = currentValue - oldValue; 
            mean = (diff + (values.size() * lastMean)) / values.size();
            
        }else{
//            diff = currentValue;
            mean = lastMean + ((currentValue - lastMean)/values.size());
        }
        
//        if(values.size() > 1){
//            mean = (diff + (values.size() * lastMean)) / values.size();
////            mean = lastMean + ((currentValue - lastMean)/values.size());
//        }else{
//            mean = currentValue;
//        }
        
        lastMean = mean;

        return mean;
    }
    
    /**
     * Return the standard deviation for the collection of raw values
     * 
     * @return double
     */
    private double calculateStd(){
        
        double deviation = 0;
        
        if(!values.isEmpty()){
            
            for(double value : values){
                deviation += (value - lastMean) * (value - lastMean);
            }
            
            deviation /= values.size();
        }
        
        return Math.sqrt(deviation);
    }
    
    /**
     * Return a collection of smoothed raw values
     * 
     * @return List<Double>
     */
    private List<Double> calculateSmoothed(){
        
        smoothed.clear();
        double curVal = 0;
        for(double value : values){
            curVal = 1.0/smoothingWindowSize*value + ((smoothingWindowSize-1)/smoothingWindowSize) * curVal;
            smoothed.add(curVal);
        }
        
        return smoothed;
    }
    
    /**
     * Return a collection of normalized smoothed values.
     * 
     * @param smoothedValues - collection of smoothed values to normalize
     * @param std - the standard deviation of the smoothed values
     * @return List<Double>
     */
    private List<Double> calculateNormalized(List<Double> smoothedValues, double std){
        
        norm.clear();
        double curVal = 0;
        for(double value : smoothedValues){
            curVal = (value - lastMean)/std;
            norm.add(curVal);
        }
        
        return norm;
    }
    
    /**
     * Returns the average signal energy of the second difference signal
     */
    private double feature1(List<Double> normalizedValues){
        
        derivative(normalizedValues, firstDifference);
        derivative(firstDifference, secondDifference);
        List<Double> featureSignal = windowedEnergy(secondDifference);
        
        double avg = 0.0;
        for(double value : featureSignal){
            avg += value;
        }
        
        return avg/featureSignal.size();
    }
    
    /**
     * output is 1/8*sampling of 4 terms:
     *  -1x(n-2)        1
     *  -2x(n-1)        2
     *  +2x(n+1)        3
     *  +x(n+2)         4
     *  
     * @param normalizedValues
     * @return
     */
    private List<Double> derivative(List<Double> normalizedValues, List<Double> deriviative){
        
        double term1, term2, term3, term4;
        
        deriviative.clear();        
        for(int i = 0; i < normalizedValues.size(); i++){
            
            //reset
            term1 = 0;
            term2 = 0;
            term3 = 0;
            term4 = 0;
            
            if(i > 1){
                term1 = -1.0*normalizedValues.get(i-2);
            }
            
            if(i > 0){
                term2 = -2.0*normalizedValues.get(i-1);
            }
            
            if((i+1) < normalizedValues.size()){
                term3 = 2.0*normalizedValues.get(i+1);
            }
            
            if((i+2) < normalizedValues.size()){
                term4 = normalizedValues.get(i+2);
            }
            
            deriviative.add(1.0/8.0*samplingRate*(term1+term2+term3+term4));
        }
        
        return deriviative;
        
    }
    
    /**
     * Returns the sum of the squares of the window
     * 
     * @param secondDeriviative
     * @return
     */
    private List<Double> windowedEnergy(List<Double> secondDeriviative){
        
        double value;
        
        featureSignal.clear();
        for(int i = 0; i < secondDeriviative.size(); i++){
            value = 0;
            for(int n = (i-(int)smoothingWindowSize); n < i; n++){
                
                if(n >= 0){
                    value += secondDeriviative.get(n) * secondDeriviative.get(n);
                }
            }
            
            featureSignal.add(Math.sqrt(value));
        }
        
        return featureSignal;
    }
    
    /**
     * Filter the latest sensor data value and populate the filter attributes collection with updated values for this filter.
     * 
     * @param newValue
     * @param filterAttributes
     * @return boolean - whether the data caused a need for a filter event to be created
     */
    private boolean filterData(double newValue,  Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> filterAttributes){
        
        //add new element at end of list
        values.add(newValue);
        
        lastMean = calculateMean(newValue);
        
        if(values.size() < windowSize){
            //haven't collected enough sensor data to send out a filtered sensor data event
            return false;
        }        
        
        double std = calculateStd();
        
        List<Double> smoothedValues = calculateSmoothed();
        
        //Debug
//        printList(smoothedValues);
        
        List<Double> normalizedValues = calculateNormalized(smoothedValues, std);
        double feature = feature1(normalizedValues);
        
        filterAttributes.put(SensorAttributeNameEnum.GSR_MEAN, new DoubleValue(SensorAttributeNameEnum.GSR_MEAN, lastMean));
        filterAttributes.put(SensorAttributeNameEnum.GSR_STD, new DoubleValue(SensorAttributeNameEnum.GSR_STD, std));
        filterAttributes.put(SensorAttributeNameEnum.GSR_FEATURE, new DoubleValue(SensorAttributeNameEnum.GSR_FEATURE, feature));
        
        return true;
    }
    
    /**
     * Filter the latest sensor data attributes.
     * 
     * @param sensor - the sensor producing the sensor values
     * @param sensorData - data from the sensor
     */
    private void filterData(AbstractSensor sensor, SensorData sensorData){
        
        filterAttributes.clear();
        
        //TODO: need better way to match sensor data attributes to filter attributes
        for(SensorAttributeNameEnum attribute : sensorData.getSensorAttributeToValue().keySet()){
            
            if(attribute == SensorAttributeNameEnum.EDA || attribute == SensorAttributeNameEnum.GSR){
                
                if(filterData(sensorData.getSensorAttributeToValue().get(attribute).getNumber().doubleValue(), filterAttributes)){
                
                    //
                    //send calculated mean, std and feature values
                    //
                    filterAttributes.putAll(sensorData.getSensorAttributeToValue());
                    SensorData sData = new SensorData(filterAttributes, sensorData.getElapsedTime());
                    handleSensorDataEvent(sData, sensor);
                }
            }
        }        

    }
    

    @Override
    void filterSensorData(SensorDataEvent sensorDataEvent) {
       
        AbstractSensor sensor = sensorDataEvent.getSensor();
        
        if(logger.isInfoEnabled()){
            logger.info("Received sensor data event to filter and create a message for - " + sensorDataEvent);
        }
        
        //update time last sent
//        timeLastSent = sensorDataEvent.getData().getElapsedTime();
        
        //add data to values
        AbstractSensorData sData = sensorDataEvent.getData();
        if(sData instanceof SensorDataCollection){
            
            for(SensorData sensorData : ((SensorDataCollection)sData).getSensorDatas()){
                filterData(sensor, sensorData);
            }
            
        }else if(sData instanceof SensorData){
            filterData(sensor, (SensorData) sData);
        }else{
            logger.error("Unable to handle sensor data event of type "+sData);
            return;
        }

    }

    @Override
    void filterSensorFilterData(SensorFilterEvent sensorFilterEvent) {
      //nothing to do yet...
    }

    @Override
    protected void writerFileCreated(String fileName) {
        //TODO: as of now our UMS db doesn't support filter data file referencing
    }
    
    @SuppressWarnings("unused")
    private void printList(List<Double> data){
        
        for(int i = 0; i < data.size(); i++){
            System.out.print(data.get(i));
            
            if((i+1) < data.size()){
                System.out.print(", ");
            }
        }
        System.out.println("\n");
    }

}
