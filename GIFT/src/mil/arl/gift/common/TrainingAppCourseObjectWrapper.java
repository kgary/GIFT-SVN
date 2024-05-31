/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.metadata.MetadataWrapper.ContentTypeEnum;

/**
 * Used to encapsulate a training application course object found in the training application
 * library.
 * 
 * @author mhoffman
 *
 */
public class TrainingAppCourseObjectWrapper implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /** contains the content of the training application xml */
    private generated.course.TrainingApplication trainingApplicationObj;
    
    /** a validation exception on the course object */
    private DetailedExceptionSerializedWrapper validationException;
    
    /** the identifier to show for this training app object when there is a validation exception */
    private String invalidObjectIdentifier;
    
    /** the content type for the training application */
    private ContentTypeEnum contentType;
    
    /**
     * Required for Gwt serialization and should not be instantiated.
     */
    public TrainingAppCourseObjectWrapper(){  }
    
    /**
     * Use this constructor if the training application object is schema valid.
     * 
     * @param trainingApplicationObj contains the content of the training application xml
     * @param the content type for the training application.  Can be null if the type couldn't be determined.
     */
    public TrainingAppCourseObjectWrapper(generated.course.TrainingApplication trainingApplicationObj, ContentTypeEnum contentType){
        
        this.setTrainingApplicationObj(trainingApplicationObj);
        this.contentType = contentType;
    }
    
    /**
     * Use this constructor if the training application object is not schema valid.
     * 
     * @param invalidObjectIdentifier identifier for the object that was attempting to be parsed/validated but failed
     * @param validationException contains useful information about the validation issue
     * @param the content type for the training application.  Can be null if the type couldn't be determined, like when
     * there was an exception parsing/validating.
     */
    public TrainingAppCourseObjectWrapper(String invalidObjectIdentifier, DetailedException validationException, ContentTypeEnum contentType){            
        this.setInvalidObjectIdentifier(invalidObjectIdentifier);
        this.setValidationException(validationException);
        this.contentType = contentType;
    }
    
    /**
     * Return the content type for the training application
     * @return can be null.
     */
    public ContentTypeEnum getContentType(){
        return contentType;
    }

    /**
     * Return the validation exception for a training application object.
     * 
     * @return will be null if there is no validation issue, otherwise the exception will contain a message.
     */
    public DetailedExceptionSerializedWrapper getValidationException() {
        return validationException;
    }

    private void setValidationException(DetailedException validationException) {            
        
        if(validationException == null){
            throw new IllegalArgumentException("The validation exception can't be null.");
        }else if(validationException.getMessage() == null){
            throw new IllegalArgumentException("The exception message can't be null.");
        }
        
        this.validationException = new DetailedExceptionSerializedWrapper(validationException);
    }

    /**
     * Return the object containing the training application content.
     * 
     * @return will be null if there was a validation exception
     */
    public generated.course.TrainingApplication getTrainingApplicationObj() {
        return trainingApplicationObj;
    }

    private void setTrainingApplicationObj(generated.course.TrainingApplication trainingApplicationObj) {
        
        if(trainingApplicationObj == null){
            throw new IllegalArgumentException("The training application object can't be null.");
        }
        
        this.trainingApplicationObj = trainingApplicationObj;
    }

    /**
     * Return the object identifier to use when the training app object couldn't be parsed
     * or validated.
     * 
     * @return will be null if there is no validation issue, otherwise will not be null or empty.
     */
    public String getInvalidObjectIdentifier() {
        return invalidObjectIdentifier;
    }

    private void setInvalidObjectIdentifier(String invalidObjectIdentifier) {
        
        if(invalidObjectIdentifier == null || invalidObjectIdentifier.isEmpty()){
            throw new IllegalArgumentException("The invalid object identifier can't be null or empty.");
        }
        
        this.invalidObjectIdentifier = invalidObjectIdentifier;
    }
    
}
