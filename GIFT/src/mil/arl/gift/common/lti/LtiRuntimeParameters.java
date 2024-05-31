/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.lti;

import mil.arl.gift.common.AbstractRuntimeParameters;

/**
 * The LtiRuntimeParameters class allow the domain session to be configured per the LTI specification
 * to report a score back to the Tool Consumer.  The parameters in this class containt he data needed
 * for the domain session to report the score properly to the Tool Consumer.
 * 
 * @author nblomberg
 *
 */
public class LtiRuntimeParameters extends AbstractRuntimeParameters {

    private static final long serialVersionUID = 1L;
    
    /** The consumer key that made the lti launch request. */
    private String consumerKey;
    /** The lis outcome service url, this is the url where GIFT will report a score value back to. */
    private String outcomeServiceUrl;
    /** The lis sourcedid, which identifies who and what course the score will be reported back to. */
    private String lisSourcedid;

    /**
     * Default constructor
     */
    public LtiRuntimeParameters(String consumerKey, String outcomeServiceUrl, String sourcedid) {
        
        if (consumerKey == null || consumerKey.isEmpty()) {
            throw new IllegalArgumentException("The consumer key cannot be null or empty.");
        }
        
        if (outcomeServiceUrl == null || outcomeServiceUrl.isEmpty()) {
            throw new IllegalArgumentException("The outcome service url cannot be null or empty.");
        }
        
        if (sourcedid == null || sourcedid.isEmpty()) {
            throw new IllegalArgumentException("The lis sourcedid cannot be null or empty.");
        }
        
        setConsumerKey(consumerKey);
        setOutcomeServiceUrl(outcomeServiceUrl);
        setLisSourcedid(sourcedid);
    }

    /**
     * @return the consumerKey
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * @param consumerKey the consumerKey to set
     */
    private void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * @return the outcomeServiceUrl
     */
    public String getOutcomeServiceUrl() {
        return outcomeServiceUrl;
    }

    /**
     * @param outcomeServiceUrl the outcomeServiceUrl to set
     */
    private void setOutcomeServiceUrl(String outcomeServiceUrl) {
        this.outcomeServiceUrl = outcomeServiceUrl;
    }

    /**
     * @return the lisSourcedid
     */
    public String getLisSourcedid() {
        return lisSourcedid;
    }

    /**
     * @param lisSourcedid the lisSourcedid to set
     */
    private void setLisSourcedid(String lisSourcedid) {
        this.lisSourcedid = lisSourcedid;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[LtiRuntimeParameters: ");
        sb.append("consumerKey=").append(getConsumerKey());
        sb.append(", outcomeServiceUrl=").append(getOutcomeServiceUrl());
        sb.append(", lisSourcedid=").append(getLisSourcedid());
        sb.append("]");

        return sb.toString();
    }
}
