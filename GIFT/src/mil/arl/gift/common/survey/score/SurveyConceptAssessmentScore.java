/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class contains scoring information about concepts.  The concept scores are derived from
 * survey responses whose questions are associated with one or more concepts.
 * 
 * @author mhoffman
 *
 */
public class SurveyConceptAssessmentScore implements ScoreInterface{

    /** collection of concept specific scores mapped by course concept name */
    private Map<String, ConceptOverallDetails> conceptToDetails;
    
    /**
     * Class constructor - set attributes
     * 
     * @param conceptToDetails collection of concept specific scores.  Can't be null.
     */
    public SurveyConceptAssessmentScore(Map<String, ConceptOverallDetails> conceptToDetails){ 
        
        if(conceptToDetails == null){
            throw new IllegalArgumentException("The concept details map can't be null.");
        }
        
        this.conceptToDetails = conceptToDetails;
    }
    
    /**
     * Return the collection of concept specific scores.
     * 
     * @return Map<String, ConceptOverallDetails> can be empty but not null.
     */
    public Map<String, ConceptOverallDetails> getConceptDetails(){
        return conceptToDetails;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SurveyConceptAssessmentScore: ");
        
        sb.append("ConceptDetails = {");
        for(String concept : conceptToDetails.keySet()){
            sb.append(" ").append(concept).append(":").append(conceptToDetails.get(concept)).append(",");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * This inner class contains scoring information for a specific concept.
     * 
     * @author mhoffman
     *
     */
    public static class ConceptOverallDetails{
        
        /** collection of correct and incorrect questions (more specifically the question ids - not the survey question ids) */
        private Set<Integer> correctQuestions;        
        private Set<Integer> incorrectQuestions;
        
        /**
         * Default constructor - use this to incrementally add correct/incorrect questions to this class
         */
        public ConceptOverallDetails(){
            correctQuestions = new HashSet<>();
            incorrectQuestions = new HashSet<>();
        }
        
        /**
         * Class constructor - set attributes
         * 
         * @param correctQuestions collection of correct questions (more specifically the question ids - not the survey question ids)
         * @param incorrectQuestions collection of incorrect questions (more specifically the question ids - not the survey question ids)
         */
        public ConceptOverallDetails(Set<Integer> correctQuestions, Set<Integer> incorrectQuestions){
            
            if(correctQuestions == null){
                throw new IllegalArgumentException("The correct questions list can't be null.");
            }
            
            if(incorrectQuestions == null){
                throw new IllegalArgumentException("The incorrect questions list can't be null.");
            }
            
            this.correctQuestions = correctQuestions;
            this.incorrectQuestions = incorrectQuestions;
        }
        
        public void addCorrectQuestion(int questionId){
            correctQuestions.add(questionId);
        }
        
        public void addIncorrectQuestion(int questionId){
            incorrectQuestions.add(questionId);
        }
        
        public Set<Integer> getCorrectQuestions(){
            return correctQuestions;
        }
        
        public Set<Integer> getIncorrectQuestions(){
            return incorrectQuestions;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ConceptOverallWrapper: ");
            sb.append(" correct = {");
            for(Integer qId : getCorrectQuestions()){
                sb.append(" ").append(qId).append(",");
            }
            sb.append("}");
            
            sb.append(", incorrect = ");
            for(Integer qId : getIncorrectQuestions()){
                sb.append(" ").append(qId).append(",");
            }
            sb.append("}");
            
            sb.append("]");
            return sb.toString();
        }
    }
}
