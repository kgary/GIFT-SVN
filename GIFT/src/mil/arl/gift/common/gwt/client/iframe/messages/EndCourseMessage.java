/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.iframe.messages;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.util.StringUtils;

/**
 * The end course message is used by the tutor to signal to the dashboard when a
 * course has ended (along with course information, such as the domain id of the
 * course that was ended).
 * 
 * @author nblomberg
 *
 */
public class EndCourseMessage extends AbstractIFrameMessage {

    /** The domain id message attribute */
    private static final String DOMAINID = "domainid";

    /** The return URL message attribute */
    private static final String RETURN_URL = "returnUrl";
    
    /** the should reload message attribute*/
    private static final String SHOULD_RELOAD = "shouldReload";

    /** The domain id of the course that is being ended */
    private String domainId;

    /** (Optional) The URL to return to once the experiment is complete */
    private String experimentReturnUrl;
    
    /** 
     * (Optional) Whether the dashboard client that handles this message should reload upon receiving it. Reloading the 
     * dashboard can become necessary if the client's state gets out of sync with the server, such as when it has been
     * disconnected for a long period of time.
     */
    private boolean shouldReload;
    
    /**
     * Constructor Sets the type of message for this class.
     */
    public EndCourseMessage() {
        setMsgType(IFrameMessageType.END_COURSE);
    }

    /**
     * Sets the domain id. The domain id is optional for
     * {@link EndCourseMessage}, but if choosing to set this parameter, the
     * value cannot blank.
     * 
     * @param domainId the domain id of the course that is being ended. Can't be
     *        blank.
     */
    public void setDomainId(String domainId) {
        if (StringUtils.isBlank(domainId)) {
            throw new IllegalArgumentException("The parameter 'domainId' cannot be blank.");
        }

        this.domainId = domainId;
    }

    /**
     * Accessor to get the domain id of the course that is being ended.
     * 
     * @return String - The domain id of the course that is being ended. Can be
     *         null.
     */
    public String getDomainId() {
        return domainId;
    }

    /**
     * Sets the experiment return URL. The URL is optional for
     * {@link EndCourseMessage}, but if choosing to set this parameter, the
     * value cannot blank.
     * 
     * @param experimentReturnUrl the URL to return to once the experiment has
     *        ended.
     */
    public void setExperimentReturnUrl(String experimentReturnUrl) {
        if (StringUtils.isBlank(experimentReturnUrl)) {
            throw new IllegalArgumentException("The parameter 'experimentReturnUrl' cannot be blank.");
        }

        this.experimentReturnUrl = experimentReturnUrl;
    }

    /**
     * Retrieve the experiment return URL.
     * 
     * @return the URL to return to once the experiment has ended. Can be null
     *         if the course was not an experiment or if the experiment does not
     *         have a specific URL to return to after it ends.
     */
    public String getExperimentReturnUrl() {
        return experimentReturnUrl;
    }

    @Override
    public void encode(JSONObject obj) {
        if (StringUtils.isNotBlank(getDomainId())) {
            obj.put(DOMAINID, new JSONString(getDomainId()));
        }

        if (StringUtils.isNotBlank(getExperimentReturnUrl())) {
            obj.put(RETURN_URL, new JSONString(getExperimentReturnUrl()));
        }
        
        obj.put(SHOULD_RELOAD, JSONBoolean.getInstance(shouldReload));
    }

    @Override
    public void decode(JSONObject obj) {
        JSONValue valDomainId = obj.get(DOMAINID);
        if (valDomainId != null && valDomainId.isString() != null) {
            setDomainId(valDomainId.isString().stringValue());
        }

        JSONValue valReturnUrl = obj.get(RETURN_URL);
        if (valReturnUrl != null && valReturnUrl.isString() != null) {
            setExperimentReturnUrl(valReturnUrl.isString().stringValue());
        }
        
        JSONValue valShouldReload = obj.get(SHOULD_RELOAD);
        if (valShouldReload != null && valShouldReload.isBoolean() != null) {
            setShouldReload(valShouldReload.isBoolean().booleanValue());
        }
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[EndCourseMessage: ")
                .append("domainId=").append(getDomainId())
                .append(", experimentReturnUrl=").append(getExperimentReturnUrl())
                .append(", shouldReload=").append(getShouldReload())
                .append("']")
                .toString();
    }

    /**
     * Gets whether the dashboard client that handles this message should reload upon receiving it. Reloading the 
     * dashboard can become necessary if the client's state gets out of sync with the server, such as when it has been
     * disconnected for a long period of time.
     * 
     * @return whether this client should reload upon ending a course
     */
    public boolean getShouldReload() {
        return shouldReload;
    }

    /**
     * Sets whether the dashboard client that handles this message should reload upon receiving it. Reloading the 
     * dashboard can become necessary if the client's state gets out of sync with the server, such as when it has been
     * disconnected for a long period of time.
     * 
     * @param shouldReload whether this client should reload upon ending a course. False by default.
     */
    public void setShouldReload(boolean shouldReload) {
        this.shouldReload = shouldReload;
    }
}
