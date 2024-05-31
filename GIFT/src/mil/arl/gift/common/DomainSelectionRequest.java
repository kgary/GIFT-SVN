/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.util.StringUtils;

/**
 * The Domain Selection Request message is sent from the Tutor to the UMS module
 * after a domain is selected.
 *
 * @author mhoffman
 *
 */
public class DomainSelectionRequest {

    /** A boolean flag that indicates whether or not GIFT is in RTA mode. */
    private static final boolean isRta = CommonProperties.getInstance().getLessonLevel().equals(LessonLevelEnum.RTA);

    /** the selected domain runtime id which is the path to the course used when taking the course */
    private String domainRuntimeId = null;

    /** the selected domain source id which is the path to the authored course */
    private String domainSourceId = null;

    /** the LMS user name of the learner selecting this domain */
    private String lmsUsername = null;

    /** information about the client making the request */
    private WebClientInformation clientInfo = null;

    /** (Optional)  When selecting the domain, an optional set of runtime parameters can be passed in to further configure the domain session. */
    private AbstractRuntimeParameters runtimeParams = null;

    /**
     * Class constructor
     *
     * @param lmsUsername - unique LMS user name of the learner selecting this
     *        domain. Can't be null or empty.
     * @param domainRuntimeId the selected domain runtime id which is the path
     *        to the course used when taking the course. Can be null but not
     *        empty. e.g. "mhoffman\2021-06-21_13-05-32\Hello World new4\Hello World new4.course.xml"
     * @param domainSourceId the selected domain source id which is the path to
     *        the authored course. Can't be null or empty. e.g. "Public/Hello World new4/Hello World new4.course.xml"
     * @param clientInfo - information about the client making the request.
     *        Can't be null.
     * @param runtimeParams - (Optional -- can be null) Allows passing of
     *        parameters determined at runtime that can help further configure
     *        the domain session.
     */
    public DomainSelectionRequest(String lmsUsername, String domainRuntimeId, String domainSourceId,
            WebClientInformation clientInfo, AbstractRuntimeParameters runtimeParams){

        setDomainRuntimeId(domainRuntimeId);

        if(domainSourceId == null || domainSourceId.isEmpty()){
            throw new IllegalArgumentException("The domain source id can't be null or empty.");
        }
        this.domainSourceId = domainSourceId;

        if(lmsUsername == null || lmsUsername.isEmpty()){
            throw new IllegalArgumentException("The lms user name can't be null or empty.");
        }
        this.lmsUsername = lmsUsername;

        if(clientInfo == null){
            throw new IllegalArgumentException("The client information can't be null.");
        }
        this.clientInfo = clientInfo;

        // The runtime parameters are optional.
        this.runtimeParams = runtimeParams;
    }

    /**
     * Return  the selected domain runtime id which is the path to the course used when taking the course
     *
     * @return e.g. "mhoffman\2021-06-21_13-05-32\Hello World new4\Hello World new4.course.xml". Wont be null or empty
     */
    public String getDomainRuntimeId(){
        return domainRuntimeId;
    }

    /**
     * Return the selected domain source id which is the path to the authored course
     *
     * @return e.g. "Public/Hello World new4/Hello World new4.course.xml"<br/>
     * Can be null if in RTA mode but can never be empty. If GIFT is in RTA
     * mode, a null value is used to indicate that the domain module
     * needs to load the course and generate its own runtime id.
     */
    public String getDomainSourceId(){
        return domainSourceId;
    }

    /**
     * Setter for the domainRuntimeId.
     *
     * @param domainRuntimeId The new value of {@link #domainRuntimeId}. Can be
     *        null if in RTA mode but can never be empty. If GIFT is in RTA
     *        mode, a null value is used to indicate that the domain module
     *        needs to load the course and generate its own runtime id.<br/>
     *        e.g. "mhoffman\2021-06-21_13-05-32\Hello World new4\Hello World new4.course.xml"
     */
    public void setDomainRuntimeId(String domainRuntimeId) {
        if (isRta) {
            if (domainRuntimeId != null && domainRuntimeId.isEmpty()) {
                throw new IllegalArgumentException("The parameter 'domainRuntimeId' cannot be empty.");
            }
        } else if (StringUtils.isBlank(domainRuntimeId)) {
            throw new IllegalArgumentException("The parameter 'domainRuntimeId' cannot be null or empty.");
        }

        this.domainRuntimeId = domainRuntimeId;
    }

    /** Return the lms username
     *
     * @return String containing the lms username.
     */
    public String getLmsUsername() {
        return lmsUsername;
    }

    /**
     * Return the information about the client making the request.
     *
     * @return WebClientInformation
     */
    public WebClientInformation getClientInformation(){
        return clientInfo;
    }

    /**
     * @return the runtimeParams
     */
    public AbstractRuntimeParameters getRuntimeParams() {
        return runtimeParams;
    }

    /**
     * @param runtimeParams the runtimeParams to set
     */
    public void setRuntimeParams(AbstractRuntimeParameters runtimeParams) {
        this.runtimeParams = runtimeParams;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DomainSelectionRequest: ");
        sb.append("Domain Runtime Id: = ");
        sb.append(getDomainRuntimeId());
        sb.append(", Domain Source Id: = ");
        sb.append(getDomainSourceId());
        sb.append(", LMS Username = ");
        sb.append(getLmsUsername());
        sb.append(", client info = ");
        sb.append(getClientInformation());
        sb.append(", runtime params = ").append(getRuntimeParams());
        sb.append("]");

        return sb.toString();
    }
}
