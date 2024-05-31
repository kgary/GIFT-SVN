/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This action is used to indicate that a user's current training application lesson is active or inactive.
 * 
 * @author nroberts
 */
public class LessonStatusAction extends AbstractAction implements IsSerializable {
    
    /**
     * The status of a training application lesson
     * 
     * @author nroberts
     */
    public enum LessonStatus{
        INITIALIZING,
        ACTIVE,
        INACTIVE;
        
        /**
         * Get whether or not the training application lesson is active (i.e. not inactive or initialized)
         * 
         * @return whether or not the training application lesson is active
         */
        public boolean isLessonActive() {
            return LessonStatus.ACTIVE.equals(this);
        }
        
        /**
         * Get whether or not the training application lesson is inactive (i.e. not active or initialized)
         * 
         * @return whether or not the training application lesson is inactive
         */
        public boolean isLessonInactive() {
            return LessonStatus.INACTIVE.equals(this);
        }
        
        /**
         * Get whether or not the training application lesson is only initialized (i.e. not active or inactive)
         * 
         * @return whether or not the training application lesson is initialized
         */
        public boolean isLessonInitialized() {
            return LessonStatus.INITIALIZING.equals(this);
        }
    }
	
	/** The training application lesson's current status */
	private LessonStatus status;

    /**
     * Default no-arg constructor required by GWT for RPC serialization
     */
    private LessonStatusAction() {
        super(ActionTypeEnum.LESSON_STATUS);
    }
    
    /**
     * Set the training application lesson's status
     * 
     * @param status the training application's lesson status
     */
    public LessonStatusAction(LessonStatus status){
        this();
        
        this.status = status;
    }
    
    /**
     * Gets the lesson status of the current training application
     * 
     * @return the training application lesson status.
     */
    public LessonStatus getLessonStatus() {
        return status;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[LessonStatusAction: ");
        sb.append(super.toString());
        sb.append(", ").append(status);
        sb.append("]");
        return sb.toString();
    }
}
