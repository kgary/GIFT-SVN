package mil.arl.gift.lms.impl.lrs.xapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts.extensionObjectKeys;

/**
 * Utility class for handling event time within xAPI Statements.
 *  
 * @author Yet Analytics
 *
 */
public class TimestampHelper {
    
    /**
     * Convert from epoch time to DateTime
     * 
     * @param epochTs - epoch time to convert
     * 
     * @return DateTime representation of epoch time
     */
    public static DateTime fromEpoch(Long epochTs) {
        return new DateTime(epochTs);
    }
    
    /**
     * Creates map from predicted, shortTerm and longTerm epoch times
     * 
     * @param predicted - predicted epoch time from performance or learner state attribute
     * @param shortTerm - shortTerm epoch time from performance or learner state attribute
     * @param longTerm - longTerm epoch time from performance or learner state attribute
     * 
     * @return map of epoch times
     */
    private static Map<String, Long> createPredictionsMap(Long predicted, Long shortTerm, Long longTerm) {
        Map<String, Long> predictions = new HashMap<String, Long>();
        predictions.put(extensionObjectKeys.PREDICTED.getValue(), predicted);
        predictions.put(extensionObjectKeys.SHORT_TERM.getValue(), shortTerm);
        predictions.put(extensionObjectKeys.LONG_TERM.getValue(), longTerm);
        return predictions;
    }
    
    /**
     * determines which epoch time is the most recent
     * 
     * @param predictions - map of predicted, shortTerm and longTerm epoch times
     * 
     * @return map entry with most recent epoch time
     */
    private static Entry<String, Long> mostRecentPrediction(Map<String, Long> predictions) {
        List<Entry<String, Long>> coll = new ArrayList<>(predictions.entrySet());
        coll.sort(Entry.comparingByValue());
        return coll.get(coll.size() - 1);
    }
    
    /**
     * Given a Learner State Attribute, determine which assessment has the most recent epoch time
     *  
     * @param attribute - Learner State Attribute containing shortTerm, longTerm and predicted epoch times
     * 
     * @return map entry of 'shortTerm', 'longTerm' or 'predicted' and associated epoch time
     */
    public static Entry<String, Long> mostRecentPrediction(LearnerStateAttribute attribute) {
        return mostRecentPrediction(createPredictionsMap(attribute.getPredictedTimestamp(), attribute.getShortTermTimestamp(), attribute.getLongTermTimestamp()));
    }
    
    /**
     * Given a Performance State Attribute, determine which assessment has the most recent epoch time
     * 
     * @param state - Performance State Attribute containing shortTerm, longTerm and predicted epoch times
     * 
     * @return map entry of 'shortTerm', 'longTerm' or 'predicted' and associated epoch time
     */
    public static Entry<String, Long> mostRecentPrediction(PerformanceStateAttribute state) {
        return mostRecentPrediction(createPredictionsMap(state.getPredictedTimestamp(), state.getShortTermTimestamp(), state.getLongTermTimestamp()));
    }
}
