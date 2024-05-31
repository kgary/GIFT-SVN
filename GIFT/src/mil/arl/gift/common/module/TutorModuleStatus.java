/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

/**
 * Class used to communicate the status of the tutor module. 
 * Primarily used to communicate the name of the Tutor Topic 
 * used for sending messages from an embedded training application
 * to GIFT's domain session. This status does not replace the 
 * ModuleStatus already used by the TutorModule, but is used in 
 * addition to the existing ModuleStatus when a Tutor Topic is created.
 * There is at most one Tutor Topic for each domain session.
 * @author tflowers
 *
 */
public class TutorModuleStatus extends ModuleStatus {
	
	/** The name of the TutorTopic where embedded app messages are delivered to */
	private String topicName;

	/**
	 * Constructs a status with the given topic name as well as
	 * the given 'base' status
	 * @param topicName The name of the Tutor Topic that embedded app messages will
	 * be sent to in order to reach the domain. Cannot be empty or null. The topic 
	 * name should be unique to a domain session and there should be at most one topic
	 * for each domain session.
	 * @param status The base status of the TutorModule. A null value will result in a 
	 * NullReferenceException. If the module name, queue name, or module type of the status
	 * is null an exception will be thrown by the super class' constructor
	 */
	public TutorModuleStatus(String topicName, ModuleStatus status) {
		super(status.getModuleName(), status.getQueueName(), status.getModuleType());
		
		//Checks to make sure that the input for the topic name is valid
		if(topicName == null || topicName.isEmpty()) {
			throw new IllegalArgumentException("The value for the topicName cannot be null or empty");
		}
		
		this.topicName = topicName;
	}

	/**
	 * Getter for the topic name.
	 * @return The string representing the topic name which is not empty or null.
	 */
	public String getTopicName() {
		return topicName;
	}
	
	/**
	 * Provides a string representation of the TutorModuleStatus. 
	 * Used by the monitor to give a text representation of the status 
	 * when used as the payload of an object
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[TutorModuleStatus: ");
		sb.append(" topicName = ");
		sb.append(getTopicName());
		sb.append(", ");
		sb.append(super.toString());
		sb.append("]");
		return sb.toString();
	}
}
