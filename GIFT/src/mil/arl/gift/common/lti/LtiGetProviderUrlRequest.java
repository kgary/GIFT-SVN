/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.lti;

import generated.course.CustomParameters;

/**
 * The LtiGetProviderUrlRequest class encapsulates the data needed to request an OAuth LTI
 * provider URL
 * 
 * @author sharrison
 *
 */
public class LtiGetProviderUrlRequest {

    /** The raw LTI url */
    private String rawUrl;

    /** The LTI provider id */
    private String ltiId;

    /** The custom parameters */
    private CustomParameters customParameters;

    /** The domain session id */
    private int domainSessionId;

    /**
     * Class constructor
     * 
     * @param ltiId the LTI provider id. Can't be null.
     * @param customParameters the custom parameters. If null is passed in, a default {@link CustomParameters} is created.
     * @param rawUrl the raw LTI url. Can't be null.
     * @param domainSessionId the domain session id. Must be a non-negative number.
     * 
     * @throws IllegalArgumentException if ltiId or rawUrl are null or if domainSessionId is negative.
     */
    public LtiGetProviderUrlRequest(String ltiId, CustomParameters customParameters, String rawUrl, int domainSessionId) {
        if (ltiId == null) {
            throw new IllegalArgumentException("The LTI Id can't be null");
        } else if (rawUrl == null) {
            throw new IllegalArgumentException("The raw URL can't be null");
        } else if (domainSessionId < 0) {
            throw new IllegalArgumentException("The domainSessionId can't negative");
        }
        
        this.ltiId = ltiId;
        this.customParameters = customParameters == null ? new CustomParameters() : customParameters;
        this.rawUrl = rawUrl;
        this.domainSessionId = domainSessionId;
    }

    /**
     * @return the rawUrl
     */
    public String getRawUrl() {
        return rawUrl;
    }

    /**
     * @return the ltiId
     */
    public String getLtiId() {
        return ltiId;
    }

    /**
     * @return the customParameters. Will never be null.
     */
    public CustomParameters getCustomParameters() {
        return customParameters;
    }

    /**
     * @return the domainSessionId
     */
    public int getDomainSessionId() {
        return domainSessionId;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LtiGetProviderUrlRequest: ");
        sb.append("rawUrl = ").append(getRawUrl());
        sb.append(", ltiId = ").append(getLtiId());
        sb.append(", customParameters = ").append(getCustomParameters());
        sb.append(", domainSessionId = ").append(getDomainSessionId());
        sb.append("]");
        return sb.toString();
    }
}
