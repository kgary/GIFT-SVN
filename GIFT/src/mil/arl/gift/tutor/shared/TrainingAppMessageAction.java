/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

/**
 * An action class that is responsible for delivering a message to 
 * the training application embedded within the Tutor Module
 * @author tflowers
 *
 */
public class TrainingAppMessageAction extends AbstractAction {

	private String message;
	
	private TrainingAppMessageAction() {
		super(ActionTypeEnum.SEND_APP_MESSASGE);
	}

	/**
	 * Constructs a new action with the specified message
	 * @param message the message to deliver to the Training application. Can not be null
	 */
	public TrainingAppMessageAction(String message) {
		this();
		
		if(message == null) {
			throw new IllegalArgumentException("The value of the message can not be null");
		}
		
		this.message = message;
	}
	
	/**
	 * Returns the message for the TrainingApplication.
	 * @return a non null value
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Sets the value of the message for the TrainingApplication.
	 * @param newValue the value to set the message to. Must not be null
	 */
	public void setMessage(String newValue) {
		if(newValue != null) {
			message = newValue;
		} else {
			throw new IllegalArgumentException("The new value of the message must not be null");
		}
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[TrainingAppMessageAction: message=");
        builder.append(message);
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }
	
	
}
