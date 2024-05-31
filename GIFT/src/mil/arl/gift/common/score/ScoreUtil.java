/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.score;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * This class contains util methods for GIFT score nodes.
 * 
 * @author mhoffman
 *
 */
public class ScoreUtil {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ScoreUtil.class);
    
    /*
     * The below variables are based on similar variables that already existed in
     * mil.arl.gift.domain.knowledge.common.metric.grade.DefaultGradeMetric. 
     * 
     * If the variables in DefaultGradeMetric change or if the logic surrounding them
     * substantially changes, then these variables should be modified as well to 
     * remain consistent.
     */
    
    /** arbitrary point values to give overall assessments in a single condition in order to calculate the condition's average assessment */
    public static final double ABOVE_EXPECTATION_SCORE = 4;  //equivalent to A grade
    public static final int AT_EXPECTATION_SCORE = 2;  //equivalent to C grade
    public static final int BELOW_EXPECTATION_SCORE = 0; //equivalent to F grade
    
    /** the average score must be under a 2.0 to get a Below expectation result (e.g. F to C- grade) */
    public static final double BELOW_EXPECTATION_UPPER_THRESHOLD = 1.7; 
    
    /** 
     * the average score must be under a 3.33 (and greater than or equal to 2.0) to get an At expectation result (e.g. C to B+ grade) 
     * This infers that greater than or equal to 3.33 is Above Expectation
     * */
    public static final double AT_EXPECTATION_UPPER_THRESHOLD = 3.33; 
    
    /*
     * The perfomAssessmentRollup method below mimics some of the behavior of the assessment 
     * rollup rules in mil.arl.gift.domain.knowledge.common.metric.grade.DefaultGradeMetric. 
     * 
     * If the these rules are changed in DefaultGradeMetric, then this method should be modified
     * to reflect those changes so that it's assessment rollup logic remains consistent.
     */
    /**
     * Calculates and sets the assessment of one or more GradedScoreNodes starting with the provided rootNode
     * and returns a mapping containing the rolled-up assessment levels of all the performance nodes
     * found within the given root node. Determined this way, the assessment level of a given node is 
     * strictly a function of the leaf nodes beneath it. Additionally, if a node contains children with
     * multiple different assessment levels, then the average of those assessments will be used as the 
     * assessment level of the parent. Unknown or null assessments are ignored by this calculation.<br/>
     * <br/>
     * IMPORTANT: This method DOES NOT use the grades set via the setGrade method! <br/>
     *            It simply does the roll-up from the child nodes.<br/>
     * <br/>           
     *            Rollup of of GradedScoreNodes with no children will always result in an assessment of
     *            UNKNOWN for said nodes.<br/>
     * 
     * @param rootNode the node to start from. If null, the returned assessments will be empty.
     * @param recursiveRollup whether to visit any child GradedScoreNodes of this rootNode and also calculate then
     * set the assessment level of that child based on its descendants.  Values:</br>
     * 1. false = only calculate then set the assessment level of the provided rootNode<br/>
     * 2. true = calculate then set the assessment level of all GradedScoreNodes starting from this rootNode then all descendants<br/>
     * 3. null = indicate that this method call is not the first but a recursive call and to keep gathering the mapping of descendants
     * but don't change the assessments of any GradedScoreNodes. 
     * @return a mapping from each performance node ID that was found to its rolled-up assessment level.
     * Will not be null, but can be empty if the root node contains no performance nodes.
     */
    public static Map<Integer, AssessmentLevelEnum> performAssessmentRollup(GradedScoreNode rootNode, Boolean recursiveRollup) {
        
        Map<Integer, AssessmentLevelEnum> assessments = new HashMap<>();
        
        if(rootNode == null) {
            return assessments;
        }
        
        /* even weights for child assessments within this  node */
        double evenNodeAssessmentsWeight = 1.0 / rootNode.getChildren().size();
        
        /* the total weight used in the current aggregate calculation within this node's children.
         * Will not be negative. */
        double nodeAssessmentsWeightToConsider = 1.0;
        
        /* total weighted score calculated so far on the scores for this single node */
        double aggregateNodeAssessmentsLevelScore = 0;
        
        /* iterate over each of this node's children to gather their assessments and calculate
         * the aggregate assessment level of their parent (i.e. the average assessment) */
        for( AbstractScoreNode child : rootNode.getChildren() ) {
            
            AssessmentLevelEnum childAssessment = null;
                     
            if(child instanceof RawScoreNode) {
                
                final RawScoreNode rawScoreChild = (RawScoreNode)child;
                childAssessment = rawScoreChild.getAssessment();
                
            } else if( child instanceof GradedScoreNode ) {
                
                final GradedScoreNode gradedChild = (GradedScoreNode)child;
                
                // Note: see method javadoc for why null is used here
                assessments.putAll(performAssessmentRollup(gradedChild, recursiveRollup != null && recursiveRollup ? recursiveRollup : null));
                
                childAssessment = assessments.get(gradedChild.getPerformanceNodeId());
            }
            
            if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(childAssessment)){
                aggregateNodeAssessmentsLevelScore += ABOVE_EXPECTATION_SCORE * evenNodeAssessmentsWeight;
            }else if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(childAssessment)){
                aggregateNodeAssessmentsLevelScore += BELOW_EXPECTATION_SCORE * evenNodeAssessmentsWeight;
            } else if(AssessmentLevelEnum.AT_EXPECTATION.equals(childAssessment)){
                aggregateNodeAssessmentsLevelScore += AT_EXPECTATION_SCORE * evenNodeAssessmentsWeight;
            }else{
                nodeAssessmentsWeightToConsider -= evenNodeAssessmentsWeight;  // don't count this node because it has unknown assessment
            }

        } 
        
        /* Use the calculated average points for this node to determine what assessment level
         * it should be given based on its child assessments.*/
        AssessmentLevelEnum nodeAssessmentToUse;
        if(nodeAssessmentsWeightToConsider >= 0.01){
            aggregateNodeAssessmentsLevelScore /= nodeAssessmentsWeightToConsider;
            
            if(aggregateNodeAssessmentsLevelScore < BELOW_EXPECTATION_UPPER_THRESHOLD){
                nodeAssessmentToUse = AssessmentLevelEnum.BELOW_EXPECTATION;
            }else if(aggregateNodeAssessmentsLevelScore >= AT_EXPECTATION_UPPER_THRESHOLD){
                nodeAssessmentToUse = AssessmentLevelEnum.ABOVE_EXPECTATION;
            }else{
                nodeAssessmentToUse = AssessmentLevelEnum.AT_EXPECTATION;
            }
        }else{
            nodeAssessmentToUse = AssessmentLevelEnum.UNKNOWN;
        }
        
        if(recursiveRollup != null) {
            // true - all graded score nodes need assessment set
            // false - the 'rootNode' provided needs assessment set, the recursive calls to this method will set to null
            if(logger.isDebugEnabled()) {
                logger.debug("Updated GradedScoreNode "+rootNode.getName()+" grade from "+rootNode.getAssessment()+" to "+nodeAssessmentToUse);
            }
            rootNode.setAssessment(nodeAssessmentToUse);
        }

        if(rootNode.getPerformanceNodeId() != null) {
            assessments.put(rootNode.getPerformanceNodeId(), nodeAssessmentToUse);
        }
        
        return assessments;
    }
}
