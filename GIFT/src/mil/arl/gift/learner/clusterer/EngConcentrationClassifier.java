/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapidminer.tools.XMLException;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorData;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.learner.common.rapidminer.ProcessedKinectSensorDataModel;
import mil.arl.gift.learner.common.rapidminer.RapidMinerInterface;
import mil.arl.gift.learner.common.rapidminer.RapidMinerProcessor;

/**
 * This classifier is responsible for classifying the short term learner state for EngConcentration attribute.
 * 
 * @author nblomberg
 *
 */
public class EngConcentrationClassifier extends AbstractClassifier {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EngConcentrationClassifier.class);
    
    RapidMinerProcessor kinectRapidMiner;
    
    // This key must match the rapidminer model.
    private static final String ENGAGED_KEY = "confidence(True)";
    
    /** the learner's current state */
    private LearnerStateAttribute learnerstate;
    
    public EngConcentrationClassifier() throws IOException, XMLException{

        kinectRapidMiner = new RapidMinerProcessor(RapidMinerInterface.ENG_CONCENTRATION_PROCESS_FILE, LearnerStateAttributeNameEnum.ENG_CONCENTRATION, 
                                                        new ProcessedKinectSensorDataModel(), ENGAGED_KEY); 
    }
    
    
    @Override
    public boolean updateState(AbstractSensorData data){
        
        boolean updated = false;
        
        if(data instanceof FilteredSensorData){
           
            FilteredSensorData filterData = (FilteredSensorData)data;
            
            if (filterData.getSensorType() == SensorTypeEnum.KINECT) {
                updated = kinectRapidMiner.updateState(filterData);
                learnerstate = (LearnerStateAttribute)kinectRapidMiner.getState();
                
            } else if (filterData.getSensorType() == SensorTypeEnum.Q){
                // $TODO$ nblomberg
                // Add support for Q sensor here.
            } else {
                logger.error("Unhandled sensor data for sensor of type: " + filterData.getSensorType());
            }
            
            logger.trace("FilteredSensorData = " + filterData);
        }
        
        return updated;
    }
    
    @Override
    public Object getState() {
        
        return learnerstate;
    }

    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return kinectRapidMiner.getAttribute();
    }

    @Override
    public Object getCurrentData() {
        // TODO Auto-generated method stub
        return null;
    }
    
    

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EngConcentrationClassifier:");
        sb.append(" current state = ").append(getState());
        sb.append("]");
        return sb.toString();
    }
}
