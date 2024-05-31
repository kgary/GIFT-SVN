/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.io.IOException;

import com.rapidminer.tools.XMLException;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.learner.common.rapidminer.ProcessedTC3DataModel;
import mil.arl.gift.learner.common.rapidminer.RapidMinerInterface;
import mil.arl.gift.learner.common.rapidminer.RapidMinerProcessor;

/**
 * This classifier is responsible for classifying the short term learner state for Anxious attribute.
 * 
 * @author mhoffman
 *
 */
public class AnxiousClassifier extends AbstractClassifier {
       
    RapidMinerProcessor tc3RapidMiner;
    
    // This key must match the rapidminer model.
    private static final String CONFIDENCE_KEY = "confidence(1)";
    
    /** the learner's current state */
    private LearnerStateAttribute learnerState;
    
    public AnxiousClassifier() throws IOException, XMLException{
        tc3RapidMiner = new RapidMinerProcessor(RapidMinerInterface.ANXIOUS_PROCESS_FILE, LearnerStateAttributeNameEnum.ANXIOUS, 
                                                       new ProcessedTC3DataModel(), CONFIDENCE_KEY);
    }
    
    
    @Override 
    public boolean updateState(TrainingAppState trainingAppState){
        
        boolean updated = false;
        updated = tc3RapidMiner.updateState(trainingAppState);
        
        learnerState = (LearnerStateAttribute) tc3RapidMiner.getState();
        
        return updated;       
    }
    
    @Override
    public Object getState() {
        return learnerState;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AnxiousClassifier:");
        sb.append(" current state = ").append(getState());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return tc3RapidMiner.getAttribute();
    }

    @Override
    public Object getCurrentData() {
        // TODO Auto-generated method stub
        return null;
    }
}
