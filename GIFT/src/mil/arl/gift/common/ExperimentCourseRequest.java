/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents a request to start an experiment's course.
 * 
 * @author nroberts
 */
public class ExperimentCourseRequest {

	/** The ID of the experiment */
    private String experimentId = null;

    /**
     * The folder of the experiment course relative to the runtime experiment
     * folder (or legacy experiment folder)
     */
    private String experimentFolder;

    /** information about the client making the request */
    private WebClientInformation clientInfo = null;
    
    /** unique identifier of the tutor client requesting the experiment to start */
    private String preSessionId;

    /**
     * Class constructor
     * 
     * @param experimentId the ID of the experiment. Can't be null.
     * @param experimentFolder the folder of the experiment course relative to
     *        the runtime experiment folder. Should only be null for legacy
     *        experiments.
     * @param clientInfo information about the client making the request. Can't
     *        be null.
     * @param preSessionId a unique identifier of the tutor client requesting
     *        the experiment to start. Can't be null.
     */
    public ExperimentCourseRequest(String experimentId, String experimentFolder, WebClientInformation clientInfo, String preSessionId){
        
        if(experimentId == null){
            throw new IllegalArgumentException("The experiment ID can't be null.");
        }
        this.experimentId = experimentId;
        this.experimentFolder = experimentFolder;
        
        if(clientInfo == null){
            throw new IllegalArgumentException("The client information can't be null.");
        }
        this.clientInfo = clientInfo;
        
        if(preSessionId == null){
            throw new IllegalArgumentException("The pre-session ID can't be null.");
        }
        this.preSessionId = preSessionId;
    }

    /**
     * Gets the experiment's ID
     * 
     * @return the experiment's ID, won't be null
     */
	public String getExperimentId() {
		return experimentId;
	}

    /**
     * Gets the experiment's folder
     * 
     * @return the experiment's folder, can be null if the experiment is in the
     *         legacy location.
     */
    public String getExperimentFolder() {
        return experimentFolder;
    }

	/**
     * Return the information about the client making the request.
     * 
     * @return WebClientInformation won't be null
     */
    public WebClientInformation getClientInformation(){
        return clientInfo;
    }
    
    /**
     * Return the unique identifier of the tutor client requesting the experiment to start
     * 
     * @return String won't be null
     */
    public String getPreSessionId(){
        return preSessionId;
    }

	@Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ExperimentCourseRequest: ");
        sb.append("Experiment Id: = ");
        sb.append(getExperimentId());
        sb.append(", experimentFolder = ");
        sb.append(getExperimentFolder());
        sb.append(", client info = ");
        sb.append(getClientInformation());
        sb.append(", pre-session id = ");
        sb.append(getPreSessionId());
        sb.append("]");

        return sb.toString();
    }
}
