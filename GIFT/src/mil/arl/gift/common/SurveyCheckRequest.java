/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.io.FileProxy;

/**
 * This class contains attributes of a survey that are needing to be checked and validated against the survey database.
 *
 * @author mhoffman
 */
public class SurveyCheckRequest {

    /** (optional - if you want to just check if the survey context exists) key for the survey in the survey context */
    private String giftKey;
    
    /** unique id for the survey context that is associated with the survey */
    private int surveyContextId;
    
    /** (optional) list of questions whose elements also need to be checked */
    private List<Question> questions = new ArrayList<>();
    
    /** (optional) the source of the survey reference (e.g. dkf or course file) */
    private FileProxy sourceReference = null;
    
    /** (optional) the contents of a knowledge assessment survey request which contains survey elements to check */
    private GetKnowledgeAssessmentSurveyRequest knowledgeAssessmentSurveyRequest = null;
    
    /** The index of the course object that this survey check request corresponds to */
    private Integer courseObjectIndex = null;
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param surveyContextId unique id for the survey context that is associated with the survey
     */
    public SurveyCheckRequest(int surveyContextId){        
        
        if(surveyContextId < 0){
            throw new IllegalArgumentException("The survey context id of "+surveyContextId+" must be non-negative.");
        }
        this.surveyContextId = surveyContextId;
    }

    /**
     * Class constructor - set attribute(s)
     * 
     * @param surveyContextId unique id for the survey context that is associated with the survey
     * @param giftKey key for the survey in the survey context
     */
    public SurveyCheckRequest(int surveyContextId, String giftKey) {   
        this(surveyContextId);
        
        if(giftKey == null){
            throw new IllegalArgumentException("The gift key can't be null.");
        }
        this.giftKey = giftKey;
    }
    
    /**
	 * Class constructor - set attribute(s)
	 * 
	 * @param surveyContextId unique id for the survey context that is associated with the survey
	 * @param courseObjectIndex The index of the course object this survey check request corresponds to. Can be null.
	 */
	public SurveyCheckRequest(int surveyContextId, Integer courseObjectIndex) {

		if (surveyContextId < 0) {
			throw new IllegalArgumentException("The survey context id of " + surveyContextId + " must be non-negative.");
		}
		
		if(courseObjectIndex != null && courseObjectIndex < 0) {
			throw new IllegalArgumentException("The course object index cannot be negative.");
		}
		this.surveyContextId = surveyContextId;
		this.courseObjectIndex = courseObjectIndex;
	}
    
	/**
	 * Class constructor - set attribute(s)
	 * 
	 * @param surveyContextId unique id for the survey context that is associated with the  survey
	 * @param courseObjectIndex the index of the course object this survey check request corresponds to.  Can be null.
	 * @param giftKey key for the survey in the survey context
	 */
	public SurveyCheckRequest(int surveyContextId, Integer courseObjectIndex, String giftKey) {
		this(surveyContextId);

		if (giftKey == null) {
			throw new IllegalArgumentException("The gift key can't be null.");
		}
		
		if(courseObjectIndex != null && courseObjectIndex < 0) {
			throw new IllegalArgumentException("The course object index cannot be negative.");
		}
		
		this.giftKey = giftKey;
		this.courseObjectIndex = courseObjectIndex;
	}
	
    /**
     * Set the contents of a knowledge assessment (e.g. Recall quadrant of Merrill's branch point course element)
     * survey request which contains survey elements to check 
     *  
     * @param knowledgeAssessmentSurveyRequest the request object containing criteria for a knowledge assessment survey 
     */
    public void setGetKnowledgeAssessmentSurveyRequest(GetKnowledgeAssessmentSurveyRequest knowledgeAssessmentSurveyRequest){
        this.knowledgeAssessmentSurveyRequest = knowledgeAssessmentSurveyRequest;
    }
    
    /**
     * Return the contents of a knowledge assessment survey request which contains survey elements to check 
     * 
     * @return GetRecallSurveyRequest contains a request for survey elements for a knowledge assessment.
     * (e.g. Recall quadrant of Merrill's branch point course element)
     * Can be null.
     */
    public GetKnowledgeAssessmentSurveyRequest getKnowledgeAssessmentSurveyRequest(){
        return knowledgeAssessmentSurveyRequest;
    }
    
    /**
     * Set the source of the survey reference (e.g. dkf or course file)
     * 
     * @param sourceReference a file containing the survey elements referenced by this class. Can be null.
     */
    public void setSourceReference(FileProxy sourceReference){
        this.sourceReference = sourceReference;
    }
    
    /**
     * Get the source of the survey reference (e.g. dkf or course file)
     * 
     * @return File a file containing the survey elements referenced by this class. Can be null.
     */
    public FileProxy getSourceReference(){
        return sourceReference;
    }

    /**
     * Return the key for the survey in the survey context
     * 
     * @return String - the gift key, unique to the survey context.  Can be null if just the survey
     * context existence is being checked
     */
    public String getGiftKey() {
        return giftKey;
    }

    /**
     * Return the unique id for the survey context that is associated with the survey
     * 
     * @return int - the survey context unique id
     */
    public int getSurveyContextId() {
        return surveyContextId;
    }
    
    /**
     * Return the index of the course object that this survey check request corresponds to
     * 
     * @return Integer - the index of the course object. Can be null.
     */
    public Integer getCourseObjectIndex() {
		return courseObjectIndex;
	}
    
    /**
     * Return the list of questions whose elements also need to be checked 
     * 
     * @return List<Question> - the collection of questions to check.  Can be empty but not null.
     */
    public List<Question> getQuestions(){
        return questions;
    }
    
    /**
     * Add a new question to the collection of questions that need to be checked.
     * 
     * @param question - the question to add.
     */
    public void addQuestion(Question question){
        
        if(question == null){
            throw new IllegalArgumentException("The question can't be null.");
        }else if(questions.contains(question)){
            throw new IllegalArgumentException("Can't add duplicate question.");
        }else if(giftKey == null){
            throw new IllegalArgumentException("Can't add a question if the gift survey key is null.");
        }
        
        questions.add(question);
    }
    
    /**
     * Add a collection of questions to the existing collection that need to be checked.
     * 
     * @param questions - collection of new questions to add
     */
    public void addQuestions(List<Question> questions){
        
        for(Question question : questions){
            addQuestion(question);
        }
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[GetSurveyRequest: ");
        sb.append("source Reference = ").append(getSourceReference());
        sb.append(", survey context id = ").append(getSurveyContextId());
        sb.append(", courseObjectIndex = ").append(getCourseObjectIndex());
        sb.append(", gift key = ").append(getGiftKey());
        
        sb.append(", questions = {");
        for(Question question : getQuestions()){
            sb.append(" ").append(question).append(",");
        }
        sb.append("}");
        
        sb.append(", knowledgeAssessmentSurveyRequest = ").append(getKnowledgeAssessmentSurveyRequest());
        
        sb.append("]");

        return sb.toString();
    }
    
    @Override
	public boolean equals(Object o) {

		if (this == o) {
			// Return true if this is the same object
			return true;
		}

		if (o != null && o instanceof SurveyCheckRequest) {
			SurveyCheckRequest request = (SurveyCheckRequest) o;
			String key = (giftKey == null) ? "" : giftKey;

			if (!key.equals(request.giftKey)) {
				// Return false if the gift keys do not match
				return false;
			}

			if (surveyContextId != request.surveyContextId) {
				return false;
			}
			
			//
			// Compare source references
			//

			if(sourceReference == null && request.sourceReference == null) {
				// If both objects are null, continue
				
			} else if (sourceReference == null || request.sourceReference == null) {
				return false;
				
			} else if (!sourceReference.equals(request.sourceReference)) {
				return false;
			}
			
			//
			// Compare questions
			//

			if(questions == null && request.questions == null) {
				// If both lists are null, continue
				
			} else if (questions == null || request.questions == null) {
				// If only one of the lists are null, return false
				return false;
				
			} else if(!questions.equals(request.questions)) {
				return false;
			}
			
			//
			// Compare knowledgeAssessmentSurveyRequests
			//

			if (knowledgeAssessmentSurveyRequest == null && request.knowledgeAssessmentSurveyRequest == null) {
				// If both objects are null, continue
				
			} else if (knowledgeAssessmentSurveyRequest == null || request.knowledgeAssessmentSurveyRequest == null) {
				// If only one of the objects are null, return false
				return false;
				
			} else if (!knowledgeAssessmentSurveyRequest.equals(request.knowledgeAssessmentSurveyRequest)) {
				return false;
			}
			
			return true;
		}

		return false;
	}
    
    @Override
	public int hashCode() {
		int result = 11 + surveyContextId  
				+ (giftKey == null ? 0 : giftKey.hashCode())
				+ (questions == null ? 0 : questions.hashCode())
				+ (sourceReference == null ? 0 : sourceReference.hashCode())
				+ (knowledgeAssessmentSurveyRequest == null ? 0 : knowledgeAssessmentSurveyRequest.hashCode());
		
		return result;
	}
    
    /**
     * Inner class used to contain question elements that need to be checked.
     * 
     * @author mhoffman
     *
     */
    public static class Question{
        
        /** the unique id of the question */
        private int questionId;
        
        /** (optional) list of replies whose elements needs to be checked */
        private List<Reply> replies = new ArrayList<>();
        
        /**
         * Class constructor - set attribute(s)
         * 
         * @param questionId - unique id of the question
         */
        public Question(int questionId){
         
            if(questionId < 0){
                throw new IllegalArgumentException("The question id of "+questionId+" must be non-negative.");
            }
            
            this.questionId = questionId;
        }
        
        /**
         * Return the unique id of the question
         * 
         * @return int - question unique id
         */
        public int getQuestionId(){
            return questionId;
        }
        
        @Override
        public boolean equals(Object otherQuestion){
            
            if(otherQuestion instanceof Question && ((Question)otherQuestion).getQuestionId() == this.getQuestionId()){
                return true;
            }else{
                return false;
            }
        }
        
        @Override
        public int hashCode(){
        	
        	// Start with prime number
        	int hash = 107;
        	int mult = 389;
        	
        	// Take another prime as multiplier, add members used in equals
        	hash = mult * hash + this.getQuestionId();
        	
        	return hash;
        }
        
        /**
         * Add a reply of this question to check.
         * 
         * @param reply - the reply to add to the collection of replies to check
         */
        public void addReply(Reply reply){
            
            if(reply == null){
                throw new IllegalArgumentException("The reply can't be null.");
            }else if(replies.contains(reply)){
                throw new IllegalArgumentException("Can't add duplicate reply.");
            }
            
            replies.add(reply);
        }
        
        /**
         * Add a collection of replies to the existing collection of replies to check.
         * 
         * @param replies - collection of replies to add
         */
        public void addReplies(List<Reply> replies){
            
            for(Reply reply : replies){
                addReply(reply);
            }
        }
        
        /**
         * Return the collection of replies to check.
         * 
         * @return List<Reply> - the replies to check
         */
        public List<Reply> getReplies(){
            return replies;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[Reply: ");
            sb.append("id = ").append(getQuestionId());
            
            sb.append(", replies = {");
            for(Reply reply : getReplies()){
                sb.append(" ").append(reply).append(",");
            }
            sb.append("}");
            
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * 
     * @author mhoffman
     *
     */
    public static class Reply{
        
        /** the unique id of a reply to a question */
        private int replyId;
        
        /**
         * Class constructor - set attribute(s)
         * 
         * @param replyId the unique id of a reply to a question
         */
        public Reply(int replyId){
            
            if(replyId < 0){
                throw new IllegalArgumentException("The reply id of "+replyId+" must be non-negative.");
            }
            this.replyId = replyId;
        }
        
        /**
         * Return the unique id of a reply to a question
         * 
         * @return int - the reply unique id
         */
        public int getReplyId(){
            return replyId;
        }
        
        @Override
        public boolean equals(Object otherReply){
            
            if(otherReply instanceof Reply && ((Reply)otherReply).getReplyId() == this.replyId){
                return true;
            }else{
                return false;
            }
        }
        
        @Override
        public int hashCode(){
        	
        	// Start with prime number
        	int hash = 97;
        	int mult = 1289;
        	
        	// Take another prime as multiplier, add members used in equals
        	hash = mult * hash + this.replyId;
        	
        	return hash;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[Reply: ");
            sb.append("id = ").append(getReplyId());
            sb.append("]");
            return sb.toString();
        }
    }

}
