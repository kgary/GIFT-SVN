/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest.ConceptParameters.QuestionTypeParameter;
import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;

/**
 * This class contains a get survey request used to retrieve a survey from the database.  The
 * survey will contain concept specific questions (as long as the questions have 
 * been authored and associated with the survey context, appropriately).  The survey
 * is useful to testing the learner on knowledge about certain concepts.
 * 
 * Note: this request is used in courses utilizing EMAP and Merrill's branch point course
 * element.
 * 
 * @author mhoffman
 *
 */
public class GetKnowledgeAssessmentSurveyRequest extends GetSurveyRequest {
    
    /** 
     * List of concepts wanting to be assessed in a survey.  The survey
     * returned for this request will only contain questions questions that are associated with these concepts.
     * 
     * This is useful for concept knowledge assessment.
     * 
     * Key: concept name
     * Value: parameters for questions related to the concept
     */
    private Map<String, ConceptParameters> conceptNameToParameters;
    
    /**
     * Class constructor
     * 
     * @param surveyContextId unique id for the survey context that is associated with the survey being requested 
     * @param conceptNameToParameters map of concepts wanting to be assessed in a survey.  Can't be null or empty.
     *              Key: concept name
     *              Value: parameters for questions related to the concept
     */
    public GetKnowledgeAssessmentSurveyRequest(int surveyContextId, Map<String, ConceptParameters> conceptNameToParameters){
        super(surveyContextId, mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY);
        
        if(conceptNameToParameters == null || conceptNameToParameters.isEmpty()){
            throw new IllegalArgumentException("The concept map must contain at least one concept.  If you don't have a collection of concepts than you should use the GetSurveyRequest class instead.");
        }
        
        this.conceptNameToParameters = conceptNameToParameters;
    } 
    
    /**
     * Create a request using the parameters provided.
     * 
     * @param surveyContextId unique id for the survey context that is associated with the survey being requested 
     * @param conceptInfoList collection of concepts from the course.  Can't be null or empty.
     * @return GetKnowledgeAssessmentSurveyRequest the new request object containing the necessary attributes to service the request
     */
    public static GetKnowledgeAssessmentSurveyRequest createRequestForConcepts(int surveyContextId, List<generated.course.ConceptQuestions> conceptInfoList){
        
        Map<String, ConceptParameters> conceptNameToParameters = new HashMap<String, ConceptParameters>();
        
        if(conceptInfoList == null || conceptInfoList.isEmpty()){
            throw new IllegalArgumentException("The concept list must contain at least one concept.  If you don't have a collection of concepts than you should use the GetSurveyRequest class instead.");
        }
        
        //build concept parameters map
        for(generated.course.ConceptQuestions concept : conceptInfoList){
            
            List<QuestionTypeParameter> qParams = new ArrayList<>();
            generated.course.ConceptQuestions.QuestionTypes qTypes = concept.getQuestionTypes();
            
            if(qTypes.getEasy().intValue() > 0){
                QuestionTypeParameter easyQParam = new QuestionTypeParameter(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, QuestionDifficultyEnum.EASY.getName(), qTypes.getEasy().intValue());
                qParams.add(easyQParam);
            }
            
            if(qTypes.getMedium().intValue() > 0){
                QuestionTypeParameter mediumQParam = new QuestionTypeParameter(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, QuestionDifficultyEnum.MEDIUM.getName(), qTypes.getMedium().intValue());
                qParams.add(mediumQParam);
            }
            
            if(qTypes.getHard().intValue() > 0){
                QuestionTypeParameter hardQParam = new QuestionTypeParameter(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, QuestionDifficultyEnum.HARD.getName(), qTypes.getHard().intValue());
                qParams.add(hardQParam);
            }            
            
            ConceptParameters parameters = new ConceptParameters(concept.getName(), qParams);
                       
            conceptNameToParameters.put(concept.getName(), parameters);
        }
        
        return new GetKnowledgeAssessmentSurveyRequest(surveyContextId, conceptNameToParameters);
    }
    
    /**
     * Create a request using the parameters provided.
     * 
     * @param surveyContextId unique id for the survey context that is associated with the survey being requested 
     * @param concepts collection of concepts from the course.  Can't be null or empty.
     * @return GetKnowledgeAssessmentSurveyRequest the new request object containing the necessary attributes to service the request
     */
    public static GetKnowledgeAssessmentSurveyRequest createRequestFromConceptSurvey(int surveyContextId, List<generated.course.ConceptQuestions> concepts){
        
        Map<String, ConceptParameters> conceptNameToParameters = new HashMap<String, ConceptParameters>();
        
        if(concepts == null || concepts.isEmpty()){
            throw new IllegalArgumentException("The concept list must contain at least one concept.  If you don't have a collection of concepts than you should use the GetSurveyRequest class instead.");
        }
        
        //build concept parameters map
        for(generated.course.ConceptQuestions concept : concepts){
            
            List<QuestionTypeParameter> qParams = new ArrayList<>();
            generated.course.ConceptQuestions.QuestionTypes qTypes = concept.getQuestionTypes();
            
            if(qTypes.getEasy().intValue() > 0){
                QuestionTypeParameter easyQParam = new QuestionTypeParameter(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, QuestionDifficultyEnum.EASY.getName(), qTypes.getEasy().intValue());
                qParams.add(easyQParam);
            }
            
            if(qTypes.getMedium().intValue() > 0){
                QuestionTypeParameter mediumQParam = new QuestionTypeParameter(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, QuestionDifficultyEnum.MEDIUM.getName(), qTypes.getMedium().intValue());
                qParams.add(mediumQParam);
            }
            
            if(qTypes.getHard().intValue() > 0){
                QuestionTypeParameter hardQParam = new QuestionTypeParameter(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, QuestionDifficultyEnum.HARD.getName(), qTypes.getHard().intValue());
                qParams.add(hardQParam);
            }            
            
            ConceptParameters parameters = new ConceptParameters(concept.getName(), qParams);
                        
            conceptNameToParameters.put(concept.getName(), parameters);
        }
        
        return new GetKnowledgeAssessmentSurveyRequest(surveyContextId, conceptNameToParameters);
    }
    
    /**
     * List of concepts wanting to be assessed in a survey.  The survey
     * returned for this request will only contain questions questions that are associated with these concepts.
     * 
     * This is useful for concept knowledge assessment.
     * 
     * Key: concept name
     * Value: parameters for questions related to the concept
     * @return won't be null or empty.
     */
    public Map<String, ConceptParameters> getConcepts(){
        return conceptNameToParameters;
    }
    
    /**
     * Return a string containing all the concept names in this request.
     * Useful for debugging purposes.
     * 
     * @return display string of all concept names in this instance
     */
    public String getConceptNames(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<String> itr = conceptNameToParameters.keySet().iterator();
        while(itr.hasNext()){
            sb.append(itr.next());
            
            if(itr.hasNext()){
                sb.append(",");
            }
        }
        sb.append("}");
        
        return sb.toString();
    }
    
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[GetKnowledgeAssessmentSurveyRequest: ");
        sb.append(super.toString());
        
        sb.append(", concepts = {");
        for(ConceptParameters concept : getConcepts().values()){
            sb.append(" ").append(concept).append(",");
        }
        sb.append("}");
        
        sb.append("]");

        return sb.toString();
    }
    
    /**
     * This inner class contains the parameters for requesting survey questions on a particular concept.
     * Some of the parameters include survey property based enumeration values as well as questions to
     * prefer and to avoid when selecting survey questions.
     * 
     * @author mhoffman
     *
     */
    public static class ConceptParameters{
     
        /** question specific parameters based on survey property enumerated values */
        private List<QuestionTypeParameter> questionParams;
        
        /** 
         * collection of preferred unique question ids for this concept
         * A question might be preferred because it was answered incorrectly.
         */
        private List<Integer> preferQuestionIds = new ArrayList<>(0);
        
        /** 
         * collection of not-preferred unique question ids for this concept
         * A question might be avoided because it was answered correctly.
         */
        private List<Integer> avoidQuestionIds = new ArrayList<>(0);
        
        /** the name of the concept to retrieve questions on (i.e. questions that will assess the learner on this concept) */
        private String conceptName;
        
        /**
         * Class constructor - set attributes
         * 
         * @param conceptName the name of the concept to retrieve questions on (i.e. questions that will assess the learner on this concept)
         * @param questionParams question specific parameters based on survey property enumerated values.  Must contain at least one entry.
         */
        public ConceptParameters(String conceptName, List<QuestionTypeParameter> questionParams){
            
            if(conceptName == null || conceptName.isEmpty()){
                throw new IllegalArgumentException("The concept name can't be null or empty.");
            }
            
            this.conceptName = conceptName;
            
            if(questionParams == null || questionParams.isEmpty()){
                throw new IllegalArgumentException("The question parameters can't be empty.");
            }
            
            this.questionParams = questionParams;
        }
        
        public List<QuestionTypeParameter> getQuestionParams(){
            return questionParams;
        }
        
        public String getConceptName(){
            return conceptName;
        }
        
        public List<Integer> getPreferredQuestions(){
            return preferQuestionIds;
        }
        
        public List<Integer> getAvoidQuestions(){
            return avoidQuestionIds;
        }
        
        public void addPreferredQuestion(int questionId){
            
            if(!preferQuestionIds.contains(questionId)){
                preferQuestionIds.add(questionId);
            }
        }
        
        public void addAvoidQuestion(int questionId){
            
            if(!avoidQuestionIds.contains(questionId)){
                avoidQuestionIds.add(questionId);
            }
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ConceptParameters: ");
            sb.append("concept = ").append(getConceptName());
            
            sb.append(", question parameters = {");
            for(QuestionTypeParameter param : getQuestionParams()){
                sb.append(" ").append(param.toString()).append(",");
            }
            sb.append("}");
            
            sb.append(", preferred questions = {");
            for(Integer qId : getPreferredQuestions()){
                sb.append(" ").append(qId).append(",");
            }
            sb.append("}");
            
            sb.append(", avoid questions = {");
            for(Integer qId : getAvoidQuestions()){
                sb.append(" ").append(qId).append(",");
            }
            sb.append("}");
            
            sb.append("]");
            
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object o) {
        	if(this == o) {
        		return true;
        	}
        	
        	if(o != null && o instanceof ConceptParameters) {
        		ConceptParameters params = (ConceptParameters) o;
        		
        		if(!conceptName.equals(params.conceptName)) {
        			return false;
        		}
        		
        		if(!params.questionParams.equals(params.questionParams)) {
        			return false;
        		}
        		
        		if(avoidQuestionIds == null && params.avoidQuestionIds == null) {
        			// If both lists are null, nothing to worry about
        			
        		} else if (avoidQuestionIds == null || params.avoidQuestionIds == null) {
        			// If only one list is null, they're not equal
        			return false;
        			
        		} else if(!avoidQuestionIds.equals(params.avoidQuestionIds)) {
        			return false;
        		}
        		
        		if(preferQuestionIds == null && params.preferQuestionIds == null) {
        			// If both lists are null, nothing to worry about
        			
        		} else if (preferQuestionIds == null || params.preferQuestionIds == null) {
        			// If only one list is null, they're not equal
        			return false;
        			
        		} else if(!preferQuestionIds.equals(params.preferQuestionIds)) {
        			return false;
        		}
        		
        	}
        	
        	return false;
        }
        
        @Override
        public int hashCode() {
        	return 37 + conceptName.hashCode()
        			+ questionParams.hashCode()
        			+ (avoidQuestionIds == null ? 0 : avoidQuestionIds.hashCode())
        			+ (preferQuestionIds == null ? 0 : preferQuestionIds.hashCode());
        }
        
        /**
         * This inner class contains the parameters for selecting a question type.  A question type is based on 
         * a survey property enumerated value.
         * 
         * @author mhoffman
         *
         */
        public static class QuestionTypeParameter{
            
            /** the number of questions to select that adhere to the other parameters in this class */
            private int numberOfQuestions;
            
            /** the enumerated property to find when searching for questions */
            private SurveyPropertyKeyEnum questionProperty;
            
            /** the value for the property to match */
            private String propertyValue;
            
            /**
             * Class constructor - set attributes
             * 
             * @param questionProperty the enumerated property to find when searching for questions
             * @param propertyValue the value for the property to match
             * @param numberOfQuestions the number of questions to select that adhere to the other parameters in this class
             */
            public QuestionTypeParameter (SurveyPropertyKeyEnum questionProperty, String propertyValue, int numberOfQuestions){
                
                if(questionProperty == null){
                    throw new IllegalArgumentException("The question property can't be null.");
                }
                
                if(propertyValue == null || propertyValue.isEmpty()){
                    throw new IllegalArgumentException("The property value can't be null or empty.");
                }
                
                if(numberOfQuestions < 1){
                    throw new IllegalArgumentException("The number of questions must be greater than zero.");
                }
                
                this.questionProperty = questionProperty;
                this.propertyValue = propertyValue;
                this.numberOfQuestions = numberOfQuestions;
            }
            
            public SurveyPropertyKeyEnum getQuestionProperty(){
                return questionProperty;
            }
            
            public String getPropertyValue(){
                return propertyValue;
            }
            
            public int getNumberOfQuestions(){
                return numberOfQuestions;
            }
            
            @Override
            public String toString(){
                
                StringBuffer sb = new StringBuffer();
                sb.append("[QuestionTypeParameter: ");
                sb.append("questionProperty = ").append(getQuestionProperty());
                sb.append(", propertyValue = ").append(getPropertyValue());
                sb.append(", number of questions = ").append(getNumberOfQuestions());
                sb.append("]");
                
                return sb.toString();
            }
            
            @Override
            public boolean equals(Object o) {
            	if(this == o) {
            		return true;
            	}
            	
            	if(o != null && o instanceof QuestionTypeParameter) {
            		QuestionTypeParameter param = (QuestionTypeParameter) o;
            		
            		if(numberOfQuestions != param.numberOfQuestions) {
            			return false;
            		}
            		
            		if(questionProperty == null && param.questionProperty == null) {
            			// If both objects are null, nothing to worry about
            			
            		} else if (questionProperty == null || param.questionProperty == null) {
						// If only one object is null, they're not equal
            			return false;
            			
            		} else if (!questionProperty.equals(param.questionProperty)) {
            			return false;
            		}
            		
            		String value = (propertyValue == null) ? "" : propertyValue;
            		if(!value.equals(param.propertyValue)) {
            			return false;
            		}
            		
            		return true;
            	}
            	
            	return false;
            }
            
            @Override
            public int hashCode() {
            	return 17 + numberOfQuestions
            			+ (propertyValue == null ? 0 : propertyValue.hashCode()) 
            			+ (questionProperty == null ? 0 : questionProperty.hashCode());
            }
        }        

    }
    
}
