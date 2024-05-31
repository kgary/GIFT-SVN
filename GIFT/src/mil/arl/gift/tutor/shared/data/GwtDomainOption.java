/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A single domain option
 *
 * @author jleonard
 */
public class GwtDomainOption implements IsSerializable {

    private String domainId;

    private String domainName;

    private String description;
    
    private String recommendationEnum;
    
    private String recommendationMessage;

    /**
     * Default constructor - required for GWT
     */
    public GwtDomainOption() {
    }

    /**
     * Constructor
     *
     * @param domainId The domain's ID
     * @param domainName The domain's name
     * @param description The description of the domain option.  This is optional therefore it can be null.
     * @param recommendationEnum If the course has a recommendation, the type of recommendation
     * @param recommendationMessage If the course has a recommendation, why the course is recommended, not recommended, or unavailable
     */
    public GwtDomainOption(String domainId, String domainName, String description, String recommendationEnum, String recommendationMessage) {

        this.domainId = domainId;
        this.domainName = domainName;
        this.description = description;
        this.recommendationEnum = recommendationEnum;
        this.recommendationMessage = recommendationMessage;
    }

    /**
     * Gets the domain's ID
     *
     * @return String The domain's ID
     */
    public String getDomainId() {

        return domainId;
    }

    /**
     * Gets the domain's name
     *
     * @return String the domain's name
     */
    public String getDomainName() {

        return domainName;
    }

    /**
     * Gets the domain's description
     *
     * @return String The domain's description.  Can be null.
     */
    public String getDescription() {

        return description;
    }
    
    /**
     * If the course has a recommendation, this will return the type of recommendation
     * 
     * @return DomainOptionRecommendationEnum The recommendation type
     */
    public String getRecommendationEnum() {
    	
    	return recommendationEnum; 
    }
    
    /**
     * If the course has a recommendation this will return a message why the course is
     * recommended, not recommended, or unavailable. If this returns null, the course
     * has no recommendation and is available to be run.
     *
     * @return String The reason why the course is recommended, not recommended, or unavailable
     */
    public String getRecommendationMessage() {
        
        return recommendationMessage;
    }

}
