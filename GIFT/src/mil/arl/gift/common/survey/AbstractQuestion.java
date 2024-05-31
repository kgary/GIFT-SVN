/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * A question that can be asked
 *
 * @author jleonard
 */
public abstract class AbstractQuestion implements Serializable {

	private static final long serialVersionUID = 1L;

	private int questionId;

    private String text;

    private SurveyItemProperties properties;

    private Set<String> categories = new HashSet<String>();
    
    private HashSet<String> visibleToUserNames = new HashSet<String>();
    
    private HashSet<String> editableToUserNames = new HashSet<String>();

    private List<Double> weightsList = null;
    
    private boolean locked = false;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public AbstractQuestion() {
    }

    /**
     * Constructor
     *
     * @param questionId The ID of the question
     * @param text The text of the question
     * @param properties The properties of the question.  Can't be null.
     * @param categories The categories the question is in.  Can be null or empty.
     * @param visibleToUserNames User names that can see the question.  Can be null or empty.
     * @param editableToUserNames User names that can edit the question.  Can be null or empty.
     */
    public AbstractQuestion(int questionId, String text, SurveyItemProperties properties, Collection<String> categories, Collection<String> visibleToUserNames, Collection<String> editableToUserNames) {

        if(properties == null){
            throw new IllegalArgumentException("The properties can't be null.");
        }
        
        this.questionId = questionId;

        this.text = text;

        this.properties = properties;

        if (categories != null) {

            this.categories.addAll(categories);
        }
        
        if(visibleToUserNames != null) {
        	this.visibleToUserNames.addAll(visibleToUserNames);
        }
        
        if(editableToUserNames != null) {
        	this.editableToUserNames.addAll(editableToUserNames);
        }
    }

    /**
     * Constructor
     *
     * Copies an existing question and gives it a new ID
     *
     * @param question The question to copy
     * @param newId The new ID of the question
     */
    public AbstractQuestion(AbstractQuestion question, int newId) {

        this.questionId = newId;

        this.text = question.getText();

        this.properties = question.getProperties();

        this.categories = question.getCategories();
        
        this.visibleToUserNames = question.getVisibleToUserNames();
        
        this.editableToUserNames = question.getEditableToUserNames();
    }

    /**
     * Constructor
     *
     * Copies an existing question and gives it new text
     *
     * @param question The question to copy
     * @param newText The new text
     */
    public AbstractQuestion(AbstractQuestion question, String newText) {

        this.questionId = question.getQuestionId();

        this.text = newText;

        this.properties = question.getProperties();

        this.categories = question.getCategories();
        
        this.visibleToUserNames = question.getVisibleToUserNames();
        
        this.editableToUserNames = question.getEditableToUserNames();
    }
    
    /**
     * Gets the lock status of the question.
     * @return True if the question is locked, false otherwise.
     */
    public boolean isLocked() {
		return locked;
	}

    /**
     * Sets the question's lock status.
     * @param locked True if it is locked, false otherwise.
     */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
     * Gets the ID of the question
     *
     * @return int The ID of the question
     */
    public int getQuestionId() {

        return questionId;
    }
    
    public void setQuestionId(int id) {
    	questionId = id;
    }

    /**
     * Gets the text of the question
     *
     * @return String The text of the question
     */
    public String getText() {

        return text;
    }
    
    /**
     * Set the question text.
     * 
     * @param text can't be null or empty.
     */
    public void setText(String text){
        
        if(text == null || text.isEmpty()){
            throw new IllegalArgumentException("The question text can't be null or empty.");
        }
        
        this.text = text;
    }

    /**
     * Gets the list of properties this question has
     *
     * @return List<GwtQuestionProperty> The list of properties this question
     * has
     */
    public SurveyItemProperties getProperties() {

        return properties;
    }

    /**
     * Adds the question into a category
     *
     * @param category a unique name for a category to add
     */
    public void addCategory(String category) {

        categories.add(category);

    }

    /**
     * Removes the question from a category
     *
     * @param category a unique name for a category to remove
     */
    public void removeCategory(String category) {

        categories.remove(category);
    }

    /**
     * Gets the list of categories this question is in
     *
     * @return Set<String> The set of categories this question is in
     */
    public Set<String> getCategories() {

        return categories;
    }

    /**
     * Gets the URI to the media to display in the question
     * (e.g. a course-relative path like "image.png" or a legacy image path 
     * like "surveyWebResources/uploadedImages/image.png")"
     *
     * @return String The URI to the media to display in the question
     */
    public String getQuestionMedia() {

        return (String) properties.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
    }

    /**
     * Sets the URI to the image to display in the question
     * (e.g. a course-relative path like "image.png" or a legacy image path 
     * like "surveyWebResources/uploadedImages/image.png")"
     *
     * @param uri The URI to the image to display in the question
     */
    public void setQuestionMedia(String uri) {

        properties.setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, uri);
    }

    /**
     * Removes an image displayed in the question
     */
    public void removeQuestionImage() {

        properties.removeProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
        properties.removeProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY);
        properties.removeProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY);
    }

    /**
     * Gets which position to display the question image at
     *
     * @return Integer The position to display the question image at
     */
    public int getQuestionImagePosition() {

        return properties.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY) != null
                ? properties.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY)
                : 0;
    }

    /**
     * Sets which position to display the question image at
     *
     * 0 - Below the question 1 - Above the question
     *
     * @param position The position to display the question image at
     */
    public void setQuestionImagePosition(int position) {

        properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY, position);
    }

    /**
     * Gets what the width of the displayed image should be
     *
     * @return Integer The width of the displayed image should be
     */
    public int getQuestionImageWidth() {

        return properties.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY) != null
                ? properties.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY)
                : 0;
    }

    /**
     * Set what the width of the displayed image should be
     *
     * The aspect ratio is preserved when it is displayed
     *
     * @param width The width of the displayed image should be
     */
    public void setQuestionImageWidth(int width) {

        properties.setIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY, width);
    }
    
    /**
     * Gets the weights associated with the correct answers of the question
     *
     * @return List<Integer> The weights of the correct answers.  Can be null or empty.
     */
    public List<Double> getAnswerWeights() {

        if (weightsList == null) {

            String weightsString = (String) getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);

            if (weightsString != null && !weightsString.isEmpty()) {

                weightsList = SurveyItemProperties.decodeDoubleListString(weightsString);

            }
        }

        return weightsList;
    }

    /**
     * Sets the weights of the correct answers of the question
     *
     * @param weightsList The weights of the correct answers of the question
     */
    public void setAnswerWeights(List<Double> weightsList) {

        String weightsString = SurveyItemProperties.encodeDoubleListString(weightsList);

        getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, weightsString);

        this.weightsList = null;
    }

    public HashSet<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(HashSet<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}

	public HashSet<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(HashSet<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
	}

	@Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Question: ");
        sb.append(" id  = ").append(getQuestionId());
        sb.append(", text = ").append(getText());
        sb.append(", categories = ");
        if (categories != null) {
            sb.append("{");
            for (String data : categories) {
                sb.append(data).append(", ");
            }
            sb.append("}");
        } else {
            sb.append(" = null");
        }
        sb.append(", properties = ").append(getProperties());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates a question based on the type
     *
     * @param questionType The type of question to create
     * @param id The ID of the question
     * @param text The text of the question
     * @param properties The properties of the question
     * @param categories The categories the question is in
     * @param visibleToUserNames User names that can see the question
     * @param editableToUserNames User names that can edit the question
     * @return GwtQuestion The created question
     */
    public static AbstractQuestion createQuestion(QuestionTypeEnum questionType, int id, String text, SurveyItemProperties properties, Collection<String> categories, Collection<String> visibleToUserNames, Collection<String> editableToUserNames) {

        if (questionType.equals(QuestionTypeEnum.FREE_RESPONSE)) {

            return new FillInTheBlankQuestion(id, text, properties, categories, visibleToUserNames, editableToUserNames);
            
        } else if (questionType.equals(QuestionTypeEnum.ESSAY)) {
        	
        	return new FillInTheBlankQuestion(id, text, properties, categories, visibleToUserNames, editableToUserNames);

        } else if (questionType.equals(QuestionTypeEnum.MULTIPLE_CHOICE)) {

            return new MultipleChoiceQuestion(id, text, properties, categories, visibleToUserNames, editableToUserNames);
            
        } else if(questionType.equals(QuestionTypeEnum.TRUE_FALSE)){
        	
       	 	return new MultipleChoiceQuestion(id, text, properties, categories, visibleToUserNames, editableToUserNames);

        } else if (questionType.equals(QuestionTypeEnum.RATING_SCALE)) {

            return new RatingScaleQuestion(id, text, properties, categories, visibleToUserNames, editableToUserNames);

        } else if (questionType.equals(QuestionTypeEnum.MATRIX_OF_CHOICES)) {

            return new MatrixOfChoicesQuestion(id, text, properties, categories, visibleToUserNames, editableToUserNames);

        } else if (questionType.equals(QuestionTypeEnum.SLIDER_BAR)) {

            return new SliderQuestion(id, text, properties, categories, visibleToUserNames, editableToUserNames);

        } else {

            throw new IllegalArgumentException("Cannot construct a question object of question type :" + questionType);
        }
    }

    /**
     * Copies a question and gives it new text
     *
     * @param question The question to copy
     * @param newText The new text of the question
     * @return GwtQuestion The copied question with new text
     */
    public static AbstractQuestion copyQuestion(AbstractQuestion question, String newText) {
        
        AbstractQuestion copiedQuestion;

        if (question instanceof FillInTheBlankQuestion) {

            copiedQuestion = new FillInTheBlankQuestion(question, newText);

        } else if (question instanceof MultipleChoiceQuestion) {

            copiedQuestion = new MultipleChoiceQuestion(question, newText);

        } else if (question instanceof RatingScaleQuestion) {

            copiedQuestion = new RatingScaleQuestion(question, newText);

        } else if (question instanceof MatrixOfChoicesQuestion) {

            copiedQuestion = new MatrixOfChoicesQuestion(question, newText);

        } else if (question instanceof SliderQuestion) {

            copiedQuestion = new SliderQuestion(question, newText);

        } else {

            throw new IllegalArgumentException("Cannot copy a question object of question type :" + question.getClass());
        }

        // Modify the question so references to non-shared option lists are copied
        for (SurveyPropertyKeyEnum propertyKey : copiedQuestion.getProperties().getKeys()) {

            Serializable value = copiedQuestion.getProperties().getPropertyValue(propertyKey);
            
            if(value instanceof OptionList) {
            
                OptionList optionList = (OptionList) value;

                if (!optionList.getIsShared()) {

                    optionList.setId(0);
                }
            }
        }

        return copiedQuestion;
    }

    /**
     * Copies a question and gives it a new ID
     *
     * @param question The question to copy
     * @param newId The new ID of the question
     * @param userName Name of the user
     * @return GwtQuestion The copied question with new ID
     */
    public static AbstractQuestion copyQuestion(AbstractQuestion question, int newId, String userName) {
        
        AbstractQuestion copiedQuestion;

        if (question instanceof FillInTheBlankQuestion) {

            copiedQuestion =  new FillInTheBlankQuestion(question, newId);

        } else if (question instanceof MultipleChoiceQuestion) {

            copiedQuestion =  new MultipleChoiceQuestion(question, newId);

        } else if (question instanceof RatingScaleQuestion) {

            copiedQuestion = new RatingScaleQuestion(question, newId);

        } else if (question instanceof MatrixOfChoicesQuestion) {

            copiedQuestion = new MatrixOfChoicesQuestion(question, newId);

        } else if (question instanceof SliderQuestion) {

            copiedQuestion = new SliderQuestion(question, newId);

        } else {

            throw new IllegalArgumentException("Cannot copy a question object of question type :" + question.getClass());
        }

        // Modify the question so references to non-shared option lists are copied
        for (SurveyPropertyKeyEnum propertyKey : copiedQuestion.getProperties().getKeys()) {

            Object propertyValue = copiedQuestion.getProperties().getPropertyValue(propertyKey);

            if (propertyValue instanceof OptionList) {

                OptionList optionList = (OptionList) propertyValue;

                if (!optionList.getIsShared()) {

                    optionList.setId(0);
                }
            }
        }
        
        copiedQuestion.getEditableToUserNames().add(userName);
        copiedQuestion.getVisibleToUserNames().add(userName);

        return copiedQuestion;
    }
    
    /**
     * Return the highest possible score from the weights provided and the
     * maximum number of selections allowed from those weights.
     *
     * @return the sum of the N highest positive weights, where N =
     * maxSelections.  Return 0.0 if the question type doesn't support
     * scoring (e.g. free response) or the scoring values aren't authored.
     */
    public abstract double getHighestPossibleScore();
    
    /**
     * Gets the URIs for all of the images that this question is associated with. This includes
     * both the main question image as well as any additional images used by subclasses of 
     * {@link AbstractQuestion} (such as the scale image for rating scale questions).
     * 
     * @return the URIs for all this question's images. Will not be null, but can be empty.
     */
    public Set<String> getAllAssociatedImages() {
        
        Set<String> images = new HashSet<>();
        
        String questionImage = getQuestionMedia();
        if(StringUtils.isNotBlank(questionImage)) {
            images.add(questionImage);
        }
        
        return images;
    }
}
