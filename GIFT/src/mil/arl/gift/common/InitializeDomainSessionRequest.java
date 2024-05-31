/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * The Initialize Domain Session Request is used to signify the domain session is beginning
 *
 * @author jleonard
 *
 */
public class InitializeDomainSessionRequest {

    /** the domain course file name being used for this domain session */
    private String courseName;
    
    /** The unique portion that makes up the name of the TutorTopic */
	private String tutorTopicId;
	
	/** the tutor client information.  Can be null. */
	private WebClientInformation clientInfo;
    
    /**
     * Class constructor
     *
     * @param courseName - the domain course file name being used for this domain session, cannot be null or empty
     * @param tutorTopicId The id of the tutor topic to create for the this domain session.
     * The tutor topic will receive messages from embedded training applications being run 
     * from the browser in the TutorModule. It can be null if no tutor topic is needed but it can't be empty.
     * @param clientInfo the tutor client information for this domain session.  Can be null but is desired.
     */
    public InitializeDomainSessionRequest(String courseName, String tutorTopicId, WebClientInformation clientInfo) {
        
    	//Validates the value used for the parameter courseName
    	if(courseName == null || courseName.isEmpty()) {
    		throw new IllegalArgumentException("The value for courseName can't be null or empty");
    	}
    	
    	//Verifies that tutor topic id contains data
		if(tutorTopicId != null && tutorTopicId.isEmpty()) {
			throw new IllegalArgumentException("The parameter tutorTopicId cannot be empty");
		}
    	
    	this.courseName = courseName;
    	this.tutorTopicId = tutorTopicId;
    	setClientInfo(clientInfo);
    }
    
    /**
     * Set the tutor client information for this domain session.
     * 
     * @param clientInfo the tutor client information for this domain session.
     */
    private void setClientInfo(WebClientInformation clientInfo) {
        this.clientInfo = clientInfo;
    }

    /**
     * Return the tutor client information for this domain session. 
     * 
     * @return can be null if not set yet
     */
    public WebClientInformation getClientInfo(){
        return clientInfo;
    } 

    /**
     * Return the domain course file name being used for this domain session
     * 
     * @return String
     */
    public String getDomainCourseFileName(){
        return courseName;
    }
	
	/**
	 * Gets the unique identifier used to construct the Tutor Topic used
	 * for sending messages from a single domain's embedded applications to the 
	 * Domain Module.
	 * @return The unique identifier as a string. Cannot be empty but can be null
	 */
	public String getTutorTopicId() {
		return tutorTopicId;
	}
	
	@Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[InitializeDomainSessionRequest: ")
        	.append(", Domain Course File = ")
        	.append(courseName)
        	.append(", Tutor Topic Name = ")
        	.append(tutorTopicId)
        	.append(", clientInfo = ")
        	.append(getClientInfo())
        	.append("]");

        return sb.toString();
    }
}
